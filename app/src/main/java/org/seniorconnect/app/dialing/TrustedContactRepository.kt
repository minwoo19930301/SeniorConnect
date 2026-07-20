package org.seniorconnect.app.dialing

import android.content.Context

class TrustedContactRepository(context: Context) {
    private val preferences = context.getSharedPreferences(
        "trusted_call_contacts",
        Context.MODE_PRIVATE,
    )

    private val slots = listOf(
        ContactSlot("contact_son", "son", "Son"),
        ContactSlot("contact_daughter", "daughter", "Daughter"),
        ContactSlot("contact_doctor", "doctor", "Doctor"),
        ContactSlot("contact_caregiver", "caregiver", "Caregiver"),
    )

    fun all(): List<TrustedContact> = slots.map { slot ->
        TrustedContact(
            id = slot.id,
            relation = slot.relation,
            displayName = preferences.getString("${slot.id}_name", slot.defaultName) ?: slot.defaultName,
            phoneNumber = preferences.getString("${slot.id}_phone", "").orEmpty(),
        )
    }

    fun findById(id: String): TrustedContact? = all().firstOrNull { it.id == id }

    fun findByRelation(relation: String): TrustedContact? =
        all().firstOrNull { it.relation.equals(relation, ignoreCase = true) }

    fun saveContact(contactId: String, displayName: String, phoneNumber: String): TrustedContact? {
        val slot = slots.firstOrNull { it.id == contactId } ?: return null
        preferences.edit()
            .putString("${slot.id}_name", displayName.ifBlank { slot.defaultName })
            .putString("${slot.id}_phone", phoneNumber.filter { it.isDigit() || it == '+' })
            .apply()
        return findById(contactId)
    }

    private data class ContactSlot(
        val id: String,
        val relation: String,
        val defaultName: String,
    )
}
