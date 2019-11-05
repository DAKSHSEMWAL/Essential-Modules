package com.mediclinic.onetoonechat.Utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build

import androidx.core.app.NotificationCompat

import com.google.firebase.messaging.RemoteMessage
import com.mediclinic.onetoonechat.R

class FirebaseMessagingService : com.google.firebase.messaging.FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        super.onMessageReceived(remoteMessage)


        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val NOTIFICATION_CHANNEL_ID = "uMe_Channel_1"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "uMe Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )

            // Configure the notification channel.
            notificationChannel.description = "Channel description"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // retrieve title and notification body from index.js function through remote
        val from_sender_id = remoteMessage.data["from_sender_id"]
        val click_action = remoteMessage.data["click_action"]
        val notificationTitle = remoteMessage.data["title"]
        val notificationBody = remoteMessage.data["body"]


        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)

        notificationBuilder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.logo)
            .setTicker("infbox.com.bd")
            .setPriority(Notification.PRIORITY_MAX)
            .setContentTitle(notificationTitle)
            .setContentText(notificationBody)


        // clicking notification goes to sender profile
        val intent = Intent(click_action)
        intent.putExtra("visitUserId", from_sender_id)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        notificationBuilder.setContentIntent(pendingIntent) // end


        val mNotificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(mNotificationId, notificationBuilder.build())


    }
}
