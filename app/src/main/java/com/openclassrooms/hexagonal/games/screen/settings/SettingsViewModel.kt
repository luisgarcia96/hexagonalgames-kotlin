package com.openclassrooms.hexagonal.games.screen.settings

import androidx.lifecycle.ViewModel
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel responsible for managing user settings, specifically notification preferences.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
  /**
   * Enables notifications for the application.
   */
  fun enableNotifications() {
    FirebaseMessaging.getInstance()
      .subscribeToTopic(CAMPAIGNS_TOPIC)
      .addOnCompleteListener { }
  }
  
  /**
   * Disables notifications for the application.
   */
  fun disableNotifications() {
    FirebaseMessaging.getInstance()
      .unsubscribeFromTopic(CAMPAIGNS_TOPIC)
      .addOnCompleteListener { }
  }

  companion object {
    private const val CAMPAIGNS_TOPIC = "campaigns"
  }
}
