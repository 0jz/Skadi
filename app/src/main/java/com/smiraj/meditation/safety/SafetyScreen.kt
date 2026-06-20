package com.smiraj.meditation.safety

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smiraj.meditation.R
import com.smiraj.meditation.scan.AccountEntry
import com.smiraj.meditation.scan.AppsSection
import com.smiraj.meditation.scan.AccountsSection
import com.smiraj.meditation.scan.DeviceSection
import com.smiraj.meditation.scan.Finding
import com.smiraj.meditation.scan.FindingSeverity
import com.smiraj.meditation.scan.LeciReport
import com.smiraj.meditation.scan.LocationSection
import com.smiraj.meditation.scan.PreflightResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyScreen(
    report: LeciReport,
    mode: SafetyMode,
    onModeChange: (SafetyMode) -> Unit,
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
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // Preflight guided-audit banner (shown when NeedsGuidedAudit)
            if (report.preflight == PreflightResult.NeedsGuidedAudit) {
                item { GuidedAuditBanner() }
            }

            // Mode toggle
            item {
                ModePicker(mode = mode, onModeChange = onModeChange)
            }

            when (mode) {
                SafetyMode.Heal -> {
                    item {
                        Text(
                            stringResource(R.string.leci_intro),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    // Section 1: Apps
                    item {
                        ReportSectionCard(
                            icon = Icons.Filled.Lock,
                            title = stringResource(R.string.leci_apps_title),
                            content = {
                                AppsContent(report.apps)
                            },
                        )
                    }
                    // Section 2: Accounts
                    item {
                        ReportSectionCard(
                            icon = Icons.Filled.AccountCircle,
                            title = stringResource(R.string.leci_accounts_title),
                            content = {
                                AccountsContent(report.accounts)
                            },
                        )
                    }
                    // Section 3: Location
                    item {
                        ReportSectionCard(
                            icon = Icons.Filled.Place,
                            title = stringResource(R.string.leci_location_title),
                            content = {
                                LocationContent(report.location)
                            },
                        )
                    }
                    // Section 4: Device
                    item {
                        ReportSectionCard(
                            icon = Icons.Filled.PhoneAndroid,
                            title = stringResource(R.string.leci_device_title),
                            content = {
                                DeviceContent(report.device)
                            },
                        )
                    }
                }

                SafetyMode.Cut -> {
                    item { CutPanel() }
                }
            }

            // Resource panel always visible
            item {
                ResourcePanel(
                    onCallAstra = onCallAstra,
                    onCallPolice = onCallPolice,
                )
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ---- Preflight banner ------------------------------------------------------

@Composable
private fun GuidedAuditBanner() {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.preflight_needs_audit_banner),
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

// ---- Mode picker -----------------------------------------------------------

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

// ---- Report section wrapper ------------------------------------------------

@Composable
private fun ReportSectionCard(
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit,
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            content()
        }
    }
}

// ---- Section 1: Apps -------------------------------------------------------

@Composable
private fun AppsContent(section: AppsSection) {
    if (!section.ready) {
        PlaceholderText(stringResource(R.string.leci_apps_not_ready))
        return
    }
    if (section.findings.isEmpty()) {
        Text(
            stringResource(R.string.leci_apps_no_findings),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    section.findings.forEach { AppFindingRow(it) }
}

@Composable
private fun AppFindingRow(finding: Finding) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(finding.appName, style = MaterialTheme.typography.bodyMedium)
            SeverityChip(finding.severity)
        }
        Text(
            finding.neutralSummary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ---- Section 2: Accounts ---------------------------------------------------

@Composable
private fun AccountsContent(section: AccountsSection) {
    if (!section.ready) {
        PlaceholderText(stringResource(R.string.leci_accounts_not_ready))
        return
    }
    if (section.entries.isEmpty()) {
        Text(
            stringResource(R.string.leci_accounts_no_entries),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    section.entries.forEach { AccountEntryRow(it) }
}

@Composable
private fun AccountEntryRow(entry: AccountEntry) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(entry.label, style = MaterialTheme.typography.bodyMedium)
            SeverityChip(entry.severity)
        }
        entry.riskReasons.forEach { reason ->
            Text(
                "• $reason",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ---- Section 3: Location ---------------------------------------------------

@Composable
private fun LocationContent(section: LocationSection) {
    if (!section.ready) {
        PlaceholderText(stringResource(R.string.leci_location_not_ready))
        return
    }
    if (section.appsWithLocation.isEmpty()) {
        Text(
            stringResource(R.string.leci_location_no_apps),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    section.appsWithLocation.forEach { appName ->
        Text(
            "• $appName",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ---- Section 4: Device -----------------------------------------------------

@Composable
private fun DeviceContent(section: DeviceSection) {
    if (!section.ready) {
        PlaceholderText(stringResource(R.string.leci_device_not_ready))
        return
    }
    if (section.checkItems.isEmpty()) {
        Text(
            stringResource(R.string.leci_device_no_items),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    section.checkItems.forEach { item ->
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(item.label, style = MaterialTheme.typography.bodyMedium)
                SeverityChip(item.severity)
            }
            Text(
                item.guidance,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ---- Seči panel (unchanged, full rework in cut-* branches) ----------------

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

// ---- Resource panel --------------------------------------------------------

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

// ---- Shared helpers --------------------------------------------------------

@Composable
private fun PlaceholderText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SeverityChip(severity: FindingSeverity) {
    val (label, color) = when (severity) {
        FindingSeverity.Low -> stringResource(R.string.risk_low) to Color(0xFF2E7D32)
        FindingSeverity.Medium -> stringResource(R.string.risk_medium) to Color(0xFFF9A825)
        FindingSeverity.High -> stringResource(R.string.risk_high) to Color(0xFFC62828)
    }
    Surface(
        color = color.copy(alpha = 0.14f),
        contentColor = color,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
