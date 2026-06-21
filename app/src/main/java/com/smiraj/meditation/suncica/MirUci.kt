package com.smiraj.meditation.suncica

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smiraj.meditation.weather.Suncica

// ===========================================================================
//  MIR — breathing & grounding
// ===========================================================================

private const val BREATH_CYCLE_MS = 4000 // 4s inhale / 4s exhale

/**
 * Themed breathing guide. A soft white circle expands (Udahni) and contracts
 * (Izdahni) on a 4s cycle, with a haptic-free, sound-free rhythm. The user
 * stays as long as she wants and taps "Završi" to return.
 */
@Composable
fun BreathingExercise(onClose: () -> Unit, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "breath")
    val scale by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(BREATH_CYCLE_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scale",
    )
    val inhaling = scale >= 0.78f

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))
        Text("Disanje", color = Suncica.TextPrimary.copy(alpha = 0.9f), fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Text("Diši zajedno sa krugom", color = Suncica.TextMuted, fontSize = 12.sp)

        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f))
                    .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.10f)),
                )
            }
            Text(
                text = if (inhaling) "Udahni" else "Izdahni",
                color = Suncica.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Light,
            )
        }

        PillButton("Završi", onClose)
        Spacer(Modifier.height(20.dp))
    }
}

private val GROUNDING_STEPS = listOf(
    "5" to "Pogledaj 5 stvari oko sebe",
    "4" to "Dodirni 4 stvari",
    "3" to "Oslušni 3 zvuka",
    "2" to "Pomiriši 2 mirisa",
    "1" to "Oseti 1 ukus",
)

/**
 * 5-4-3-2-1 grounding technique. One plain prompt at a time, tap to advance.
 * No animations competing for attention.
 */
@Composable
fun GroundingExercise(onClose: () -> Unit, modifier: Modifier = Modifier) {
    var step by remember { mutableIntStateOf(0) }
    val (number, prompt) = GROUNDING_STEPS[step]
    val isLast = step == GROUNDING_STEPS.lastIndex

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))
        Text("Uzemljavanje", color = Suncica.TextPrimary.copy(alpha = 0.9f), fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Text("5-4-3-2-1 tehnika", color = Suncica.TextMuted, fontSize = 12.sp)

        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(number, color = Suncica.TextPrimary, fontSize = 72.sp, fontWeight = FontWeight.Thin)
                Spacer(Modifier.height(12.dp))
                Text(
                    prompt,
                    color = Suncica.TextSecondary,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { PillButton("Završi", onClose) }
            Box(Modifier.weight(1f)) {
                PillButton(if (isLast) "Iz početka" else "Dalje") {
                    step = if (isLast) 0 else step + 1
                }
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

// ===========================================================================
//  UČI — education articles
// ===========================================================================

data class Article(val number: String, val title: String, val subtitle: String, val body: List<String>)

val ARTICLES = listOf(
    Article(
        "1", "Šta je grooming?", "Znakovi i zaštita",
        listOf(
            "Grooming je postupno pridobijanje poverenja sa ciljem zloupotrebe. Često počinje prijateljski — komplimenti, pažnja, pokloni i poruke da si „posebna“.",
            "Znakovi: osoba traži da razgovor ostane tajna, postavlja lična pitanja, traži fotografije ili pokušava da te izoluje od prijatelja i porodice.",
            "Zaštita: veruj svom osećaju. Niko ko ti želi dobro neće tražiti tajnost ni slike. Sačuvaj poruke i razgovaraj sa osobom od poverenja.",
        ),
    ),
    Article(
        "2", "Oblici nasilja", "Fizičko, digitalno...",
        listOf(
            "Nasilje nije samo fizičko. Emocionalno nasilje uključuje ponižavanje, kontrolu i pretnje. Digitalno nasilje je praćenje telefona, čitanje poruka i lažno predstavljanje.",
            "Finansijsko nasilje je kada neko kontroliše tvoj novac ili ti onemogućava da radiš. Sve su to oblici zlostavljanja.",
            "Ako prepoznaješ sebe u ovome — nisi kriva i nisi sama. Postoji podrška kojoj možeš da se obratiš.",
        ),
    ),
    Article(
        "3", "Digitalna bezbednost", "Praćenje i zaštita",
        listOf(
            "Praćenje najčešće nije skrivena „špijunska“ aplikacija, već zloupotreba legitimnog pristupa: deljena lokacija, porodični nalozi, Find My ili pristup tvojim lozinkama.",
            "Proveri: ko ima pristup tvom Google/Apple nalogu, da li je uključeno deljenje lokacije i koje aplikacije koriste lokaciju i mikrofon (vidi karticu Sken).",
            "Važno: ne menjaj ništa naglo ako si u riziku. Prvo napravi plan — naglo isključivanje može upozoriti nasilnika.",
        ),
    ),
    Article(
        "4", "Ako se nešto desi", "Koraci i ko zvati",
        listOf(
            "Prvo idi na bezbedno mesto. Tek onda prijavljuj. Tvoja bezbednost je važnija od bilo kog dokaza.",
            "Koga zvati u Srbiji: policija 192, hitna pomoć 194, evropski broj za hitne slučajeve 112. SOS telefon za žene: 0800 100 600 (besplatno, 24/7).",
            "Sačuvaj dokaze: snimci ekrana, datumi i kratke beleške. Čuvaj ih na uređaju kome nasilnik nema pristup.",
        ),
    ),
    Article(
        "5", "Pomozi nekome", "Prepoznaj znakove",
        listOf(
            "Znakovi kod prijateljice: povlačenje, strah od partnerove reakcije, nagle promene planova, modrice ili stalna kontrola preko telefona.",
            "Šta reći: „Tu sam za tebe“ i „Nisi ti kriva“. Šta NE reći: ne osuđuj i ne požuruj odluke — to može da je udalji.",
            "Uputi je na podršku nežno: podeli broj SOS telefona ili ovu aplikaciju, ali poštuj njen tempo i izbor.",
        ),
    ),
)

/** A single education article, with back navigation. */
@Composable
fun ArticleScreen(article: Article, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val scroll = rememberScrollState()
    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp).verticalScroll(scroll)) {
        Spacer(Modifier.height(14.dp))
        Text("‹ Nazad", color = Suncica.TextSecondary, fontSize = 13.sp, modifier = Modifier.clickable { onBack() })
        Spacer(Modifier.height(14.dp))
        Text(article.title, color = Suncica.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(2.dp))
        Text(article.subtitle, color = Suncica.TextMuted, fontSize = 12.sp)
        Spacer(Modifier.height(16.dp))
        article.body.forEach { para ->
            Text(para, color = Suncica.TextSecondary, fontSize = 14.sp, lineHeight = 22.sp)
            Spacer(Modifier.height(14.dp))
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ===========================================================================
//  Shared
// ===========================================================================

@Composable
private fun PillButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .border(0.5.dp, Suncica.CardBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = Suncica.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
