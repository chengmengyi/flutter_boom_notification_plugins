package com.boom.notification.flutter_boom_notification_plugins

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.util.Log

/** Observes new rows in external MediaStore files and emits one broadcast-notification trigger. */
internal object BroadcastFileObserverHelper {
    private const val TAG = "BroadcastFileObserver"
    private const val PREFS_NAME = "flutter_boom_notification_plugins"
    private const val KEY_LAST_FILE_ID = "broadcast_file_observer_last_id"
    private const val KEY_LAST_FILE_DATE = "broadcast_file_observer_last_date"
    private val lock = Any()
    private var thread: HandlerThread? = null
    private var observer: ContentObserver? = null

    fun start(context: Context) {
        val appContext = context.applicationContext
        synchronized(lock) {
            if (observer != null) return
            val handlerThread = HandlerThread("boom-broadcast-file-observer").apply { start() }
            val handler = Handler(handlerThread.looper)
            val contentObserver = object : ContentObserver(handler) {
                override fun onChange(selfChange: Boolean) {
                    checkLatestFile(appContext, notify = true)
                }
            }
            runCatching {
                appContext.contentResolver.registerContentObserver(
                    MediaStore.Files.getContentUri("external"),
                    true,
                    contentObserver,
                )
                thread = handlerThread
                observer = contentObserver
                checkLatestFile(appContext, notify = false)
                Log.d(TAG, "start registered")
            }.onFailure {
                handlerThread.quitSafely()
                Log.d(TAG, "start failed error=${it.message}")
            }
        }
    }

    fun stop(context: Context) {
        synchronized(lock) {
            observer?.let { runCatching { context.applicationContext.contentResolver.unregisterContentObserver(it) } }
            observer = null
            thread?.quitSafely()
            thread = null
        }
        Log.d(TAG, "stop")
    }

    private fun checkLatestFile(context: Context, notify: Boolean) {
        if (!NotificationRemoteConfigManager.isFeatureEnabled(
                context,
                NotificationRemoteConfigManager.KEY_BROADCAST_ENABLED,
            )
        ) return
        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DATE_ADDED)
        val latest = runCatching {
            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${MediaStore.Files.FileColumns.DATE_ADDED} DESC, ${MediaStore.Files.FileColumns._ID} DESC",
            )?.use { cursor ->
                if (!cursor.moveToFirst()) null else {
                    cursor.getLong(0) to cursor.getLong(1)
                }
            }
        }.getOrElse {
            Log.d(TAG, "query failed error=${it.message}")
            null
        } ?: return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val oldId = prefs.getLong(KEY_LAST_FILE_ID, -1L)
        val oldDate = prefs.getLong(KEY_LAST_FILE_DATE, 0L)
        prefs.edit().putLong(KEY_LAST_FILE_ID, latest.first).putLong(KEY_LAST_FILE_DATE, latest.second).apply()
        if (!notify || oldId < 0L) return
        if (latest.second > oldDate || (latest.second == oldDate && latest.first > oldId)) {
            Log.d(TAG, "new file id=${latest.first} date=${latest.second}")
            FlutterBoomNotificationPluginsPlugin.handleUnlockBroadcast(
                context,
                FlutterBoomNotificationPluginsPlugin.ACTION_FILE_CHANGED,
            )
        }
    }
}
