package com.smiraj.meditation.meditation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.smiraj.meditation.R
import com.smiraj.meditation.TimerState

private val PRESETS = listOf(3, 5, 10, 15)

@Composable
fun MeditationScreen(
    timer: TimerState,
    onSelectPreset: (Int) -> Unit,
    onCustomDurationEntered: (Int) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var custom by remember { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current

    fun submitCustom() {
        val value = custom.trim().toIntOrNull() ?: return
        custom = ""
        keyboard?.hide()
        // ALL custom input flows through this one callback (see AppViewModel).
        onCustomDurationEntered(value)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(Modifier.height(32.dp))
        BreathingCircle(active = timer.running)

        Spacer(Modifier.height(24.dp))
        Text(
            text = formatTime(if (timer.running) timer.remainingSec else timer.totalSec),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(32.dp))

        if (!timer.running) {
            // Preset chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                PRESETS.forEach { m ->
                    FilterChip(
                        selected = timer.plannedMin == m && custom.isBlank(),
                        onClick = { custom = ""; onSelectPreset(m) },
                        label = { Text("$m ${stringResource(R.string.minutes_short)}") },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Custom minutes — the single numeric field. In Phase 2 the secret
            // code is entered here; submitCustom() is the only path out.
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = custom,
                    onValueChange = { input -> custom = input.filter(Char::isDigit).take(3) },
                    label = { Text(stringResource(R.string.custom_minutes)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { submitCustom() }),
                    modifier = Modifier.width(160.dp),
                )
                Spacer(Modifier.width(12.dp))
                OutlinedButton(
                    onClick = { submitCustom() },
                    enabled = custom.isNotBlank(),
                ) { Text(stringResource(R.string.minutes_short)) }
            }

            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
            ) { Text(stringResource(R.string.start), style = MaterialTheme.typography.titleLarge) }
        } else {
            Button(
                onClick = onStop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
            ) { Text(stringResource(R.string.stop), style = MaterialTheme.typography.titleLarge) }
        }
    }
}

private fun formatTime(totalSec: Int): String {
    val m = totalSec / 60
    val s = totalSec % 60
    return "%d:%02d".format(m, s)
}
