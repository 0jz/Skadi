package com.smiraj.meditation.emergency

import android.content.Context
import android.content.Intent
import android.net.Uri

object EmergencySms {
    fun composerIntent(context: Context): Intent {
        val contact = EmergencyContactStore.get(context)
        val phone = contact.phone.filter { it.isDigit() || it == '+' }
        return Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")).apply {
            putExtra("sms_body", EmergencyContact.MESSAGE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
