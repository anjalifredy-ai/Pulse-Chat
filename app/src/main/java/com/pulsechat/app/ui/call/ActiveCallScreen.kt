package com.pulsechat.app.ui.call

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulsechat.app.data.model.CallStatus
import com.pulsechat.app.ui.components.CircleAvatar

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
            .background(Color(0xFF0B141A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEnd) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    "End-to-End Encrypted",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                state.remoteName.ifBlank { "Pulse Call" },
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (state.status) {
                    CallStatus.RINGING -> "Ringing…"
                    CallStatus.CONNECTING -> "Connecting…"
                    CallStatus.CONNECTED -> state.durationLabel.ifBlank { "00:00" }
                    CallStatus.RECONNECTING -> "Reconnecting…"
                    else -> state.durationLabel.ifBlank { "00:00" }
                },
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircleAvatar(
                photoUrl = null,
                name = state.remoteName.ifBlank { "U" },
                size = 140.dp
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF1F2C34))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CallRoundButton(
                    icon = Icons.Default.VolumeUp,
                    selected = state.speakerOn,
                    onClick = { viewModel.toggleSpeaker() }
                )
                CallRoundButton(
                    icon = if (state.cameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                    selected = state.cameraOn,
                    onClick = { viewModel.toggleCamera() }
                )
                CallRoundButton(
                    icon = if (state.micMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    selected = state.micMuted,
                    onClick = { viewModel.toggleMute() }
                )
                IconButton(
                    onClick = {
                        viewModel.endCall()
                        onEnd()
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935))
                ) {
                    Icon(
                        Icons.Default.CallEnd,
                        contentDescription = "End",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
