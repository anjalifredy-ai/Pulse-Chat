package com.pulsechat.app.data.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Message(
    @DocumentId
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val type: MessageType = MessageType.TEXT,
    val text: String? = null,
    val mediaUrl: String? = null,
    val mediaThumbnailUrl: String? = null,
    val mediaMimeType: String? = null,
    val mediaSize: Long = 0,
    val mediaDuration: Long = 0, // ms for audio/video
    val mediaWidth: Int = 0,
    val mediaHeight: Int = 0,
    val fileName: String? = null,
    val replyToMessageId: String? = null,
    val replyToText: String? = null,
    val replyToSenderId: String? = null,
    val forwardedFrom: String? = null,
    val reactions: Map<String, String> = emptyMap(), // userId -> emoji
    val starredBy: List<String> = emptyList(),
    val deletedFor: List<String> = emptyList(),
    val deletedForEveryone: Boolean = false,
    val status: MessageStatus = MessageStatus.SENDING,
    val deliveredTo: List<String> = emptyList(),
    val readBy: List<String> = emptyList(),
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    val clientId: String? = null // for offline optimistic updates
)

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    VOICE,
    DOCUMENT,
    CONTACT,
    LOCATION,
    STICKER,
    GIF,
    SYSTEM
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}
