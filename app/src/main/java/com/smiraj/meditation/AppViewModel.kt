package com.smiraj.meditation

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smiraj.meditation.data.AppDatabase
import com.smiraj.meditation.data.Session
import com.smiraj.meditation.data.SettingsStore
import com.smiraj.meditation.data.SessionRepository
import com.smiraj.meditation.data.UserSettings
import com.smiraj.meditation.data.computeStreak
import com.smiraj.meditation.safety.SafetyMode
import com.smiraj.meditation.scan.AccountAudit
import com.smiraj.meditation.scan.AccountsSection
import com.smiraj.meditation.scan.AppsSection
import com.smiraj.meditation.scan.CsvPasswordImporter
import com.smiraj.meditation.scan.DeviceAudit
import com.smiraj.meditation.scan.DeviceSection
import com.smiraj.meditation.scan.FindingSeverity
import com.smiraj.meditation.scan.LeciReport
import com.smiraj.meditation.scan.LocationAudit
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

    /** True while a CSV is being parsed from a content URI. */
    private val _csvImporting = MutableStateFlow(false)
    val csvImporting: StateFlow<Boolean> = _csvImporting.asStateFlow()

    private var tickJob: Job? = null

    val streak: Int get() = computeStreak(sessions.value)

    // ---- Hidden-layer navigation ------------------------------------------

    fun exitToCover() {
        _screen.value = Screen.Meditation
        _safetyMode.value = SafetyMode.Heal
        _scanSnapshot.value = ScanSnapshot.empty()
        // Wipe sensitive report data (account entries, passwords) from memory
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
     * Runs real special-access preflight, builds the full Leči report.
     * Demo accounts are loaded immediately. CSV import via [loadDemoCsv] or
     * [importCsvFromUri] merges additional accounts into the existing list.
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

        val locationApps = snapshot.findings
            .filter { f -> f.signals.any { it.startsWith("Lokacija") } }
            .map { it.appName }

        _leciReport.value = LeciReport.demo().copy(
            preflight = preflight,
            apps = AppsSection(findings = snapshot.findings, ready = snapshot.ranAtMillis > 0),
            // Accounts: start with demo entries visible immediately.
            // CSV import via loadDemoCsv() / importCsvFromUri() merges additional entries.
            accounts = AccountsSection(entries = AccountAudit.demoAccounts(), ready = true),
            location = LocationSection(
                appsWithLocation = locationApps,
                familyFindings = LocationAudit.demoFindings(),
                coarsenedMessage = LocationAudit.coarsenedMessage(),
                ready = true,
            ),
            device = DeviceSection(checkItems = DeviceAudit.buildCheckItems(specialAccess), ready = true),
        )

        _screen.value = Screen.Safety
    }

    // ---- CSV import --------------------------------------------------------

    /**
     * Load the bundled demo CSV from assets and populate the accounts section.
     * Shows the same set of entries on every demo run.
     */
    fun loadDemoCsv() {
        val entries = CsvPasswordImporter.demo(getApplication())
        updateAccountsFromCsv(entries)
    }

    /**
     * Parse a user-selected CSV file from a content URI and populate the
     * accounts section. File is read once on an IO thread; plaintext is not
     * written back to disk.
     */
    fun importCsvFromUri(uri: Uri) {
        viewModelScope.launch {
            _csvImporting.value = true
            val entries = withContext(Dispatchers.IO) {
                CsvPasswordImporter.fromUri(getApplication(), uri)
            }
            updateAccountsFromCsv(entries)
            _csvImporting.value = false
        }
    }

    /**
     * Merge CSV-derived entries with whatever accounts are already in the report.
     * Accounts whose label (case-insensitive) already exists are skipped to avoid
     * duplicates; new ones are appended at the end.
     */
    private fun updateAccountsFromCsv(entries: List<com.smiraj.meditation.scan.CsvPasswordEntry>) {
        val csvEntries = if (entries.isEmpty()) emptyList()
        else AccountAudit.fromCsvEntries(entries)

        val existing = _leciReport.value.accounts.entries
        val existingLabels = existing.map { it.label.lowercase() }.toSet()
        val toAdd = csvEntries.filter { it.label.lowercase() !in existingLabels }
        val merged = existing + toAdd

        _leciReport.value = _leciReport.value.copy(
            accounts = AccountsSection(entries = merged, ready = true),
        )
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
