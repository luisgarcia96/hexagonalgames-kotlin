package com.openclassrooms.hexagonal.games.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.ui.MainActivity

object NotificationHelper {
  fun showCampaignNotification(context: Context, title: String, body: String) {
    val channelId = context.getString(R.string.notification_channel_campaigns_id)
    ensureChannel(context, channelId)

    val notification = NotificationCompat.Builder(context, channelId)
      .setSmallIcon(R.drawable.ic_notifications)
      .setContentTitle(title)
      .setContentText(body)
      .setStyle(NotificationCompat.BigTextStyle().bigText(body))
      .setContentIntent(buildContentIntent(context))
      .setAutoCancel(true)
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .build()

    NotificationManagerCompat.from(context)
      .notify(System.currentTimeMillis().toInt(), notification)
  }

  private fun ensureChannel(context: Context, channelId: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val name = context.getString(R.string.notification_channel_campaigns_name)
      val description = context.getString(R.string.notification_channel_campaigns_description)
      val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT).apply {
        this.description = description
      }
      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      manager.createNotificationChannel(channel)
    }
  }

  private fun buildContentIntent(context: Context): PendingIntent {
    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    return PendingIntent.getActivity(
      context,
      0,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
  }
}
