package com.openclassrooms.hexagonal.games.data.repository

import com.openclassrooms.hexagonal.games.data.model.CommentEntity
import com.openclassrooms.hexagonal.games.data.service.PostApi
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class PostRepositoryTest {

  @Test
  fun posts_exposesFlowFromApi() = runBlocking {
    val expected = listOf(samplePost("p1"))
    val postApi = mockk<PostApi>()
    every { postApi.getPostsOrderByCreationDateDesc() } returns flowOf(expected)

    val repository = PostRepository(postApi)

    assertEquals(expected, repository.posts.first())
  }

  @Test
  fun addPost_delegatesToApi() = runBlocking {
    val postApi = mockk<PostApi>(relaxed = true)
    coEvery { postApi.addPost(any()) } returns Unit
    val repository = PostRepository(postApi)

    repository.addPost(samplePost("p1"))

    coVerify(exactly = 1) { postApi.addPost(any()) }
  }

  @Test
  fun addComment_delegatesToApi() = runBlocking {
    val postApi = mockk<PostApi>(relaxed = true)
    coEvery { postApi.addComment(any(), any()) } returns Unit
    val repository = PostRepository(postApi)

    repository.addComment("post-1", CommentEntity(id = "c1", text = "Hello"))

    coVerify(exactly = 1) { postApi.addComment("post-1", any()) }
  }

  @Test
  fun deleteOperations_delegateToApi() = runBlocking {
    val postApi = mockk<PostApi>(relaxed = true)
    coEvery { postApi.deletePost(any()) } returns Unit
    coEvery { postApi.deleteComment(any(), any()) } returns Unit
    val repository = PostRepository(postApi)

    repository.deletePost("post-1")
    repository.deleteComment("post-1", "comment-1")

    coVerify(exactly = 1) { postApi.deletePost("post-1") }
    coVerify(exactly = 1) { postApi.deleteComment("post-1", "comment-1") }
  }

  private fun samplePost(id: String): Post {
    return Post(
      id = id,
      title = "Title",
      description = "Desc",
      photoUrl = null,
      timestamp = 1L,
      author = User(id = "u1", firstname = "Jane", lastname = "Doe")
    )
  }
}
