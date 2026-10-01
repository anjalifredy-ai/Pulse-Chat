package com.pulsechat.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pulsechat.app.data.model.StatusItem
import com.pulsechat.app.data.model.StatusType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatusRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val uid get() = auth.currentUser?.uid

    fun observeRecentStatuses(): Flow<List<StatusItem>> = callbackFlow {
        val now = Date()
        val listener = firestore.collection("statuses")
            .whereGreaterThan("expiresAt", now)
            .orderBy("expiresAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull {
                    it.toObject(StatusItem::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun postTextStatus(text: String, backgroundColor: String = "#0A84FF"): Result<Unit> {
        val currentUid = uid ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val expires = Calendar.getInstance().apply { add(Calendar.HOUR, 24) }.time
            val item = StatusItem(
                userId = currentUid,
                type = StatusType.TEXT,
                text = text,
                backgroundColor = backgroundColor,
                createdAt = Date(),
                expiresAt = expires
            )
            firestore.collection("statuses").add(item).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun postMediaStatus(
        type: StatusType,
        mediaUrl: String,
        thumbnailUrl: String? = null
    ): Result<Unit> {
        val currentUid = uid ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val expires = Calendar.getInstance().apply { add(Calendar.HOUR, 24) }.time
            val item = StatusItem(
                userId = currentUid,
                type = type,
                mediaUrl = mediaUrl,
                thumbnailUrl = thumbnailUrl,
                createdAt = Date(),
                expiresAt = expires
            )
            firestore.collection("statuses").add(item).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markViewed(statusId: String): Result<Unit> {
        val currentUid = uid ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val ref = firestore.collection("statuses").document(statusId)
            firestore.runTransaction { tx ->
                val snap = tx.get(ref)
                val item = snap.toObject(StatusItem::class.java) ?: return@runTransaction
                if (currentUid !in item.viewedBy) {
                    tx.update(ref, "viewedBy", item.viewedBy + currentUid)
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteStatus(statusId: String): Result<Unit> {
        val currentUid = uid ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val ref = firestore.collection("statuses").document(statusId)
            val snap = ref.get().await()
            val item = snap.toObject(StatusItem::class.java)
            if (item?.userId != currentUid) {
                return Result.failure(Exception("Not your status"))
            }
            ref.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
