package com.pulsechat.app.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Conversation(
    @DocumentId
    val id: String = "",
    val type: ConversationType = ConversationType.DIRECT,
    val participants: List<String> = emptyList(),
    val participantDetails: Map<String, ParticipantInfo> = emptyMap(),
    val lastMessage: LastMessage? = null,
    val unreadCounts: Map<String, Int> = emptyMap(),
    val mutedBy: List<String> = emptyList(),
    val archivedBy: List<String> = emptyList(),
    val pinnedBy: List<String> = emptyList(),
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    // Group specific
    val groupName: String? = null,
    val groupPhotoUrl: String? = null,
    val groupDescription: String? = null,
    val admins: List<String> = emptyList(),
    val createdBy: String? = null,
    val inviteLink: String? = null,
    val onlyAdminsCanMessage: Boolean = false,
    val onlyAdminsCanEditInfo: Boolean = false
)

enum class ConversationType {
    DIRECT, GROUP
}

data class ParticipantInfo(
    val displayName: String = "",
    val photoUrl: String? = null,
    val phoneNumber: String = ""
)

data class LastMessage(
    val id: String = "",
    val text: String = "",
    val type: MessageType = MessageType.TEXT,
    val senderId: String = "",
    val timestamp: Date? = null,
    val status: MessageStatus = MessageStatus.SENT
)
