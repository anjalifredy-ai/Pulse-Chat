package com.pulsechat.app.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsechat.app.data.model.User
import com.pulsechat.app.data.repository.ContactRepository
import com.pulsechat.app.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _selected = MutableStateFlow<Set<String>>(emptySet())
    val selected: StateFlow<Set<String>> = _selected.asStateFlow()

    private val _creating = MutableStateFlow(false)
    val creating: StateFlow<Boolean> = _creating.asStateFlow()

    private val _createdId = MutableStateFlow<String?>(null)
    val createdId: StateFlow<String?> = _createdId.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadUsers() {
        viewModelScope.launch {
            _users.value = contactRepository.getAllUsers()
        }
    }

    fun toggle(uid: String) {
        _selected.update { set ->
            if (set.contains(uid)) set - uid else set + uid
        }
    }

    fun createGroup(name: String) {
        if (name.isBlank() || _selected.value.isEmpty()) return
        viewModelScope.launch {
            _creating.value = true
            _error.value = null
            val result = groupRepository.createGroup(
                name = name.trim(),
                memberIds = _selected.value.toList(),
                photoUrl = null
            )
            _creating.value = false
            result.fold(
                onSuccess = { id -> _createdId.value = id },
                onFailure = { e -> _error.value = e.message ?: "Failed to create group" }
            )
        }
    }
}
