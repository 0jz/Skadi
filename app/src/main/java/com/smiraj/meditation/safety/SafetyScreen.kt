package com.smiraj.meditation.safety

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smiraj.meditation.R
import com.smiraj.meditation.scan.AccountEntry
import com.smiraj.meditation.scan.AccountsSection
import com.smiraj.meditation.scan.AppsSection
import com.smiraj.meditation.scan.DeviceSection
import com.smiraj.meditation.scan.Finding
import com.smiraj.meditation.scan.FindingSeverity
import com.smiraj.meditation.scan.LeciReport
import com.smiraj.meditation.scan.LocationFinding
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
    onLoadDemoCsv: () -> Unit,
    onImportCsv: (Uri) -> Unit,
    csvImporting: Boolean = false,
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

            if (report.preflight == PreflightResult.NeedsGuidedAudit) {
                item { GuidedAuditBanner() }
            }

            item {
                ResourcePanel(onCallAstra = onCallAstra, onCallPolice = onCallPolice)
            }

            item { ModePicker(mode = mode, onModeChange = onModeChange) }

            when (mode) {
                SafetyMode.Heal -> {
                    item {
                        Text(
                            stringResource(R.string.leci_intro),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    item {
                        ReportSectionCard(
                            icon = Icons.Filled.Lock,
                            title = stringResource(R.string.leci_apps_title),
                            content = { AppsContent(report.apps) },
                        )
                    }
                    item {
                        ReportSectionCard(
                            icon = Icons.Filled.AccountCircle,
                            title = stringResource(R.string.leci_accounts_title),
                            content = {
                                AccountsContent(
                                    section = report.accounts,
                                    csvImporting = csvImporting,
                                    onLoadDemoCsv = onLoadDemoCsv,
                                    onImportCsv = onImportCsv,
                                )
                            },
                        )
                    }
                    item {
                        ReportSectionCard(
                            icon = Icons.Filled.Place,
                            title = stringResource(R.string.leci_location_title),
                            content = { LocationContent(report.location) },
                        )
                    }
                    item {
                        ReportSectionCard(
                            icon = Icons.Filled.PhoneAndroid,
                            title = stringResource(R.string.leci_device_title),
                            content = { DeviceContent(report.device) },
                        )
                    }
                }

                SafetyMode.Cut -> {
                    item {
                        CutPanel(
                            accounts = report.accounts.entries,
                            accountsReady = report.accounts.ready,
                            csvImporting = csvImporting,
                            onLoadDemoCsv = onLoadDemoCsv,
                            onImportCsv = onImportCsv,
                            locationFindings = report.location.familyFindings,
                        )
                    }
                }
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
private fun ModePicker(mode: SafetyMode, onModeChange: (SafetyMode) -> Unit) {
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
private fun ReportSectionCard(icon: ImageVector, title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            content()
        }
    }
}

// ---- Section 1: Apps -------------------------------------------------------

@Composable
private fun AppsContent(section: AppsSection) {
    if (!section.ready) { PlaceholderText(stringResource(R.string.leci_apps_not_ready)); return }
    if (section.findings.isEmpty()) {
        Text(stringResource(R.string.leci_apps_no_findings),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    section.findings.forEach { AppFindingRow(it) }
}

@Composable
private fun AppFindingRow(finding: Finding) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(finding.appName, style = MaterialTheme.typography.bodyMedium)
            SeverityChip(finding.severity)
        }
        Text(finding.neutralSummary, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ---- Section 2: Accounts ---------------------------------------------------

@Composable
private fun AccountsContent(
    section: AccountsSection,
    csvImporting: Boolean,
    onLoadDemoCsv: () -> Unit,
    onImportCsv: (Uri) -> Unit,
) {
    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { onImportCsv(it) } }

    if (section.entries.isNotEmpty()) {
        section.entries.forEachIndexed { index, entry ->
            if (index > 0) Spacer(Modifier.height(12.dp))
            AccountEntryRow(entry)
        }
        Spacer(Modifier.height(10.dp))
    } else {
        PlaceholderText(stringResource(R.string.leci_accounts_not_ready))
        Spacer(Modifier.height(10.dp))
    }

    if (csvImporting) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CircularProgressIndicator(modifier = Modifier
                .height(18.dp)
                .width(18.dp), strokeWidth = 2.dp)
            Text(stringResource(R.string.csv_importing), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        Text(
            stringResource(R.string.csv_add_hint),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onLoadDemoCsv) {
                Text(stringResource(R.string.csv_load_demo))
            }
            OutlinedButton(onClick = { csvLauncher.launch(arrayOf("text/*", "*/*")) }) {
                Icon(Icons.Filled.FileOpen, contentDescription = null,
                    modifier = Modifier.padding(end = 6.dp))
                Text(stringResource(R.string.csv_import))
            }
        }
    }
}

@Composable
private fun AccountEntryRow(entry: AccountEntry) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.label, style = MaterialTheme.typography.bodyMedium)
                if (entry.username != null) {
                    Text(
                        maskUsername(entry.username),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            SeverityChip(entry.severity)
        }
        entry.riskReasons.forEach { reason ->
            Text("• $reason", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (entry.suggestedPassword != null) {
            Text(
                stringResource(R.string.account_suggested_password, entry.suggestedPassword),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

private fun maskUsername(username: String): String {
    if (username.contains("@")) {
        val (local, domain) = username.split("@", limit = 2)
        val maskedLocal = if (local.length <= 2) local
        else local.take(2) + "*".repeat((local.length - 2).coerceAtMost(4))
        return "$maskedLocal@$domain"
    }
    return if (username.length <= 3) username else username.take(2) + "***"
}

// ---- Section 3: Location ---------------------------------------------------

@Composable
private fun LocationContent(section: LocationSection) {
    if (!section.ready) { PlaceholderText(stringResource(R.string.leci_location_not_ready)); return }

    // 1. Apps CURRENTLY using location (last 5 min via AppOpsManager) — highest priority
    if (section.activeLocationApps.isNotEmpty()) {
        ActiveLocationRisk(
            sources = section.activeLocationApps,
            titleRes = R.string.leci_location_active_now_title,
            bodyRes = R.string.leci_location_active_now_body,
        )
        Spacer(Modifier.height(12.dp))
    }

    // 2. All apps with location permission (broader — may or may not be active right now)
    Text(
        stringResource(R.string.leci_location_apps_header),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.height(4.dp))
    if (section.appsWithLocation.isNotEmpty()) {
        section.appsWithLocation.forEach { appName ->
            Text("• $appName", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.leci_location_apps_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        Text(
            stringResource(R.string.leci_location_no_apps),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    // 3. Family / account-level sharing (guided findings)
    if (section.familyFindings.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.leci_location_family_header),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        section.familyFindings.forEach { LocationFindingRow(it) }
    }

    // 4. Coarsened message template
    if (section.coarsenedMessage != null) {
        Spacer(Modifier.height(12.dp))
        LocationMessageCard(section.coarsenedMessage)
    }
}

@Composable
private fun ActiveLocationRisk(
    sources: List<String>,
    titleRes: Int = R.string.leci_location_active_title,
    bodyRes: Int = R.string.leci_location_active_body,
) {
    Surface(
        color = Color(0xFFC62828).copy(alpha = 0.14f),
        contentColor = Color(0xFFC62828),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(titleRes),
                    style = MaterialTheme.typography.labelMedium)
                SeverityChip(FindingSeverity.High)
            }
            Text(
                stringResource(bodyRes, sources.joinToString(", ")),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun LocationFindingRow(finding: LocationFinding) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(finding.label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            SeverityChip(finding.severity)
        }
        Text(finding.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedButton(
            onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(finding.actionUrl))) },
            modifier = Modifier.padding(top = 2.dp),
        ) {
            Icon(Icons.Filled.OpenInBrowser, contentDescription = null,
                modifier = Modifier.padding(end = 6.dp))
            Text(stringResource(R.string.location_check_settings))
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun LocationMessageCard(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.leci_location_message_title),
                style = MaterialTheme.typography.labelMedium)
            Text(message, style = MaterialTheme.typography.bodySmall)
            Text(stringResource(R.string.leci_location_message_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
        }
    }
}

// ---- Section 4: Device -----------------------------------------------------

@Composable
private fun DeviceContent(section: DeviceSection) {
    if (!section.ready) { PlaceholderText(stringResource(R.string.leci_device_not_ready)); return }
    if (section.checkItems.isEmpty()) {
        Text(stringResource(R.string.leci_device_no_items),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    section.checkItems.forEach { item ->
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(item.label, style = MaterialTheme.typography.bodyMedium)
                SeverityChip(item.severity)
            }
            Text(item.guidance, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ---- Seči panel ------------------------------------------------------------

@Composable
private fun CutPanel(
    accounts: List<AccountEntry>,
    accountsReady: Boolean,
    csvImporting: Boolean,
    onLoadDemoCsv: () -> Unit,
    onImportCsv: (Uri) -> Unit,
    locationFindings: List<LocationFinding> = emptyList(),
) {
    val context = LocalContext.current
    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { onImportCsv(it) } }

    val generalSteps = listOf(
        R.string.cut_step_document,
        R.string.cut_step_passwords,
        R.string.cut_step_sessions,
        R.string.cut_step_remove,
        R.string.cut_step_support,
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.cut_title), style = MaterialTheme.typography.titleMedium)
            generalSteps.forEachIndexed { index, label ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                    Text("${index + 1}.", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary)
                    Text(stringResource(label), style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f))
                }
            }
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(R.string.cut_accounts_title), style = MaterialTheme.typography.titleMedium)

            when {
                csvImporting -> {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator(modifier = Modifier
                            .height(18.dp)
                            .width(18.dp), strokeWidth = 2.dp)
                        Text(stringResource(R.string.csv_importing),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                !accountsReady -> {
                    Text(stringResource(R.string.cut_accounts_import_prompt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onLoadDemoCsv) {
                            Text(stringResource(R.string.csv_load_demo))
                        }
                        OutlinedButton(onClick = { csvLauncher.launch(arrayOf("text/*", "*/*")) }) {
                            Icon(Icons.Filled.FileOpen, contentDescription = null,
                                modifier = Modifier.padding(end = 6.dp))
                            Text(stringResource(R.string.csv_import))
                        }
                    }
                }

                else -> {
                    val accountsWithPassword = accounts.filter { it.suggestedPassword != null }
                    if (accountsWithPassword.isEmpty()) {
                        Text(stringResource(R.string.cut_accounts_all_strong),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text(stringResource(R.string.cut_accounts_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)

                        val checkedState = remember(accountsWithPassword.map { it.label }) {
                            mutableStateOf(List(accountsWithPassword.size) { false })
                        }

                        accountsWithPassword.forEachIndexed { index, entry ->
                            val checked = checkedState.value[index]
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        IconButton(onClick = {
                                            val updated = checkedState.value.toMutableList()
                                            updated[index] = !checked
                                            checkedState.value = updated
                                        }) {
                                            Icon(
                                                imageVector = if (checked) Icons.Filled.CheckBox
                                                else Icons.Filled.CheckBoxOutlineBlank,
                                                contentDescription = null,
                                                tint = if (checked) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        Column {
                                            Text(entry.label, style = MaterialTheme.typography.bodyMedium)
                                            if (entry.username != null) {
                                                Text(
                                                    maskUsername(entry.username),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        }
                                    }
                                    IconButton(onClick = {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse(entry.securityUrl))
                                        )
                                    }) {
                                        Icon(
                                            Icons.Filled.OpenInBrowser,
                                            contentDescription = stringResource(R.string.account_open_security_page),
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                                if (entry.suggestedPassword != null) {
                                    Text(
                                        stringResource(R.string.account_suggested_password, entry.suggestedPassword),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(start = 48.dp),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onLoadDemoCsv) {
                            Text(stringResource(R.string.csv_reload_demo))
                        }
                        OutlinedButton(onClick = { csvLauncher.launch(arrayOf("text/*", "*/*")) }) {
                            Icon(Icons.Filled.FileOpen, contentDescription = null,
                                modifier = Modifier.padding(end = 6.dp))
                            Text(stringResource(R.string.csv_import))
                        }
                    }
                }
            }
        }
    }

    if (locationFindings.isNotEmpty()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.cut_location_title),
                    style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.cut_location_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                listOf(
                    R.string.cut_location_step_maps,
                    R.string.cut_location_step_family,
                    R.string.cut_location_step_findmy,
                    R.string.cut_location_step_carrier,
                ).forEachIndexed { index, label ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top) {
                        Text("${index + 1}.", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary)
                        Text(stringResource(label), style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f))
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.cut_location_family_account_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.cut_location_family_account_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                locationFindings.forEach { finding ->
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(finding.label,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(finding.actionUrl)))
                        }) {
                            Text(stringResource(R.string.location_check_settings),
                                style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

// ---- Resource panel --------------------------------------------------------

@Composable
private fun ResourcePanel(onCallAstra: () -> Unit, onCallPolice: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          