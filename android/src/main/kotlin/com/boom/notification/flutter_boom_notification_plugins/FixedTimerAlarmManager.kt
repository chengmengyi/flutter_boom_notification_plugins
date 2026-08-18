package com.boom.notification.flutter_boom_notification_plugins

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/** A dynamic AlarmManager fallback for every configured local-notification schedule. */
object FixedTimerAlarmManager {
    private const val TAG = "FixedTimerAlarmManager"
    private const val PREFS_NAME = "flutter_boom_notification_plugins"
    private const val KEY_PREFIX = "fixed_timer_alarm_"
    private const val ACTION_PREFIX =
        "com.boom.notification.flutter_boom_notification_plugins.TIMER_ALARM_"
    private const val EXTRA_SCHEDULE_ID = "fixed_timer_schedule_id"
    private val lock = Any()

    fun schedule(context: Context, scheduleId: Int, update: Boolean) {
        val config = LocalNotificationScheduler.timerWorkConfigs(context)
            .firstOrNull { it.scheduleId == scheduleId } ?: return
        scheduleConfig(context.applicationContext, config, resetTime = update)
    }

    fun restore(context: Context) {
        val appContext = context.applicationContext
        val configs = LocalNotificationScheduler.timerWorkConfigs(appContext)
        configs.forEach { scheduleConfig(appContext, it, resetTime = false) }
        Log.d(TAG, "restore count=${configs.size}")
    }

    fun handleAlarm(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        val scheduleId = intent.getIntExtra(EXTRA_SCHEDULE_ID, 0)
        if (scheduleId == 0) return
        val config = LocalNotificationScheduler.timerWorkConfigs(appContext)
            .firstOrNull { it.scheduleId == scheduleId }
        if (config == null) {
            cancelSchedule(appContext, scheduleId, clear = true)
            return
        }
        val source = "fixed_alarm_$scheduleId"
        runCatching {
            val displayed = LocalNotificationScheduler.tryDeliverFromTimerWork(
                appContext,
                scheduleId,
                source,
            )
            val foregroundRequested =
                KeepAliveNotificationHelper.startOrUpdateForegroundService(appContext, source)
            Log.d(TAG, "handleAlarm scheduleId=$scheduleId displayed=$displayed foregroundRequested=$foregroundRequested")
        }.onFailure {
            Log.d(TAG, "handleAlarm failed scheduleId=$scheduleId error=${it.message}")
        }
        advanceAndScheduleNext(appContext, config)
    }

    fun cancelAll(context: Context, clear: Boolean) {
        LocalNotificationScheduler.timerWorkConfigs(context).forEach {
            cancelSchedule(context.applicationContext, it.scheduleId, clear)
        }
        // Cancel PendingIntents created by the previous three-slot implementation.
        listOf(431001, 431002, 431003).forEachIndexed { index, requestCode ->
            val legacyIntent = Intent(context, FixedTimerAlarmReceiver::class.java).apply {
                action = "$ACTION_PREFIX${index + 1}"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                legacyIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )
            if (pendingIntent != null) {
                (context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    fun cancel(context: Context, scheduleId: Int, clear: Boolean = true) {
        cancelSchedule(context.applicationContext, scheduleId, clear)
    }

    private fun scheduleConfig(
        context: Context,
        config: LocalNotificationScheduler.TimerWorkConfig,
        resetTime: Boolean,
    ) {
        val triggerAt = synchronized(lock) {
            val storedInterval = prefs(context).getLong(key(config.scheduleId, "interval"), 0L)
            val storedNext = prefs(context).getLong(key(config.scheduleId, "next_at"), 0L)
            val next = if (!resetTime && storedInterval == config.intervalMillis && storedNext > 0L) {
                storedNext
            } else {
                System.currentTimeMillis() + config.intervalMillis
            }
            prefs(context).edit()
                .putLong(key(config.scheduleId, "interval"), config.intervalMillis)
                .putLong(key(config.scheduleId, "next_at"), next)
                .apply()
            next
        }
        setAlarm(context, config.scheduleId, triggerAt)
    }

    private fun advanceAndScheduleNext(
        context: Context,
        config: LocalNotificationScheduler.TimerWorkConfig,
    ) {
        val next = synchronized(lock) {
            val now = System.currentTimeMillis()
            var candidate = prefs(context).getLong(key(config.scheduleId, "next_at"), now) +
                config.intervalMillis
            while (candidate <= now) candidate += config.intervalMillis
            prefs(context).edit().putLong(key(config.scheduleId, "next_at"), candidate).apply()
            candidate
        }
        setAlarm(context, config.scheduleId, next)
    }

    private fun setAlarm(context: Context, scheduleId: Int, triggerAt: Long) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = createPendingIntent(context, scheduleId, PendingIntent.FLAG_UPDATE_CURRENT)
        manager.cancel(pendingIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    private fun cancelSchedule(context: Context, scheduleId: Int, clear: Boolean) {
        val pendingIntent = createPendingIntent(context, scheduleId, PendingIntent.FLAG_NO_CREATE)
        if (pendingIntent != null) {
            (context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.cancel(pendingIntent)
            pendingIntent.cancel()
        }
        if (clear) {
            prefs(context).edit()
                .remove(key(scheduleId, "interval"))
                .remove(key(scheduleId, "next_at"))
                .apply()
        }
    }

    private fun createPendingIntent(context: Context, scheduleId: Int, flag: Int): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            scheduleId,
            Intent(context, FixedTimerAlarmReceiver::class.java).apply {
                action = "$ACTION_PREFIX$scheduleId"
                putExtra(EXTRA_SCHEDULE_ID, scheduleId)
            },
            flag or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun key(scheduleId: Int, suffix: String) = "$KEY_PREFIX${scheduleId}_$suffix"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
