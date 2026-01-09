package com.openclassrooms.hexagonal.games.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
  val email: String = "",
  val password: String = "",
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val isSignedIn: Boolean = false,
  val infoMessage: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
  private val authRepository: AuthRepository,
) : ViewModel() {
  
  private val formState = MutableStateFlow(AuthUiState())
  
  val uiState: StateFlow<AuthUiState> = combine(
    formState,
    authRepository.authState
  ) { form, user ->
    val signedIn = user != null
    form.copy(
      isSignedIn = signedIn,
      isLoading = if (signedIn) false else form.isLoading,
      errorMessage = if (signedIn) null else form.errorMessage,
      infoMessage = if (signedIn) null else form.infoMessage,
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5_000),
    AuthUiState(isSignedIn = authRepository.currentUser != null)
  )
  
  fun onEmailChanged(email: String) {
    formState.update { it.copy(email = email, errorMessage = null) }
  }
  
  fun onPasswordChanged(password: String) {
    formState.update { it.copy(password = password, errorMessage = null) }
  }
  
  fun signIn() {
    val email = formState.value.email
    val password = formState.value.password
    submitAuth(
      block = { authRepository.signIn(email.trim(), password) },
      defaultError = "Unable to sign in"
    )
  }
  
  fun signOut() {
    authRepository.signOut()
  }
  
  fun signUp() {
    submitAuth(
      block = { authRepository.signUp(formState.value.email.trim(), formState.value.password) },
      defaultError = "Unable to create account"
    )
  }
  
  fun sendPasswordResetEmail() {
    val email = formState.value.email
    if (email.isBlank()) {
      formState.update { it.copy(errorMessage = "Email is required") }
      return
    }
    viewModelScope.launch {
      formState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
      runCatching { authRepository.sendPasswordReset(email.trim()) }
        .onFailure { throwable ->
          formState.update {
            it.copy(
              isLoading = false,
              errorMessage = throwable.message ?: "Unable to send reset email"
            )
          }
        }
        .onSuccess {
          formState.update {
            it.copy(
              isLoading = false,
              infoMessage = "Password reset email sent"
            )
          }
        }
    }
  }
  
  fun clearMessages() {
    formState.update { it.copy(errorMessage = null, infoMessage = null) }
  }
  
  private fun submitAuth(block: suspend () -> Unit, defaultError: String) {
    val email = formState.value.email
    val password = formState.value.password
    if (email.isBlank() || password.isBlank()) {
      formState.update { it.copy(errorMessage = "Email and password are required") }
      return
    }
    
    viewModelScope.launch {
      formState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
      runCatching { block() }
        .onFailure { throwable ->
          formState.update {
            it.copy(
              isLoading = false,
              errorMessage = throwable.message ?: defaultError
            )
          }
        }
        .onSuccess {
          formState.update { it.copy(isLoading = false) }
        }
    }
  }
}
