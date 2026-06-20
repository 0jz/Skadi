package com.smiraj.meditation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smiraj.meditation.data.AppDatabase
import com.smiraj.meditation.data.Session
import com.smiraj.meditation.data.SettingsStore
import com.smiraj.meditation.data.SessionRepository
import com.smiraj.meditation.data.UserSettings
import com.smiraj.meditation.data.computeStreak
import com.smiraj.meditation.safety.SafetyMode
import com.smiraj.meditation.scan.ScanSnapshot
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The breathing animation phase, derived from elapsed time. */
enum class BreathPhase { IN, OUT }

data class TimerState(
    val running: Boolean = false,
    val plannedMin: Int = 5,
    val totalSec: Int = 5 * 60,
    val remainingSec: Int = 5 * 60,
    val justFinished: Boolean = false,
) {
    val progress: Float
        get() = if (totalSec == 0) 0f else 1f - remainingSec.toFloat() / totalSec
}

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SessionRepository(AppDatabase.get(app).sessionDao())
    private val settingsStore = SettingsStore(app)

    val sessions: StateFlow<List<Session>> =
        repo.sessions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalSeconds: StateFlow<Int> =
        repo.totalSeconds.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val settings: StateFlow<UserSettings> =
        settingsStore.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    private val _timer = MutableStateFlow(TimerState())
    val timer: StateFlow<TimerState> = _timer.asStateFlow()

    private val _screen = MutableStateFlow(Screen.Meditation)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    private val _scanSnapshot = MutableStateFlow(ScanSnapshot.empty())
    val scanSnapshot: StateFlow<ScanSnapshot> = _scanSnapshot.asStateFlow()

    private val _safetyMode = MutableStateFlow(SafetyMode.Heal)
    val safetyMode: StateFlow<SafetyMode> = _safetyMode.asStateFlow()

    private val _generatedPassword = MutableStateFlow(generatePassword())
    val generatedPassword: StateFlow<String> = _generatedPassword.asStateFlow()

    private val _healSnapshotPrepared = MutableStateFlow(false)
    val healSnapshotPrepared: StateFlow<Boolean> = _healSnapshotPrepared.asStateFlow()

    private var tickJob: Job? = null

    val streak: Int get() = computeStreak(sessions.value)

    // ---- Hidden-layer navigation ------------------------------------------

    fun exitToCover() {
        _screen.value = Screen.Meditation
        _safetyMode.value = SafetyMode.Heal
        _healSnapshotPrepared.value = false
    }

    fun openSafetyGate() {
        if (_screen.value == Screen.Diagnostics) _screen.value = Screen.SafetyGate
    }

    fun returnToDiagnostics() {
        _screen.value = Screen.Diagnostics
    }

    fun confirmSafetyGate() {
        _screen.value = Screen.Safety
    }

    fun setSafetyMode(mode: SafetyMode) {
        _safetyMode.value = mode
    }

    fun prepareHealSnapshot() {
        _healSnapshotPrepared.value = true
    }

    fun regeneratePassword() {
        _generatedPassword.value = generatePassword()
    }

    companion object {
        /**
         * Default out-of-range secret code for the "Prilagodi" field. A real
         * session is coerced to at least 1 minute, so 0 cannot collide with the
         * cover app's normal behavior.
         */
        const val TRIGGER_CODE = 0
    }

    // ---- Session selection / start ----------------------------------------

    fun selectPreset(minutes: Int) {
        if (_timer.value.running) return
        _timer.value = TimerState(plannedMin = minutes, totalSec = minutes * 60, remainingSec = minutes * 60)
    }

    /**
     * SINGLE FUNNEL for the custom-duration field. Every custom value the user
     * types arrives here, parsed to minutes.
     *
     * Phase 2 (per SKADI_DESIGN_MEDITATION.md §4) adds the magic-value check at
     * the TOP of this function — if the entered value equals the user's secret
     * code, navigate to the hidden layer INSTEAD of starting a session. Keeping
     * all entry through this one method is why Phase 1 stays a clean meditation
     * app and the trigger is a small, isolated addition later.
     */
    fun onCustomDurationEntered(minutes: Int) {
        if (minutes == TRIGGER_CODE) {
            _scanSnapshot.value = ScanSnapshot.empty()
            _screen.value = Screen.Diagnostics
            return
        }

        val safe = minutes.coerceIn(1, 180)
        selectPreset(safe)
        start()
    }

    fun start() {
        if (_timer.value.running) return
        val s = _timer.value
        _timer.value = s.copy(running = true, remainingSec = s.totalSec, justFinished = false)
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (_timer.value.remainingSec > 0) {
                delay(1000)
                val cur = _timer.value
                if (!cur.running) return@launch
                _timer.value = cur.copy(remainingSec = (cur.remainingSec - 1).coerceAtLeast(0))
            }
            finish(completed = true)
        }
    }

    /** User-initiated stop before the timer ends. Records partial session. */
    fun stop() {
        if (!_timer.value.running) return
        finish(completed = false)
    }

    private fun finish(completed: Boolean) {
        val s = _timer.value
        tickJob?.cancel()
        tickJob = null
        val elapsed = s.totalSec - s.remainingSec
        if (elapsed >= 10) { // ignore accidental taps under 10s
            viewModelScope.launch { repo.record(elapsed, s.plannedMin, completed) }
        }
        _timer.value = s.copy(running = false, remainingSec = s.totalSec, justFinished = completed)
    }

    fun clearFinishedFlag() {
        _timer.value = _timer.value.copy(justFinished = false)
    }

    // ---- Settings ----------------------------------------------------------

    fun setAmbient(ambient: com.smiraj.meditation.data.Ambient) {
        viewModelScope.launch { settingsStore.setAmbient(ambient) }
    }

    fun setKeepScreenOn(value: Boolean) {
        viewModelScope.launch { settingsStore.setKeepScreenOn(value) }
    }
}

private fun generatePassword(): String {
    val lower = "abcdefghijkmnopqrstuvwxyz"
    val upper = "ABCDEFGHJKLMNPQRSTUVWXYZ"
    val digits = "23456789"
    val symbols = "!@#%*?"
    val all = lower + upper + digits + symbols
    val required = listOf(lower.random(), upper.random(), digits.random(), symbols.random())
    return (required + List(12) { all.random() })
        .shuffled()
        .joinToString("")
}
