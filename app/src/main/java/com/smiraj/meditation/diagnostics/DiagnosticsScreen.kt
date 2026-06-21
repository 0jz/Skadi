package com.smiraj.meditation.diagnostics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smiraj.meditation.R
import com.smiraj.meditation.scan.BleTrackerFinding
import com.smiraj.meditation.scan.Finding
import com.smiraj.meditation.scan.FindingSeverity
import com.smiraj.meditation.scan.ScanSnapshot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    snapshot: ScanSnapshot,
    isScanning: Boolean,
    onBack: () -> Unit,
    onOpenSafetyGate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.diag_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.diag_back),
                        )
                    }
                },
            )
        },
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    stringResource(R.string.diag_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            // ---- App permission scan results --------------------------------
            when {
                isScanning -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    stringResource(R.string.diag_scanning),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
                snapshot.findings.isEmpty() && snapshot.ranAtMillis > 0 -> {
                    item {
                        Text(
                            stringResource(R.string.diag_no_findings),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                snapshot.findings.isEmpty() -> {
                    item {
                        Text(
                            stringResource(R.string.diag_checking),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                else -> {
                    items(snapshot.findings) { finding ->
                        FindingCard(finding)
                    }
                }
            }

            // ---- BLE tracker scan results -----------------------------------
            item {
                BleTrackerSection(
                    trackers = snapshot.bleTrackers,
                    scanned = snapshot.bleScanned,
                    appScanDone = !isScanning,
                )
            }

            item {
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = onOpenSafetyGate,
                    enabled = !isScanning,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.diag_review_options))
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ---- BLE tracker section ---------------------------------------------------

@Composable
private fun BleTrackerSection(
    trackers: List<BleTrackerFinding>,
    scanned: Boolean,
    appScanDone: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Bluetooth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    stringResource(R.string.diag_ble_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            when {
                !appScanDone -> {
                    // Wait for pac