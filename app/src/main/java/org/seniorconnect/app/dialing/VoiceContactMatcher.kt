package org.seniorconnect.app.dialing

class VoiceContactMatcher(private val repository: TrustedContactRepository) {
    fun match(transcript: String): MatchResult {
        val normalized = transcript.lowercase()

        if (listOf("emergency", "ambulance", "police", "fire").any { normalized.contains(it) }) {
            return MatchResult.Emergency
        }

        val relation = when {
            normalized.contains("son") -> "son"
            normalized.contains("daughter") -> "daughter"
            normalized.contains("doctor") -> "doctor"
            normalized.contains("caregiver") || normalized.contains("care giver") -> "caregiver"
            else -> null
        }

        val contact = relation?.let(repository::findByRelation)
        return if (contact == null) MatchResult.NoMatch else MatchResult.Contact(contact)
    }

    sealed interface MatchResult {
        data class Contact(val trustedContact: TrustedContact) : MatchResult
        data object NoMatch : MatchResult
        data object Emergency : MatchResult
    }
}
