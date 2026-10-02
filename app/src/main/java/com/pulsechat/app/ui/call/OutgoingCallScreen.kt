package com.pulsechat.app.ui.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsechat.app.ui.components.CircleAvatar
import com.pulsechat.app.ui.theme.PulseRed
import kotlinx.coroutines.delay

@Composable
fun OutgoingCallScreen(
    name: String,
    photoUrl: String?,
    isVideo: Boolean,
    onEnd: () -> Unit
) {
    // Auto-end after 45s if not answered (UI demo until full WebRTC connect)
    LaunchedEffect(Unit) {
        delay(45_000)
        onEnd()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(64.dp))
            Text(
                if (isVideo) "Video calling…" else "Calling…",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircleAvatar(photoUrl = photoUrl, name = name, size = 120.dp)
            Spacer(modifier = Modifier.height(24.dp))
            Text(name, color = Color.White, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Ringing…", color = Color.White.copy(alpha = 0.6f))
        }

        IconButton(
            onClick = onEnd,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(PulseRed)
        ) {
            Icon(
                Icons.Default.CallEnd,
                contentDescription = "End",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}
