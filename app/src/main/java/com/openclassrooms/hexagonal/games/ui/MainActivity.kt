package com.openclassrooms.hexagonal.games.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openclassrooms.hexagonal.games.screen.Screen
import com.openclassrooms.hexagonal.games.screen.ad.AddScreen
import com.openclassrooms.hexagonal.games.screen.auth.AuthViewModel
import com.openclassrooms.hexagonal.games.screen.auth.PasswordResetScreen
import com.openclassrooms.hexagonal.games.screen.auth.SignInScreen
import com.openclassrooms.hexagonal.games.screen.auth.SignUpScreen
import com.openclassrooms.hexagonal.games.screen.homefeed.HomefeedScreen
import com.openclassrooms.hexagonal.games.screen.settings.SettingsScreen
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the application. This activity serves as the entry point and container for the navigation
 * fragment. It handles setting up the toolbar, navigation controller, and action bar behavior.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    setContent {
      val authViewModel: AuthViewModel = hiltViewModel()
      val authUiState = authViewModel.uiState.collectAsStateWithLifecycle()
      val navController = rememberNavController()
      
      HexagonalGamesTheme {
        if (authUiState.value.isSignedIn) {
          HexagonalGamesNavHost(navHostController = navController, authViewModel = authViewModel)
        } else {
          AuthNavHost(
            authViewModel = authViewModel
          )
        }
      }
    }
  }
  
}

@Composable
fun HexagonalGamesNavHost(navHostController: NavHostController, authViewModel: AuthViewModel) {
  NavHost(
    navController = navHostController,
    startDestination = Screen.Homefeed.route
  ) {
    composable(route = Screen.Homefeed.route) {
      HomefeedScreen(
        onPostClick = {
          //TODO
        },
        onSettingsClick = {
          navHostController.navigate(Screen.Settings.route)
        },
        onLogoutClick = {
          authViewModel.signOut()
        },
        onFABClick = {
          navHostController.navigate(Screen.AddPost.route)
        }
      )
    }
    composable(route = Screen.AddPost.route) {
      AddScreen(
        onBackClick = { navHostController.navigateUp() },
        onSaveClick = { navHostController.navigateUp() }
      )
    }
    composable(route = Screen.Settings.route) {
      SettingsScreen(
        onBackClick = { navHostController.navigateUp() }
      )
    }
  }
}

private const val SIGN_IN_ROUTE = "auth/signin"
private const val SIGN_UP_ROUTE = "auth/signup"
private const val RESET_ROUTE = "auth/reset"

@Composable
private fun AuthNavHost(authViewModel: AuthViewModel) {
  val navController = rememberNavController()
  val uiState = authViewModel.uiState.collectAsStateWithLifecycle()
  
  NavHost(
    navController = navController,
    startDestination = SIGN_IN_ROUTE
  ) {
    composable(route = SIGN_IN_ROUTE) {
      SignInScreen(
        state = uiState.value,
        onEmailChanged = {
          authViewModel.clearMessages()
          authViewModel.onEmailChanged(it)
        },
        onPasswordChanged = {
          authViewModel.clearMessages()
          authViewModel.onPasswordChanged(it)
        },
        onSignInClick = authViewModel::signIn,
        onCreateAccountClick = {
          authViewModel.clearMessages()
          navController.navigate(SIGN_UP_ROUTE)
        },
        onForgotPasswordClick = {
          authViewModel.clearMessages()
          navController.navigate(RESET_ROUTE)
        }
      )
    }
    composable(route = SIGN_UP_ROUTE) {
      SignUpScreen(
        state = uiState.value,
        onEmailChanged = {
          authViewModel.clearMessages()
          authViewModel.onEmailChanged(it)
        },
        onPasswordChanged = {
          authViewModel.clearMessages()
          authViewModel.onPasswordChanged(it)
        },
        onSignUpClick = authViewModel::signUp,
        onBackToSignIn = {
          authViewModel.clearMessages()
          navController.popBackStack()
        }
      )
    }
    composable(route = RESET_ROUTE) {
      PasswordResetScreen(
        state = uiState.value,
        onEmailChanged = {
          authViewModel.clearMessages()
          authViewModel.onEmailChanged(it)
        },
        onSendReset = authViewModel::sendPasswordResetEmail,
        onBack = {
          authViewModel.clearMessages()
          navController.popBackStack()
        }
      )
    }
  }
}
