package com.pulsechat.app.data.repository

import android.app.Activity
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.pulsechat.app.data.media.CloudinaryUploader
import com.pulsechat.app.data.model.PrivacySettings
import com.pulsechat.app.data.model.User
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val cloudinary: CloudinaryUploader
) {
    fun currentFirebaseUser() = auth.currentUser

    suspend fun currentUser(): User? {
        val fbUser = auth.currentUser ?: return null
        return try {
            val snap = firestore.collection("users").document(fbUser.uid).get().await()
            snap.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun sendOtp(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {}

            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                onError(e.message ?: "Verification failed")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                onCodeSent(verificationId)
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun verifyOtp(verificationId: String, code: String): Result<User> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            val result = auth.signInWithCredential(credential).await()
            val fbUser = result.user ?: return Result.failure(Exception("No user"))

            val userRef = firestore.collection("users").document(fbUser.uid)
            val existing = userRef.get().await()
            val user = if (existing.exists()) {
                existing.toObject(User::class.java) ?: User(uid = fbUser.uid)
            } else {
                val newUser = User(
                    uid = fbUser.uid,
                    phoneNumber = fbUser.phoneNumber ?: "",
                    displayName = "",
                    about = "Hey there! I am using Pulse Chat.",
                    createdAt = Date(),
                    updatedAt = Date(),
                    privacy = PrivacySettings()
                )
                userRef.set(newUser).await()
                newUser
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        displayName: String,
        about: String,
        photoUri: Uri?
    ): Result<Unit> {
        val fbUser = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
        return try {
            var photoUrl: String? = null
            if (photoUri != null) {
                // Free Cloudinary — no Firebase Storage / Blaze
                val upload = cloudinary.uploadBlocking(photoUri, "profile_photos")
                photoUrl = upload.getOrElse {
                    return Result.failure(it)
                }
            }

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .apply { if (photoUrl != null) setPhotoUri(Uri.parse(photoUrl)) }
                .build()
            fbUser.updateProfile(profileUpdates).await()

            val updates = mutableMapOf<String, Any>(
                "displayName" to displayName,
                "about" to about,
                "updatedAt" to Date()
            )
            if (photoUrl != null) updates["photoUrl"] = photoUrl

            firestore.collection("users").document(fbUser.uid)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        auth.signOut()
    }

    suspend fun deleteAccount(): Result<Unit> {
        val fbUser = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
        return try {
            firestore.collection("users").document(fbUser.uid).delete().await()
            fbUser.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
