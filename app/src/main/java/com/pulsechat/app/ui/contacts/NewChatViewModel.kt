package com.pulsechat.app.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsechat.app.data.model.User
import com.pulsechat.app.data.repository.ChatRepository
import com.pulsechat.app.data.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewChatViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    fun loadContacts() {
        viewModelScope.launch {
            val device = contactRepository.loadDeviceContacts()
            val matched = contactRepository.discoverUsers(device)
            _users.value = matched
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            _users.value = contactRepository.searchUsers(query)
        }
    }

    suspend fun startChatWith(userId: String): Result<String> {
        return chatRepository.createDirectConversation(userId)
    }
}
