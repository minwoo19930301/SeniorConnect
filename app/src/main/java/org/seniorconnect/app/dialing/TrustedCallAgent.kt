package org.seniorconnect.app.dialing

/**
 * TrustedCallAgent — decides what to do with a spoken request.
 *
 * Strategy:
 *  1. Try fast local keyword match (no network, instant).
 *  2. If local match fails, ask Gemini to interpret the request.
 *  3. Parse Gemini's reply: it either names a relation/contact or
 *     returns a SHORT clarifying question for the elder.
 *  4. If Gemini is unavailable (no network, error), fall back to
 *     local name search so the app never completely breaks.
 *
 * ALL calls here are synchronous — the caller (DialingActivity) must
 * run resolveSpokenRequest() and refineByName() on a background thread.
 */
class TrustedCallAgent(private val localMatcher: VoiceContactMatcher) {

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * First turn: elder just spoke for the first time.
     * Tries local match first, then Gemini if needed.
     */
    fun resolveSpokenRequest(
        transcript: String,
        contacts: List<TrustedContact>,
    ): AgentDecision {
        // 1. Local fast path
        when (val local = localMatcher.match(transcript)) {
            is VoiceContactMatcher.MatchResult.Contact   -> return AgentDecision.Contact(local.trustedContact)
            is VoiceContactMatcher.MatchResult.Ambiguous -> return AgentDecision.MultipleFound(local.candidates)
            VoiceContactMatcher.MatchResult.Emergency    -> return AgentDecision.Emergency
            VoiceContactMatcher.MatchResult.NoMatch      -> { /* fall through to Gemini */ }
        }

        // 2. Ask Gemini
        return askGemini(transcript, contacts)
    }

    /**
     * Follow-up turn: elder gave a name or responded to a clarifying question.
     */
    fun refineByName(
        transcript: String,
        contacts: List<TrustedContact>,
    ): AgentDecision {
        if (localMatcher.isRejection(transcript)) return AgentDecision.RejectedContact

        // Try local name search first
        val candidates = localMatcher.matchByName(transcript)
        return when {
            candidates.size == 1 -> AgentDecision.Contact(candidates.first())
            candidates.size > 1  -> AgentDecision.MultipleFound(candidates)
            else                 -> askGemini(transcript, contacts) // Gemini fallback
        }
    }

    // ── Gemini integration ────────────────────────────────────────────────────

    private fun askGemini(
        transcript: String,
        contacts: List<TrustedContact>,
    ): AgentDecision {
        val contactList = contacts.joinToString(", ") { "${it.relation} (${it.displayName})" }

        val prompt = """
            You are helping an elderly person make a phone call on their Android phone.
            Their saved trusted contacts are: $contactList
            
            The elderly person said: "$transcript"
            
            Your job:
            - If you can match their request to ONE contact, reply with ONLY the relation word.
              Example: son
            - If multiple contacts could match, reply with ONLY: MULTIPLE: relation1, relation2
              Example: MULTIPLE: son, doctor
            - If you cannot match at all, ask ONE short clarifying question in simple words (max 12 words).
              Example: What is the name of the person you want to call?
            - NEVER include explanations, greetings, or extra text.
            - NEVER make up contacts that are not in the list above.
        """.trimIndent()

        val reply = GeminiClient.ask(prompt)
            ?: return localFallback(transcript) // network error → local fallback

        return parseGeminiReply(reply.trim(), contacts)
    }

    private fun parseGeminiReply(
        reply: String,
        contacts: List<TrustedContact>,
    ): AgentDecision {
        val sanitized = reply.lowercase().trim().removeSuffix(".").removeSuffix("?")
        if (sanitized.isBlank()) return localFallback("")

        // Case 1: MULTIPLE: son, doctor
        if (sanitized.startsWith("multiple:")) {
            val relations = sanitized.removePrefix("multiple:").split(",").map { it.trim().removeSuffix(".") }
            val matched = contacts.filter { c -> 
                relations.any { rel -> 
                    c.relation.equals(rel, ignoreCase = true) || c.displayName.equals(rel, ignoreCase = true) 
                } 
            }
            return if (matched.isNotEmpty()) AgentDecision.MultipleFound(matched)
            else AgentDecision.NeedsMoreInfo(reply)
        }

        // Case 2: single relation word or name matched exactly
        val singleContact = contacts.firstOrNull { 
            it.relation.equals(sanitized, ignoreCase = true) || it.displayName.equals(sanitized, ignoreCase = true)
        }
        if (singleContact != null) return AgentDecision.Contact(singleContact)

        // Case 3: Gemini returned a clarifying question
        return AgentDecision.NeedsMoreInfo(reply)
    }

    /** Last resort when Gemini is unreachable. */
    private fun localFallback(transcript: String): AgentDecision {
        val candidates = localMatcher.matchByName(transcript)
        return when {
            candidates.size == 1 -> AgentDecision.Contact(candidates.first())
            candidates.size > 1  -> AgentDecision.MultipleFound(candidates)
            else -> AgentDecision.NeedsMoreInfo(
                "I did not understand. What is the name of the person you want to call?"
            )
        }
    }

    // ── Decision types ────────────────────────────────────────────────────────

    sealed interface AgentDecision {
        /** Confident single-contact match. */
        data class Contact(val trustedContact: TrustedContact) : AgentDecision
        /** Multiple contacts could match — show all to the elder. */
        data class MultipleFound(val candidates: List<TrustedContact>) : AgentDecision
        /** Agent needs more info — message is a question to show the elder. */
        data class NeedsMoreInfo(val question: String) : AgentDecision
        /** Elder said "no" / "that's not them". */
        data object RejectedContact : AgentDecision
        /** Emergency words detected. */
        data object Emergency : AgentDecision
        /** Completely unresolvable even after Gemini. */
        data object NoMatch : AgentDecision
    }
}
