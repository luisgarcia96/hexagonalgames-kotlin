package com.openclassrooms.hexagonal.games.screen.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel responsible for managing user settings, specifically notification preferences.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
  /**
   * Enables notifications for the application.
   * TODO: Implement the logic to enable notifications, likely involving interactions with a notification manager.
   */
  fun enableNotifications() {
    //TODO
  }
  
  /**
   * Disables notifications for the application.
   * TODO: Implement the logic to disable notifications, likely involving interactions with a notification manager.
   */
  fun disableNotifications() {
    //TODO
  }
  
}
