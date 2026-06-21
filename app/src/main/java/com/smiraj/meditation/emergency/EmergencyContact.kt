package com.smiraj.meditation.emergency

object EmergencyContact {
    const val NAME = "Ana"
    const val PHONE = "+381600000000"
    const val MESSAGE = "Sunčica SOS: Treba mi pomoć. Molim te pozovi me i proveri gde sam. Ako se ne javim, kontaktiraj 112."
}

data class EmergencyContactInfo(
    val name: String,
    val phone: String,
) {
    val label: String
        get() = "$name · $phone"

    companion object {
        val Default = EmergencyContactInfo(EmergencyContact.NAME, EmergencyContact.PHONE)
    }
}
