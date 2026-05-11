package com.example.dnevnik.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaRecorder
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaRecorder: MediaRecorder? = null

    fun createImageFile(): File {
        val timeStamp = System.currentTimeMillis()
        val imageDir = File(context.filesDir, "images")
        if (!imageDir.exists()) imageDir.mkdirs()
        return File(imageDir, "IMG_$timeStamp.jpg")
    }

    fun createVideoFile(): File {
        val timeStamp = System.currentTimeMillis()
        val videoDir = File(context.filesDir, "videos")
        if (!videoDir.exists()) videoDir.mkdirs()
        return File(videoDir, "VID_$timeStamp.mp4")
    }

    fun createAudioFile(): File {
        val timeStamp = System.currentTimeMillis()
        val audioDir = File(context.filesDir, "audio")
        if (!audioDir.exists()) audioDir.mkdirs()
        return File(audioDir, "AUD_$timeStamp.mp3")
    }

    fun compressImage(imageFile: File, maxWidth: Int = 1024, maxHeight: Int = 1024): File {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(imageFile.absolutePath, options)

        val scaleFactor = minOf(
            maxWidth / options.outWidth,
            maxHeight / options.outHeight,
            1
        )

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = when {
                scaleFactor <= 0.5f -> 2
                scaleFactor <= 0.25f -> 4
                else -> 1
            }
        }

        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath, decodeOptions)
        val compressedFile = File(imageFile.parent, "compressed_${imageFile.name}")

        FileOutputStream(compressedFile).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        }

        bitmap.recycle()
        return compressedFile
    }

    fun startAudioRecording(outputFile: File) {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(44100)
            setAudioEncodingBitRate(96000)
            setOutputFile(outputFile.absolutePath)
            prepare()
            start()
        }
    }

    fun stopAudioRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
    }

    fun getAudioAmplitude(): Int {
        return mediaRecorder?.maxAmplitude ?: 0
    }

    fun deleteMediaFile(file: File) {
        if (file.exists()) {
            file.delete()
        }
    }

    fun deleteMediaFile(uri: Uri) {
        try {
            context.contentResolver.delete(uri, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}