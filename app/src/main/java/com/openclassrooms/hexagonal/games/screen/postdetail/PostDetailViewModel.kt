package com.openclassrooms.hexagonal.games.screen.postdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.repository.AuthRepository
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.screen.postdetail.POST_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
  private val postRepository: PostRepository,
  authRepository: AuthRepository,
  savedStateHandle: SavedStateHandle
) : ViewModel() {

  val postId: String = checkNotNull(savedStateHandle.get<String>(POST_ID_ARG))
  val currentUserId: String? = authRepository.currentUser?.uid

  val post: StateFlow<Post?> = postRepository.getPost(postId)
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = null
    )

  private val _events = MutableSharedFlow<PostDetailEvent>(extraBufferCapacity = 1)
  val events = _events.asSharedFlow()

  fun canDeletePost(post: Post?): Boolean {
    val userId = currentUserId
    return userId != null && post?.author?.id == userId
  }

  fun deletePost() {
    if (!canDeletePost(post.value)) {
      _events.tryEmit(PostDetailEvent.Error("Only the author can delete this post"))
      return
    }

    viewModelScope.launch {
      runCatching { postRepository.deletePost(postId) }
        .onFailure { throwable ->
          _events.tryEmit(
            PostDetailEvent.Error(throwable.message ?: "Unable to delete post")
          )
        }
        .onSuccess {
          _events.tryEmit(PostDetailEvent.PostDeleted)
        }
    }
  }

  fun deleteComment(commentId: String, commentAuthorId: String?) {
    val userId = currentUserId
    if (userId == null || commentAuthorId != userId) {
      _events.tryEmit(PostDetailEvent.Error("Only the author can delete this comment"))
      return
    }

    viewModelScope.launch {
      runCatching { postRepository.deleteComment(postId, commentId) }
        .onFailure { throwable ->
          _events.tryEmit(
            PostDetailEvent.Error(throwable.message ?: "Unable to delete comment")
          )
        }
    }
  }
}

sealed interface PostDetailEvent {
  data object PostDeleted : PostDetailEvent
  data class Error(val message: String) : PostDetailEvent
}
