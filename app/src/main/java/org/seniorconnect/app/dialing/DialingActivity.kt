package org.seniorconnect.app.dialing

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale
import java.util.concurrent.Executors
import org.seniorconnect.app.R

/**
 * DialingActivity — conversational call-placement flow powered by Gemini AI.
 *
 * Conversation states:
 *   PICKING      — home grid: "Who do you want to call?"
 *   THINKING     — waiting for Gemini response (spinner message shown)
 *   CLARIFYING   — Gemini returned a question for the elder
 *   CANDIDATES   — multiple contacts found; show all so elder picks one
 *   CONFIRMING   — single contact identified; "I found X. Call them?"
 *   SETUP        — slot has no number yet; ask the elder to enter one
 */
class DialingActivity : Activity() {

    private enum class ConvState { PICKING, THINKING, CLARIFYING, CANDIDATES, CONFIRMING, SETUP }

    private val repository: TrustedContactRepository by lazy { TrustedContactRepository(this) }
    private val matcher: VoiceContactMatcher by lazy { VoiceContactMatcher(repository) }
    private val agent: TrustedCallAgent by lazy { TrustedCallAgent(matcher) }
    private val policy: DialingPolicy by lazy { DialingPolicy(repository) }
    private val dialIntentLauncher = DialIntentLauncher()

    // Background thread for Gemini network calls
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var root: LinearLayout
    private lateinit var prompt: TextView

