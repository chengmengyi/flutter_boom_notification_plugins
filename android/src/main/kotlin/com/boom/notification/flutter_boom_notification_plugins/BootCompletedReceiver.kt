package com.boom.notification.flutter_boom_notification_plugins

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlin.concurrent.thread

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val action = intent.action ?: "unknown"
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        thread(name = "boom-boot-notification") {
            try {
                Log.d("LocalNotificationPlugin", "BootCompletedReceiver.onReceive action=$action")
                FlutterBoomNotificationPluginsPlugin.restoreAfterBoot(appContext, action)
                if (action == Intent.ACTION_BOOT_COMPLETED) {
                    FlutterBoomNotificationPluginsPlugin.handleUnlockBroadcast(appContext, action)
                }
            } catch (throwable: Throwable) {
                Log.d("LocalNotificationPlugin", "BootCompletedReceiver failed action=$action error=${throwable.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
