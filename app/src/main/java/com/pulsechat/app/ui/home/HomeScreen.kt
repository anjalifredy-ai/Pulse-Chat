package com.pulsechat.app.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pulsechat.app.R
import com.pulsechat.app.ui.calls.CallsTab
import com.pulsechat.app.ui.chats.ChatsTab
import com.pulsechat.app.ui.settings.SettingsTab
import com.pulsechat.app.ui.status.StatusTab

@Composable
fun HomeScreen(
    onOpenChat: (String) -> Unit,
    onNewChat: () -> Unit,
    onSettings: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Chat, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_chats)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Update, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_status)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Call, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_calls)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_settings)) }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = onNewChat) {
                    Icon(Icons.Default.Chat, contentDescription = stringResource(R.string.new_chat))
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> ChatsTab(
                modifier = Modifier.padding(padding),
                onOpenChat = onOpenChat
            )
            1 -> StatusTab(modifier = Modifier.padding(padding))
            2 -> CallsTab(modifier = Modifier.padding(padding))
            3 -> SettingsTab(
                modifier = Modifier.padding(padding),
                onOpenSettings = onSettings
            )
        }
    }
}
