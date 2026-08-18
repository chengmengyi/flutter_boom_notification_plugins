package com.boom.notification.flutter_boom_notification_plugins

import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Applies broadcast-only daily and interval limits before the shared notification limiter. */
internal object BroadcastNotificationLimiter {
    private const val TAG = "BroadcastNotifLimit"
    private const val KEY_DAILY_COUNT = "boom_notification_broadcast_daily_show_count"
    private const val KEY_DAILY_DATE = "boom_notification_broadcast_daily_show_date"
    private const val KEY_LAST_SHOW_TIME = "boom_notification_last_show_broadcast_notification_time"
    private const val MILLIS_PER_MINUTE = 60_000L
    private val lock = Any()
    private var attemptInProgress = false

    fun isBroadcastPayload(payload: String?): Boolean =
        payload == "lock" ||
            payload == "USER_PRESENT" ||
            payload == "ACTION_POWER_CONNECTED" ||
            payload == "ACTION_POWER_DISCONNECTED" ||
            payload == "BATTERY_CHANGED" ||
            payload == "CLOSE_SYSTEM_DIALOGS" ||
            payload == "FILE_CHANGED" ||
            payload == "BOOT_COMPLETED"
            || payload == "EXIT_BACKGROUND"
            || payload == "HOME_KEY"
            || payload == "RECENT_APPS_KEY"
            || payload == "AD_CLICK"

    fun beginShowAttempt(context: Context, payload: String?): Boolean {
        if (!isBroadcastPayload(payload)) return true
        synchronized(lock) {
            if (attemptInProgress) return false
            val config = NotificationRemoteConfigManager.getBroadcastConfig(context) ?: return false
            val now = System.currentTimeMillis()
            val today = currentDate()
            val prefs = preferences(context)
            val count = if (prefs.getString(KEY_DAILY_DATE, null) == today) {
                prefs.getInt(KEY_DAILY_COUNT, 0)
            } else {
                0
            }
            val limit = config.optInt("send_limit", 0).coerceAtLeast(0)
            if (count >= limit) {
                Log.d(TAG, "blocked payload=$payload reason=daily_limit count=$count limit=$limit")
                return false
            }
            val intervalMinutes = config.optLong("send_interval_minutes", 0L).coerceAtLeast(0L)
            val intervalMillis = safeMinutesToMillis(intervalMinutes)
            val lastShowTime = prefs.getLong(KEY_LAST_SHOW_TIME, 0L)
            if (lastShowTime > 0L && now - lastShowTime < intervalMillis) {
                Log.d(TAG, "blocked payload=$payload reason=interval elapsed=${now - lastShowTime} required=$intervalMillis")
                return false
            }
            attemptInProgress = true
            return true
        }
    }

    fun recordShown(context: Context, payload: String?) {
        if (!isBroadcastPayload(payload)) return
        synchronized(lock) {
            val prefs = preferences(context)
            val today = currentDate()
            val oldCount = if (prefs.getString(KEY_DAILY_DATE, null) == today) {
                prefs.getInt(KEY_DAILY_COUNT, 0)
            } else {
                0
            }
            val now = System.currentTimeMillis()
            prefs.edit()
                .putString(KEY_DAILY_DATE, today)
                .putInt(KEY_DAILY_COUNT, oldCount + 1)
                .putLong(KEY_LAST_SHOW_TIME, now)
                .commit()
            attemptInProgress = false
            Log.d(TAG, "recorded payload=$payload time=$now count=${oldCount + 1}")
        }
    }

    fun cancelShowAttempt(payload: String?) {
        if (!isBroadcastPayload(payload)) return
        synchronized(lock) { attemptInProgress = false }
    }

    private fun preferences(context: Context) =
        context.applicationContext.getSharedPreferences(
            NotificationRemoteConfigManager.PREFS_NAME,
            Context.MODE_PRIVATE,
        )

    private fun safeMinutesToMillis(minutes: Long): Long =
        if (minutes >= Long.MAX_VALUE / MILLIS_PER_MINUTE) Long.MAX_VALUE
        else minutes * MILLIS_PER_MINUTE

    private fun currentDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}
