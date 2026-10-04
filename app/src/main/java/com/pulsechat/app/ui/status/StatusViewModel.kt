package com.pulsechat.app.ui.status

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val cloudinary: CloudinaryUploader
) : ViewModel() {

    val statuses: StateFlow<List<StatusItem>> = statusRepository
        .observeRecentStatuses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun postQuickTextStatus(text: String) {
        viewModelScope.launch {
            statusRepository.postTextStatus(text)
        }
    }

    fun postMediaStatus(uri: Uri, type: StatusType) {
        viewModelScope.launch {
            val upload = cloudinary.uploadBlocking(uri, "status_media")
            upload.onSuccess { url ->
                statusRepository.postMediaStatus(type = type, mediaUrl = url)
            }
        }
    }
}
