package com.smiraj.meditation

import android.Manifest
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smiraj.meditation.diagnostics.DiagnosticsScreen
import com.smiraj.meditation.emergency.EmergencySms
import com.smiraj.meditation.safety.PreflightBlockedScreen
import com.smiraj.meditation.safety.SafetyGateScreen
import com.smiraj.meditation.safety.SafetyScreen
import com.smiraj.meditation.suncica.BlackoutScreen
import com.smiraj.meditation.suncica.SafeApp
import com.smiraj.meditation.ui.theme.SmirajTheme
import com.smiraj.meditation.weather.PinGateScreen
import com.smiraj.meditation.weather.WeatherScreen

class MainActivity : FragmentActivity() {
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

@Composable
private fun SmirajApp(vm: AppViewModel = viewModel()) {
    val screen by vm.screen.collectAsStateWithLifecycle()
    val view = LocalView.current
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        if (grants[Manifest.permission.READ_CONTACTS] == true) {
            vm.loadDeviceContacts()
        }
    }

    val sendEmergencySms: () -> Unit = {
        runCatching {
            context.startActivity(EmergencySms.composerIntent(context))
        }.onFailure {
            Toast.makeText(context, "Poruke nisu dostupne", Toast.LENGTH_SHORT).show()
        }
    }

    val callNumber: (String) -> Unit = { number ->
        val uri = Uri.parse("tel:$number")
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            runCatching {
                context.startActivity(Intent(Intent.ACTION_CALL, uri))
            }.onFailure {
                context.startActivity(Intent(Intent.ACTION_DIAL, uri))
            }
        } else {
            context.startActivity(Intent(Intent.ACTION_DIAL, uri))
        }
    }

    val loadContacts: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            vm.loadDeviceContacts()
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.READ_CONTACTS))
        }
    }

    LaunchedEffect(screen) {
        val window = (view.context as? FragmentActivity)?.window ?: return@LaunchedEffect
        if (screen == Screen.Meditation) {
            // Cover (weather decoy) — behaves like a normal app, no FLAG_SECURE.
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) vm.lockToPinGate()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    when (screen) {
        Screen.PinGate -> {
            BackHandler { vm.exitToCover() }
            PinGateScreen(
                onSubmitPin = vm::submitLaunchPin,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Layer 1 — public face: the Sunčica weather decoy.
        Screen.Meditation -> WeatherScreen(
            modifier = Modifier.fillMaxSize(),
        )

        // Layer 2 — hidden real app: themed 5-tab safety app.
        Screen.SafeApp -> {
            BackHandler { vm.exitToCover() }
            val snapshot by vm.scanSnapshot.collectAsStateWithLifecycle()
            val scanHistory by vm.scanHistory.collectAsStateWithLifecycle()
            val isScanning by vm.isScanning.collectAsStateWithLifecycle()
            val emergencyContact by vm.emergencyContact.collectAsStateWithLifecycle()
            val deviceContacts by vm.deviceContacts.collectAsStateWithLifecycle()
            SafeApp(
                isScanning = isScanning,
                scanSnapshot = snapshot,
                scanHistory = scanHistory,
                emergencyContact = emergencyContact,
                deviceContacts = deviceContacts,
                onScan = vm::startScan,
                onDial = callNumber,
                onImportEmergencyContact = vm::selectEmergencyContact,
                onLoadContacts = loadContacts,
                onSendEmergencyMessage = sendEmergencySms,
                onEmergency = {
                    sendEmergencySms()
                    vm.triggerEmergencyBlackout()
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        Screen.Blackout -> {
            BackHandler { vm.exitToCover() }
            BlackoutScreen(modifier = Modifier.fillMaxSize())
        }

        Screen.Diagnostics -> {
            BackHandler { vm.exitToCover() }
            val snapshot by vm.scanSnapshot.collectAsStateWithLifecycle()
            val isScanning by vm.isScanning.collectAsStateWithLifecycle()
            DiagnosticsScreen(
                snapshot = snapshot,
                isScanning = isScanning,
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

        Screen.PreflightBlocked -> {
            BackHandler { vm.exitToCover() }
            PreflightBlockedScreen(onReturnToCover = vm::exitToCover)
        }

        Screen.Safety -> {
            BackHandler { vm.exitToCover() }
            val context = LocalContext.current
            val report by vm.leciReport.collectAsStateWithLifecycle()
            val mode by vm.safetyMode.collectAsStateWithLifecycle()
            val csvImporting by vm.csvImporting.collectAsStateWithLifecycle()
            SafetyScreen(
                report = report,
                mode = mode,
                onModeChange = vm::setSafetyMode,
                onCallAstra = {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:0117850000")))
                },
                onCallPolice = {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:192")))
                },
                onBack = vm::exitToCover,
                onLoadDemoCsv = vm::loadDemoCsv,
                onImportCsv = vm::importCsvFromUri,
                csvImporting = csvImporting,
            )
        }
    }
}
