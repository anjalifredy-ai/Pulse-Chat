package com.pulsechat.app.data.media

data class UploadProgress(
    val bytesTransferred: Long,
    val totalBytes: Long,
    val downloadUrl: String? = null,
    val error: String? = null
) {
    val fraction: Float
        get() = if (totalBytes > 0) bytesTransferred.toFloat() / totalBytes else 0f
}
