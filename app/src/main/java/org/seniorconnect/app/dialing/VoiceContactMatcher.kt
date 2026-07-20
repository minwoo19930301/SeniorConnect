package org.seniorconnect.app.dialing

class VoiceContactMatcher(private val repository: TrustedContactRepository) {

    private val emergencyWords = listOf("emergency", "ambulance", "police", "fire", "911")

    // Relation aliases the elder might say
    private val relationAliases = mapOf(
        "son"       to listOf("son", "boy", "my boy", "my son"),
        "daughter"  to listOf("daughter", "girl", "my girl", "my daughter"),
        "doctor"    to listOf("doctor", "doc", "physician", "my doctor"),
        "caregiver" to listOf("caregiver", "care giver", "care taker", "caretaker", "nurse", "helper", "my caregiver"),
    )

    /** Primary match: relation keyword → single contact. */
    fun match(transcript: String): MatchResult {
        val normalized = transcript.lowercase()

        if (emergencyWords.any { normalized.contains(it) }) return MatchResult.Emergency

        // 1. Try relation-based match first
        val relation = relationAliases.entries
            .firstOrNull { (_, aliases) -> aliases.any { normalized.contains(it) } }
            ?.key
        val byRelation = relation?.let(repository::findByRelation)
        if (byRelation != null) return MatchResult.Contact(byRelation)

        // 2. Try display-name fuzzy match
        val candidates = matchByName(normalized)
        return when {
            candidates.size == 1 -> MatchResult.Contact(candidates.first())
            candidates.size > 1  -> MatchResult.Ambiguous(candidates)
            else                 -> MatchResult.NoMatch
        }
    }

    /**
     * Fuzzy name search across all saved trusted contacts.
     * Returns every contact whose displayName contains any word from the transcript.
     */
    fun matchByName(transcript: String): List<TrustedContact> {
        val words = transcript.lowercase()
            .split(Regex("\\s+"))
            .filter { it.length >= 2 }
        return repository.all().filter { contact ->
            val nameLower = contact.displayName.lowercase()
            words.any { nameLower.contains(it) }
        }
    }

    /** Check whether the transcript sounds like a rejection ("no", "not", "wrong", "different"). */
    fun isRejection(transcript: String): Boolean {
        val words = transcript.lowercase().split(Regex("\\s+"))
        val rejections = listOf("no", "not", "wrong", "different", "nope", "nah", "stop", "cancel")
        return words.any { it in rejections } || transcript.lowercase().contains("that's not")
    }

    sealed interface MatchResult {
        data class Contact(val trustedContact: TrustedContact) : MatchResult
        /** Multiple contacts matched; agent should show all candidates. */
        data class Ambiguous(val candidates: List<TrustedContact>) : MatchResult
        data object NoMatch : MatchResult
        data object Emergency : MatchResult
    }
}
