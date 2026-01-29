package com.openclassrooms.hexagonal.games.data.service

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.openclassrooms.hexagonal.games.data.model.PostEntity
import com.openclassrooms.hexagonal.games.data.model.toDomain
import com.openclassrooms.hexagonal.games.data.model.toEntity
import com.openclassrooms.hexagonal.games.domain.model.Post
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PostFirestoreApi @Inject constructor(
  private val firestore: FirebaseFirestore
) : PostApi {
  private val postsCollection = firestore.collection("posts")

  override fun getPostsOrderByCreationDateDesc(): Flow<List<Post>> = callbackFlow {
    val registration = postsCollection
      .orderBy("timestamp", Query.Direction.DESCENDING)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          close(error)
          return@addSnapshotListener
        }

        val posts = snapshot?.documents?.mapNotNull { document ->
          val entity = document.toObject(PostEntity::class.java)
          entity?.copy(id = document.id)?.toDomain()
        } ?: emptyList()

        trySend(posts).isSuccess
      }

    awaitClose { registration.remove() }
  }

  override suspend fun addPost(post: Post) {
    postsCollection.document(post.id).set(post.toEntity()).await()
  }
}
