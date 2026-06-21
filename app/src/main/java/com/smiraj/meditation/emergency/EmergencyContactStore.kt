package com.smiraj.meditation.emergency

import android.content.Context

object EmergencyContactStore {
    private const val PREFS = "suncica_emergency_contact"
    private const val KEY_NAME = "name"
    private const val KEY_PHONE = "phone"

    fun get(context: Context): EmergencyContactInfo {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_NAME, null)
        val phone = prefs.getString(KEY_PHONE, null)
        return if (name.isNullOrBlank() || phone.isNullOrBlank()) {
            EmergencyContactInfo.Default
        } else {
            EmergencyContactInfo(name, phone)
        }
    }

    fun save(context: Context, contact: EmergencyContactInfo) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_NAME, contact.name)
            .putString(KEY_PHONE, contact.phone)
            .apply()
    }
}
