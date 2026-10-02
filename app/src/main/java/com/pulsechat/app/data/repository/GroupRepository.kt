package com.pulsechat.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pulsechat.app.data.model.Conversation
import com.pulsechat.app.data.model.ConversationType
import com.pulsechat.app.data.model.ParticipantInfo
import kotlinx.coroutines.tasks.await
import java.util.Date
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
        memberIds: List<String>,
        photoUrl: String?
    ): Result<String> {
        val currentUid = uid ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val participants = (memberIds + currentUid).distinct()
            val details = mutableMapOf<String, ParticipantInfo>()
            for (id in participants) {
                val snap = firestore.collection("users").document(id).get().await()
                val displayName = snap.getString("displayName") ?: ""
                val photo = snap.getString("photoUrl")
                val phone = snap.getString("phoneNumber") ?: ""
                details[id] = ParticipantInfo(
                    displayName = displayName,
                    photoUrl = photo,
                    phoneNumber = phone
                )
            }
            val ref = firestore.collection("conversations").document()
            val conv = Conversation(
                id = ref.id,
                type = ConversationType.GROUP,
                participants = participants,
                participantDetails = details,
                groupName = name,
                groupPhotoUrl = photoUrl,
                admins = listOf(currentUid),
                createdBy = currentUid,
                createdAt = Date(),
                updatedAt = Date()
            )
            ref.set(conv).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
