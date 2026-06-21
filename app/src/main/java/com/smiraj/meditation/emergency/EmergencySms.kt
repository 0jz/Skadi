package com.smiraj.meditation.emergency

import android.content.Context
import android.telephony.SmsManager

object EmergencySms {
    fun sendToEmergencyContact(context: Context) {
        val contact = EmergencyContactStore.get(context)
        send(contact.phone, EmergencyContact.MESSAGE)
    }

    fun send(phone: String, message: String = EmergencyContact.MESSAGE) {
        SmsManager.getDefault().sendTextMessage(
            phone,
            null,
            message,
            null,
            null,
        )
    }
}
