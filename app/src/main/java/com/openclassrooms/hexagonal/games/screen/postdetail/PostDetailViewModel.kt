package com.openclassrooms.hexagonal.games.screen.postdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.screen.postdetail.POST_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
  postRepository: PostRepository,
  savedStateHandle: SavedStateHandle
) : ViewModel() {

  val postId: String = checkNotNull(savedStateHandle.get<String>(POST_ID_ARG))

  val post: StateFlow<Post?> = postRepository.getPost(postId)
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = null
    )
}
