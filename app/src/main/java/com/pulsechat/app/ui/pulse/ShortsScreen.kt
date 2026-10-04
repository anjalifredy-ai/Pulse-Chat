package com.pulsechat.app.ui.pulse

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.pulsechat.app.data.model.PulseFeed
import com.pulsechat.app.data.model.PulseVideo

@Composable
fun ShortsScreen(modifier: Modifier = Modifier) {
    val videos = PulseFeed.shorts
    val pagerState = rememberPagerState(pageCount = { videos.size })
    val likes = remember { mutableStateMapOf<String, Boolean>() }
    val saves = remember { mutableStateMapOf<String, Boolean>() }
    val context = LocalContext.current
    val view = LocalView.current

    // Immersive while on Shorts
    DisposableEffect(Unit) {
        val window = (view.context as? android.app.Activity)?.window
        val controller = window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)
            WindowInsetsControllerCompat(it, view).apply {
                hide(WindowInsetsCompat.Type.statusBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        onDispose {
            controller?.show(WindowInsetsCompat.Type.statusBars())
            window?.let { WindowCompat.setDecorFitsSystemWindows(it, true) }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val video = videos[page]
            ShortPage(
                video = video,
                liked = likes[video.id] == true,
                saved = saves[video.id] == true,
                onLike = { likes[video.id] = !(likes[video.id] ?: false) },
                onSave = {
                    saves[video.id] = !(saves[video.id] ?: false)
                    Toast.makeText(
                        context,
                        if (saves[video.id] == true) "Saved" else "Removed",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onComment = {
                    Toast.makeText(context, "Comments coming soon", Toast.LENGTH_SHORT).show()
                },
                onShare = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "${video.title}\n${video.watchUrl}")
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Short"))
                }
            )
        }
    }
}

@Composable
private fun ShortPage(
    video: PulseVideo,
    liked: Boolean,
    saved: Boolean,
    onLike: () -> Unit,
    onSave: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        YoutubeShortPlayer(
            embedUrl = video.embedUrl,
            modifier = Modifier.fillMaxSize()
        )

        // Right side actions
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp, bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ShortAction(
                icon = if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                label = if (liked) "Liked" else "Like",
                tint = if (liked) Color(0xFFFF4D6A) else Color.White,
                onClick = onLike
            )
            ShortAction(
                icon = Icons.Default.ChatBubbleOutline,
                label = "Comment",
                onClick = onComment
            )
            ShortAction(
                icon = Icons.Default.Share,
                label = "Share",
                onClick = onShare
            )
            ShortAction(
                icon = if (saved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                label = if (saved) "Saved" else "Save",
                tint = if (saved) Color(0xFFFFD54F) else Color.White,
                onClick = onSave
            )
        }

        // Bottom title
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 72.dp, bottom = 24.dp)
        ) {
            Text(
                "@${video.channel}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                video.title,
                color = Color.White,
                fontSize = 14.sp,
                maxLines = 2
            )
            Text(
                video.views,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ShortAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f))
        ) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(28.dp))
        }
        Text(label, color = Color.White, fontSize = 11.sp)
    }
}

private fun Modifier.background(color: Color) = this.then(
    androidx.compose.foundation.background(color)
)
