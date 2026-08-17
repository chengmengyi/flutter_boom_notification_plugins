package com.boom.notification.flutter_boom_notification_plugins

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class LocalFirebaseMessageService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        try {
            val context = applicationContext
            val data = remoteMessage.data
            val title =
                data["title"]
                    ?: remoteMessage.notification?.title
                    ?: ""
            val body =
                data["body"]
                    ?: remoteMessage.notification?.body
                    ?: ""
            val image =
                data["imageUrl"]
                    ?: data["image"]
                    ?: ""
            val messageId =
                data["id"]?.toIntOrNull()
                    ?: ((System.currentTimeMillis() % Int.MAX_VALUE).toInt())
            FlutterBoomNotificationPluginsPlugin.showFcmNotification(
                context = context,
                title = title,
                body = body,
                messageId = messageId,
                image = image,
            )
            Log.d(
                "LocalNotificationPlugin",
                "LocalFirebaseMessageService.onMessageReceived messageId=$messageId title=$title",
            )
        } catch (e: Exception) {
            Log.d(
                "LocalNotificationPlugin",
                "LocalFirebaseMessageService.onMessageReceived failed error=${e.message}",
            )
        }
    }
}
