package com.pulsechat.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulsechat.app.R
import com.pulsechat.app.ui.auth.AuthViewModel
import com.pulsechat.app.ui.components.CircleAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab(
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val state by authViewModel.authState.collectAsState()
    var showWallpaper by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(title = { Text("Settings", fontWeight = FontWeight.Bold) })

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleAvatar(
                photoUrl = state.photoUrl,
                name = state.displayName.ifBlank { "User" },
                size = 64.dp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    state.displayName.ifBlank { "Pulse user" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Hey there! I am using Pulse Chat.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider()
        SettingsItem("Account") {}
        SettingsItem("Privacy") {}
        SettingsItem("Chat wallpaper") { showWallpaper = true }
        SettingsItem("Notifications") {}
        SettingsItem("Help") {}
        HorizontalDivider()
        SettingsItem(
            text = stringResource(R.string.logout),
            onClick = { authViewModel.logout() }
        )
    }

    if (showWallpaper) {
        val colors = listOf(
            Color(0xFF0B141A),
            Color(0xFF1A1A2E),
            Color(0xFF0F2027),
            Color(0xFF2C1810),
            Color(0xFF1B4332),
            Color(0xFF3D0C11)
        )
        AlertDialog(
            onDismissRequest = { showWallpaper = false },
            title = { Text("Chat wallpaper") },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    colors.forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                .clickable { showWallpaper = false }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWallpaper = false }) { Text("Done") }
            }
        )
    }
}

@Composable
private fun SettingsItem(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    )
}
