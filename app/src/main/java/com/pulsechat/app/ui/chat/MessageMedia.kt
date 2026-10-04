package com.pulsechat.app.ui.chat

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pulsechat.app.data.model.Message
import com.pulsechat.app.data.model.MessageStatus
import com.pulsechat.app.data.model.MessageType
import com.pulsechat.app.ui.theme.TickBlue
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MessageBubble(
    message: Message,
    isMine: Boolean,
    onOpenDocument: (String, String) -> Unit,
    onOpenImage: (String) -> Unit = {}
) {
    val bg = if (isMine) Color(0xFF2A1F4D) else Color(0xFF1C1C22)
    val time = message.createdAt?.let {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(it)
    } ?: ""
    val isRead = message.status == MessageStatus.READ || message.readBy.isNotEmpty()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMine) 16.dp else 4.dp,
                        bottomEnd = if (isMine) 4.dp else 16.dp
                    )
                )
                .background(bg)
                .padding(6.dp)
        ) {
            when (message.type) {
                MessageType.IMAGE -> {
                    val url = message.mediaUrl
                    if (!url.isNullOrBlank()) {
                        AsyncImage(
                            model = url,
                            contentDescription = "Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onOpenImage(url) }
                        )
                    }
                    if (!message.text.isNullOrBlank() && message.text != "📷 Photo") {
                        Text(
                            message.text!!,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }

                MessageType.VOICE, MessageType.AUDIO -> {
                    VoiceBubble(url = message.mediaUrl, durationMs = message.mediaDuration)
                }

                MessageType.DOCUMENT -> {
                    Row(
                        modifier = Modifier
                            .clickable {
                                message.mediaUrl?.let {
                                    onOpenDocument(it, message.fileName ?: "Document")
                                }
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                message.fileName ?: "Document",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text("Tap to open", color = TickBlue, fontSize = 12.sp)
                        }
                    }
                }

                MessageType.VIDEO -> {
                    val url = message.mediaUrl
                    if (!url.isNullOrBlank()) {
                        Box {
                            AsyncImage(
                                model = url,
                                contentDescription = "Video",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onOpenImage(url) }
                            )
                            Text(
                                "▶ Video",
                                color = Color.White,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                else -> {
                    Text(
                        text = message.text.orEmpty().ifBlank { "Message" },
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 4.dp, top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (time.isNotBlank()) {
                    Text(time, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
                if (isMine) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = if (isRead) Icons.Default.DoneAll else Icons.Default.Done,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = if (isRead) TickBlue else Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun VoiceBubble(url: String?, durationMs: Long) {
    var playing by remember { mutableStateOf(false) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(url) {
        onDispose {
            player?.release()
            player = null
            playing = false
        }
    }

    val secs = (durationMs / 1000).coerceAtLeast(1)
    val label = String.format("%d:%02d", secs / 60, secs % 60)

    Row(
        modifier = Modifier
            .padding(4.dp)
            .widthIn(min = 180.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                if (url.isNullOrBlank()) return@IconButton
                try {
                    if (playing) {
                        player?.pause()
                        playing = false
                    } else {
                        if (player == null) {
                            player = MediaPlayer().apply {
                                setDataSource(url)
                                prepare()
                                setOnCompletionListener { playing = false }
                                start()
                            }
                        } else {
                            player?.start()
                        }
                        playing = true
                    }
                } catch (_: Exception) {
                    playing = false
                }
            },
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (playing) "Pause" else "Play",
                tint = Color.White
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text("Voice message", color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Text(label, color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp)
        }
    }
}
