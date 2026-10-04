package com.pulsechat.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("pulse_settings")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val readReceipts = booleanPreferencesKey("read_receipts")
    private val enterIsSend = booleanPreferencesKey("enter_is_send")
    private val wallpaper = stringPreferencesKey("chat_wallpaper")
    private val darkMode = booleanPreferencesKey("dark_mode")

    val readReceiptsEnabled: Flow<Boolean> = context.dataStore.data.map { it[readReceipts] ?: true }
    val enterToSend: Flow<Boolean> = context.dataStore.data.map { it[enterIsSend] ?: false }
    val wallpaperKey: Flow<String> = context.dataStore.data.map { it[wallpaper] ?: "default" }
    val darkModeEnabled: Flow<Boolean> = context.dataStore.data.map { it[darkMode] ?: true }

    suspend fun setReadReceipts(enabled: Boolean) {
        context.dataStore.edit { it[readReceipts] = enabled }
    }

    suspend fun setEnterToSend(enabled: Boolean) {
        context.dataStore.edit { it[enterIsSend] = enabled }
    }

    suspend fun setWallpaper(key: String) {
        context.dataStore.edit { it[wallpaper] = key }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[darkMode] = enabled }
    }
}
