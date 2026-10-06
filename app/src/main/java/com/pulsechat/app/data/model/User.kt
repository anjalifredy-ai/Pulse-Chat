package com.pulsechat.app.data.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class User(
    @DocumentId
    val uid: String = "",
    val phoneNumber: String = "", // also used for email on email login
    val displayName: String = "",
    /** Lowercase copy for search */
    val displayNameLower: String = "",
    val username: String? = null,
    val usernameLower: String? = null,
    val about: String = "Hey there! I am using Pulse Chat.",
    val photoUrl: String? = null,
    val fcmTokens: List<String> = emptyList(),
    val lastSeen: Date? = null,
    val isOnline: Boolean = false,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    val privacy: PrivacySettings = PrivacySettings(),
    val blockedUsers: List<String> = emptyList()
)

data class PrivacySettings(
    val lastSeen: Visibility = Visibility.EVERYONE,
    val profilePhoto: Visibility = Visibility.EVERYONE,
    val about: Visibility = Visibility.EVERYONE,
    val status: Visibility = Visibility.CONTACTS,
    val readReceipts: Boolean = true,
    val groups: Visibility = Visibility.EVERYONE
)

enum class Visibility {
    EVERYONE, CONTACTS, NOBODY
}
