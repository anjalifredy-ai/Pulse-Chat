package com.pulsechat.app.data.media

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class UploadProgress(
    val bytesTransferred: Long,
    val totalBytes: Long,
    val downloadUrl: String? = null,
    val error: String? = null
) {
    val fraction: Float
        get() = if (totalBytes > 0) bytesTransferred.toFloat() / totalBytes else 0f
}

@Singleton
class MediaUploader @Inject constructor(
    private val storage: FirebaseStorage
) {
    fun upload(
        uri: Uri,
        folder: String,
        fileName: String = UUID.randomUUID().toString()
    ): Flow<UploadProgress> = callbackFlow {
        val ref = storage.reference.child("$folder/$fileName")
        val task = ref.putFile(uri)

        task.addOnProgressListener { snapshot ->
            trySend(
                UploadProgress(
                    bytesTransferred = snapshot.bytesTransferred,
                    totalBytes = snapshot.totalByteCount
                )
            )
        }.addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { url ->
                trySend(
                    UploadProgress(
                        bytesTransferred = 1,
                        totalBytes = 1,
                        downloadUrl = url.toString()
                    )
                )
                close()
            }.addOnFailureListener { e ->
                trySend(UploadProgress(0, 0, error = e.message))
                close(e)
            }
        }.addOnFailureListener { e ->
            trySend(UploadProgress(0, 0, error = e.message))
            close(e)
        }

        awaitClose {
            task.cancel()
        }
    }

    suspend fun uploadBlocking(uri: Uri, folder: String): Result<String> {
        return try {
            val ref = storage.reference.child("$folder/${UUID.randomUUID()}")
            ref.putFile(uri).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
