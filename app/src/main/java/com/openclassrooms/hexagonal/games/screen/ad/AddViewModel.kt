package com.openclassrooms.hexagonal.games.screen.ad

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.repository.AuthRepository
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.data.repository.StorageRepository
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * This ViewModel manages data and interactions related to adding new posts in the AddScreen.
 * It utilizes dependency injection to retrieve a PostRepository instance for interacting with post data.
 */
@HiltViewModel
class AddViewModel @Inject constructor(
  private val postRepository: PostRepository,
  private val authRepository: AuthRepository,
  private val storageRepository: StorageRepository
) : ViewModel() {
  
  /**
   * Internal mutable state flow representing the current post being edited.
   */
  private var _post = MutableStateFlow(
    Post(
      id = UUID.randomUUID().toString(),
      title = "",
      description = "",
      photoUrl = null,
      timestamp = System.currentTimeMillis(),
      author = null
    )
  )
  
  /**
   * Public state flow representing the current post being edited.
   * This is immutable for consumers.
   */
  val post: StateFlow<Post>
    get() = _post
  
  /**
   * StateFlow derived from the post that emits a FormError if the title is empty, null otherwise.
   */
  val error = post.map {
    verifyPost()
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = null,
  )

  private val _selectedMedia = MutableStateFlow<SelectedMedia?>(null)
  val selectedMedia: StateFlow<SelectedMedia?>
    get() = _selectedMedia

  private val _isSaving = MutableStateFlow(false)
  val isSaving: StateFlow<Boolean>
    get() = _isSaving

  private val _saveErrorMessage = MutableStateFlow<String?>(null)
  val saveErrorMessage: StateFlow<String?>
    get() = _saveErrorMessage

  private val _saveCompleted = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  val saveCompleted = _saveCompleted.asSharedFlow()
  
  /**
   * Handles form events like title and description changes.
   *
   * @param formEvent The form event to be processed.
   */
  fun onAction(formEvent: FormEvent) {
    when (formEvent) {
      is FormEvent.DescriptionChanged -> {
        _post.value = _post.value.copy(
          description = formEvent.description
        )
      }
      
      is FormEvent.TitleChanged -> {
        _post.value = _post.value.copy(
          title = formEvent.title
        )
      }
    }
  }
  
  fun onMediaSelected(uri: Uri?, contentType: String?) {
    _selectedMedia.value = uri?.let { SelectedMedia(it, contentType) }
  }

  /**
   * Attempts to add the current post to the repository after setting the author.
   *
   * TODO: Implement logic to retrieve the current user.
   */
  fun addPost() {
    if (_isSaving.value) return

    viewModelScope.launch {
      _isSaving.value = true
      _saveErrorMessage.value = null

      val currentUser = authRepository.currentUser
      if (currentUser == null) {
        _saveErrorMessage.value = "User not authenticated"
        _isSaving.value = false
        return@launch
      }

      val authorName = currentUser.displayName
        ?: currentUser.email?.substringBefore("@")
        ?: "User"

      val author = User(
        id = currentUser.uid,
        firstname = authorName,
        lastname = ""
      )

      runCatching {
        val selectedMedia = _selectedMedia.value
        val photoUrl = if (selectedMedia != null) {
          storageRepository.uploadPostImage(
            postId = _post.value.id,
            imageUri = selectedMedia.uri,
            contentType = selectedMedia.contentType
          )
        } else {
          null
        }

        postRepository.addPost(
          _post.value.copy(
            author = author,
            photoUrl = photoUrl,
            timestamp = System.currentTimeMillis()
          )
        )
      }.onFailure { throwable ->
        _saveErrorMessage.value = throwable.message ?: "Unable to save post"
        _isSaving.value = false
      }.onSuccess {
        _isSaving.value = false
        _saveCompleted.tryEmit(Unit)
      }
    }
  }
  
  /**
   * Verifies mandatory fields of the post
   * and returns a corresponding FormError if so.
   *
   * @return A FormError.TitleError if title is empty, null otherwise.
   */
  private fun verifyPost(): FormError? {
    return if (_post.value.title.isEmpty()) {
      FormError.TitleError
    } else {
      null
    }
  }
  
}

data class SelectedMedia(
  val uri: Uri,
  val contentType: String?
)
