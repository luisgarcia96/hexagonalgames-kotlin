package com.openclassrooms.hexagonal.games.screen.ad

import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.hexagonal.games.data.repository.AuthRepository
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.data.repository.StorageRepository
import com.openclassrooms.hexagonal.games.domain.model.Post
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
class AddViewModelTest {

  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  @Test
  fun addPost_whenUserIsNotAuthenticated_setsErrorAndStopsSaving() = runTest {
    val postRepository = mockk<PostRepository>(relaxed = true)
    val authRepository = mockk<AuthRepository>(relaxed = true)
    val storageRepository = mockk<StorageRepository>(relaxed = true)
    every { authRepository.currentUser } returns null

    val viewModel = AddViewModel(postRepository, authRepository, storageRepository)
    viewModel.onAction(FormEvent.TitleChanged("My title"))

    viewModel.addPost()
    advanceUntilIdle()

    assertEquals("User not authenticated", viewModel.saveErrorMessage.value)
    assertFalse(viewModel.isSaving.value)
    coVerify(exactly = 0) { postRepository.addPost(any()) }
  }

  @Test
  fun addPost_whenSuccess_savesPostWithAuthorAndEmitsCompletion() = runTest {
    val postRepository = mockk<PostRepository>(relaxed = true)
    val authRepository = mockk<AuthRepository>(relaxed = true)
    val storageRepository = mockk<StorageRepository>(relaxed = true)

    val firebaseUser = mockk<FirebaseUser>()
    every { firebaseUser.uid } returns "uid-42"
    every { firebaseUser.displayName } returns "John"
    every { firebaseUser.email } returns "john@doe.com"
    every { authRepository.currentUser } returns firebaseUser

    val postSlot = slot<Post>()
    coEvery { postRepository.addPost(capture(postSlot)) } returns Unit

    val viewModel = AddViewModel(postRepository, authRepository, storageRepository)
    viewModel.onAction(FormEvent.TitleChanged("Post title"))
    viewModel.onAction(FormEvent.DescriptionChanged("Description"))

    val completion = async { viewModel.saveCompleted.first() }
    viewModel.addPost()
    advanceUntilIdle()
    completion.await()

    coVerify(exactly = 1) { postRepository.addPost(any()) }
    coVerify(exactly = 0) { storageRepository.uploadPostImage(any(), any(), any()) }
    assertEquals("uid-42", postSlot.captured.author?.id)
    assertEquals("John", postSlot.captured.author?.firstname)
    assertNull(postSlot.captured.photoUrl)
    assertFalse(viewModel.isSaving.value)
    assertNull(viewModel.saveErrorMessage.value)
  }

  @Test
  fun addPost_whenRepositoryFails_setsErrorMessage() = runTest {
    val postRepository = mockk<PostRepository>(relaxed = true)
    val authRepository = mockk<AuthRepository>(relaxed = true)
    val storageRepository = mockk<StorageRepository>(relaxed = true)

    val firebaseUser = mockk<FirebaseUser>()
    every { firebaseUser.uid } returns "uid-42"
    every { firebaseUser.displayName } returns "John"
    every { firebaseUser.email } returns "john@doe.com"
    every { authRepository.currentUser } returns firebaseUser

    coEvery { postRepository.addPost(any()) } throws IllegalStateException("Save failed")

    val viewModel = AddViewModel(postRepository, authRepository, storageRepository)
    viewModel.onAction(FormEvent.TitleChanged("Post title"))

    viewModel.addPost()
    advanceUntilIdle()

    assertEquals("Save failed", viewModel.saveErrorMessage.value)
    assertFalse(viewModel.isSaving.value)
  }
}
