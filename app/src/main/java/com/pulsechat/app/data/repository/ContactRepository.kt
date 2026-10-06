package com.pulsechat.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pulsechat.app.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    /** Last error for UI (e.g. permission denied). */
    var lastError: String? = null
        private set

    suspend fun getAllUsers(): List<User> {
        val currentUid = auth.currentUser?.uid ?: return emptyList()
        return try {
            lastError = null
            val docs = firestore.collection("users").get().await().documents
            docs.mapNotNull { doc ->
                try {
                    doc.toObject(User::class.java)?.copy(uid = doc.id)
                } catch (_: Exception) {
                    // Fallback if model mismatch
                    User(
                        uid = doc.id,
                        displayName = doc.getString("displayName") ?: "",
                        displayNameLower = (doc.getString("displayName") ?: "").lowercase(),
                        username = doc.getString("username"),
                        phoneNumber = doc.getString("phoneNumber") ?: "",
                        photoUrl = doc.getString("photoUrl"),
                        about = doc.getString("about") ?: ""
                    )
                }
            }.filter { it.uid != currentUid && it.uid.isNotBlank() }
        } catch (e: Exception) {
            lastError = e.message
            emptyList()
        }
    }

    /**
     * Search by display name, username, or email — client-side filter so
     * partial name works without Firestore indexes.
     */
    suspend fun searchUsers(query: String): List<User> {
        val all = getAllUsers()
        val q = query.trim().lowercase()
        if (q.isBlank()) return all
        return all.filter { u ->
            u.displayName.lowercase().contains(q) ||
                u.displayNameLower.contains(q) ||
                (u.username?.lowercase()?.contains(q) == true) ||
                (u.usernameLower?.contains(q) == true) ||
                u.phoneNumber.lowercase().contains(q)
        }
    }
}
