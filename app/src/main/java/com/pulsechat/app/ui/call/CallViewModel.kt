package com.pulsechat.app.ui.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsechat.app.data.model.CallStatus
import com.pulsechat.app.data.repository.CallRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CallUiState(
    val callId: String = "",
    val remoteName: String = "",
    val status: CallStatus = CallStatus.RINGING,
    val isVideo: Boolean = false,
    val micMuted: Boolean = false,
    val cameraOn: Boolean = true,
    val speakerOn: Boolean = false,
    val durationSeconds: Long = 0
) {
    val durationLabel: String
        get() {
            val m = durationSeconds / 60
            val s = durationSeconds % 60
            return "%d:%02d".format(m, s)
        }
}

@HiltViewModel
class CallViewModel @Inject constructor(
    private val callRepository: CallRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null
    private var timerJob: Job? = null

    fun attach(callId: String) {
        _uiState.update { it.copy(callId = callId) }
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            callRepository.observeCall(callId).collect { session ->
                if (session == null) return@collect
                _uiState.update {
                    it.copy(
                        status = session.status,
                        isVideo = session.type.name == "VIDEO"
                    )
                }
                if (session.status == CallStatus.CONNECTED && timerJob == null) {
                    startTimer()
                }
                if (session.status == CallStatus.ENDED ||
                    session.status == CallStatus.MISSED ||
                    session.status == CallStatus.REJECTED ||
                    session.status == CallStatus.CANCELLED
                ) {
                    timerJob?.cancel()
                }
            }
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(durationSeconds = it.durationSeconds + 1) }
            }
        }
    }

    fun toggleMute() {
        _uiState.update { it.copy(micMuted = !it.micMuted) }
        // WebRtcClient.setMicrophoneMute would be called from UI layer holding the client
    }

    fun toggleSpeaker() {
        _uiState.update { it.copy(speakerOn = !it.speakerOn) }
    }

    fun toggleCamera() {
        _uiState.update { it.copy(cameraOn = !it.cameraOn) }
    }

    fun switchCamera() {
        // Flip front/rear via WebRtcClient
    }

    fun endCall() {
        val id = _uiState.value.callId
        if (id.isNotBlank()) {
            viewModelScope.launch {
                callRepository.endCall(id, CallStatus.ENDED)
            }
        }
        timerJob?.cancel()
        observeJob?.cancel()
    }

    fun detach() {
        observeJob?.cancel()
        timerJob?.cancel()
    }
}
