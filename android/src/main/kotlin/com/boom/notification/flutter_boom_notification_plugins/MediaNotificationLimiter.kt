package com.boom.notification.flutter_boom_notification_plugins

import android.content.Context
import android.util.Log

/** Applies the randomly selected media config's first-delay and interval policy. */
internal object MediaNotificationLimiter {
    private const val TAG = "MediaNotificationLimit"
    private const val KEY_LAST_SHOW_TIME = "boom_notification_last_show_media_notification_time"
    private const val MILLIS_PER_MINUTE = 60_000L
    private val lock = Any()
    private var attemptInProgress = false

    fun beginShowAttempt(
        context: Context,
        firstDelayMinutes: Long,
        intervalMinutes: Long,
    ): Boolean = synchronized(lock) {
        if (attemptInProgress) return false
        if (!RepeatNotificationLimiter.hasElapsedFirstDelay(context, firstDelayMinutes)) {
            Log.d(TAG, "blocked reason=first_delay minutes=$firstDelayMinutes")
            return false
        }
        val lastShowTime = preferences(context).getLong(KEY_LAST_SHOW_TIME, 0L)
        val intervalMillis = safeMinutesToMillis(intervalMinutes.coerceAtLeast(0L))
        val now = System.currentTimeMillis()
        if (lastShowTime > 0L && now - lastShowTime < intervalMillis) {
            Log.d(TAG, "blocked reason=interval elapsed=${now - lastShowTime} required=$intervalMillis")
            return false
        }
        attemptInProgress = true
        true
    }

    fun recordShown(context: Context) = synchronized(lock) {
        val now = System.currentTimeMillis()
        preferences(context).edit().putLong(KEY_LAST_SHOW_TIME, now).commit()
        attemptInProgress = false
        Log.d(TAG, "recorded time=$now")
    }

    fun cancelShowAttempt() = synchronized(lock) {
        attemptInProgress = false
    }

    private fun preferences(context: Context) =
        context.applicationContext.getSharedPreferences(
            NotificationRemoteConfigManager.PREFS_NAME,
            Context.MODE_PRIVATE,
        )

    private fun safeMinutesToMillis(minutes: Long): Long =
        if (minutes >= Long.MAX_VALUE / MILLIS_PER_MINUTE) Long.MAX_VALUE
        else minutes * MILLIS_PER_MINUTE
}
