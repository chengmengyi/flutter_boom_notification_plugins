package com.boom.notification.flutter_boom_notification_plugins

import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Applies one shared first-delay, interval and daily-limit policy to repeat notifications. */
internal object RepeatNotificationLimiter {
    private const val TAG = "RepeatNotificationLimit"
    const val KEY_FIRST_INSTALL_TIME = "boom_notification_first_install_time"
    const val KEY_LAST_SHOW_TIME = "boom_notification_last_show_notification_time"
    private const val KEY_DAILY_SHOW_COUNT = "boom_notification_daily_show_count"
    private const val KEY_DAILY_SHOW_DATE = "boom_notification_daily_show_date"
    private const val KEY_FIRST_DELAY_MINUTES = "first_send_delay_minutes"
    private const val KEY_INTERVAL_MINUTES = "notification_interval_minutes"
    private const val KEY_TOTAL_LIMIT = "notification_total_limit"
    private const val MILLIS_PER_MINUTE = 60_000L
    private val lock = Any()
    private var attemptInProgress = false

    fun savePolicy(
        context: Context,
        firstDelayMinutes: Long,
        intervalMinutes: Long,
        totalLimit: Int,
    ) {
        preferences(context)
            .edit()
            .putLong(KEY_FIRST_DELAY_MINUTES, firstDelayMinutes.coerceAtLeast(0L))
            .putLong(KEY_INTERVAL_MINUTES, intervalMinutes.coerceAtLeast(0L))
            .putInt(KEY_TOTAL_LIMIT, totalLimit.coerceAtLeast(0))
            .commit()
    }

    /** Must run before initNotification replies to Flutter; the value is written only once. */
    fun ensureFirstInstallTime(context: Context) {
        synchronized(lock) {
            val prefs = preferences(context)
            if (!prefs.contains(KEY_FIRST_INSTALL_TIME)) {
                val now = System.currentTimeMillis()
                prefs.edit().putLong(KEY_FIRST_INSTALL_TIME, now).commit()
                Log.d(TAG, "first install time saved time=$now")
            }
        }
    }

    fun hasElapsedFirstDelay(
        context: Context,
        firstDelayMinutes: Long,
    ): Boolean {
        val firstInstallTime = preferences(context).getLong(KEY_FIRST_INSTALL_TIME, 0L)
        if (firstInstallTime <= 0L) return false
        val requiredMillis = safeMinutesToMillis(firstDelayMinutes)
        return System.currentTimeMillis() - firstInstallTime >= requiredMillis
    }

    fun isRepeatNotification(payload: String?): Boolean =
        when (payload?.trim()) {
            "fcm",
            "local",
            "media",
            "lock",
            "USER_PRESENT",
            "ACTION_POWER_CONNECTED",
            "ACTION_POWER_DISCONNECTED",
            "BATTERY_CHANGED",
            "SCREEN_ON",
            "SCREEN_OFF",
            "PACKAGE_ADDED",
            "PACKAGE_REMOVED",
            "PACKAGE_REPLACED",
            "CLOSE_SYSTEM_DIALOGS",
            "CONFIGURATION_CHANGED",
            "FILE_CHANGED",
            "BOOT_COMPLETED",
            "EXIT_BACKGROUND",
            "HOME_KEY",
            "RECENT_APPS_KEY",
            "AD_CLICK",
            "notify_new_file",
            -> true
            else -> false
        }

    /** Reserves a single in-process send attempt without persisting send time or count. */
    fun beginShowAttempt(
        context: Context,
        payload: String?,
    ): Boolean {
        if (!isRepeatNotification(payload)) return true
        synchronized(lock) {
            if (attemptInProgress) {
                Log.d(TAG, "blocked payload=$payload reason=another_attempt_in_progress")
                return false
            }
            val prefs = preferences(context)
            val now = System.currentTimeMillis()
            val firstInstallTime = prefs.getLong(KEY_FIRST_INSTALL_TIME, 0L)
            val firstDelayMillis = safeMinutesToMillis(prefs.getLong(KEY_FIRST_DELAY_MINUTES, 0L))
            if (firstInstallTime <= 0L || now - firstInstallTime <= firstDelayMillis) {
                Log.d(
                    TAG,
                    "blocked payload=$payload reason=first_delay elapsed=${now - firstInstallTime} required=$firstDelayMillis",
                )
                return false
            }
            val lastShowTime = prefs.getLong(KEY_LAST_SHOW_TIME, 0L)
            val intervalMillis = safeMinutesToMillis(prefs.getLong(KEY_INTERVAL_MINUTES, 0L))
            if (lastShowTime > 0L && now - lastShowTime <= intervalMillis) {
                Log.d(
                    TAG,
                    "blocked payload=$payload reason=interval elapsed=${now - lastShowTime} required=$intervalMillis",
                )
                return false
            }
            val today = currentDate()
            val savedDate = prefs.getString(KEY_DAILY_SHOW_DATE, null)
            val currentCount = if (savedDate == today) prefs.getInt(KEY_DAILY_SHOW_COUNT, 0) else 0
            val totalLimit = prefs.getInt(KEY_TOTAL_LIMIT, 0)
            if (currentCount >= totalLimit) {
                Log.d(
                    TAG,
                    "blocked payload=$payload reason=daily_limit count=$currentCount limit=$totalLimit date=$today",
                )
                return false
            }
            attemptInProgress = true
            return true
        }
    }

    /** Persists state only after NotificationManager.notify has completed successfully. */
    fun recordShown(
        context: Context,
        payload: String?,
    ) {
        if (!isRepeatNotification(payload)) return
        synchronized(lock) {
            val prefs = preferences(context)
            val now = System.currentTimeMillis()
            val today = currentDate()
            val savedDate = prefs.getString(KEY_DAILY_SHOW_DATE, null)
            val oldCount = if (savedDate == today) prefs.getInt(KEY_DAILY_SHOW_COUNT, 0) else 0
            val newCount = oldCount + 1
            prefs.edit()
                .putLong(KEY_LAST_SHOW_TIME, now)
                .putString(KEY_DAILY_SHOW_DATE, today)
                .putInt(KEY_DAILY_SHOW_COUNT, newCount)
                .commit()
            attemptInProgress = false
            Log.d(TAG, "recorded payload=$payload time=$now count=$newCount date=$today")
        }
    }

    fun cancelShowAttempt(payload: String?) {
        if (!isRepeatNotification(payload)) return
        synchronized(lock) {
            attemptInProgress = false
        }
    }

    private fun preferences(context: Context) =
        context.applicationContext.getSharedPreferences(
            NotificationRemoteConfigManager.PREFS_NAME,
            Context.MODE_PRIVATE,
        )

    private fun safeMinutesToMillis(minutes: Long): Long =
        if (minutes >= Long.MAX_VALUE / MILLIS_PER_MINUTE) Long.MAX_VALUE
        else minutes.coerceAtLeast(0L) * MILLIS_PER_MINUTE

    private fun currentDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}
