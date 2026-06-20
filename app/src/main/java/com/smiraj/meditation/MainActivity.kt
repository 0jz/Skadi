package com.smiraj.meditation

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smiraj.meditation.data.Ambient
import com.smiraj.meditation.history.HistoryScreen
import com.smiraj.meditation.meditation.AmbientPlayer
import com.smiraj.meditation.meditation.MeditationScreen
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
                onKeepScreenOnChange = vm::setKeepScreenOn,
                modifier = contentModifier,
            )
        }
    }
}
