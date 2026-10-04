package com.pulsechat.app.ui.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pulsechat.app.data.media.CloudinaryUploader
import com.pulsechat.app.data.model.CallType
import com.pulsechat.app.data.model.Conversation
import com.pulsechat.app.data.model.ConversationType
import com.pulsechat.app.data.model.Message
import com.pulsechat.app.data.model.MessageType
import com.pulsechat.app.data.repository.CallRepository
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
import java.io.File
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val callRepository: CallRepository,
    private val cloudinary: CloudinaryUploader,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val conversationId = MutableStateFlow<String?>(null)
    val currentUid = MutableStateFlow(auth.currentUser?.uid)

    private val _title = MutableStateFlow("Chat")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _photoUrl = MutableStateFlow<String?>(null)
    val photoUrl: StateFlow<String?> = _photoUrl.asStateFlow()

    private val _otherUserId = MutableStateFlow<String?>(null)
    val otherUserId: StateFlow<String?> = _otherUserId.asStateFlow()

    private val _uploading = MutableStateFlow(false)
    val uploading: StateFlow<Boolean> = _uploading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _startedCallId = MutableStateFlow<String?>(null)
    val startedCallId: StateFlow<String?> = _startedCallId.asStateFlow()

    val messages: StateFlow<List<Message>> = conversationId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else chatRepository.observeMessages(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun load(id: String) {
        conversationId.value = id
        viewModelScope.launch {
            chatRepository.markMessagesRead(id)
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
            } else if (conv.participants.size == 1) {
                _title.value = "My (You)"
                _photoUrl.value = conv.participantDetails[myUid]?.photoUrl
            } else {
                val otherId = conv.participants.firstOrNull { it != myUid }
                _otherUserId.value = otherId
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

    fun sendMessage(text: String) {
        val id = conversationId.value ?: return
        viewModelScope.launch { chatRepository.sendTextMessage(id, text) }
    }

    fun sendMedia(uri: Uri, type: MessageType, fileName: String? = null, durationMs: Long = 0) {
        val id = conversationId.value ?: return
        viewModelScope.launch {
            _uploading.value = true
            _error.value = null
            val upload = cloudinary.uploadBlocking(uri, "chat_media")
            upload.fold(
                onSuccess = { url ->
                    chatRepository.sendMediaMessage(
                        conversationId = id,
                        type = type,
                        mediaUrl = url,
                        fileName = fileName,
                        mediaDuration = durationMs,
                        caption = fileName
                    )
                },
                onFailure = { e -> _error.value = e.message ?: "Upload failed" }
            )
            _uploading.value = false
        }
    }

    fun sendVoiceFile(file: File, durationMs: Long) {
        val id = conversationId.value ?: return
        viewModelScope.launch {
            _uploading.value = true
            val upload = cloudinary.uploadFile(file, "voice_messages")
            upload.fold(
                onSuccess = { url ->
                    chatRepository.sendMediaMessage(
                        conversationId = id,
                        type = MessageType.VOICE,
                        mediaUrl = url,
                        mediaDuration = durationMs,
                        caption = "Voice message"
                    )
                },
                onFailure = { e -> _error.value = e.message }
            )
            _uploading.value = false
        }
    }

    fun sendLocation(lat: Double, lng: Double) {
        val id = conversationId.value ?: return
        viewModelScope.launch {
            chatRepository.sendTextMessage(id, "📍 Location\nhttps://maps.google.com/?q=$lat,$lng")
        }
    }

    fun sendContact(name: String, phone: String) {
        val id = conversationId.value ?: return
        viewModelScope.launch {
            chatRepository.sendTextMessage(id, "👤 $name\n📞 $phone")
        }
    }

    fun deleteForMe(messageId: String) {
        val id = conversationId.value ?: return
        viewModelScope.launch {
            chatRepository.deleteMessageForMe(id, messageId)
        }
    }

    fun deleteForEveryone(messageId: String) {
        val id = conversationId.value ?: return
        viewModelScope.launch {
            chatRepository.deleteMessageForEveryone(id, messageId)
        }
    }

    fun startVoiceCall() = startCall(CallType.VOICE)
    fun startVideoCall() = startCall(CallType.VIDEO)

    private fun startCall(type: CallType) {
        val other = _otherUserId.value
        if (other.isNullOrBlank()) {
            _startedCallId.value = "local_${System.currentTimeMillis()}"
            return
        }
        viewModelScope.launch {
            callRepository.startCall(other, type).fold(
                onSuccess = { _startedCallId.value = it },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun clearStartedCall() { _startedCallId.value = null }
}
