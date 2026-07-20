package org.seniorconnect.app.dialing

class DialingPolicy(private val repository: TrustedContactRepository) {
    fun canOpenDialer(contactId: String, elderConfirmed: Boolean): PolicyDecision {
        if (!elderConfirmed) {
            return PolicyDecision.Deny("No call was opened.")
        }

        val contact = repository.findById(contactId)
            ?: return PolicyDecision.Deny("I could not find that trusted contact.")

        if (!contact.id.startsWith("contact_")) {
            return PolicyDecision.Deny("That contact is not allowed.")
        }

        if (contact.phoneNumber.isBlank()) {
            return PolicyDecision.Deny("That trusted contact has no saved phone number.")
        }

        return PolicyDecision.Allow(contact)
    }

    sealed interface PolicyDecision {
        data class Allow(val contact: TrustedContact) : PolicyDecision
        data class Deny(val message: String) : PolicyDecision
    }
}
