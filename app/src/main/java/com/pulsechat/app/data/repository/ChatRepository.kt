package com.pulsechat.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pulsechat.app.data.model.Conversation
import com.pulsechat.app.data.model.ConversationType
import com.pulsechat.app.data.model.LastMessage
import com.pulsechat.app.data.model.Message
import com.pulsechat.app.data.model.MessageStatus
import com.pulsechat.app.data.model.MessageType
import com.pulsechat.app.data.model.ParticipantInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val uid get() = auth.currentUser?.uid

    fun observeConversations(): Flow<List<Conversation>> = callbackFlow {
        val currentUid = uid
        if (currentUid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("conversations")
            .whereArrayContains("participants", currentUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull {
                    it.toObject(Conversation::class.java)?.copy(id = it.id)
                }?.sortedByDescending { it.updatedAt?.time ?: 0L } ?: emptyList()
                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    fun observeMessages(conversationId: String): Flow<List<Message>> = callbackFlow {
        val listener = firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .limitToLast(200)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull {
                    it.toObject(Message::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendTextMessage(conversationId: String, text: String): Result<Unit> {
        val currentUid = uid ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val clientId = UUID.randomUUID().toString()
            val ref = firestore.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .document()
            val message = Message(
                id = ref.id,
                conversationId = conversationId,
                senderId = currentUid,
                type = MessageType.TEXT,
                text = text,
                status = MessageStatus.SENT,
                createdAt = Date(),
                clientId = clientId
            )
            ref.set(message).await()

            firestore.collection("conversations").document(conversationId)
                .update(
                    mapOf(
                        "lastMessage" to LastMessage(
                            id = ref.id,
                            text = text,
                            type = MessageType.TEXT,
                            senderId = currentUid,
                            timestamp = Date(),
                            status = MessageStatus.SENT
                        ),
                        "updatedAt" to Date()
                    )
                ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun loadParticipantInfo(userId: String): ParticipantInfo {
        return try {
            val snap = firestore.collection("users").document(userId).get().await()
            ParticipantInfo(
                displayName = snap.getString("displayName") ?: "",
                photoUrl = snap.getString("photoUrl"),
                phoneNumber = snap.getString("phoneNumber") ?: ""
            )
        } catch (_: Exception) {
            ParticipantInfo()
        }
    }

    suspend fun createDirectConversation(otherUserId: String): Result<String> {
        val currentUid = uid ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val existing = firestore.collection("conversations")
                .whereArrayContains("participants", currentUid)
                .get().await()
                .documents
                .mapNotNull { it.toObject(Conversation::class.java)?.copy(id = it.id) }
                .firstOrNull {
                    it.type == ConversationType.DIRECT &&
                        it.participants.size == 2 &&
                        it.participants.contains(otherUserId)
                }
            if (existing != null) return Result.success(existing.id)

            val me = loadParticipantInfo(currentUid)
            val other = loadParticipantInfo(otherUserId)
            val ref = firestore.collection("conversations").document()
            val conv = Conversation(
                id = ref.id,
                type = ConversationType.DIRECT,
                participants = listOf(currentUid, otherUserId),
                participantDetails = mapOf(currentUid to me, otherUserId to other),
                createdAt = Date(),
                updatedAt = Date()
            )
            ref.set(conv).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Message yourself (notes) — single-participant conversation */
    suspend fun createSelfConversation(myUid: String): Result<String> {
        return try {
            val existing = firestore.collection("conversations")
                .whereArrayContains("participants", myUid)
                .get().await()
                .documents
                .mapNotNull { it.toObject(Conversation::class.java)?.copy(id = it.id) }
                .firstOrNull {
                    it.type == ConversationType.DIRECT &&
                        it.participants.size == 1 &&
                        it.participants.contains(myUid)
                }
            if (existing != null) return Result.success(existing.id)

            val me = loadParticipantInfo(myUid)
            val selfInfo = me.copy(
                displayName = if (me.displayName.isBlank()) "You"
                else "${me.displayName} (You)"
            )
            val ref = firestore.collection("conversations").document()
            val conv = Conversation(
                id = ref.id,
                type = ConversationType.DIRECT,
                participants = listOf(myUid),
                participantDetails = mapOf(myUid to selfInfo),
                createdAt = Date(),
                updatedAt = Date()
            )
            ref.set(conv).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMessageForMe(conversationId: String, messageId: String): Result<Unit> {
        val currentUid = uid ?: return Result.failure(Exception("Not authenticated"))
        return try {
            firestore.collection("conversations").document(conversationId)
                .collection("messages").document(messageId)
                .update("deletedFor", FieldValue.arrayUnion(currentUid))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addReaction(conversationId: String, messageId: String, emoji: String): Result<Unit> {
        val currentUid = uid ?: return Result.failure(Exception("Not authenticated"))
        return try {
            firestore.collection("conversations").document(conversationId)
                .collection("messages").document(messageId)
                .update("reactions.$currentUid", emoji)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markConversationRead(conversationId: String): Result<Unit> {
        val currentUid = uid ?: return Result.failure(Exception("Not authenticated"))
        return try {
            firestore.collection("conversations").document(conversationId)
                .update("unreadCounts.$currentUid", 0)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
