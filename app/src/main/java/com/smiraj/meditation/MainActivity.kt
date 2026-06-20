package com.smiraj.meditation

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smiraj.meditation.data.Ambient
import com.smiraj.meditation.diagnostics.DiagnosticsScreen
import com.smiraj.meditation.history.HistoryScreen
import com.smiraj.meditation.meditation.AmbientPlayer
import com.smiraj.meditation.meditation.MeditationScreen
import com.smiraj.meditation.safety.SafetyGateScreen
import com.smiraj.meditation.safety.SafetyScreen
import com.smiraj.meditation.settings.SettingsScreen
import com.smiraj.meditation.ui.theme.SmirajTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmirajTheme {
                SmirajApp()
            }
        }
    }
}

private enum class Tab(val labelRes: Int, val icon: ImageVector) {
    Meditate(R.string.tab_meditate, Icons.Filled.Air),
    History(R.string.tab_history, Icons.Filled.BarChart),
    Settings(R.string.tab_settings, Icons.Filled.Settings),
}

@Composable
private fun SmirajApp(vm: AppViewModel = viewModel()) {
    val screen by vm.screen.collectAsStateWithLifecycle()
    val view = LocalView.current

    LaunchedEffect(screen) {
        val window = (view.context as? ComponentActivity)?.window ?: return@LaunchedEffect
        if (screen == Screen.Meditation) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) vm.exitToCover()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    when (screen) {
        Screen.Meditation -> CoverApp(vm)
        Screen.Diagnostics -> {
            BackHandler { vm.exitToCover() }
            val snapshot by vm.scanSnapshot.collectAsStateWithLifecycle()
            DiagnosticsScreen(
                snapshot = snapshot,
                onBack = vm::exitToCover,
                onOpenSafetyGate = vm::openSafetyGate,
            )
        }
        Screen.SafetyGate -> {
            BackHandler { vm.returnToDiagnostics() }
            SafetyGateScreen(
                onConfirm = vm::confirmSafetyGate,
                onCancel = vm::returnToDiagnostics,
            )
        }
        Screen.Safety -> {
            BackHandler { vm.exitToCover() }
            val context = LocalContext.current
            val snapshot by vm.scanSnapshot.collectAsStateWithLifecycle()
            val mode by vm.safetyMode.collectAsStateWithLifecycle()
            val generatedPassword by vm.generatedPassword.collectAsStateWithLifecycle()
            val healSnapshotPrepared by vm.healSnapshotPrepared.collectAsStateWithLifecycle()
            SafetyScreen(
                snapshot = snapshot,
                mode = mode,
                generatedPassword = generatedPassword,
                healSnapshotPrepared = healSnapshotPrepared,
                onModeChange = vm::setSafetyMode,
                onPrepareHealSnapshot = vm::prepareHealSnapshot,
                onRegeneratePassword = vm::regeneratePassword,
                onOpenPasswordCheckup = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PASSWORD_CHECKUP_URL)))
                },
                onCallAstra = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:0117850000"))) },
                onCallPolice = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:192"))) },
                onBack = vm::exitToCover,
            )
        }
    }
}

private const val PASSWORD_CHECKUP_URL = "https://passwords.google.com/checkup/start"

// ---- Cover (Level 1): the meditation app ----------------------------------

@Composable
private fun CoverApp(vm: AppViewModel) {
    var tab by remember { mutableStateOf(Tab.Meditate) }

    val timer by vm.timer.collectAsStateWithLifecycle()
    val sessions by vm.sessions.collectAsStateWithLifecycle()
    val totalSeconds by vm.totalSeconds.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val ambientPlayer = remember { AmbientPlayer(context) }

    // Play/stop ambient with the session (no-op until audio files are bundled).
    LaunchedEffect(timer.running, settings.ambient) {
        if (timer.running && settings.ambient != Ambient.NONE) {
            ambientPlayer.play(settings.ambient)
        } else {
            ambientPlayer.stop()
        }
    }

    // Keep the screen awake during a running session if the user opted in.
    val view = LocalView.current
    LaunchedEffect(timer.running, settings.keepScreenOn) {
        val keep = timer.running && settings.keepScreenOn
        val window = (view.context as? ComponentActivity)?.window ?: return@LaunchedEffect
        if (keep) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = { Icon(t.icon, contentDescription = null) },
                        label = { Text(stringResource(t.labelRes)) },
                    )
                }
            }
        }
    ) { inner ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(inner)
        when (tab) {
            Tab.Meditate -> MeditationScreen(
                timer = timer,
                onSelectPreset = vm::selectPreset,
                onCustomDurationEntered = vm::onCustomDurationEntered,
                onStart = vm::start,
                onStop = vm::stop,
                modifier = contentModifier,
            )
            Tab.History -> HistoryScreen(
                sessions = sessions,
                streak = vm.streak,
                totalSeconds = totalSeconds,
                modifier = contentModifier,
            )
            Tab.Settings -> SettingsScreen(
                settings = settings,
                onAmbientChange = vm::setAmbient,
                modifier = contentModifier,
            )
        }
    }
}
