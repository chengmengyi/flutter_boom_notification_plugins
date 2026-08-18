package com.boom.notification.flutter_boom_notification_plugins

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlin.concurrent.thread

class LocalNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        thread(name = "boom-local-notification-receiver") {
            try {
                if (FlutterBoomNotificationPluginsPlugin.isNotificationBlocked(appContext)) {
                    Log.d("LocalNotificationPlugin", "LocalNotificationReceiver blocked")
                    return@thread
                }
                Log.d("LocalNotificationPlugin", "LocalNotificationReceiver.onReceive background")
                if (!LocalNotificationScheduler.handleAlarm(appContext, intent)) {
                    // Migrate an Alarm PendingIntent created by a previous plugin version.
                    FlutterBoomNotificationPluginsPlugin.showNotificationFromIntent(appContext, intent)
                    LocalNotificationScheduler.register(appContext, intent)
                }
            } catch (throwable: Throwable) {
                Log.d("LocalNotificationPlugin", "LocalNotificationReceiver failed error=${throwable.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
