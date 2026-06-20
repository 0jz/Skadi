package com.smiraj.meditation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smiraj.meditation.R
import com.smiraj.meditation.data.Ambient
import com.smiraj.meditation.data.UserSettings

@Composable
fun SettingsScreen(
    settings: UserSettings,
    onAmbientChange: (Ambient) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            stringResource(R.string.ambient_sound),
            style = MaterialTheme.typography.titleLarge,
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                AmbientOption(R.string.ambient_none, Ambient.NONE, settings.ambient, onAmbientChange)
                AmbientOption(R.string.ambient_rain, Ambient.RAIN, settings.ambient, onAmbientChange)
                AmbientOption(R.string.ambient_forest, Ambient.FOREST, settings.ambient, onAmbientChange)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.keep_screen_on),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                Switch(checked = settings.keepScreenOn, onCheckedChange = onKeepScreenOnChange)
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.about), style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(R.string.about_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun AmbientOption(
    labelRes: Int,
    value: Ambient,
    current: Ambient,
    onSelect: (Ambient) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .selectable(selected = current == value, onClick = { onSelect(value) })
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = current == value, onClick = { onSelect(value) })
        Text(
            stringResource(labelRes),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