    private var convState: ConvState = ConvState.PICKING
    private var pendingContact: TrustedContact? = null
    private var pendingCandidates: List<TrustedContact> = emptyList()
    private var lastMessage = ""

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showContactPicker(getString(R.string.trusted_call_title))
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdownNow()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != SPEECH_REQUEST_CODE || resultCode != RESULT_OK) return
        val transcript = data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            .orEmpty()
        handleVoiceTranscript(transcript)
    }

    // ── Screens ───────────────────────────────────────────────────────────────

    /** Home grid — four large contact buttons. */
    private fun showContactPicker(message: String = getString(R.string.trusted_call_title)) {
        convState = ConvState.PICKING
        pendingContact = null
        pendingCandidates = emptyList()
        lastMessage = message

        root = verticalRoot()
        prompt = promptView(message)
        root.addView(prompt)

        val grid = GridLayout(this).apply {
            columnCount = 2
            rowCount = 2
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f,
            )
        }

        for (contact in repository.all()) {
            val label = if (contact.phoneNumber.isBlank()) {
                "${contact.relation.replaceFirstChar { it.titlecase(Locale.US) }}\nSet up"
            } else {
                contact.relation.replaceFirstChar { it.titlecase(Locale.US) }
            }
            grid.addView(gridButton(label).apply {
                setOnClickListener {
                    if (contact.phoneNumber.isBlank()) showContactSetup(contact)
                    else showConfirmation(contact)
                }
            })
        }

        root.addView(grid)
        setContentView(root)
    }

    /** Thinking screen — shown while Gemini is processing. */
    private fun showThinking(userSaid: String) {
        convState = ConvState.THINKING
        val message = "Let me find that for you…"
        lastMessage = message

        root = verticalRoot()
        prompt = promptView(message)
        root.addView(prompt)

        root.addView(promptView("You said: \"$userSaid\"").apply {
            textSize = 20f
        })

        setContentView(root)
    }

    /**
     * Clarifying screen — Gemini returned a question.
     * Shows the question and a Speak button so the elder can answer.
     */
    private fun showClarifying(question: String) {
        convState = ConvState.CLARIFYING
        lastMessage = question

        root = verticalRoot()
        prompt = promptView(question)
        root.addView(prompt)

        root.addView(fullWidthPrimaryButton(getString(R.string.trusted_call_speak_name)).apply {
            setOnClickListener { startVoiceInput() }
        })
        root.addView(fullWidthPrimaryButton(getString(R.string.trusted_call_take_me_home)).apply {
            setOnClickListener { finish() }
        })

        setContentView(root)
    }

    /**
     * Candidates screen — multiple contacts matched.
     * One button per candidate so the elder taps the right one.
     */
    private fun showCandidates(candidates: List<TrustedContact>, message: String) {
        convState = ConvState.CANDIDATES
        pendingCandidates = candidates
        lastMessage = message

        root = verticalRoot()
        prompt = promptView(message)
        root.addView(prompt)

        for (candidate in candidates) {
            val label = "${candidate.displayName} (${candidate.relation.replaceFirstChar { it.titlecase(Locale.US) }})"
            root.addView(fullWidthPrimaryButton(label).apply {
                setOnClickListener { showConfirmation(candidate) }
            })
        }

        root.addView(fullWidthPrimaryButton("None of these — try again").apply {
            setOnClickListener {
                showClarifying("Who did you mean? Please say their name clearly.")
            }
        })

        setContentView(root)
    }

    /** Confirmation screen — "I found X. Do you want to call X?" */
    private fun showConfirmation(contact: TrustedContact) {
        convState = ConvState.CONFIRMING
        pendingContact = contact
        val message = "I found ${contact.displayName}. Do you want to call ${contact.displayName}?"
        lastMessage = message

        root = verticalRoot()
        prompt = promptView(message)
        root.addView(prompt)

        root.addView(fullWidthPrimaryButton(getString(R.string.trusted_call_yes_call)).apply {
            setOnClickListener { confirmAndOpenDialer(contact) }
        })
        root.addView(fullWidthPrimaryButton(getString(R.string.trusted_call_no)).apply {
            setOnClickListener {
                showClarifying("Okay. Who do you want to call? Please say their name.")
            }
        })

        setContentView(root)
    }

    /** Setup screen — trusted slot has no number yet. */
    private fun showContactSetup(contact: TrustedContact) {
        convState = ConvState.SETUP
        val relationLabel = contact.relation.replaceFirstChar { it.titlecase(Locale.US) }
        val message = "Add the trusted $relationLabel phone number."
        lastMessage = message

        root = verticalRoot()
        prompt = promptView(message)
        root.addView(prompt)

        val nameInput = setupInput(contact.displayName, InputType.TYPE_CLASS_TEXT)
        val phoneInput = setupInput("Phone number", InputType.TYPE_CLASS_PHONE)

        root.addView(nameInput)
        root.addView(phoneInput)
        root.addView(fullWidthPrimaryButton("Save trusted contact").apply {
            setOnClickListener {
                val saved = repository.saveContact(
                    contact.id,
                    nameInput.text.toString(),
                    phoneInput.text.toString(),
                )
                if (saved == null || saved.phoneNumber.isBlank()) showContactSetup(contact)
                else showConfirmation(saved)
            }
        })
        root.addView(fullWidthPrimaryButton(getString(R.string.trusted_call_no)).apply {
            setOnClickListener { showContactPicker("No number was saved. Who do you want to call?") }
        })

        setContentView(root)
    }

    // ── Voice handling ────────────────────────────────────────────────────────

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say who you want to call")
        }
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
        } catch (_: ActivityNotFoundException) {
            showContactPicker("Speech is not available. Please tap a trusted contact.")
        }
    }

    /**
     * Routes the transcript based on the current conversation state.
     * Network calls (Gemini) run on the executor thread; UI updates post back to main.
     */
    private fun handleVoiceTranscript(transcript: String) {
        when (convState) {
            ConvState.PICKING, ConvState.SETUP -> resolveAsync(transcript, isRefinement = false)
            ConvState.CLARIFYING               -> resolveAsync(transcript, isRefinement = true)
            ConvState.CANDIDATES               -> handleCandidatesTurn(transcript)
            ConvState.CONFIRMING               -> handleConfirmationTurn(transcript)
            ConvState.THINKING                 -> { /* ignore input while waiting */ }
        }
    }

    /**
     * Runs the agent on a background thread so the network call doesn't block the UI.
     * Shows "Let me find that for you…" while waiting.
     */
    private fun resolveAsync(transcript: String, isRefinement: Boolean) {
        showThinking(transcript)
        val contacts = repository.all()

        executor.execute {
            val decision = if (isRefinement) {
                agent.refineByName(transcript, contacts)
            } else {
                agent.resolveSpokenRequest(transcript, contacts)
            }
            mainHandler.post { applyDecision(decision) }
        }
    }

    private fun applyDecision(decision: TrustedCallAgent.AgentDecision) {
        if (convState != ConvState.THINKING) return

        when (decision) {
            is TrustedCallAgent.AgentDecision.Contact -> {
                if (decision.trustedContact.phoneNumber.isBlank()) {
                    showContactSetup(decision.trustedContact)
                } else {
                    showConfirmation(decision.trustedContact)
                }
            }

            is TrustedCallAgent.AgentDecision.MultipleFound ->
                showCandidates(
                    decision.candidates,
                    "I found ${decision.candidates.size} possible contacts. Which one did you mean?",
                )

            is TrustedCallAgent.AgentDecision.NeedsMoreInfo ->
                showClarifying(decision.question)

            TrustedCallAgent.AgentDecision.Emergency ->
                showContactPicker("For an emergency, use the phone emergency button or ask a nearby person for help.")

            TrustedCallAgent.AgentDecision.RejectedContact ->
                showContactPicker("Okay. Who do you want to call?")

            TrustedCallAgent.AgentDecision.NoMatch ->
                showClarifying("I could not find that contact. What is the name of the person you want to call?")
        }
    }

    /** Elder is responding while looking at a candidates list. */
    private fun handleCandidatesTurn(transcript: String) {
        if (matcher.isRejection(transcript)) {
            showClarifying("None of those? What is the name of the person you want to call?")
            return
        }
        val words = transcript.lowercase().split(Regex("\\s+")).filter { it.length >= 2 }
        val narrowed = pendingCandidates.filter { c ->
            words.any { c.displayName.lowercase().contains(it) }
        }
        when {
            narrowed.size == 1 -> showConfirmation(narrowed.first())
            narrowed.size > 1  -> showCandidates(narrowed, "I still found more than one. Which one?")
            else               -> resolveAsync(transcript, isRefinement = true)
        }
    }

    /** Elder is replying "yes" or "no" on the confirmation screen. */
    private fun handleConfirmationTurn(transcript: String) {
        val t = transcript.lowercase().trim()
        val words = t.split(Regex("\\s+"))

        val isYes = words.any { it in listOf("yes", "yeah", "yep", "sure", "ok", "okay") }
        val isNo = words.any { it in listOf("no", "nope", "nah", "not", "wrong", "cancel", "stop") }

        // If it's a simple "call" or "call him" etc., treat as yes
        val isCallHim = t == "call" || t == "call him" || t == "call her" || t == "call them"

        when {
            isYes || isCallHim -> pendingContact?.let { confirmAndOpenDialer(it) }
                ?: showContactPicker("Something went wrong. Who do you want to call?")
            isNo  -> showClarifying("Okay. Who do you want to call? Please say their name.")
            else  -> resolveAsync(transcript, isRefinement = false)
        }
    }

    // ── Dialer ────────────────────────────────────────────────────────────────

    private fun confirmAndOpenDialer(contact: TrustedContact) {
        when (val decision = policy.canOpenDialer(contact.id, elderConfirmed = true)) {
            is DialingPolicy.PolicyDecision.Allow -> dialIntentLauncher.openDialer(this, decision.contact.phoneNumber)
            is DialingPolicy.PolicyDecision.Deny  -> showContactPicker(decision.message)
        }
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private fun verticalRoot(): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(getColor(R.color.screen_background))
            setPadding(24.dp, 32.dp, 24.dp, 24.dp)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }

    private fun promptView(text: String): TextView =
        TextView(this).apply {
            this.text = text
            setTextColor(getColor(R.color.text_primary))
            textSize = 30f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { bottomMargin = 20.dp }
        }

    private fun gridButton(text: String): Button =
        Button(this).apply {
            this.text = text
            isAllCaps = false
            textSize = 24f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setTextColor(getColor(R.color.button_text))
            setBackgroundResource(R.drawable.action_call_button)
            minHeight = 112.dp
            gravity = Gravity.CENTER
        }.apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(8.dp, 8.dp, 8.dp, 8.dp)
            }
            setPadding(20.dp, 20.dp, 20.dp, 20.dp)
        }

    private fun fullWidthPrimaryButton(text: String): Button =
        Button(this).apply {
            this.text = text
            isAllCaps = false
            textSize = 24f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setTextColor(getColor(R.color.button_text))
            setBackgroundResource(R.drawable.action_call_button)
            gravity = Gravity.CENTER
            minHeight = 96.dp
            setPadding(20.dp, 20.dp, 20.dp, 20.dp)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = 8.dp
                bottomMargin = 8.dp
            }
        }

    private fun setupInput(hintText: String, inputKind: Int): EditText =
        EditText(this).apply {
            hint = hintText
            inputType = inputKind
            textSize = 22f
            setSingleLine(true)
            setTextColor(getColor(R.color.text_primary))
            setHintTextColor(getColor(R.color.text_primary))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = 8.dp
                bottomMargin = 8.dp
            }
            minHeight = 72.dp
        }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    companion object {
        private const val SPEECH_REQUEST_CODE = 1001
    }
}
