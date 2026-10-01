package com.pulsechat.app.data.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class CallSession(
    @DocumentId
    val id: String = "",
    val callerId: String = "",
    val calleeId: String = "",
    val type: CallType = CallType.VOICE,
    val status: CallStatus = CallStatus.RINGING,
    val offerSdp: String? = null,
    val answerSdp: String? = null,
    val createdAt: Date? = null,
    val endedAt: Date? = null,
    val durationSeconds: Long = 0
)

enum class CallType { VOICE, VIDEO }

enum class CallStatus {
    RINGING,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    ENDED,
    MISSED,
    REJECTED,
    CANCELLED
}

data class IceCandidatePayload(
    val sdpMid: String = "",
    val sdpMLineIndex: Int = 0,
    val candidate: String = "",
    val fromUid: String = ""
)
