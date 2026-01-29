package com.openclassrooms.hexagonal.games.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.openclassrooms.hexagonal.games.R

class HexagonalMessagingService : FirebaseMessagingService() {
  override fun onNewToken(token: String) {}

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    val title = remoteMessage.notification?.title
      ?: remoteMessage.data["title"]
      ?: getString(R.string.app_name)
    val body = remoteMessage.notification?.body
      ?: remoteMessage.data["body"]
      ?: remoteMessage.data["message"]

    if (body.isNullOrBlank()) return

    NotificationHelper.showCampaignNotification(this, title, body)
  }
}
