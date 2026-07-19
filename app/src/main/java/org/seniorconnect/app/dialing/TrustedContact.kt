package org.seniorconnect.app.dialing

data class TrustedContact(
    val id: String,
    val relation: String,
    val displayName: String,
    val phoneNumber: String,
)
