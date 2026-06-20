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
import com.smiraj.meditation.scan.AppsSection
import com.smiraj.meditation.scan.LeciReport
import com.smiraj.meditation.scan.LocationSection
import com.smiraj.meditation.scan.PackageScanner
import com.smiraj.meditation.scan.PreflightResult
import com.smiraj.meditation.scan.ScanSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private val packageScanner = PackageScanner(app)

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

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

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
     * Triggered when the user enters the secret code.
     * Resets state and runs the package scanner in the background so results
     * are ready by the time the user reaches the Safety screen.
     */
    fun enterDiagnostics() {
        _scanSnapshot.value = ScanSnapshot.empty()
        _leciReport.value = LeciReport.demo()
        _screen.value = Screen.Diagnostics
        launchScan()
    }

    private fun launchScan() {
        viewModelScope.launch {
            _isScanning.value = true
            val findings = withContext(Dispatchers.Default) {
                packageScanner.scan()
            }
            _scanSnapshot.value = ScanSnapshot(
                findings = findings,
                ranAtMillis = System.currentTimeMillis(),
            )
            _isScanning.value = false
        }
    }

    /**
     * Runs preflight then opens the report.
     * Builds LeciReport from current scan results before navigating.
     */
    fun confirmSafetyGate() {
        val preflight = runPreflight()
        val snapshot = _scanSnapshot.value

        // Build AppsSection from real scan results
        val appsSection = AppsSection(
            findings = snapshot.findings,
            ready = snapshot.ranAtMillis > 0,
        )

        // LocationSection: derive app names with location permission from findings
        val locationApps = snapshot.findings
            .filter { f -> f.signals.any { it.startsWith("Lokacija") } }
            .map { it.appName }
        val locationSection = LocationSection(
            appsWithLocation = locationApps,
            ready = snapshot.ranAtMillis > 0,
        )

        _leciReport.value = LeciReport.demo().copy(
            preflight = preflight,
            apps = appsSection,
            location = locationSection,
        )

        _screen.value = when (preflight) {
            PreflightResult.BlockedByAccessibilityRisk -> Screen.PreflightBlocked
            else -> Screen.Safety
        }
    }

    /**
     * Placeholder preflight — returns Clear until feature/real-scanner-special-access
     * adds real Accessibility / NotificationListener / DeviceAdmin checks.
     */
    private fun runPreflight(): PreflightResult {
        // TODO(feature/real-scanner-special-access)
        return PreflightResult.Clear
    }

    fun setSafetyMode(mode: SafetyMode) {
        _safetyMode.value = mode
    }

    companion object {
        const val TRIGGER_CODE = 0
    }

    // ---- Session selection / start ----------------------------------------

    fun selectPreset(minutes: Int) {
        if (_timer.value.running) return
        _timer.value = TimerState(plannedMin = minutes, totalSec = minutes * 60, remainingSec = minutes * 60)
    }

    fun onCustomDurationEntered(minutes: Int) {
        if (minutes == TRIGGER_CODE) {
            enterDiagnostics()
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
