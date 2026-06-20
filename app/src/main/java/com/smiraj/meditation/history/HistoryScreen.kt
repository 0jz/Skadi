package com.smiraj.meditation.history

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smiraj.meditation.R
import com.smiraj.meditation.data.Session
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    sessions: List<Session>,
    streak: Int,
    totalSeconds: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Summary cards
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                value = "$streak",
                label = stringResource(R.string.streak_days),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                value = "${sessions.size}",
                label = stringResource(R.string.total_sessions),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                value = "${totalSeconds / 60}",
                label = stringResource(R.string.total_minutes),
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(20.dp))

        if (sessions.isEmpty()) {
            Text(
                text = stringResource(R.string.no_sessions),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sessions, key = { it.id }) { session -> SessionRow(session) }
            }
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(92.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

private val dateFmt = SimpleDateFormat("EEE, d MMM • HH:mm", Locale("sr"))

@Composable
private fun SessionRow(session: Session) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                dateFmt.format(Date(session.startedAt)),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                "${session.durationSec / 60}:${(session.durationSec % 60).toString().padStart(2, '0')}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
