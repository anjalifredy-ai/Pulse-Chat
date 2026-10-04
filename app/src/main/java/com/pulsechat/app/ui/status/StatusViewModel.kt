package com.pulsechat.app.ui.status

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pulsechat.app.data.media.CloudinaryUploader
import com.pulsechat.app.data.model.StatusItem
import com.pulsechat.app.data.model.StatusType
import com.pulsechat.app.data.repository.StatusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatusViewModel @Inject constructor(
    private val statusRepository: StatusRepository,
    private val cloudinary: CloudinaryUploader,
    private val auth: FirebaseAuth
) : ViewModel() {

    val statuses: StateFlow<List<StatusItem>> = statusRepository
        .observeRecentStatuses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myUid: String? get() = auth.currentUser?.uid

    fun postQuickTextStatus(text: String, color: String = "#5B8DEF") {
        viewModelScope.launch {
            statusRepository.postTextStatus(text, color)
        }
    }

    fun postMediaStatus(uri: Uri, type: StatusType, caption: String? = null) {
        viewModelScope.launch {
            val upload = cloudinary.uploadBlocking(uri, "status_media")
            upload.onSuccess { url ->
                statusRepository.postMediaStatus(type = type, mediaUrl = url, caption = caption)
            }
        }
    }

    fun editTextStatus(statusId: String, newText: String, color: String?) {
        viewModelScope.launch {
            statusRepository.updateTextStatus(statusId, newText, color)
        }
    }

    fun deleteStatus(statusId: String) {
        viewModelScope.launch {
            statusRepository.deleteStatus(statusId)
        }
    }

    fun markViewed(statusId: String) {
        viewModelScope.launch {
            statusRepository.markViewed(statusId)
        }
    }
}
