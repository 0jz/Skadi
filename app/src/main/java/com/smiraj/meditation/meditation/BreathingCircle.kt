package com.smiraj.meditation.meditation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smiraj.meditation.R

private const val CYCLE_MS = 4000 // 4s inhale, 4s exhale

/**
 * Central breathing guide. When [active] it slowly expands (inhale) and
 * contracts (exhale) on a 4s cycle, with the phase label underneath. When idle
 * it rests at mid-size so the screen still looks alive.
 */
@Composable
fun BreathingCircle(
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "breath")
    val scale by transition.animateFloat(
        initialValue = 0.62f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(CYCLE_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scale",
    )
    // Phase: first half of the cycle is inhale (growing), second half exhale.
    val growing = scale >= 0.81f
    val effectiveScale = if (active) scale else 0.8f

    val primary = MaterialTheme.colorScheme.primary
    val container = MaterialTheme.colorScheme.primaryContainer

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(
            modifier = Modifier
                .size(240.dp)
                .scale(effectiveScale)
        ) {
            val r = size.minDimension / 2f
            // Soft filled core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(container, primary),
                    radius = r,
                ),
                radius = r,
            )
            // Outer ring
            drawCircle(
                color = primary,
                radius = r,
                style = Stroke(width = r * 0.04f),
            )
        }
        if (active) {
            Text(
                text = stringResource(if (growing) R.string.breathe_in else R.string.breathe_out),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}
