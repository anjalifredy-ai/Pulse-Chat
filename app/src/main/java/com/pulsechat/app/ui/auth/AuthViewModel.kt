package com.pulsechat.app.ui.auth

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pulsechat.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = false,
    val hasProfile: Boolean = false,
    val displayName: String = "",
    val photoUrl: String? = null,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener { auth ->
        viewModelScope.launch {
            refreshFromAuth(auth.currentUser != null)
        }
    }

    init {
        firebaseAuth.addAuthStateListener(authListener)
        checkAuthState()
    }

    override fun onCleared() {
        firebaseAuth.removeAuthStateListener(authListener)
        super.onCleared()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            refreshFromAuth(firebaseAuth.currentUser != null)
        }
    }

    private suspend fun refreshFromAuth(hasFirebaseUser: Boolean) {
        if (!hasFirebaseUser) {
            _authState.update {
                AuthUiState(isLoading = false, isAuthenticated = false, hasProfile = false)
            }
            return
        }
        val fb = firebaseAuth.currentUser
        val firestoreUser = try {
            authRepository.currentUser()
        } catch (_: Exception) {
            null
        }
        val name = firestoreUser?.displayName?.takeIf { it.isNotBlank() }
            ?: fb?.displayName.orEmpty()
        val photo = firestoreUser?.photoUrl ?: fb?.photoUrl?.toString()
        _authState.update {
            it.copy(
                isLoading = false,
                isAuthenticated = true,
                hasProfile = name.isNotBlank(),
                displayName = name,
                photoUrl = photo,
                error = null
            )
        }
    }

    fun getGoogleSignInIntent(): Intent = authRepository.getGoogleSignInIntent()

    fun handleGoogleResult(data: Intent?) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.signInWithGoogle(data)
            result.fold(
                onSuccess = { user ->
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            hasProfile = user.displayName.isNotBlank(),
                            displayName = user.displayName,
                            photoUrl = user.photoUrl
                        )
                    }
                },
                onFailure = { e ->
                    _authState.update {
                        it.copy(isLoading = false, error = e.message ?: "Google sign-in failed")
                    }
                }
            )
        }
    }

    fun signInEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.signInWithEmail(email, password)
            result.fold(
                onSuccess = { user ->
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            hasProfile = user.displayName.isNotBlank(),
                            displayName = user.displayName,
                            photoUrl = user.photoUrl
                        )
                    }
                },
                onFailure = { e ->
                    _authState.update {
                        it.copy(isLoading = false, error = e.message ?: "Login failed")
                    }
                }
            )
        }
    }

    fun registerEmail(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.registerWithEmail(email, password, displayName)
            result.fold(
                onSuccess = { user ->
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            hasProfile = user.displayName.isNotBlank(),
                            displayName = user.displayName,
                            photoUrl = user.photoUrl
                        )
                    }
                },
                onFailure = { e ->
                    _authState.update {
                        it.copy(isLoading = false, error = e.message ?: "Registration failed")
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
                        it.copy(isLoading = false, hasProfile = true, displayName = displayName)
                    }
                },
                onFailure = { e ->
                    _authState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun clearError() {
        _authState.update { it.copy(error = null) }
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
