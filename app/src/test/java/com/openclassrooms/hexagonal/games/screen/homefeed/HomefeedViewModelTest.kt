package com.openclassrooms.hexagonal.games.screen.homefeed

import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.data.service.PostFakeApi
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import com.openclassrooms.hexagonal.games.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomefeedViewModelTest {

  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  @Test
  fun init_exposesPostsFromRepository() = runTest {
    val repository = PostRepository(PostFakeApi())

    val viewModel = HomefeedViewModel(repository)
    advanceUntilIdle()

    assertTrue(viewModel.posts.value.isNotEmpty())
  }

  @Test
  fun whenRepositoryChanges_updatesExposedPosts() = runTest {
    val repository = PostRepository(PostFakeApi())
    val viewModel = HomefeedViewModel(repository)
    advanceUntilIdle()

    val post = Post(
      id = "new-post",
      title = "Brand new",
      description = "Desc",
      photoUrl = null,
      timestamp = 999L,
      author = User(id = "u-1", firstname = "John", lastname = "Doe")
    )

    repository.addPost(post)
    advanceUntilIdle()

    assertEquals("new-post", viewModel.posts.value.first().id)
  }
}
