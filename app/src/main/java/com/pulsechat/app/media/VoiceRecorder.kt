package com.pulsechat.app.media

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/**
 * Simple press-and-hold voice message recorder.
 * Produces an AAC/M4A file suitable for upload.
 */
class VoiceRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startTimeMs: Long = 0

    val isRecording: Boolean get() = recorder != null

    fun start(): File {
        stopInternal()
        val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
        outputFile = file

        val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        rec.setAudioSource(MediaRecorder.AudioSource.MIC)
        rec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        rec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        rec.setAudioEncodingBitRate(128000)
        rec.setAudioSamplingRate(44100)
        rec.setOutputFile(file.absolutePath)
        rec.prepare()
        rec.start()
        recorder = rec
        startTimeMs = System.currentTimeMillis()
        return file
    }

    /** Returns the recorded file and duration in ms, or null if cancelled / failed. */
    fun stop(): Pair<File, Long>? {
        val file = outputFile
        val duration = System.currentTimeMillis() - startTimeMs
        stopInternal()
        return if (file != null && file.exists() && file.length() > 0) {
            file to duration
        } else {
            file?.delete()
            null
        }
    }

    fun cancel() {
        stopInternal()
        outputFile?.delete()
        outputFile = null
    }

    private fun stopInternal() {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {
        }
        recorder = null
    }
}
