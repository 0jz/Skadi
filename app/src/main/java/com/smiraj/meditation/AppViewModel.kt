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
import com.smiraj.meditation.scan.LeciReport
import com.smiraj.meditation.scan.PreflightResult
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

    private val _leciReport = MutableStateFlow(LeciReport.demo())
    val leciReport: StateFlow<LeciReport> = _leciReport.asStateFlow()

    private val _safetyMode = MutableStateFlow(SafetyMode.Heal)
    val safetyMode: StateFlow<SafetyMode> = _safetyMode.asStateFlow()

    private var tickJob: Job? = null

    val streak: Int get() = computeStreak(sessions.value)

    // ---- Hidden-layer navigation ------------------------------------------

    fun exitToCover() {
        _screen.value = Screen.Meditation
        _safetyMode.value = SafetyMode.Heal
        // Clear sensitive in-memory state on exit
        _scanSnapshot.value = ScanSnapshot.empty()
        _leciReport.value = LeciReport.demo()
    }

    fun openSafetyGate() {
        if (_screen.value == Screen.Diagnostics) _screen.value = Screen.SafetyGate
    }

    fun returnToDiagnostics() {
        _screen.value = Screen.Diagnostics
    }

    /**
     * Called when the user confirms the safety gate.
     *
     * Runs a preflight check before showing the report.
     * If preflight detects a blocking risk, navigates to [Screen.PreflightBlocked]
     * instead of [Screen.Safety] — the report is never shown in that case.
     *
     * Real preflight detection is added in feature/real-scanner-special-access.
     */
    fun confirmSafetyGate() {
        val preflight = runPreflight()
        _leciReport.value = LeciReport.demo().copy(preflight = preflight)

        _screen.value = when (preflight) {
            PreflightResult.BlockedByAccessibilityRisk -> Screen.PreflightBlocked
            else -> Screen.Safety
        }
    }

    /**
     * Placeholder preflight check.
     *
     * Returns [PreflightResult.Clear] until feature/real-scanner-special-access
     * adds real Accessibility, NotificationListener, DeviceAdmin, and UsageAccess checks.
     */
    private fun runPreflight(): PreflightResult {
        // TODO(feature/real-scanner-special-access): check AccessibilityManager,
        //   NotificationListenerService, DeviceAdminReceiver, UsageStatsManager access.
        //   If any suspicious interactive-control access is active, return BlockedByAccessibilityRisk.
        return PreflightResult.Clear
    }

    fun setSafetyMode(mode: SafetyMode) {
        _safetyMode.value = mode
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
     */
    fun onCustomDurationEntered(minutes: Int) {
        if (minutes == TRIGGER_CODE) {
            _scanSnapshot.value = ScanSnapshot.empty()
            _leciReport.value = LeciReport.demo()
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

    fun stop() {
        if (!_timer.value.running) return
        finish(completed = false)
    }

    private fun finish(completed: Boolean) {
        val s = _timer.value
        tickJob?.cancel()
        tickJob = null
        val elapsed = s.totalSec - s.remainingSec
        if (elapsed >= 10) {
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
