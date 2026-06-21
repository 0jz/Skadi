package com.smiraj.meditation.suncica

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smiraj.meditation.emergency.EmergencyContactInfo
import com.smiraj.meditation.scan.Finding
import com.smiraj.meditation.scan.FindingSeverity
import com.smiraj.meditation.scan.ScanSnapshot
import com.smiraj.meditation.weather.CardDivider
import com.smiraj.meditation.weather.FrostedCard
import com.smiraj.meditation.weather.ListRow
import com.smiraj.meditation.weather.NavItem
import com.smiraj.meditation.weather.ScreenHeader
import com.smiraj.meditation.weather.Suncica
import com.smiraj.meditation.weather.SuncicaBackground
import com.smiraj.meditation.weather.SuncicaNavBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Layer 2 — the hidden real app, themed identically to the weather decoy.
 * Five tabs (SOS, Mapa, Sken, Mir, Uči) matching the design handoff (HTM).
 *
 * Reached only via the secret entry on the weather screen. A panic exit
 * (system back) drops straight back to the cover — handled by the caller.
 */

private val NAV_ITEMS = listOf(
    NavItem("🛡", "SOS"),   // 🛡
    NavItem("📍", "Mapa"),  // 📍
    NavItem("🔍", "Sken"),  // 🔍
    NavItem("🤍", "Mir"),   // 🤍
    NavItem("📖", "Uči"),   // 📖
)

@Composable
fun SafeApp(
    isScanning: Boolean,
    scanSnapshot: ScanSnapshot,
    scanHistory: List<ScanSnapshot>,
    emergencyContact: EmergencyContactInfo,
    deviceContacts: List<EmergencyContactInfo>,
    onScan: () -> Unit,
    onDial: (String) -> Unit,
    onImportEmergencyContact: (EmergencyContactInfo) -> Unit,
    onLoadContacts: () -> Unit,
    onSendEmergencyMessage: () -> Unit,
    onEmergency: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableIntStateOf(0) }

    SuncicaBackground(modifier = modifier) {
        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            Box(Modifier.weight(1f)) {
                when (selected) {
                    0 -> SosTab(
                        emergencyContact = emergencyContact,
                        deviceContacts = deviceContacts,
                        onDial = onDial,
                        onImportEmergencyContact = onImportEmergencyContact,
                        onLoadContacts = onLoadContacts,
                        onSendEmergencyMessage = onSendEmergencyMessage,
                        onEmergency = onEmergency,
                    )
                    1 -> InteractiveMapaTab()
                    2 -> SkenTab(isScanning, scanSnapshot, scanHistory, onScan)
                    3 -> MirTab(onDial)
                    else -> UciTab()
                }
            }
            SuncicaNavBar(
                items = NAV_ITEMS,
                selectedIndex = selected,
                onSelect = { selected = it },
            )
        }
    }
}

private fun tabModifier() = Modifier
    .fillMaxSize()
    .padding(horizontal = 16.dp)

// ---- SOS (R1) -------------------------------------------------------------

