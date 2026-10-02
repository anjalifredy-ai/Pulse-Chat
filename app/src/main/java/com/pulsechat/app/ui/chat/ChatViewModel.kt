package com.pulsechat.app.ui.chat

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pulsechat.app.data.model.Conversation
import com.pulsechat.app.data.model.ConversationType
import com.pulsechat.app.data.model.Message
import com.pulsechat.app.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val conversationId = MutableStateFlow<String?>(null)

    val currentUid = MutableStateFlow(auth.currentUser?.uid)

    private val _title = MutableStateFlow("Chat")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _photoUrl = MutableStateFlow<String?>(null)
    val photoUrl: StateFlow<String?> = _photoUrl.asStateFlow()

    private val _wallpaperColor = MutableStateFlow(Color(0xFF0B141A))
    val wallpaperColor: StateFlow<Color> = _wallpaperColor.asStateFlow()

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
            loadConversationMeta(id)
        }
    }

    private suspend fun loadConversationMeta(id: String) {
        try {
            val snap = firestore.collection("conversations").document(id).get().await()
            val conv = snap.toObject(Conversation::class.java)?.copy(id = snap.id) ?: return
            val myUid = auth.currentUser?.uid
            if (conv.type == ConversationType.GROUP) {
                _title.value = conv.groupName ?: "Group"
                _photoUrl.value = conv.groupPhotoUrl
            } else {
                val otherId = conv.participants.firstOrNull { it != myUid }
                val info = otherId?.let { conv.participantDetails[it] }
                if (info != null && info.displayName.isNotBlank()) {
                    _title.value = info.displayName
                    _photoUrl.value = info.photoUrl
                } else if (otherId != null) {
                    val userSnap = firestore.collection("users").document(otherId).get().await()
                    _title.value = userSnap.getString("displayName") ?: "Chat"
                    _photoUrl.value = userSnap.getString("photoUrl")
                }
            }
        } catch (_: Exception) { }
    }

    fun setWallpaper(color: Color) {
        _wallpaperColor.value = color
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
