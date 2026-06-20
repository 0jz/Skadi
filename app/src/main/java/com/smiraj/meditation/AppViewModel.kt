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
import com.smiraj.meditation.scan.DeviceCheckItem
import com.smiraj.meditation.scan.DeviceSection
import com.smiraj.meditation.scan.FindingSeverity
import com.smiraj.meditation.scan.LeciReport
import com.smiraj.meditation.scan.LocationSection
import com.smiraj.meditation.scan.PackageScanner
import com.smiraj.meditation.scan.PreflightResult
import com.smiraj.meditation.scan.ScanSnapshot
import com.smiraj.meditation.scan.SpecialAccessChecker
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
    private val specialAccessChecker = SpecialAccessChecker(app)

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
     * Runs real special-access preflight, then builds LeciReport from scan
     * results and navigates to the appropriate screen.
     *
     * If [PreflightResult.BlockedByAccessibilityRisk]: go to PreflightBlocked.
     * Otherwise: build report and go to Safety.
     */
    fun confirmSafetyGate() {
        val specialAccess = specialAccessChecker.check()
        val preflight = when {
            specialAccess.hasBlockingRisk    -> PreflightResult.BlockedByAccessibilityRisk
            specialAccess.hasGuidedAuditRisk -> PreflightResult.NeedsGuidedAudit
            else                             -> PreflightResult.Clear
        }

        if (preflight == PreflightResult.BlockedByAccessibilityRisk) {
            _screen.value = Screen.PreflightBlocked
            return
        }

        val snapshot = _scanSnapshot.value

        val appsSection = AppsSection(
            findings = snapshot.findings,
            ready = snapshot.ranAtMillis > 0,
        )

        val locationApps = snapshot.findings
            .filter { f -> f.signals.any { it.startsWith("Lokacija") } }
            .map { it.appName }
        val locationSection = LocationSection(
            appsWithLocation = locationApps,
            ready = snapshot.ranAtMillis > 0,
        )

        val deviceItems = buildDeviceCheckItems(specialAccess)
        val deviceSection = DeviceSection(
            checkItems = deviceItems,
            ready = true,
        )

        _leciReport.value = LeciReport.demo().copy(
            preflight = preflight,
            apps = appsSection,
            location = locationSection,
            device = deviceSection,
        )

        _screen.value = Screen.Safety
    }

    /**
     * Converts [SpecialAccessChecker.Result] into [DeviceCheckItem] list
     * for display in the Leči report DeviceSection.
     * No destructive actions — guidance only.
     */
    private fun buildDeviceCheckItems(
        result: SpecialAccessChecker.Result,
    ): List<DeviceCheckItem> {
        val items = mutableListOf<DeviceCheckItem>()

        result.notificationListeners.forEach { app ->
            items += DeviceCheckItem(
                label = "Pristup obaveštenjima: ${app.appName}",
                guidance = "Podešavanja → Aplikacije → Poseban pristup → " +
                    "Pristup obaveštenjima → ukloni ako ne prepoznaješ.",
                severity = FindingSeverity.Medium,
            )
        }

        result.deviceAdmins.forEach { app ->
            items += DeviceCheckItem(
                label = "Admin uređaja: ${app.appName}",
                guidance = "Podešavanja → Bezbednost → Admin aplikacije uređaja → " +
                    "deaktiviraj ako ne prepoznaješ.",
                severity = FindingSeverity.Medium,
            )
        }

        // Always add usage-access guidance (we can't read it without the permission)
        items += DeviceCheckItem(
            label = "Pristup korišćenju aplikacija",
            guidance = "Podešavanja → Aplikacije → Poseban pristup → " +
                "Pristup podacima o korišćenju → proveri nepoznate aplikacije.",
            severity = FindingSeverity.Low,
        )

        return items
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
