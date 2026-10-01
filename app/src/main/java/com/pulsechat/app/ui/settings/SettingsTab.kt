package com.pulsechat.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulsechat.app.R
import com.pulsechat.app.ui.auth.AuthViewModel

@Composable
fun SettingsTab(
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    Column(modifier = modifier.fillMaxSize()) {
        SettingsItem(stringResource(R.string.account)) {}
        SettingsItem(stringResource(R.string.privacy)) {}
        SettingsItem(stringResource(R.string.notifications)) {}
        SettingsItem(stringResource(R.string.storage_data)) {}
        SettingsItem(stringResource(R.string.appearance)) {}
        SettingsItem(stringResource(R.string.help)) {}
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
