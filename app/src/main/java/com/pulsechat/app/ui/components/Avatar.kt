package com.pulsechat.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun CircleAvatar(
    photoUrl: String?,
    name: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(avatarColor(name)),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = name,
                modifier = Modifier.size(size).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = initial,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value / 2.4f).sp
            )
        }
    }
}

private fun avatarColor(name: String): Color {
    val colors = listOf(
        Color(0xFF6C63FF),
        Color(0xFF00BFA6),
        Color(0xFFFF6B6B),
        Color(0xFFFFA726),
        Color(0xFF42A5F5),
        Color(0xFFAB47BC),
        Color(0xFF26A69A),
        Color(0xFFEF5350)
    )
    val index = (name.hashCode().and(0x7FFFFFFF)) % colors.size
    return colors[index]
}
