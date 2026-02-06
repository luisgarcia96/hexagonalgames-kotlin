package com.openclassrooms.hexagonal.games.screen.postdetail

import androidx.lifecycle.SavedStateHandle
import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.hexagonal.games.data.repository.AuthRepository
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import com.openclassrooms.hexagonal.games.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PostDetailViewModelTest {

  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  @Test
  fun canDeletePost_returnsTrueWhenCurrentUserIsAuthor() {
    val postRepository = mockk<PostRepository>(relaxed = true)
    every { postRepository.getPost("post-1") } returns flowOf(samplePost(authorId = "author-1"))

    val authRepository = mockk<AuthRepository>(relaxed = true)
    val currentUser = mockk<FirebaseUser>()
    every { currentUser.uid } returns "author-1"
    every { authRepository.currentUser } returns currentUser

    val viewModel = PostDetailViewModel(
      postRepository = postRepository,
      authRepository = authRepository,
      savedStateHandle = SavedStateHandle(mapOf(POST_ID_ARG to "post-1"))
    )

    assertTrue(viewModel.canDeletePost(samplePost(authorId = "author-1")))
    assertFalse(viewModel.canDeletePost(samplePost(authorId = "someone-else")))
  }

  @Test
  fun deletePost_whenUserIsNotAuthor_emitsErrorAndDoesNotCallRepository() = runTest {
    val postRepository = mockk<PostRepository>(relaxed = true)
    every { postRepository.getPost("post-1") } returns flowOf(samplePost(authorId = "author-1"))

    val authRepository = mockk<AuthRepository>(relaxed = true)
    val currentUser = mockk<FirebaseUser>()
    every { currentUser.uid } returns "other-user"
    every { authRepository.currentUser } returns currentUser

    val viewModel = PostDetailViewModel(
      postRepository = postRepository,
      authRepository = authRepository,
      savedStateHandle = SavedStateHandle(mapOf(POST_ID_ARG to "post-1"))
    )

    val events = mutableListOf<PostDetailEvent>()
    val eventCollectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
      viewModel.events.collect { events += it }
    }

    viewModel.deletePost()
    advanceUntilIdle()

    val event = events.first()
    assertTrue(event is PostDetailEvent.Error)
    assertEquals("Only the author can delete this post", (event as PostDetailEvent.Error).message)
    coVerify(exactly = 0) { postRepository.deletePost(any()) }
    eventCollectJob.cancel()
  }

  @Test
  fun deletePost_whenRepositorySucceeds_emitsPostDeletedEvent() = runTest {
    val postRepository = mockk<PostRepository>(relaxed = true)
    every { postRepository.getPost("post-1") } returns flowOf(samplePost(authorId = "author-1"))
    coEvery { postRepository.deletePost("post-1") } returns Unit

    val authRepository = mockk<AuthRepository>(relaxed = true)
    val currentUser = mockk<FirebaseUser>()
    every { currentUser.uid } returns "author-1"
    every { authRepository.currentUser } returns currentUser

    val viewModel = PostDetailViewModel(
      postRepository = postRepository,
      authRepository = authRepository,
      savedStateHandle = SavedStateHandle(mapOf(POST_ID_ARG to "post-1"))
    )

    val postCollectJob = launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.post.collect {} }
    val events = mutableListOf<PostDetailEvent>()
    val eventCollectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
      viewModel.events.collect { events += it }
    }
    advanceUntilIdle()

    viewModel.deletePost()
    advanceUntilIdle()

    assertEquals(PostDetailEvent.PostDeleted, events.first())
    coVerify(exactly = 1) { postRepository.deletePost("post-1") }
    postCollectJob.cancel()
    eventCollectJob.cancel()
  }

  @Test
  fun deleteComment_whenUserIsNotAuthor_emitsError() = runTest {
    val postRepository = mockk<PostRepository>(relaxed = true)
    every { postRepository.getPost("post-1") } returns flowOf(samplePost(authorId = "author-1"))

    val authRepository = mockk<AuthRepository>(relaxed = true)
    val currentUser = mockk<FirebaseUser>()
    every { currentUser.uid } returns "other-user"
    every { authRepository.currentUser } returns currentUser

    val viewModel = PostDetailViewModel(
      postRepository = postRepository,
      authRepository = authRepository,
      savedStateHandle = SavedStateHandle(mapOf(POST_ID_ARG to "post-1"))
    )

    val events = mutableListOf<PostDetailEvent>()
    val eventCollectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
      viewModel.events.collect { events += it }
    }

    viewModel.deleteComment(commentId = "comment-1", commentAuthorId = "author-1")
    advanceUntilIdle()

    val event = events.first()
    assertTrue(event is PostDetailEvent.Error)
    assertEquals("Only the author can delete this comment", (event as PostDetailEvent.Error).message)
    coVerify(exactly = 0) { postRepository.deleteComment(any(), any()) }
    eventCollectJob.cancel()
  }

  @Test
  fun deleteComment_whenAuthorized_callsRepository() = runTest {
    val postRepository = mockk<PostRepository>(relaxed = true)
    every { postRepository.getPost("post-1") } returns flowOf(samplePost(authorId = "author-1"))
    coEvery { postRepository.deleteComment("post-1", "comment-1") } returns Unit

    val authRepository = mockk<AuthRepository>(relaxed = true)
    val currentUser = mockk<FirebaseUser>()
    every { currentUser.uid } returns "author-1"
    every { authRepository.currentUser } returns currentUser

    val viewModel = PostDetailViewModel(
      postRepository = postRepository,
      authRepository = authRepository,
      savedStateHandle = SavedStateHandle(mapOf(POST_ID_ARG to "post-1"))
    )

    viewModel.deleteComment(commentId = "comment-1", commentAuthorId = "author-1")
    advanceUntilIdle()

    coVerify(exactly = 1) { postRepository.deleteComment("post-1", "comment-1") }
  }

  private fun samplePost(authorId: String): Post {
    return Post(
      id = "post-1",
      title = "Title",
      description = "Description",
      photoUrl = null,
      timestamp = 1L,
      author = User(
        id = authorId,
        firstname = "Jane",
        lastname = "Doe"
      )
    )
  }
}
