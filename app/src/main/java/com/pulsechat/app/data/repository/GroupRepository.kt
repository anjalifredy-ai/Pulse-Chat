package com.pulsechat.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pulsechat.app.data.model.Conversation
import com.pulsechat.app.data.model.ConversationType
import com.pulsechat.app.data.model.ParticipantInfo
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val uid get() = auth.currentUser?.uid

    suspend fun createGroup(
        name: String,
        description: String,
        memberIds: List<String>,
        photoUrl: String? = null
    ): Result<String> {
        val currentUid = uid ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val allMembers = (memberIds + currentUid).distinct()
            val ref = firestore.collection("conversations").document()
            val inviteLink = "pulsechat://group/${ref.id}?invite=${UUID.randomUUID().toString().take(8)}"

            val conv = Conversation(
                id = ref.id,
                type = ConversationType.GROUP,
                participants = allMembers,
                groupName = name,
                groupDescription = description,
                groupPhotoUrl = photoUrl,
                admins = listOf(currentUid),
                createdBy = currentUid,
                inviteLink = inviteLink,
                createdAt = Date(),
                updatedAt = Date()
            )
            ref.set(conv).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addMembers(conversationId: String, memberIds: List<String>): Result<Unit> {
        val currentUid = uid ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val ref = firestore.collection("conversations").document(conversationId)
            val snap = ref.get().await()
            val conv = snap.toObject(Conversation::class.java)
                ?: return Result.failure(Exception("Group not found"))
            if (currentUid !in conv.admins) {
                return Result.failure(Exception("Only admins can add members"))
            }
            val updated = (conv.participants + memberIds).distinct()
            ref.update("participants", updated, "updatedAt", Date()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeMember(conversationId: String, memberId: String): Result<Unit> {
        val currentUid = uid ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val ref = firestore.collection("conversations").document(conversationId)
            val snap = ref.get().await()
            val conv = snap.toObject(Conversation::class.java)
                ?: return Result.failure(Exception("Group not found"))
            if (currentUid !in conv.admins && currentUid != memberId) {
                return Result.failure(Exception("Not allowed"))
            }
            val updated = conv.participants.filter { it != memberId }
            val admins = conv.admins.filter { it != memberId }
            ref.update(
                mapOf(
                    "participants" to updated,
                    "admins" to admins,
                    "updatedAt" to Date()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun leaveGroup(conversationId: String): Result<Unit> {
        val currentUid = uid ?: return Result.failure(Exception("Not authenticated"))
        return removeMember(conversationId, currentUid)
    }

    suspend fun promoteAdmin(conversationId: String, userId: String): Result<Unit> {
        val currentUid = uid ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val ref = firestore.collection("conversations").document(conversationId)
            val snap = ref.get().await()
            val conv = snap.toObject(Conversation::class.java)
                ?: return Result.failure(Exception("Group not found"))
            if (currentUid !in conv.admins) {
                return Result.failure(Exception("Only admins can promote"))
            }
            val admins = (conv.admins + userId).distinct()
            ref.update("admins", admins, "updatedAt", Date()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGroupInfo(
        conversationId: String,
        name: String? = null,
        description: String? = null,
        photoUrl: String? = null
    ): Result<Unit> {
        val currentUid = uid ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val ref = firestore.collection("conversations").document(conversationId)
            val snap = ref.get().await()
            val conv = snap.toObject(Conversation::class.java)
                ?: return Result.failure(Exception("Group not found"))
            if (conv.onlyAdminsCanEditInfo && currentUid !in conv.admins) {
                return Result.failure(Exception("Only admins can edit info"))
            }
            val updates = mutableMapOf<String, Any>("updatedAt" to Date())
            name?.let { updates["groupName"] = it }
            description?.let { updates["groupDescription"] = it }
            photoUrl?.let { updates["groupPhotoUrl"] = it }
            ref.update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun revokeInviteLink(conversationId: String): Result<String> {
        val currentUid = uid ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val ref = firestore.collection("conversations").document(conversationId)
            val snap = ref.get().await()
            val conv = snap.toObject(Conversation::class.java)
                ?: return Result.failure(Exception("Group not found"))
            if (currentUid !in conv.admins) {
                return Result.failure(Exception("Only admins can revoke link"))
            }
            val newLink = "pulsechat://group/${conversationId}?invite=${UUID.randomUUID().toString().take(8)}"
            ref.update("inviteLink", newLink, "updatedAt", Date()).await()
            Result.success(newLink)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
