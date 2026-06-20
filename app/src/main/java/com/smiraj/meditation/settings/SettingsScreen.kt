package com.smiraj.meditation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
