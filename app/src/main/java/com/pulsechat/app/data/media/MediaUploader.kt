package com.pulsechat.app.data.media

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-facing media upload API.
 * Uses Cloudinary (free) — not Firebase Storage (no Blaze required).
 */
@Singleton
class MediaUploader @Inject constructor(
    private val cloudinary: CloudinaryUploader
) {
    fun upload(uri: Uri, folder: String = "pulse_chat"): Flow<UploadProgress> =
        cloudinary.upload(uri, folder)

    suspend fun uploadBlocking(uri: Uri, folder: String = "pulse_chat"): Result<String> =
        cloudinary.uploadBlocking(uri, folder)
}
