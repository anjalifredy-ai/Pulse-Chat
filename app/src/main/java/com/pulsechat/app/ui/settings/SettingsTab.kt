package com.pulsechat.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Column(modifier = modifier.fillMaxSize()) {
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
        SettingsItem("Chat wallpaper") {}
        SettingsItem("Notifications") {}
        SettingsItem("Help") {}
        HorizontalDivider()
        SettingsItem(
            text = stringResource(R.string.logout),
            onClick = { authViewModel.logout() }
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
