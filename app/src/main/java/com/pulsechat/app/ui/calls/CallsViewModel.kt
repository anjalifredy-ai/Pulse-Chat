package com.pulsechat.app.ui.calls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsechat.app.data.model.CallSession
import com.pulsechat.app.data.repository.CallRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallsViewModel @Inject constructor(
    private val callRepository: CallRepository
) : ViewModel() {

    private val _history = MutableStateFlow<List<CallSession>>(emptyList())
    val history: StateFlow<List<CallSession>> = _history.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _history.value = callRepository.getCallHistory()
        }
    }
}
