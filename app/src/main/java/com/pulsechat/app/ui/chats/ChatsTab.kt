package com.pulsechat.app.ui.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulsechat.app.R
import com.pulsechat.app.data.model.Conversation
import com.pulsechat.app.data.model.ConversationType
import com.pulsechat.app.ui.components.CircleAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsTab(
    modifier: Modifier = Modifier,
    onOpenChat: (String) -> Unit,
    viewModel: ChatsViewModel = hiltViewModel()
) {
    val conversations by viewModel.conversations.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Chats", fontWeight = FontWeight.Bold) })

        if (conversations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.no_chats),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.start_conversation),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(conversations, key = { it.id }) { conv ->
                    ConversationItem(
                        conversation = conv,
                        onClick = { onOpenChat(conv.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    val title = when {
        conversation.type == ConversationType.GROUP ->
            conversation.groupName ?: "Group"
        else ->
            conversation.participantDetails.values.firstOrNull()?.displayName
                ?: "Chat"
    }
    val photo = when {
        conversation.type == ConversationType.GROUP -> conversation.groupPhotoUrl
        else -> conversation.participantDetails.values.firstOrNull()?.photoUrl
    }
    val lastText = conversation.lastMessage?.text ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleAvatar(photoUrl = photo, name = title, size = 52.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = lastText.ifBlank { "Tap to chat" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