@Composable
private fun SosTab(
    emergencyContact: EmergencyContactInfo,
    deviceContacts: List<EmergencyContactInfo>,
    onDial: (String) -> Unit,
    onImportEmergencyContact: (EmergencyContactInfo) -> Unit,
    onLoadContacts: () -> Unit,
    onSendEmergencyMessage: () -> Unit,
    onEmergency: () -> Unit,
) {
    var showCancel by remember { mutableStateOf(false) }
    var showContacts by remember { mutableStateOf(false) }

    if (showCancel) {
        EmergencyCancelOverlay(
            onCancel = { showCancel = false },
            onEmergency = onEmergency,
        )
    } else {
        Column(tabModifier()) {
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Sunčica", color = Suncica.TextPrimary.copy(alpha = 0.9f), fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Box(
                Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Suncica.Safe.copy(alpha = 0.25f))
                    .border(0.5.dp, Suncica.Safe.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp),
            ) {
                Text("✓ Sigurna si", color = Suncica.TextPrimary.copy(alpha = 0.85f), fontSize = 10.sp)
            }
        }

        Spacer(Modifier.height(24.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("DRŽI ZA POMOĆ", color = Suncica.TextMuted, fontSize = 10.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(14.dp))
            ConcentricRings(onReleased = { showCancel = true })
            Spacer(Modifier.height(12.dp))
            Text(
                "Pritisni i drži\nza slanje upozorenja",
                color = Suncica.TextSecondary.copy(alpha = 0.65f),
                fontSize = 11.sp,
            )
        }

        Spacer(Modifier.height(24.dp))
        FrostedCard {
            ContactRow("SMS", "${emergencyContact.name} — pošalji SOS SMS") { onSendEmergencyMessage() }
            CardDivider()
            ContactRow("CALL", "${emergencyContact.name} — pozovi odmah") { onDial(emergencyContact.phone) }
            CardDivider()
            ContactRow("IMP", "Importuj hitni kontakt", emergencyContact.phone) {
                onLoadContacts()
                showContacts = !showContacts
            }
            if (showContacts) {
                CardDivider()
                if (deviceContacts.isEmpty()) {
                    ContactRow("...", "Nema učitanih kontakata", "Dozvoli kontakte pa pokušaj opet") { onLoadContacts() }
                } else {
                    deviceContacts.take(6).forEach { contact ->
                        CardDivider()
                        ContactRow("👤", contact.name, contact.phone) {
                            onImportEmergencyContact(contact)
                            showContacts = false
                        }
                    }
                }
            }
            CardDivider()
            ContactRow("👤", "Ana — prijateljica") { }
            CardDivider()
            ContactRow("👤", "Mama") { }
            CardDivider()
            ContactRow("🏛", "Hitna — 112") { onDial("112") }
        }
        }
    }
}

@Composable
private fun ConcentricRings(onReleased: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    Box(
        Modifier
            .size(130.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.06f))
            .border(0.5.dp, Suncica.CardBorder, CircleShape)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    pressed = true
                    waitForUpOrCancellation()
                    pressed = false
                    onReleased()
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(if (pressed) 112.dp else 104.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = if (pressed) 0.16f else 0.08f))
                .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("🛡", fontSize = 34.sp)
            }
        }
    }
}

