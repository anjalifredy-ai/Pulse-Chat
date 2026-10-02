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
    suspend fun getAllUsers(): List<User> {
        val currentUid = auth.currentUser?.uid ?: return emptyList()
        return try {
            firestore.collection("users")
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(User::class.java)?.copy(uid = it.id) }
                .filter { it.uid != currentUid }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
