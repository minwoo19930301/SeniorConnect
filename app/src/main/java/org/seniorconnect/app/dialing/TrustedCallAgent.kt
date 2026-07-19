package org.seniorconnect.app.dialing

class TrustedCallAgent(private val localMatcher: VoiceContactMatcher) {
    fun resolveSpokenRequest(transcript: String): AgentDecision =
        when (val match = localMatcher.match(transcript)) {
            is VoiceContactMatcher.MatchResult.Contact -> AgentDecision.Contact(match.trustedContact)
            VoiceContactMatcher.MatchResult.Emergency -> AgentDecision.Emergency
            VoiceContactMatcher.MatchResult.NoMatch -> AgentDecision.NoMatch
        }

    sealed interface AgentDecision {
        data class Contact(val trustedContact: TrustedContact) : AgentDecision
        data object NoMatch : AgentDecision
        data object Emergency : AgentDecision
    }
}
