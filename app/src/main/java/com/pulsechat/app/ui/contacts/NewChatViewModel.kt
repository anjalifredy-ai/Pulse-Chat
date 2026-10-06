package com.pulsechat.app.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
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
    private val chatRepository: ChatRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadContacts() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            val list = contactRepository.getAllUsers()
            _users.value = list
            if (list.isEmpty() && contactRepository.lastError != null) {
                _error.value = contactRepository.lastError
            }
            _loading.value = false
        }
    }

    fun search(q: String) {
        _query.value = q
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            val list = contactRepository.searchUsers(q)
            _users.value = list
            if (list.isEmpty() && contactRepository.lastError != null) {
                _error.value = contactRepository.lastError
            }
            _loading.value = false
        }
    }

    suspend fun startChatWith(userId: String): Result<String> {
        return chatRepository.createDirectConversation(userId)
    }

    suspend fun startSelfChat(): Result<String> {
        val myUid = auth.currentUser?.uid
            ?: return Result.failure(Exception("Not logged in"))
        return chatRepository.createSelfConversation(myUid)
    }
}
