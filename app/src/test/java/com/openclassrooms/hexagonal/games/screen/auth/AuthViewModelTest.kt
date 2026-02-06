package com.openclassrooms.hexagonal.games.screen.auth

import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.hexagonal.games.data.repository.AuthRepository
import com.openclassrooms.hexagonal.games.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  @Test
  fun signIn_whenCredentialsMissing_setsValidationError() = runTest {
    val authRepository = mockk<AuthRepository>(relaxed = true)
    every { authRepository.authState } returns MutableStateFlow<FirebaseUser?>(null)
    every { authRepository.currentUser } returns null

    val viewModel = AuthViewModel(authRepository)
    val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

    viewModel.signIn()
    advanceUntilIdle()

    assertEquals("Email and password are required", viewModel.uiState.value.errorMessage)
    collectJob.cancel()
  }

  @Test
  fun signIn_whenCredentialsValid_callsRepositoryWithTrimmedEmail() = runTest {
    val authRepository = mockk<AuthRepository>(relaxed = true)
    every { authRepository.authState } returns MutableStateFlow<FirebaseUser?>(null)
    every { authRepository.currentUser } returns null
    coEvery { authRepository.signIn("john@doe.com", "secret") } returns Unit

    val viewModel = AuthViewModel(authRepository)
    val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

    viewModel.onEmailChanged(" john@doe.com ")
    viewModel.onPasswordChanged("secret")
    viewModel.signIn()
    advanceUntilIdle()

    coVerify(exactly = 1) { authRepository.signIn("john@doe.com", "secret") }
    assertNull(viewModel.uiState.value.errorMessage)
    assertFalse(viewModel.uiState.value.isLoading)
    collectJob.cancel()
  }

  @Test
  fun sendPasswordResetEmail_whenEmailMissing_setsValidationError() = runTest {
    val authRepository = mockk<AuthRepository>(relaxed = true)
    every { authRepository.authState } returns MutableStateFlow<FirebaseUser?>(null)
    every { authRepository.currentUser } returns null

    val viewModel = AuthViewModel(authRepository)
    val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

    viewModel.sendPasswordResetEmail()
    advanceUntilIdle()

    assertEquals("Email is required", viewModel.uiState.value.errorMessage)
    collectJob.cancel()
  }

  @Test
  fun deleteAccount_whenRepositoryFails_exposesErrorMessage() = runTest {
    val authRepository = mockk<AuthRepository>(relaxed = true)
    every { authRepository.authState } returns MutableStateFlow<FirebaseUser?>(null)
    every { authRepository.currentUser } returns null
    coEvery { authRepository.deleteAccount() } throws IllegalStateException("Delete failed")

    val viewModel = AuthViewModel(authRepository)
    val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

    viewModel.deleteAccount()
    advanceUntilIdle()

    assertEquals("Delete failed", viewModel.uiState.value.errorMessage)
    assertFalse(viewModel.uiState.value.isLoading)
    collectJob.cancel()
  }

  @Test
  fun signOut_callsRepositoryAndClearsMessages() = runTest {
    val authRepository = mockk<AuthRepository>(relaxed = true)
    every { authRepository.authState } returns MutableStateFlow<FirebaseUser?>(null)
    every { authRepository.currentUser } returns null
    every { authRepository.signOut() } just runs

    val viewModel = AuthViewModel(authRepository)
    val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

    viewModel.signIn()
    advanceUntilIdle()
    viewModel.signOut()
    advanceUntilIdle()

    verify(exactly = 1) { authRepository.signOut() }
    assertNull(viewModel.uiState.value.errorMessage)
    assertNull(viewModel.uiState.value.infoMessage)
    collectJob.cancel()
  }
}
