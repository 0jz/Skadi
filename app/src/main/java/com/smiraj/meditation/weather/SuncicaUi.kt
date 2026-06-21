package com.smiraj.meditation.weather

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared "Apple Weather" visual language for Sunčica.
 *
 * Both layers — the public weather decoy and the hidden safety app — use these
 * exact tokens and components, so there is no visual switch when the user
 * enters the real app. Tokens come straight from the design handoff (HTM).
 */
object Suncica {
    // Backgrounds
    val BgTop = Color(0xFF1C3A5E)
    val BgMid = Color(0xFF0D2240)
    val BgBottom = Color(0xFF081830)
    val CardBg = Color.White.copy(alpha = 0.12f)
    val CardBorder = Color.White.copy(alpha = 0.18f)
    val Divider = Color.White.copy(alpha = 0.10f)

    // Text
    val TextPrimary = Color.White
    val TextSecondary = Color.White.copy(alpha = 0.75f)
    val TextMuted = Color.White.copy(alpha = 0.45f)

    // Status colours — used sparingly
    val Safe = Color(0xFF4CD964)
    val Warning = Color(0xFFFF9500)
    val Danger = Color(0xFFFF3B30)

    // Nav
    val NavActive = Color.White
    val NavInactive = Color.White.copy(alpha = 0.30f)

    val gradient: Brush
        get() = Brush.verticalGradient(
            0.0f to BgTop,
            0.6f to BgMid,
            1.0f to BgBottom,
        )
}

/** Full-screen deep-blue gradient backdrop with system-bar insets applied. */
@Composable
fun SuncicaBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Suncica.gradient),
    ) { content() }
}

/** Frosted-glass card. */
@Composable
fun FrostedCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Suncica.CardBg)
            .border(0.5.dp, Suncica.CardBorder, RoundedCornerShape(14.dp)),
    ) { content() }
}

/** Hairline divider used inside cards. */
@Composable
fun CardDivider(modifier: Modifier = Modifier) {
    Spacer(
        modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(Suncica.Divider)
    )
}

/** Screen title + subtitle block (white / muted), matching the HTM headers. */
@Composable
fun ScreenHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(title, color = Suncica.TextPrimary.copy(alpha = 0.9f), fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(2.dp))
        Text(subtitle, color = Suncica.TextMuted, fontSize = 12.sp)
    }
}

/**
 * Standard list row: leading icon (emoji), title + subtitle, optional chevron.
 * Used by the Mir, Uči and Sken cards.
 */
@Composable
fun ListRow(
    leading: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    leadingIsBadge: Boolean = false,
    showChevron: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val rowMod = if (onClick != null) modifier.clickable { onClick() } else modifier
    Row(
        modifier = rowMod
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(if (leadingIsBadge) 26.dp else 30.dp)
                .clip(if (leadingIsBadge) CircleShape else RoundedCornerShape(8.dp))
                .background(Suncica.CardBg)
                .border(
                    if (leadingIsBadge) 0.5.dp else 0.dp,
                    if (leadingIsBadge) Suncica.CardBorder else Color.Transparent,
                    if (leadingIsBadge) CircleShape else RoundedCornerShape(8.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(leading, fontSize = if (leadingIsBadge) 12.sp else 15.sp, color = Suncica.TextPrimary)
        }
        Spacer(Modifier.size(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Suncica.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(1.dp))
            Text(subtitle, color = Suncica.TextMuted, fontSize = 11.sp)
        }
        if (showChevron) {
            Text("›", color = Suncica.TextPrimary.copy(alpha = 0.25f), fontSize = 16.sp)
        }
    }
}

/** Bottom navigation bar — icons + labels, white when active, 30% white when not. */
@Composable
fun SuncicaNavBar(
    items: List<NavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Suncica.BgBottom)
            .padding(top = 8.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, item ->
            val active = index == selectedIndex
            val tint = if (active) Suncica.NavActive else Suncica.NavInactive
            Column(
                modifier = Modifier.clickable { onSelect(index) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(item.icon, fontSize = 20.sp, color = tint)
                Spacer(Modifier.height(3.dp))
                Text(item.label, fontSize = 9.sp, color = tint)
            }
        }
    }
}

data class NavItem(val icon: String, val label: String)
