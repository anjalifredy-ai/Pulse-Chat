package com.pulsechat.app.ui.status

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsechat.app.data.model.StatusItem
import com.pulsechat.app.data.repository.StatusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatusViewModel @Inject constructor(
    private val statusRepository: StatusRepository
) : ViewModel() {

    var showCreateDialog by mutableStateOf(false)

    val statuses: StateFlow<List<StatusItem>> = statusRepository
        .observeRecentStatuses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun postQuickTextStatus(text: String) {
        viewModelScope.launch {
            statusRepository.postTextStatus(text)
        }
    }
}
