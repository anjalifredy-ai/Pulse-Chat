package com.pulsechat.app.data.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class StatusItem(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val type: StatusType = StatusType.TEXT,
    val text: String? = null,
    val mediaUrl: String? = null,
    val thumbnailUrl: String? = null,
    val backgroundColor: String? = null,
    val createdAt: Date? = null,
    val expiresAt: Date? = null,
    val viewedBy: List<String> = emptyList()
)

enum class StatusType { TEXT, IMAGE, VIDEO }
