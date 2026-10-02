package com.pulsechat.app.ui.contacts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulsechat.app.data.model.User
import com.pulsechat.app.ui.components.CircleAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatScreen(
    onBack: () -> Unit,
    onUserSelected: (String) -> Unit,
    onNewGroup: () -> Unit,
    viewModel: NewChatViewModel = hiltViewModel()
) {
    val users by viewModel.users.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadContacts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New chat") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNewGroup)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.GroupAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Text("New group", style = MaterialTheme.typography.titleMedium)
            }

            if (users.isEmpty()) {
                Text(
                    "No other users yet. Ask a friend to register on Pulse Chat.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyColumn {
                items(users, key = { it.uid }) { user ->
                    UserRow(user = user, onClick = { onUserSelected(user.uid) })
                }
            }
        }
    }
}

@Composable
private fun UserRow(user: User, onClick: () -> Unit) {
    val name = user.displayName.ifBlank { user.phoneNumber.ifBlank { "User" } }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleAvatar(photoUrl = user.photoUrl, name = name)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(name, style = MaterialTheme.typography.titleMedium)
            Text(
                user.about,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