@Composable
private fun EmergencyCancelOverlay(
    onCancel: () -> Unit,
    onEmergency: () -> Unit,
) {
    var secondsLeft by remember { mutableIntStateOf(5) }
    var pin by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            kotlinx.coroutines.delay(1000)
            secondsLeft -= 1
        }
        onEmergency()
    }

    LaunchedEffect(pin) {
        when {
            pin == "0" -> onCancel()
            pin.isNotEmpty() -> onEmergency()
        }
    }

    Column(
        tabModifier(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(44.dp))
        Text("Slanje pomoći za $secondsLeft", color = Suncica.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Unesi svoj PIN da otkažeš. Pogrešan PIN nastavlja hitni protokol.",
            color = Suncica.TextSecondary,
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", ""),
            ).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { digit ->
                        if (digit.isBlank()) {
                            Spacer(Modifier.size(52.dp))
                        } else {
                            Box(
                                Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .border(0.5.dp, Suncica.CardBorder, CircleShape)
                                    .clickable { pin = digit },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(digit, color = Suncica.TextPrimary, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BlackoutScreen(modifier: Modifier = Modifier) {
    Box(modifier.background(Color.Black))
}

@Composable
private fun ContactRow(icon: String, name: String, subtitle: String? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon, fontSize = 13.sp, color = Suncica.TextSecondary)
        Spacer(Modifier.size(10.dp))
        Column(Modifier.weight(1f)) {
            Text(name, fontSize = 12.sp, color = Suncica.TextPrimary.copy(alpha = 0.85f))
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(1.dp))
                Text(subtitle, fontSize = 10.sp, color = Suncica.TextMuted)
            }
        }
    }
}

// ---- Mapa (R4) ------------------------------------------------------------

private data class RiskArea(
    val name: String,
    val district: String,
    val level: String,
    val note: String,
    val advice: String,
    val color: Color,
    val x: Float,
    val y: Float,
)

private val RISK_AREAS = listOf(
    RiskArea("Studentski park", "Stari grad", "Bezbedno", "Dobra osvetljenost i dosta prolaznika do 22h.", "Drži se glavnih staza i najavi dolazak osobi od poverenja.", Suncica.Safe, 0.20f, 0.24f),
    RiskArea("Zeleni venac", "Centar", "Oprezno", "Gužva, prelazi i više prijavljenih neprijatnih dobacivanja uveče.", "Biraj osvetljenu stranu ulice i proveri prevoz pre polaska.", Suncica.Warning, 0.58f, 0.42f),
    RiskArea("Mračni prolaz kod stanice", "Savski trg", "Rizično", "Slaba vidljivost i malo otvorenih lokala posle 21h.", "Izbegni prolaz; idi duž glavne ulice ili pozovi pratnju.", Suncica.Danger, 0.32f, 0.72f),
    RiskArea("Bulevar kralja Aleksandra", "Vračar", "Bezbedno", "Otvorene radnje, frekventan prevoz i dobra vidljivost.", "Koristi stajališta sa više ljudi i sačuvaj bateriju.", Suncica.Safe, 0.76f, 0.22f),
)

@Composable
private fun InteractiveMapaTab() {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(RISK_AREAS[1]) }
    val filtered = remember(query) {
        val needle = query.trim().lowercase()
        if (needle.isEmpty()) RISK_AREAS else RISK_AREAS.filter {
            it.name.lowercase().contains(needle) || it.district.lowercase().contains(needle)
        }
    }

    Column(tabModifier().verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(12.dp))
        ScreenHeader("Bezbednost u blizini", "Beograd, Srbija")
        Spacer(Modifier.height(10.dp))
        SearchBox(query = query, onQueryChange = { query = it })
        Spacer(Modifier.height(10.dp))
        RiskMap(
            areas = filtered.ifEmpty { RISK_AREAS },
            selected = selected,
            onSelect = { selected = it },
            modifier = Modifier.fillMaxWidth().height(250.dp),
        )
        Spacer(Modifier.height(10.dp))
        RiskDetail(selected)
        Spacer(Modifier.height(10.dp))
        FrostedCard {
            val rows = filtered.take(3)
            rows.forEachIndexed { index, area ->
                ListRow(
                    leading = "",
                    title = area.name,
                    subtitle = "${area.level} · ${area.district}",
                    showChevron = false,
                ) { selected = area }
                if (index < rows.lastIndex) CardDivider()
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Legend(Suncica.Safe, "Bezbedno")
            Legend(Suncica.Warning, "Oprezno")
            Legend(Suncica.Danger, "Rizično")
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SearchBox(query: String, onQueryChange: (String) -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .border(0.5.dp, Suncica.CardBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp),
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = TextStyle(color = Suncica.TextPrimary, fontSize = 12.sp),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text("Pretraži kvart ili ulicu...", color = Suncica.TextMuted, fontSize = 11.sp)
                }
                inner()
            },
        )
    }
}

