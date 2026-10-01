package com.pulsechat.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsechat.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = false,
    val hasProfile: Boolean = false,
    val phoneNumber: String = "",
    val verificationId: String? = null,
    val error: String? = null,
    val otpSent: Boolean = false,
    val resendSeconds: Int = 0
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            val user = authRepository.currentUser()
            _authState.update {
                it.copy(
                    isLoading = false,
                    isAuthenticated = user != null,
                    hasProfile = user?.displayName?.isNotBlank() == true
                )
            }
        }
    }

    fun sendOtp(phoneNumber: String, activity: android.app.Activity) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            authRepository.sendOtp(
                phoneNumber = phoneNumber,
                activity = activity,
                onCodeSent = { verificationId ->
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            otpSent = true,
                            verificationId = verificationId,
                            phoneNumber = phoneNumber,
                            resendSeconds = 60
                        )
                    }
                    startResendTimer()
                },
                onError = { message ->
                    _authState.update {
                        it.copy(isLoading = false, error = message)
                    }
                }
            )
        }
    }

    fun verifyOtp(code: String) {
        val verificationId = _authState.value.verificationId ?: return
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.verifyOtp(verificationId, code)
            result.fold(
                onSuccess = { user ->
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            hasProfile = user.displayName.isNotBlank()
                        )
                    }
                },
                onFailure = { e ->
                    _authState.update {
                        it.copy(isLoading = false, error = e.message ?: "Invalid code")
                    }
                }
            )
        }
    }

    fun saveProfile(displayName: String, about: String, photoUri: android.net.Uri?) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.updateProfile(displayName, about, photoUri)
            result.fold(
                onSuccess = {
                    _authState.update {
                        it.copy(isLoading = false, hasProfile = true)
                    }
                },
                onFailure = { e ->
                    _authState.update {
                        it.copy(isLoading = false, error = e.message)
                    }
                }
            )
        }
    }

    fun clearError() {
        _authState.update { it.copy(error = null) }
    }

    private fun startResendTimer() {
        viewModelScope.launch {
            while (_authState.value.resendSeconds > 0) {
                kotlinx.coroutines.delay(1000)
                _authState.update { it.copy(resendSeconds = it.resendSeconds - 1) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.update {
                AuthUiState(isLoading = false, isAuthenticated = false, hasProfile = false)
            }
        }
    }
}
