package com.openclassrooms.hexagonal.games.screen.comment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.model.CommentEntity
import com.openclassrooms.hexagonal.games.data.model.UserEntity
import com.openclassrooms.hexagonal.games.data.repository.AuthRepository
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.screen.postdetail.POST_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddCommentViewModel @Inject constructor(
  private val postRepository: PostRepository,
  private val authRepository: AuthRepository,
  savedStateHandle: SavedStateHandle
) : ViewModel() {

  private val postId: String = checkNotNull(savedStateHandle.get<String>(POST_ID_ARG))

  private val _comment = MutableStateFlow("")
  val comment = _comment.asStateFlow()

  private val _isSaving = MutableStateFlow(false)
  val isSaving = _isSaving.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage = _errorMessage.asStateFlow()

  private val _saveCompleted = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  val saveCompleted = _saveCompleted.asSharedFlow()

  fun onCommentChanged(value: String) {
    _comment.value = value
    _errorMessage.value = null
  }

  fun addComment() {
    if (_isSaving.value) return

    val message = _comment.value.trim()
    if (message.isEmpty()) {
      _errorMessage.value = "Comment cannot be empty"
      return
    }

    val currentUser = authRepository.currentUser
    if (currentUser == null) {
      _errorMessage.value = "User not authenticated"
      return
    }

    val authorName = currentUser.displayName
      ?: currentUser.email?.substringBefore("@")
      ?: "User"

    viewModelScope.launch {
      _isSaving.value = true
      _errorMessage.value = null

      val comment = CommentEntity(
        id = UUID.randomUUID().toString(),
        text = message,
        timestamp = System.currentTimeMillis(),
        author = UserEntity(
          id = currentUser.uid,
          firstname = authorName,
          lastname = ""
        )
      )

      try {
        postRepository.addComment(postId = postId, comment = comment)
        _isSaving.value = false
        _saveCompleted.tryEmit(Unit)
      } catch (throwable: Throwable) {
        _isSaving.value = false
        _errorMessage.value = throwable.message ?: "Unable to save comment"
      }
    }
  }
}
