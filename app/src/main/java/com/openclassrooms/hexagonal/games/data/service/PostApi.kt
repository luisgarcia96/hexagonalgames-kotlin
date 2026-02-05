package com.openclassrooms.hexagonal.games.data.service

import com.openclassrooms.hexagonal.games.data.model.CommentEntity
import com.openclassrooms.hexagonal.games.domain.model.Post
import kotlinx.coroutines.flow.Flow

/**
 * This interface defines the contract for interacting with Post data from a data source.
 * It outlines the methods for retrieving and adding Posts, abstracting the underlying
 * implementation details of fetching and persisting data.
 */
interface PostApi {
  /**
   * Retrieves a list of Posts ordered by their creation date in descending order.
   *
   * @return A list of Posts sorted by creation date (newest first).
   */
  fun getPostsOrderByCreationDateDesc(): Flow<List<Post>>

  /**
   * Retrieves a single Post by its ID.
   *
   * @param postId The identifier of the post.
   */
  fun getPost(postId: String): Flow<Post?>
  
  /**
   * Adds a new Post to the data source.
   *
   * @param post The Post object to be added.
   */
  suspend fun addPost(post: Post)

  /**
   * Adds a new comment to a post.
   *
   * @param postId The identifier of the parent post.
   * @param comment The comment to add.
   */
  suspend fun addComment(postId: String, comment: CommentEntity)

  /**
   * Deletes a Post and its related data from the data source.
   *
   * @param postId The identifier of the post.
   */
  suspend fun deletePost(postId: String)

  /**
   * Deletes a comment from a given post.
   *
   * @param postId The identifier of the parent post.
   * @param commentId The identifier of the comment.
   */
  suspend fun deleteComment(postId: String, commentId: String)
}
