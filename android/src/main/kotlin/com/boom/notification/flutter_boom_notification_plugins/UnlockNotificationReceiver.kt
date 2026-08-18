package com.boom.notification.flutter_boom_notification_plugins

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import kotlin.concurrent.thread

class UnlockNotificationReceiver : BroadcastReceiver() {
    companion object {
        private val instances = mutableMapOf<String, UnlockNotificationReceiver>()

        fun add(
            context: Context,
            action: String,
            addPackageDataScheme: Boolean = false,
        ) {
            val key = if (addPackageDataScheme) "$action#package" else action
            if (instances.containsKey(key)) {
                return
            }
            val receiver = UnlockNotificationReceiver().apply {
                broadcastAction = action
            }
            val intentFilter =
                IntentFilter().apply {
                    addAction(action)
                    if (addPackageDataScheme) {
                        addDataScheme("package")
                    }
                }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(
                    receiver,
                    intentFilter,
                    Context.RECEIVER_EXPORTED,
                )
            } else {
                context.registerReceiver(receiver, intentFilter)
            }
            instances[key] = receiver
            Log.d(
                "LocalNotificationPlugin",
                "UnlockNotificationReceiver.add action=$action addPackageDataScheme=$addPackageDataScheme",
            )
        }

        fun removeAll(context: Context) {
            instances.values.forEach {
                try {
                    context.unregisterReceiver(it)
                } catch (_: Exception) {
                }
            }
            instances.clear()
            Log.d("LocalNotificationPlugin", "UnlockNotificationReceiver.removeAll")
        }
    }

    private var broadcastAction: String? = null

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (broadcastAction != null && broadcastAction != intent.action) return
        if (intent.action == Intent.ACTION_BATTERY_CHANGED && isInitialStickyBroadcast) return
        val resolvedAction =
            if (intent.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
                when (intent.getStringExtra("reason")?.lowercase()) {
                    "homekey" -> FlutterBoomNotificationPluginsPlugin.ACTION_HOME_KEY
                    "recentapps", "recent_apps" ->
                        FlutterBoomNotificationPluginsPlugin.ACTION_RECENT_APPS_KEY
                    else -> return
                }.also {
                    FlutterBoomNotificationPluginsPlugin.recordNavigationSystemEvent()
                }
            } else {
                intent.action
            }
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        thread(name = "boom-broadcast-notification") {
            try {
                FlutterBoomNotificationPluginsPlugin.handleUnlockBroadcast(appContext, resolvedAction)
            } catch (throwable: Throwable) {
                Log.d("LocalNotificationPlugin", "UnlockNotificationReceiver failed action=${intent.action} error=${throwable.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
