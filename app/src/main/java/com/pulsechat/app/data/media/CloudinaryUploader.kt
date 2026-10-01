package com.pulsechat.app.data.media

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Free-tier friendly media upload via Cloudinary unsigned preset.
 * No API secret in the app — only cloud name + unsigned upload preset.
 *
 * Setup (mobile browser):
 * 1. https://cloudinary.com → free signup
 * 2. Settings → Upload → Add upload preset → Signing mode: Unsigned
 * 3. Put cloud name + preset name in BuildConfig / local defaults below
 */
@Singleton
class CloudinaryUploader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Replace these after creating your free Cloudinary account.
     * Or set via BuildConfig fields later.
     */
    companion object {
        // TODO: user replaces these two values
        const val CLOUD_NAME = "YOUR_CLOUD_NAME"
        const val UPLOAD_PRESET = "pulse_chat_unsigned"
    }

    fun upload(
        uri: Uri,
        folder: String = "pulse_chat"
    ): Flow<UploadProgress> = flow {
        emit(UploadProgress(0, 100))
        val result = uploadBlocking(uri, folder)
        result.fold(
            onSuccess = { url ->
                emit(UploadProgress(100, 100, downloadUrl = url))
            },
            onFailure = { e ->
                emit(UploadProgress(0, 0, error = e.message ?: "Upload failed"))
            }
        )
    }.flowOn(Dispatchers.IO)

    suspend fun uploadBlocking(uri: Uri, folder: String = "pulse_chat"): Result<String> =
        withContext(Dispatchers.IO) {
            if (CLOUD_NAME == "YOUR_CLOUD_NAME") {
                return@withContext Result.failure(
                    Exception("Cloudinary not configured. Set CLOUD_NAME and UPLOAD_PRESET in CloudinaryUploader.")
                )
            }
            try {
                val temp = uriToTempFile(uri)
                val mediaType = context.contentResolver.getType(uri)?.toMediaTypeOrNull()
                    ?: "application/octet-stream".toMediaTypeOrNull()

                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", temp.name, temp.asRequestBody(mediaType))
                    .addFormDataPart("upload_preset", UPLOAD_PRESET)
                    .addFormDataPart("folder", folder)
                    .build()

                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/auto/upload")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    val json = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("Cloudinary error: $json"))
                    }
                    val secureUrl = JSONObject(json).optString("secure_url")
                    if (secureUrl.isBlank()) {
                        return@withContext Result.failure(Exception("No URL in Cloudinary response"))
                    }
                    Result.success(secureUrl)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun uploadFile(file: File, folder: String = "pulse_chat"): Result<String> =
        withContext(Dispatchers.IO) {
            if (CLOUD_NAME == "YOUR_CLOUD_NAME") {
                return@withContext Result.failure(Exception("Cloudinary not configured"))
            }
            try {
                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file",
                        file.name,
                        file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                    )
                    .addFormDataPart("upload_preset", UPLOAD_PRESET)
                    .addFormDataPart("folder", folder)
                    .build()

                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/auto/upload")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    val json = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("Cloudinary error: $json"))
                    }
                    val secureUrl = JSONObject(json).optString("secure_url")
                    if (secureUrl.isBlank()) {
                        return@withContext Result.failure(Exception("No URL"))
                    }
                    Result.success(secureUrl)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun uriToTempFile(uri: Uri): File {
        val temp = File(context.cacheDir, "upload_${System.currentTimeMillis()}")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(temp).use { output -> input.copyTo(output) }
        } ?: throw IllegalArgumentException("Cannot read uri")
        return temp
    }
}
