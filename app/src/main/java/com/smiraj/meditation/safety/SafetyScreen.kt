package com.smiraj.meditation.safety

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smiraj.meditation.R
import com.smiraj.meditation.scan.Finding
import com.smiraj.meditation.scan.ScanSnapshot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyScreen(
    snapshot: ScanSnapshot,
    mode: SafetyMode,
    healSnapshotPrepared: Boolean,
    onModeChange: (SafetyMode) -> Unit,
    onPrepareHealSnapshot: () -> Unit,
    onCallAstra: () -> Unit,
    onCallPolice: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.safety_title)) },
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    stringResource(R.string.safety_intro),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            items(snapshot.findings) { finding ->
                SafetyFindingCard(finding)
            }
            item {
                ModePicker(mode = mode, onModeChange = onModeChange)
            }
            item {
                when (mode) {
                    SafetyMode.Heal -> HealPanel(
                        prepared = healSnapshotPrepared,
                        onPrepareSnapshot = onPrepareHealSnapshot,
                    )
                    SafetyMode.Cut -> CutPanel(
                    )
                }
            }
            item {
                ResourcePanel(
                    onCallAstra = onCallAstra,
                    onCallPolice = onCallPolice,
                )
            }
        }
    }
}

@Composable
private fun SafetyFindingCard(finding: Finding) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(finding.appName, style = MaterialTheme.typography.titleMedium)
            Text(finding.safetySummary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ModePicker(
    mode: SafetyMode,
    onModeChange: (SafetyMode) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        FilterChip(
            selected = mode == SafetyMode.Heal,
            onClick = { onModeChange(SafetyMode.Heal) },
            label = { Text(stringResource(R.string.mode_heal)) },
        )
        FilterChip(
            selected = mode == SafetyMode.Cut,
            onClick = { onModeChange(SafetyMode.Cut) },
            label = { Text(stringResource(R.string.mode_cut)) },
        )
    }
}

@Composable
private fun HealPanel(
    prepared: Boolean,
    onPrepareSnapshot: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.heal_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.heal_body), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.heal_map_stub), style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onPrepareSnapshot, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (prepared) {
                        stringResource(R.string.heal_snapshot_ready)
                    } else {
                        stringResource(R.string.heal_prepare_snapshot)
                    }
                )
            }
        }
    }
}

@Composable
private fun CutPanel() {
    val steps = listOf(
        R.string.cut_step_document,
        R.string.cut_step_passwords,
        R.string.cut_step_sessions,
        R.string.cut_step_remove,
        R.string.cut_step_support,
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.cut_title), style = MaterialTheme.typography.titleMedium)
            steps.forEachIndexed { index, label ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        stringResource(label),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ResourcePanel(
    onCallAstra: () -> Unit,
    onCallPolice: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.resources_title), style = MaterialTheme.typography.titleMedium)
            OutlinedButton(onClick = onCallAstra, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.call_astra))
            }
            OutlinedButton(onClick = onCallPolice, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.call_police))
            }
        }
    }
}
