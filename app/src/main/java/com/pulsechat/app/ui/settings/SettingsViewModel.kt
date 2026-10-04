package com.pulsechat.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsechat.app.data.prefs.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferences
) : ViewModel() {

    val readReceipts: StateFlow<Boolean> = prefs.readReceiptsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val enterToSend: StateFlow<Boolean> = prefs.enterToSend
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    suspend fun setReadReceipts(enabled: Boolean) = prefs.setReadReceipts(enabled)
    suspend fun setEnterToSend(enabled: Boolean) = prefs.setEnterToSend(enabled)
}
