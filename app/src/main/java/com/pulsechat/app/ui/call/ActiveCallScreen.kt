package com.pulsechat.app.ui.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulsechat.app.data.model.CallStatus
import com.pulsechat.app.ui.theme.PulseGreen
import com.pulsechat.app.ui.theme.PulseRed

@Composable
fun ActiveCallScreen(
    callId: String,
    onEnd: () -> Unit,
    viewModel: CallViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    DisposableEffect(callId) {
        viewModel.attach(callId)
        onDispose { viewModel.detach() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = state.remoteName.ifBlank { "Pulse Call" },
                    color = Color.White,
                    fontSize = 28.sp,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when (state.status) {
                        CallStatus.RINGING -> "Ringing…"
                        CallStatus.CONNECTING -> "Connecting…"
                        CallStatus.CONNECTED -> state.durationLabel
                        CallStatus.RECONNECTING -> "Reconnecting…"
                        else -> state.status.name
                    },
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CallControl(
                    icon = if (state.micMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    label = if (state.micMuted) "Unmute" else "Mute",
                    onClick = { viewModel.toggleMute() }
                )
                CallControl(
                    icon = Icons.Default.VolumeUp,
                    label = "Speaker",
                    onClick = { viewModel.toggleSpeaker() }
                )
                if (state.isVideo) {
                    CallControl(
                        icon = if (state.cameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        label = "Camera",
                        onClick = { viewModel.toggleCamera() }
                    )
                    CallControl(
                        icon = Icons.Default.Cameraswitch,
                        label = "Flip",
                        onClick = { viewModel.switchCamera() }
                    )
                }
            }

            IconButton(
                onClick = {
                    viewModel.endCall()
                    onEnd()
                },
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(PulseRed)
            ) {
                Icon(
                    Icons.Default.CallEnd,
                    contentDescription = "End call",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CallControl(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            Icon(icon, contentDescription = label, tint = Color.White)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
    }
}
