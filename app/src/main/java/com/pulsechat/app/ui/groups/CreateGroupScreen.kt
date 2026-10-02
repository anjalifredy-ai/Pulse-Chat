package com.pulsechat.app.ui.groups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulsechat.app.ui.components.CircleAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
    viewModel: CreateGroupViewModel = hiltViewModel()
) {
    val users by viewModel.users.collectAsState()
    val selected by viewModel.selected.collectAsState()
    val creating by viewModel.creating.collectAsState()
    val createdId by viewModel.createdId.collectAsState()
    val error by viewModel.error.collectAsState()
    var groupName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadUsers() }

    LaunchedEffect(createdId) {
        createdId?.let { onCreated(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New group") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (creating) {
                        CircularProgressIndicator(modifier = Modifier.padding(12.dp))
                    } else {
                        IconButton(
                            onClick = { viewModel.createGroup(groupName) },
                            enabled = groupName.isNotBlank() && selected.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Create")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Select members (${selected.size})",
                style = MaterialTheme.typography.titleSmall
            )
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                items(users, key = { it.uid }) { user ->
                    val isSelected = selected.contains(user.uid)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggle(user.uid) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircleAvatar(
                            photoUrl = user.photoUrl,
                            name = user.displayName.ifBlank { user.phoneNumber }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            user.displayName.ifBlank { user.phoneNumber },
                            modifier = Modifier.weight(1f)
                        )
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { viewModel.toggle(user.uid) }
                        )
                    }
                }
            }
        }
    }
}
