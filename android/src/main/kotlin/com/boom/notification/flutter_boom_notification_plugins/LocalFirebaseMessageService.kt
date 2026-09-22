package com.boom.notification.flutter_boom_notification_plugins

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class LocalFirebaseMessageService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val context = applicationContext
        val deliveredPriority = remoteMessage.priority
        val originalPriority = remoteMessage.originalPriority
        if (deliveredPriority == RemoteMessage.PRIORITY_HIGH) {
            Log.w(
                KeepAliveNotificationHelper.FCM_TEST_LOG_TAG,
                "FCM_HIGH_PRIORITY_RECEIVED originalPriority=$originalPriority deliveredPriority=$deliveredPriority",
            )
            val startRequested = try {
                KeepAliveNotificationHelper.startOrUpdateForegroundService(
                    context = context,
                    reason = KeepAliveNotificationHelper.FCM_HIGH_PRIORITY_REASON,
                    allowStartFailureRecovery = false,
                )
            } catch (e: Exception) {
                Log.d(
                    "LocalNotificationPlugin",
                    "FCM foreground service failed error=${e.javaClass.simpleName}:${e.message}",
                )
                false
            }
            Log.d(
                "LocalNotificationPlugin",
                "FCM foreground service startRequested=$startRequested originalPriority=$originalPriority deliveredPriority=$deliveredPriority",
            )
            if (!startRequested) {
                Log.e(
                    KeepAliveNotificationHelper.FCM_TEST_LOG_TAG,
                    "FCM_FOREGROUND_SERVICE_NOT_STARTED start request was rejected or skipped",
                )
            }
        } else {
            Log.d(
                "LocalNotificationPlugin",
                "FCM foreground service skipped originalPriority=$originalPriority deliveredPriority=$deliveredPriority",
            )
        }
        try {
            val data = remoteMessage.data
            val title =
                data["title"]
                    ?: remoteMessage.notification?.title
                    ?: ""
            val body =
                data["body"]
                    ?: remoteMessage.notification?.body
                    ?: ""
            val messageId =
                data["id"]?.toIntOrNull()
                    ?: ((System.currentTimeMillis() % Int.MAX_VALUE).toInt())
            FlutterBoomNotificationPluginsPlugin.showFcmNotification(
                context = context,
                title = title,
                body = body,
                messageId = messageId,
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
