package com.boom.notification.flutter_boom_notification_plugins

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlin.concurrent.thread

class FixedTimerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        thread(name = "boom-fixed-timer-receiver") {
            try {
                FixedTimerAlarmManager.handleAlarm(appContext, intent)
            } catch (throwable: Throwable) {
                Log.d(TAG, "onReceive failed action=${intent.action} error=${throwable.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "FixedTimerAlarmReceiver"
    }
}
