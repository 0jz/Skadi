package com.smiraj.meditation.emergency

import android.telephony.SmsManager

object EmergencySms {
    fun sendToEmergencyContact() {
        SmsManager.getDefault().sendTextMessage(
            EmergencyContact.PHONE,
            null,
            EmergencyContact.MESSAGE,
            null,
            null,
        )
    }
}
