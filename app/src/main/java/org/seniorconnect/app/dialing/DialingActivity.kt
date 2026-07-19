package org.seniorconnect.app.dialing

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
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
import org.seniorconnect.app.R

class DialingActivity : Activity() {
    private val repository: TrustedContactRepository by lazy { TrustedContactRepository(this) }
    private val trustedCallAgent: TrustedCallAgent by lazy {
        TrustedCallAgent(VoiceContactMatcher(repository))
    }
    private val policy: DialingPolicy by lazy { DialingPolicy(repository) }
    private val dialIntentLauncher = DialIntentLauncher()

    private lateinit var root: LinearLayout
    private lateinit var prompt: TextView
    private var pendingContact: TrustedContact? = null
    private var lastMessage = "Who do you want to call?"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showContactPicker()
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

    private fun showContactPicker(message: String = getString(R.string.trusted_call_title)) {
        pendingContact = null
        lastMessage = message
        root = verticalRoot()
        prompt = promptView(message)
        root.addView(prompt)

        val grid = GridLayout(this).apply {
            columnCount = 2
            rowCount = 2
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f,
            )
        }

        for (contact in repository.all()) {
            val label = if (contact.phoneNumber.isBlank()) {
                "${contact.relation.replaceFirstChar { it.titlecase(Locale.US) }}\nSet up"
            } else {
                contact.relation.replaceFirstChar { it.titlecase(Locale.US) }
            }
            grid.addView(
                gridButton(label).apply {
                    setOnClickListener {
                        if (contact.phoneNumber.isBlank()) {
                            showContactSetup(contact)
                        } else {
                            showConfirmation(contact)
                        }
                    }
                },
            )
        }

        root.addView(grid)
        addControlRow(includeSpeak = true)
        setContentView(root)
    }

    private fun showContactSetup(contact: TrustedContact) {
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
        root.addView(
            fullWidthPrimaryButton("Save trusted contact").apply {
                setOnClickListener {
                    val savedContact = repository.saveContact(
                        contact.id,
                        nameInput.text.toString(),
                        phoneInput.text.toString(),
                    )

                    if (savedContact == null || savedContact.phoneNumber.isBlank()) {
                        showContactSetup(contact)
                    } else {
                        showConfirmation(savedContact)
                    }
                }
            },
        )
        root.addView(
            fullWidthPrimaryButton(getString(R.string.trusted_call_no)).apply {
                setOnClickListener { showContactPicker("No number was saved. Who do you want to call?") }
            },
        )

        addControlRow(includeSpeak = false)
        setContentView(root)
    }

    private fun showConfirmation(contact: TrustedContact) {
        pendingContact = contact
        val message = "I found ${contact.displayName}. Do you want to call ${contact.displayName}?"
        lastMessage = message

        root = verticalRoot()
        prompt = promptView(message)
        root.addView(prompt)

        root.addView(
            fullWidthPrimaryButton(getString(R.string.trusted_call_yes_call)).apply {
                setOnClickListener { confirmAndOpenDialer(contact) }
            },
        )
        root.addView(
            fullWidthPrimaryButton(getString(R.string.trusted_call_no)).apply {
                showContactPicker("No call was opened. Who do you want to call?")
            },
        )

        addControlRow(includeSpeak = false)
        setContentView(root)
    }

    private fun confirmAndOpenDialer(contact: TrustedContact) {
        when (val decision = policy.canOpenDialer(contact.id, elderConfirmed = true)) {
            is DialingPolicy.PolicyDecision.Allow -> {
                dialIntentLauncher.openDialer(this, decision.contact.phoneNumber)
            }
            is DialingPolicy.PolicyDecision.Deny -> {
                showContactPicker(decision.message)
            }
        }
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say who you want to call")
        }

        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
        } catch (_: ActivityNotFoundException) {
            showContactPicker("Speech is not available. Please tap a trusted contact.")
        }
    }

    private fun handleVoiceTranscript(transcript: String) {
        when (val decision = trustedCallAgent.resolveSpokenRequest(transcript)) {
            is TrustedCallAgent.AgentDecision.Contact -> showConfirmation(decision.trustedContact)
            TrustedCallAgent.AgentDecision.Emergency -> showContactPicker(
                "For an emergency, use the phone emergency button or ask a nearby person for help.",
            )
            TrustedCallAgent.AgentDecision.NoMatch -> showContactPicker(
                "I could not find that trusted person. Please choose one button.",
            )
        }
    }

    private fun addControlRow(includeSpeak: Boolean) {
        root.findViewWithTag<View>(CONTROL_ROW_TAG)?.let(root::removeView)

        val controls = LinearLayout(this).apply {
            tag = CONTROL_ROW_TAG
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }

        if (includeSpeak) {
            controls.addView(controlButton(getString(R.string.trusted_call_speak_name)) {
                startVoiceInput()
            })
        }

        controls.addView(controlButton(getString(R.string.trusted_call_repeat_slowly)) {
            prompt.text = lastMessage
        })
        controls.addView(controlButton(getString(R.string.trusted_call_take_me_home)) {
            finish()
        })
        controls.addView(controlButton(getString(R.string.trusted_call_stop)) {
            showContactPicker("Stopped. Who do you want to call?")
        })

        root.addView(controls)
    }

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
            ).apply {
                bottomMargin = 20.dp
            }
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

    private fun controlButton(text: String, onClick: () -> Unit): Button =
        Button(this).apply {
            this.text = text
            isAllCaps = false
            textSize = 20f
            setTextColor(getColor(R.color.button_text))
            setBackgroundResource(R.drawable.large_action_button)
            setOnClickListener { onClick() }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = 8.dp
            }
            minHeight = 64.dp
        }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    companion object {
        private const val SPEECH_REQUEST_CODE = 1001
        private const val CONTROL_ROW_TAG = "trusted_call_controls"
    }
}
