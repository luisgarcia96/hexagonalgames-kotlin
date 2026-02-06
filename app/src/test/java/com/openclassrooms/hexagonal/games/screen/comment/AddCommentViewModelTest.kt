package com.openclassrooms.hexagonal.games.screen.comment

import androidx.lifecycle.SavedStateHandle
import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.hexagonal.games.data.model.CommentEntity
import com.openclassrooms.hexagonal.games.data.repository.AuthRepository
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.screen.postdetail.POST_ID_ARG
import com.openclassrooms.hexagonal.games.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddCommentViewModelTest {

  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  @Test
  fun addComment_whenCommentIsBlank_setsValidationError() = runTest {
    val postRepository = mockk<PostRepository>(relaxed = true)
    val authRepository = mockk<AuthRepository>(relaxed = true)
    every { authRepository.currentUser } returns null

    val viewModel = AddCommentViewModel(
      postRepository = postRepository,
      authRepository = authRepository,
      savedStateHandle = SavedStateHandle(mapOf(POST_ID_ARG to "post-1"))
    )

    viewModel.onCommentChanged("   ")
    viewModel.addComment()
    advanceUntilIdle()

    assertEquals("Comment cannot be empty", viewModel.errorMessage.value)
    coVerify(exactly = 0) { postRepository.addComment(any(), any()) }
  }

  @Test
  fun addComment_whenUserNotAuthenticated_setsError() = runTest {
    val postRepository = mockk<PostRepository>(relaxed = true)
    val authRepository = mockk<AuthRepository>(relaxed = true)
    every { authRepository.currentUser } returns null

    val viewModel = AddCommentViewModel(
      postRepository = postRepository,
      authRepository = authRepository,
      savedStateHandle = SavedStateHandle(mapOf(POST_ID_ARG to "post-1"))
    )

    viewModel.onCommentChanged("Hello")
    viewModel.addComment()
    advanceUntilIdle()

    assertEquals("User not authenticated", viewModel.errorMessage.value)
    assertFalse(viewModel.isSaving.value)
    coVerify(exactly = 0) { postRepository.addComment(any(), any()) }
  }

  @Test
  fun addComment_whenSuccess_persistsCommentAndEmitsCompletion() = runTest {
    val postRepository = mockk<PostRepository>(relaxed = true)
    val authRepository = mockk<AuthRepository>(relaxed = true)

    val firebaseUser = mockk<FirebaseUser>()
    every { firebaseUser.uid } returns "uid-1"
    every { firebaseUser.displayName } returns "Jane"
    every { firebaseUser.email } returns "jane@doe.com"
    every { authRepository.currentUser } returns firebaseUser

    val commentSlot = slot<CommentEntity>()
    coEvery { postRepository.addComment("post-1", capture(commentSlot)) } returns Unit

    val viewModel = AddCommentViewModel(
      postRepository = postRepository,
      authRepository = authRepository,
      savedStateHandle = SavedStateHandle(mapOf(POST_ID_ARG to "post-1"))
    )

    viewModel.onCommentChanged("  Nice post  ")
    val completion = async { viewModel.saveCompleted.first() }

    viewModel.addComment()
    advanceUntilIdle()
    completion.await()

    coVerify(exactly = 1) { postRepository.addComment("post-1", any()) }
    assertEquals("Nice post", commentSlot.captured.text)
    assertEquals("uid-1", commentSlot.captured.author?.id)
    assertEquals("Jane", commentSlot.captured.author?.firstname)
    assertFalse(viewModel.isSaving.value)
    assertNull(viewModel.errorMessage.value)
  }

  @Test
  fun addComment_whenRepositoryFails_setsErrorMessage() = runTest {
    val postRepository = mockk<PostRepository>(relaxed = true)
    val authRepository = mockk<AuthRepository>(relaxed = true)

    val firebaseUser = mockk<FirebaseUser>()
    every { firebaseUser.uid } returns "uid-1"
    every { firebaseUser.displayName } returns "Jane"
    every { authRepository.currentUser } returns firebaseUser

    coEvery { postRepository.addComment(any(), any()) } throws IllegalStateException("Cannot save")

    val viewModel = AddCommentViewModel(
      postRepository = postRepository,
      authRepository = authRepository,
      savedStateHandle = SavedStateHandle(mapOf(POST_ID_ARG to "post-1"))
    )

    viewModel.onCommentChanged("Hello")
    viewModel.addComment()
    advanceUntilIdle()

    assertEquals("Cannot save", viewModel.errorMessage.value)
    assertFalse(viewModel.isSaving.value)
  }
}
