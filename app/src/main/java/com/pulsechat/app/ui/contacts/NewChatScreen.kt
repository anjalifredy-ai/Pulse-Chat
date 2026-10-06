package com.pulsechat.app.ui.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.pulsechat.app.data.model.User
import com.pulsechat.app.ui.components.CircleAvatar
import com.pulsechat.app.ui.theme.PulsePurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatScreen(
    onBack: () -> Unit,
    onUserSelected: (String) -> Unit,
    onNewGroup: () -> Unit,
    onMessageYourself: () -> Unit = {},
    viewModel: NewChatViewModel = hiltViewModel()
) {
    val users by viewModel.users.collectAsState()
    val query by viewModel.query.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val me = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(Unit) { viewModel.loadContacts() }

    Scaffold(
        containerColor = Color(0xFF0A0A0C),
        topBar = {
            TopAppBar(
                title = { Text("New chat", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadContacts() }) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121218))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFF0A0A0C))
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.search(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Name ya username type karo…", color = Color.White.copy(0.4f)) },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, null, tint = Color.White.copy(0.5f))
                },
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = PulsePurple,
                    unfocusedBorderColor = Color.White.copy(0.2f),
                    cursorColor = PulsePurple
                )
            )

            // Message yourself
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onMessageYourself)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleAvatar(
                    photoUrl = me?.photoUrl?.toString(),
                    name = me?.displayName ?: "You",
                    size = 48.dp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Message yourself", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("Notes, links, reminders", color = Color.White.copy(0.5f), fontSize = 13.sp)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNewGroup)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.GroupAdd, null, tint = PulsePurple, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("New group", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            Text(
                "People on Pulse Chat",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.White.copy(0.5f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            when {
                loading -> {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PulsePurple)
                    }
                }
                error != null -> {
                    Text(
                        "Error: $error\n\nFirebase Console → Firestore → Rules mein users read allow karo (firestore.rules file dekho).",
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFFFF4D6A),
                        fontSize = 13.sp
                    )
                }
                users.isEmpty() && query.isNotBlank() -> {
                    Text(
                        "\"$query\" se koi nahi mila.\n\nDusre phone pe wahi name se profile set karke login hona chahiye.",
                        modifier = Modifier.padding(16.dp),
                        color = Color.White.copy(0.5f),
                        fontSize = 14.sp
                    )
                }
                users.isEmpty() -> {
                    Text(
                        "Abhi koi aur user nahi dikh raha.\n\n1. Dusre mobile pe Pulse Chat install karo\n2. Email se account banao (naya)\n3. Profile mein name daalo\n4. Yahan refresh dabao — name se search karo",
                        modifier = Modifier.padding(16.dp),
                        color = Color.White.copy(0.5f),
                        fontSize = 14.sp
                    )
                }
                else -> {
                    LazyColumn {
                        items(users, key = { it.uid }) { user ->
                            UserRow(user = user, onClick = { onUserSelected(user.uid) })
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserRow(user: User, onClick: () -> Unit) {
    val name = user.displayName.ifBlank { user.username ?: user.phoneNumber.ifBlank { "User" } }
    val subtitle = buildString {
        if (!user.username.isNullOrBlank()) append("@${user.username}")
        if (user.phoneNumber.isNotBlank()) {
            if (isNotEmpty()) append(" · ")
            append(user.phoneNumber)
        }
        if (isEmpty()) append(user.about)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleAvatar(photoUrl = user.photoUrl, name = name, size = 48.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(name, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color.White.copy(0.5f), fontSize = 13.sp)
        }
    }
}
