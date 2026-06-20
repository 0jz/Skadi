package com.smiraj.meditation.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class Ambient { NONE, RAIN, FOREST }

data class UserSettings(
    val ambient: Ambient = Ambient.NONE,
    val keepScreenOn: Boolean = true,
)

private val Context.dataStore by preferencesDataStore(name = "smiraj_prefs")

class SettingsStore(private val context: Context) {

    private val keyAmbient = stringPreferencesKey("ambient")
    private val keyKeepOn = booleanPreferencesKey("keep_screen_on")

    val settings: Flow<UserSettings> = context.dataStore.data.map { p ->
        UserSettings(
            ambient = runCatching { Ambient.valueOf(p[keyAmbient] ?: "NONE") }
                .getOrDefault(Ambient.NONE),
            keepScreenOn = p[keyKeepOn] ?: true,
        )
    }

    suspend fun setAmbient(ambient: Ambient) {
        context.dataStore.edit { it[keyAmbient] = ambient.name }
    }

    suspend fun setKeepScreenOn(value: Boolean) {
        context.dataStore.edit { it[keyKeepOn] = value }
    }
}
