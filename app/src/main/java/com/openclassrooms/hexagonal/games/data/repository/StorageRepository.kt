package com.openclassrooms.hexagonal.games.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepository @Inject constructor(
  private val firebaseStorage: FirebaseStorage
) {
  suspend fun uploadPostImage(
    postId: String,
    imageUri: Uri,
    contentType: String?
  ): String {
    val fileName = UUID.randomUUID().toString()
    val reference = firebaseStorage.reference.child("posts/$postId/$fileName")
    val metadata = storageMetadata {
      this.contentType = contentType ?: "image/jpeg"
    }

    reference.putFile(imageUri, metadata).await()
    return reference.downloadUrl.await().toString()
  }
}
