package com.pulsechat.app.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.pulsechat.app.R
import com.pulsechat.app.data.media.CloudinaryUploader
import com.pulsechat.app.data.model.PrivacySettings
import com.pulsechat.app.data.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val cloudinary: CloudinaryUploader,
    @ApplicationContext private val context: Context
) {
    companion object {
        // From google-services.json oauth_client client_type 3 (Web)
        private const val WEB_CLIENT_ID =
            "225740234992-f1lr0gucd5ktb3vu26ee06no1et0hhmd.apps.googleusercontent.com"
    }

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

    fun getGoogleSignInIntent(): Intent {
        val webClientId = try {
            val fromRes = context.getString(R.string.default_web_client_id)
            if (fromRes.isNotBlank()) fromRes else WEB_CLIENT_ID
        } catch (e: Exception) {
            WEB_CLIENT_ID
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build()
        return GoogleSignIn.getClient(context, gso).signInIntent
    }

    suspend fun signInWithGoogle(data: Intent?): Result<User> {
        return try {
            if (data == null) {
                return Result.failure(Exception("Google sign-in cancelled"))
            }
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
                ?: return Result.failure(
                    Exception("No ID token. Add SHA-1 in Firebase (see GOOGLE_LOGIN.md)")
                )
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val fbUser = result.user ?: return Result.failure(Exception("No user"))
            Result.success(
                ensureUserDoc(
                    fbUser.uid,
                    fbUser.email,
                    fbUser.displayName,
                    fbUser.photoUrl?.toString()
                )
            )
        } catch (e: ApiException) {
            val msg = when (e.statusCode) {
                10 -> "Google Error 10: Firebase mein SHA-1 add karo (GOOGLE_LOGIN.md). Email se login karo."
                12501 -> "Google sign-in cancelled"
                7 -> "Network error. Check internet."
                else -> "Google failed (code ${e.statusCode}). Use Email login."
            }
            Result.failure(Exception(msg))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Google sign-in failed"))
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val fbUser = result.user ?: return Result.failure(Exception("No user"))
            Result.success(ensureUserDoc(fbUser.uid, fbUser.email, fbUser.displayName, fbUser.photoUrl?.toString()))
        } catch (e: Exception) {
            Result.failure(Exception(friendlyAuthError(e)))
        }
    }

    suspend fun registerWithEmail(email: String, password: String, displayName: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val fbUser = result.user ?: return Result.failure(Exception("No user"))
            if (displayName.isNotBlank()) {
                fbUser.updateProfile(
                    UserProfileChangeRequest.Builder().setDisplayName(displayName.trim()).build()
                ).await()
            }
            Result.success(
                ensureUserDoc(
                    fbUser.uid,
                    fbUser.email,
                    displayName.ifBlank { fbUser.displayName },
                    null
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception(friendlyAuthError(e)))
        }
    }

    private fun friendlyAuthError(e: Exception): String {
        val m = e.message.orEmpty()
        return when {
            m.contains("PASSWORD", ignoreCase = true) -> "Password must be at least 6 characters"
            m.contains("EMAIL", ignoreCase = true) && m.contains("EXIST", ignoreCase = true) ->
                "Account already exists. Tap Sign in below."
            m.contains("INVALID", ignoreCase = true) -> "Wrong email or password"
            m.contains("NETWORK", ignoreCase = true) -> "Network error. Check internet."
            else -> m.ifBlank { "Login failed" }
        }
    }

    private suspend fun ensureUserDoc(
        uid: String,
        email: String?,
        displayName: String?,
        photoUrl: String?
    ): User {
        val userRef = firestore.collection("users").document(uid)
        val existing = userRef.get().await()
        if (existing.exists()) {
            return existing.toObject(User::class.java) ?: User(uid = uid)
        }
        val newUser = User(
            uid = uid,
            phoneNumber = email ?: "",
            displayName = displayName.orEmpty(),
            about = "Hey there! I am using Pulse Chat.",
            photoUrl = photoUrl,
            createdAt = Date(),
            updatedAt = Date(),
            privacy = PrivacySettings()
        )
        userRef.set(newUser).await()
        return newUser
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
                val upload = cloudinary.uploadBlocking(photoUri, "profile_photos")
                photoUrl = upload.getOrElse { return Result.failure(it) }
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
            firestore.collection("users").document(fbUser.uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        try {
            GoogleSignIn.getClient(
                context,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            ).signOut()
        } catch (_: Exception) { }
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
