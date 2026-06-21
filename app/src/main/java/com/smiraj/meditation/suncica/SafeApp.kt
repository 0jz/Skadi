package com.smiraj.meditation.suncica

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onScan: () -> Unit,
    onDial: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableIntStateOf(0) }

    SuncicaBackground(modifier = modifier) {
        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            Box(Modifier.weight(1f)) {
                when (selected) {
                    0 -> SosTab(onDial)
                    1 -> MapaTab()
                    2 -> SkenTab(isScanning, scanSnapshot, onScan)
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
private fun SosTab(onDial: (String) -> Unit) {
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
            ConcentricRings()
            Spacer(Modifier.height(12.dp))
            Text(
                "Pritisni i drži\nza slanje upozorenja",
                color = Suncica.TextSecondary.copy(alpha = 0.65f),
                fontSize = 11.sp,
            )
        }

        Spacer(Modifier.height(24.dp))
        FrostedCard {
            ContactRow("👤", "Ana — prijateljica") { }
            CardDivider()
            ContactRow("👤", "Mama") { }
            CardDivider()
            ContactRow("🏛", "Hitna — 112") { onDial("112") }
        }
    }
}

@Composable
private fun ConcentricRings() {
    Box(
        Modifier
            .size(130.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.06f))
            .border(0.5.dp, Suncica.CardBorder, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
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
private fun ContactRow(icon: String, name: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon, fontSize = 14.sp, color = Suncica.TextSecondary)
        Spacer(Modifier.size(10.dp))
        Text(name, fontSize = 12.sp, color = Suncica.TextPrimary.copy(alpha = 0.85f))
    }
}

// ---- Mapa (R4) ------------------------------------------------------------

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
private fun SkenTab(isScanning: Boolean, snapshot: ScanSnapshot, onScan: () -> Unit) {
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
            ListRow("📧", "Proveri nalog", "Provale u baze podataka") { }
            CardDivider()
            ListRow("📋", "Istorija skeniranja", "Prethodni rezultati") { }
        }
        Spacer(Modifier.height(12.dp))
    }
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
                ARTICLES.forEach