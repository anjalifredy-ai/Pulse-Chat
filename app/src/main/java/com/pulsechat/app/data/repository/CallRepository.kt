package com.pulsechat.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pulsechat.app.data.model.CallSession
import com.pulsechat.app.data.model.CallStatus
import com.pulsechat.app.data.model.CallType
import com.pulsechat.app.data.model.IceCandidatePayload
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Call signaling over Firestore.
 * Documents under `calls/{callId}` hold offer/answer SDP.
 * Subcollection `candidates` holds ICE candidates.
 */
@Singleton
class CallRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val uid get() = auth.currentUser?.uid

    suspend fun startCall(calleeId: String, type: CallType): Result<String> {
        val currentUid = uid ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val ref = firestore.collection("calls").document()
            val session = CallSession(
                id = ref.id,
                callerId = currentUid,
                calleeId = calleeId,
                type = type,
                status = CallStatus.RINGING,
                createdAt = Date()
            )
            ref.set(session).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setOffer(callId: String, sdp: String): Result<Unit> {
        return try {
            firestore.collection("calls").document(callId)
                .update("offerSdp", sdp, "status", CallStatus.CONNECTING.name)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setAnswer(callId: String, sdp: String): Result<Unit> {
        return try {
            firestore.collection("calls").document(callId)
                .update("answerSdp", sdp, "status", CallStatus.CONNECTED.name)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addIceCandidate(callId: String, payload: IceCandidatePayload): Result<Unit> {
        return try {
            firestore.collection("calls").document(callId)
                .collection("candidates")
                .add(payload)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeCall(callId: String): Flow<CallSession?> = callbackFlow {
        val listener = firestore.collection("calls").document(callId)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snap?.toObject(CallSession::class.java)?.copy(id = snap.id))
            }
        awaitClose { listener.remove() }
    }

    fun observeIceCandidates(callId: String, excludeUid: String): Flow<IceCandidatePayload> = callbackFlow {
        val listener = firestore.collection("calls").document(callId)
            .collection("candidates")
            .addSnapshotListener { snap, error ->
                if (error != null) return@addSnapshotListener
                snap?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val payload = change.document.toObject(IceCandidatePayload::class.java)
                        if (payload.fromUid != excludeUid) {
                            trySend(payload)
                        }
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun endCall(callId: String, status: CallStatus = CallStatus.ENDED): Result<Unit> {
        return try {
            firestore.collection("calls").document(callId)
                .update(
                    mapOf(
                        "status" to status.name,
                        "endedAt" to Date()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCallHistory(limit: Long = 50): List<CallSession> {
        val currentUid = uid ?: return emptyList()
        return try {
            val asCaller = firestore.collection("calls")
                .whereEqualTo("callerId", currentUid)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit)
                .get().await()
            val asCallee = firestore.collection("calls")
                .whereEqualTo("calleeId", currentUid)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit)
                .get().await()
            (asCaller.documents + asCallee.documents)
                .mapNotNull { it.toObject(CallSession::class.java)?.copy(id = it.id) }
                .distinctBy { it.id }
                .sortedByDescending { it.createdAt }
                .take(limit.toInt())
        } catch (e: Exception) {
            emptyList()
        }
    }
}