@Composable
private fun RiskMap(
    areas: List<RiskArea>,
    selected: RiskArea,
    onSelect: (RiskArea) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(0.5.dp, Suncica.Divider, RoundedCornerShape(12.dp)),
    ) {
        repeat(4) { row ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .offset(y = maxHeight * ((row + 1) / 5f))
                    .background(Color.White.copy(alpha = 0.06f)),
            )
        }
        repeat(3) { col ->
            Box(
                Modifier
                    .height(maxHeight)
                    .fillMaxWidth(0.003f)
                    .offset(x = maxWidth * ((col + 1) / 4f))
                    .background(Color.White.copy(alpha = 0.06f)),
            )
        }
        areas.forEach { area ->
            val isSelected = area == selected
            Box(
                Modifier
                    .offset(x = maxWidth * area.x, y = maxHeight * area.y)
                    .size(if (isSelected) 58.dp else 46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(area.color.copy(alpha = if (isSelected) 0.62f else 0.38f))
                    .border(
                        if (isSelected) 2.dp else 0.5.dp,
                        Color.White.copy(alpha = if (isSelected) 0.85f else 0.28f),
                        RoundedCornerShape(14.dp),
                    )
                    .clickable { onSelect(area) },
                contentAlignment = Alignment.Center,
            ) {
                Text(area.level.first().toString(), color = Suncica.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        Box(
            Modifier
                .offset(x = maxWidth * 0.48f, y = maxHeight * 0.58f)
                .size(14.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(3.dp, Suncica.BgTop, CircleShape),
        )
    }
}

@Composable
private fun RiskDetail(area: RiskArea) {
    FrostedCard {
        Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(area.color))
                Spacer(Modifier.size(8.dp))
                Text(area.name, color = Suncica.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.weight(1f))
                Text(area.level, color = area.color, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(6.dp))
            Text(area.note, color = Suncica.TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Text(area.advice, color = Suncica.TextMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun MapaTab() {
    Column(tabModifier()) {
        Spacer(Modifier.height(12.dp))
        ScreenHeader("Bezbednost u blizini", "Beograd, Srbija")
        Spacer(Modifier.height(10.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.10f))
                .border(0.5.dp, Suncica.CardBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp),
        ) {
            Text("🔍  Pretraži kvart ili ulicu...", color = Suncica.TextMuted, fontSize = 11.sp)
        }

        Spacer(Modifier.height(10.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(0.5.dp, Suncica.Divider, RoundedCornerShape(12.dp)),
        ) {
            Zone(Suncica.Safe, 70.dp, 56.dp, 20.dp, 30.dp)
            Zone(Suncica.Safe, 60.dp, 50.dp, 230.dp, 40.dp)
            Zone(Suncica.Warning, 64.dp, 54.dp, 120.dp, 150.dp)
            Zone(Suncica.Danger, 54.dp, 44.dp, 40.dp, 230.dp)
            Box(
                Modifier
                    .offset(x = 150.dp, y = 160.dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Suncica.BgTop, CircleShape),
            )
        }

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Legend(Suncica.Safe, "Bezbedno")
            Legend(Suncica.Warning, "Oprezno")
            Legend(Suncica.Danger, "Rizično")
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun Zone(color: Color, w: androidx.compose.ui.unit.Dp, h: androidx.compose.ui.unit.Dp, x: androidx.compose.ui.unit.Dp, y: androidx.compose.ui.unit.Dp) {
    Box(
        Modifier
            .offset(x = x, y = y)
            .size(w, h)
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.4f)),
    )
}

@Composable
private fun Legend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.size(4.dp))
        Text(label, color = Suncica.TextMuted, fontSize = 10.sp)
    }
}

// ---- Sken (R6) ------------------------------------------------------------

@Composable
private fun SkenTab(
    isScanning: Boolean,
    snapshot: ScanSnapshot,
    scanHistory: List<ScanSnapshot>,
    onScan: () -> Unit,
) {
    var showHistory by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()
    Column(tabModifier().verticalScroll(scroll)) {
        Spacer(Modifier.height(12.dp))
        ScreenHeader("Tvoj uređaj", "Digitalna bezbednost")
        Spacer(Modifier.height(10.dp))

        val lastScan = if (snapshot.ranAtMillis > 0L) "Poslednje skeniranje: upravo" else "Poslednje skeniranje: nikad"
        FrostedCard {
            Column(
                Modifier.fillMaxWidth().padding(vertical = 20.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(lastScan, color = Suncica.TextMuted, fontSize = 10.sp)
                Spacer(Modifier.height(18.dp))
                Box(
                    Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.10f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                        .clickable(enabled = !isScanning) { onScan() },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔍", fontSize = 30.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (isScanning) "Skeniranje…" else "Skeniraj sada",
                            color = Suncica.TextSecondary,
                            fontSize = 10.sp,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        if (snapshot.ranAtMillis > 0L && !isScanning) {
            ScanResults(snapshot.findings)
            Spacer(Modifier.height(10.dp))
        }

        FrostedCard {
            ListRow("📧", "Proveri nalog", "Plan: uvoz mejla ili CSV, zatim provera poznatih curenja", showChevron = false) { }
            CardDivider()
            ListRow("📋", "Istorija skeniranja", "${scanHistory.size} zapisa") { showHistory = !showHistory }
            if (showHistory) {
                CardDivider()
                ScanHistory(scanHistory)
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun ScanHistory(history: List<ScanSnapshot>) {
    if (history.isEmpty()) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Još nema skeniranja.", color = Suncica.TextMuted, fontSize = 11.sp)
        }
        return
    }
    history.forEachIndexed { index, item ->
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val high = item.findings.count { it.severity == FindingSeverity.High }
            val medium = item.findings.count { it.severity == FindingSeverity.Medium }
            val color = when {
                high > 0 -> Suncica.Danger
                medium > 0 -> Suncica.Warning
                else -> Suncica.Safe
            }
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(Modifier.size(10.dp))
            Column(Modifier.weight(1f)) {
                Text(formatScanTime(item.ranAtMillis), color = Suncica.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text("${item.findings.size} nalaza · BLE ${if (item.bleScanned) "proveren" else "u toku"}", color = Suncica.TextMuted, fontSize = 10.sp)
            }
        }
        if (index < history.lastIndex) CardDivider()
    }
}

private fun formatScanTime(millis: Long): String {
    if (millis <= 0L) return "Nepoznato vreme"
    return SimpleDateFormat("dd.MM. HH:mm", Locale.getDefault()).format(Date(millis))
}

@Composable
private fun ScanResults(findings: List<Finding>) {
    FrostedCard {
        if (findings.isEmpty()) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(Suncica.Safe))
                Spacer(Modifier.size(10.dp))
                Text("Nismo pronašli tragače. Uređaj izgleda čist.", color = Suncica.TextSecondary, fontSize = 12.sp)
            }
        } else {
            findings.forEachIndexed { i, f ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(severityColor(f.severity)))
                    Spacer(Modifier.size(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(f.appName, color = Suncica.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(f.neutralSummary, color = Suncica.TextMuted, fontSize = 11.sp)
                    }
                }
                if (i < findings.lastIndex) CardDivider()
            }
        }
    }
}

private fun severityColor(s: FindingSeverity): Color = when (s) {
    FindingSeverity.Low -> Suncica.Safe
    FindingSeverity.Medium -> Suncica.Warning
    FindingSeverity.High -> Suncica.Danger
}

// ---- Mir (R11) ------------------------------------------------------------

@Composable
private fun MirTab(onDial: (String) -> Unit) {
    // 0 = list, 1 = breathing, 2 = grounding
    var sub by remember { mutableIntStateOf(0) }
    when (sub) {
        1 -> BreathingExercise(onClose = { sub = 0 })
        2 -> GroundingExercise(onClose = { sub = 0 })
        else -> Column(tabModifier()) {
            Spacer(Modifier.height(12.dp))
            ScreenHeader("Mir i podrška", "Večeras za tebe")
            Spacer(Modifier.height(10.dp))

            FrostedCard(modifier = Modifier.clickable { sub = 1 }) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center,
                        ) { Text("🌬", fontSize = 18.sp) }
                        Spacer(Modifier.size(10.dp))
                        Column {
                            Text("Večernje disanje", color = Suncica.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("2 min · bez zvuka", color = Suncica.TextMuted, fontSize = 10.sp)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Box(Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.12f))) {
                        Box(Modifier.fillMaxWidth(0.35f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.65f)))
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            FrostedCard {
                ListRow("🌬", "Disanje", "Smiri se polako") { sub = 1 }
                CardDivider()
                ListRow("👁", "Uzemljavanje", "5-4-3-2-1 tehnika") { sub = 2 }
                CardDivider()
                ListRow("📞", "Pozovi podršku", "0800 100 600", showChevron = false) { onDial("0800100600") }
            }
        }
    }
}

// ---- Uči (R15) ------------------------------------------------------------

@Composable
private fun UciTab() {
    var selected by remember { mutableIntStateOf(-1) }
    if (selected in ARTICLES.indices) {
        ArticleScreen(ARTICLES[selected], onBack = { selected = -1 })
    } else {
        Column(tabModifier()) {
            Spacer(Modifier.height(12.dp))
            ScreenHeader("Uči i zaštiti se", "Znanje je zaštita")
            Spacer(Modifier.height(10.dp))
            FrostedCard {
                ARTICLES.forEachIndexed { i, a ->
                    ListRow(a.number, a.title, a.subtitle, leadingIsBadge = true) { selected = i }
                    if (i < ARTICLES.lastIndex) CardDivider()
                }
            }
        }
    }
}
