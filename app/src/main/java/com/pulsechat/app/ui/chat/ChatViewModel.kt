package com.pulsechat.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pulsechat.app.data.model.Message
import com.pulsechat.app.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val conversationId = MutableStateFlow<String?>(null)

    val currentUid = MutableStateFlow(auth.currentUser?.uid)

    val messages: StateFlow<List<Message>> = conversationId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else chatRepository.observeMessages(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun load(id: String) {
        conversationId.value = id
        viewModelScope.launch {
            chatRepository.markConversationRead(id)
        }
    }

    fun sendMessage(text: String) {
        val id = conversationId.value ?: return
        viewModelScope.launch {
            chatRepository.sendTextMessage(id, text)
        }
    }

    fun deleteForMe(messageId: String) {
        val id = conversationId.value ?: return
        viewModelScope.launch {
            chatRepository.deleteMessageForMe(id, messageId)
        }
    }

    fun react(messageId: String, emoji: String) {
        val id = conversationId.value ?: return
        viewModelScope.launch {
            chatRepository.addReaction(id, messageId, emoji)
        }
    }
}
