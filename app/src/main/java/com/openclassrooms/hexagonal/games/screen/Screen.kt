package com.openclassrooms.hexagonal.games.screen

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.openclassrooms.hexagonal.games.screen.postdetail.POST_ID_ARG

sealed class Screen(
  val route: String,
  val navArguments: List<NamedNavArgument> = emptyList()
) {
  data object Homefeed : Screen("homefeed")
  
  data object AddPost : Screen("addPost")
  
  data object Settings : Screen("settings")

  data object AddComment : Screen(
    route = "addComment/{$POST_ID_ARG}",
    navArguments = listOf(
      navArgument(POST_ID_ARG) { type = NavType.StringType }
    )
  ) {
    fun createRoute(postId: String) = "addComment/$postId"
  }

  data object PostDetail : Screen(
    route = "postDetail/{$POST_ID_ARG}",
    navArguments = listOf(
      navArgument(POST_ID_ARG) { type = NavType.StringType }
    )
  ) {
    fun createRoute(postId: String) = "postDetail/$postId"
  }
}
