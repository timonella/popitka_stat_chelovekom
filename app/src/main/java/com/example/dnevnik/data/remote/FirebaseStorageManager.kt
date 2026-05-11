package com.example.dnevnik.data.remote

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStorageManager @Inject constructor(
    private val storage: FirebaseStorage
) {
    private val imagesRef: StorageReference = storage.reference.child("images")
    private val videosRef: StorageReference = storage.reference.child("videos")
    private val audioRef: StorageReference = storage.reference.child("audio")

    suspend fun uploadImage(file: File, entryId: String): String {
        val fileRef = imagesRef.child("${entryId}/${file.name}")
        fileRef.putFile(android.net.Uri.fromFile(file)).await()
        return fileRef.downloadUrl.await().toString()
    }

    suspend fun uploadVideo(file: File, entryId: String): String {
        val fileRef = videosRef.child("${entryId}/${file.name}")
        fileRef.putFile(android.net.Uri.fromFile(file)).await()
        return fileRef.downloadUrl.await().toString()
    }

    suspend fun uploadAudio(file: File, entryId: String): String {
        val fileRef = audioRef.child("${entryId}/${file.name}")
        fileRef.putFile(android.net.Uri.fromFile(file)).await()
        return fileRef.downloadUrl.await().toString()
    }

    suspend fun deleteFile(url: String) {
        try {
            storage.getReferenceFromUrl(url).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteEntryFolder(entryId: String) {
        try {
            imagesRef.child(entryId).listAll().await().items.forEach { it.delete().await() }
            videosRef.child(entryId).listAll().await().items.forEach { it.delete().await() }
            audioRef.child(entryId).listAll().await().items.forEach { it.delete().await() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}