package com.pulsechat.app.ui.calls

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulsechat.app.data.model.CallSession
import com.pulsechat.app.data.model.CallStatus
import com.pulsechat.app.data.model.CallType
import com.pulsechat.app.ui.theme.PulseGreen
import com.pulsechat.app.ui.theme.PulseRed

@Composable
fun CallsTab(
    modifier: Modifier = Modifier,
    viewModel: CallsViewModel = hiltViewModel()
) {
    val history by viewModel.history.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    if (history.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No call history", style = MaterialTheme.typography.titleMedium)
        }
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(history, key = { it.id }) { call ->
                CallHistoryRow(call)
            }
        }
    }
}

@Composable
private fun CallHistoryRow(call: CallSession) {
    val isMissed = call.status == CallStatus.MISSED || call.status == CallStatus.REJECTED
    val icon = when {
        isMissed -> Icons.Default.CallMissed
        call.callerId.isNotBlank() -> Icons.Default.CallMade
        else -> Icons.Default.CallReceived
    }
    val tint = if (isMissed) PulseRed else PulseGreen

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (call.type == CallType.VIDEO) "Video call" else "Voice call",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = call.status.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (call.type == CallType.VIDEO) {
            Icon(Icons.Default.Videocam, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
