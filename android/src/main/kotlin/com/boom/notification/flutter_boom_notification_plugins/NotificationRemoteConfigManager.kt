package com.boom.notification.flutter_boom_notification_plugins

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

internal class NotificationRemoteConfigManager(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val preferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val isDebuggable =
        appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    @Volatile
    private var activeConfig: JSONObject? = null

    /**
     * 本地远程配置优先；本地不存在或不可解析时才使用调用方传入的默认 data JSON。
     * 远程刷新始终在后台执行，不阻塞 initNotification 的后续初始化。
     */
    fun resolveInitialConfig(config: Map<*, *>): JSONObject? {
        val cached = preferences.getString(KEY_CACHED_CONFIG, null)
        if (!cached.isNullOrBlank()) {
            parseConfigObject(cached)?.let {
                debugLog("Initial config source=local-cache\n$cached")
                return it
            }
            debugLog("Local cached config is invalid; removing it\n$cached")
            preferences.edit().remove(KEY_CACHED_CONFIG).apply()
        }
        val defaultConfig = config["defaultConfig"] as? String
        val parsedDefaultConfig = parseConfigObject(defaultConfig)
        debugLog(
            if (parsedDefaultConfig != null) {
                "Initial config source=defaultConfig\n$defaultConfig"
            } else {
                "defaultConfig is invalid\n$defaultConfig"
            },
        )
        return parsedDefaultConfig
    }

    fun activate(config: JSONObject) {
        activeConfig = config
        preferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, config.optBoolean("enabled", false)).commit()
        RepeatNotificationLimiter.savePolicy(
            context = appContext,
            firstDelayMinutes = config.optLong("first_send_delay_minutes", 0L),
            intervalMinutes = config.optLong("notification_interval_minutes", 0L),
            totalLimit = config.optInt("notification_total_limit", 0),
        )
        RepeatNotificationLimiter.ensureFirstInstallTime(appContext)
        debugLog("Notification master switch enabled=${config.optBoolean("enabled", false)}")
    }

    fun currentConfig(): JSONObject? = activeConfig

    /**
     * 远程配置优先，失败时使用有效本地缓存，本地无效时使用 defaultConfig。
     * 回调一定在主线程执行，调用方可在回调完成通知初始化后再回复 MethodChannel。
     */
    fun resolveForInitialization(
        config: Map<*, *>,
        onResolved: (JSONObject) -> Unit,
        onFailure: () -> Unit,
    ) {
        scope.launch {
            val fallbackConfig = resolveInitialConfig(config)
            val requestAvailable = config["request"] is Map<*, *>
            val resolvedConfig =
                if (!requestAvailable) {
                    fallbackConfig
                } else {
                    try {
                        val remoteConfig = requestAndParse(config)
                        preferences.edit().putString(KEY_CACHED_CONFIG, remoteConfig.toString()).apply()
                        debugLog("Remote config saved to local cache\n$remoteConfig")
                        remoteConfig
                    } catch (error: Exception) {
                        logRemoteFailure(error)
                        fallbackConfig
                    }
                }
            mainHandler.post {
                if (resolvedConfig == null) {
                    onFailure()
                } else {
                    activate(resolvedConfig)
                    onResolved(resolvedConfig)
                }
            }
        }
    }

    private fun logRemoteFailure(error: Exception) {
        // 远程配置是增强能力；任何请求、状态、JSON、提取或映射异常均进入本地/default 兜底。
        if (isDebuggable) {
            Log.e(
                TAG,
                "Remote notification config failed; using local/default config",
                error,
            )
        } else {
            Log.w(TAG, "Remote notification config unavailable; using local/default config")
        }
    }

    fun close() {
        scope.cancel()
    }

    private fun requestAndParse(config: Map<*, *>): JSONObject {
        val requestConfig = config["request"] as? Map<*, *>
            ?: error("Missing notification config request")
        val responseRule = config["responseRule"] as? Map<*, *> ?: emptyMap<Any, Any>()
        val url = requestConfig["url"]?.toString()?.toHttpUrlOrNull()
            ?: error("Invalid notification config URL")
        val urlBuilder = url.newBuilder()
        (requestConfig["queryParameters"] as? Map<*, *>)?.forEach { (key, value) ->
            if (key != null && value != null) {
                if (value is Iterable<*>) {
                    value.forEach { item ->
                        if (item != null) urlBuilder.addQueryParameter(key.toString(), item.toString())
                    }
                } else {
                    urlBuilder.addQueryParameter(key.toString(), value.toString())
                }
            }
        }

        val connectTimeout =
            (requestConfig["connectTimeoutMilliseconds"] as? Number)?.toLong()
                ?.coerceIn(MIN_TIMEOUT_MILLIS, MAX_TIMEOUT_MILLIS)
                ?: DEFAULT_CONNECT_TIMEOUT_MILLIS
        val readTimeout =
            (requestConfig["readTimeoutMilliseconds"] as? Number)?.toLong()
                ?.coerceIn(MIN_TIMEOUT_MILLIS, MAX_TIMEOUT_MILLIS)
                ?: DEFAULT_READ_TIMEOUT_MILLIS
        val client =
            OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .callTimeout(connectTimeout + readTimeout, TimeUnit.MILLISECONDS)
                .build()

        val method = requestConfig["method"]?.toString()?.uppercase() ?: "POST"
        val bodyValue = requestConfig["body"]
        val requestBody =
            when (bodyValue) {
                null -> null
                is String -> bodyValue.toRequestBody(JSON_MEDIA_TYPE)
                else -> jsonValueToString(bodyValue).toRequestBody(JSON_MEDIA_TYPE)
            }
        val requestBuilder = Request.Builder().url(urlBuilder.build())
        (requestConfig["headers"] as? Map<*, *>)?.forEach { (key, value) ->
            if (key != null && value != null) requestBuilder.header(key.toString(), value.toString())
        }
        requestBuilder.method(
            method,
            when (method) {
                "GET", "HEAD" -> null
                else -> requestBody ?: EMPTY_REQUEST_BODY
            },
        )

        val builtRequest = requestBuilder.build()
        debugLog(
            buildString {
                append("Remote config request")
                append("\nmethod=").append(method)
                append("\nurl=").append(builtRequest.url)
                append("\nheaders=\n").append(builtRequest.headers)
                append("body=\n")
                append(
                    when (bodyValue) {
                        null -> "null"
                        is String -> bodyValue
                        else -> jsonValueToString(bodyValue)
                    },
                )
                append("\nconnectTimeoutMilliseconds=").append(connectTimeout)
                append("\nreadTimeoutMilliseconds=").append(readTimeout)
            },
        )

        client.newCall(builtRequest).execute().use { response ->
            debugLog(
                "Remote config HTTP response status=${response.code} message=${response.message}",
            )
            if (!response.isSuccessful) error("Unexpected HTTP status")
            val responseText = response.body?.string()?.takeUnless { it.isBlank() }
                ?: error("Empty notification config response")
            debugLog("Remote config raw response\n$responseText")
            val responseJson = JSONObject(responseText)
            validateBusinessCode(responseJson, responseRule)
            val dataPath = responseRule["dataPath"]?.toString().orEmpty().ifBlank { "data" }
            val data = extractPath(responseJson, dataPath) as? JSONObject
                ?: error("Notification config data is not an object")
            val mapping = (config["fieldMapping"] as? Map<*, *>)
                .orEmpty()
                .entries
                .associate { it.key.toString() to it.value.toString() }
            debugLog("Remote config extracted data\n$data")
            debugLog("Remote config field mapping standard->remote\n${JSONObject(mapping)}")
            val mapped =
                remapObject(data, mapping.entries.associate { (standard, remote) -> remote to standard })
            debugLog("Remote config mapped standard data\n$mapped")
            return mapped
        }
    }

    private fun validateBusinessCode(
        response: JSONObject,
        rule: Map<*, *>,
    ) {
        val codePath = rule["codePath"]?.toString().orEmpty().ifBlank { "code" }
        val actualCode = extractPath(response, codePath) ?: error("Missing business code")
        val successCodes = (rule["successCodes"] as? Iterable<*>)?.toList() ?: listOf(200)
        if (successCodes.none { codesEqual(it, actualCode) }) error("Business request failed")
    }

    private fun extractPath(root: Any, path: String): Any? {
        var current: Any? = root
        for (part in path.split('.').filter { it.isNotBlank() }) {
            current =
                when (current) {
                    is JSONObject -> if (current.has(part) && !current.isNull(part)) current.get(part) else null
                    is JSONArray -> part.toIntOrNull()?.let { index ->
                        if (index in 0 until current.length()) current.get(index) else null
                    }
                    else -> null
                }
            if (current == null || current == JSONObject.NULL) return null
        }
        return current
    }

    private fun remapObject(
        source: JSONObject,
        remoteToStandard: Map<String, String>,
    ): JSONObject {
        val target = JSONObject()
        source.keys().forEach { remoteKey ->
            val standardKey = remoteToStandard[remoteKey] ?: remoteKey
            target.put(standardKey, remapValue(source.get(remoteKey), remoteToStandard))
        }
        return target
    }

    private fun remapValue(
        value: Any?,
        remoteToStandard: Map<String, String>,
    ): Any? =
        when (value) {
            is JSONObject -> remapObject(value, remoteToStandard)
            is JSONArray -> JSONArray().apply {
                for (index in 0 until value.length()) {
                    put(remapValue(value.get(index), remoteToStandard))
                }
            }
            else -> value
        }

    private fun parseConfigObject(value: String?): JSONObject? {
        if (value.isNullOrBlank()) return null
        return try {
            JSONObject(value)
        } catch (_: Exception) {
            null
        }
    }

    private fun jsonValueToString(value: Any): String =
        when (value) {
            is Map<*, *> -> JSONObject(value.mapKeys { it.key.toString() }).toString()
            is Iterable<*> -> JSONArray(value.toList()).toString()
            else -> JSONObject.wrap(value)?.toString() ?: "null"
        }

    private fun codesEqual(left: Any?, right: Any?): Boolean =
        left?.toString()?.trim() == right?.toString()?.trim()

    private fun debugLog(message: String) {
        if (!isDebuggable) return
        if (message.isEmpty()) {
            Log.d(TAG, message)
            return
        }
        message.chunked(LOG_CHUNK_SIZE).forEachIndexed { index, chunk ->
            if (index == 0) {
                Log.d(TAG, chunk)
            } else {
                Log.d(TAG, "[continued $index] $chunk")
            }
        }
    }

    companion object {
        fun areNotificationsEnabled(context: Context): Boolean =
            context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_NOTIFICATIONS_ENABLED, false)

        const val TAG = "NotificationRemoteCfg"
        const val PREFS_NAME = "flutter_boom_notification_remote_config"
        const val KEY_CACHED_CONFIG = "cached_standard_data_json"
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val DEFAULT_CONNECT_TIMEOUT_MILLIS = 30_000L
        const val DEFAULT_READ_TIMEOUT_MILLIS = 30_000L
        const val MIN_TIMEOUT_MILLIS = 250L
        const val MAX_TIMEOUT_MILLIS = 30_000L
        const val LOG_CHUNK_SIZE = 3_500
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        val EMPTY_REQUEST_BODY = ByteArray(0).toRequestBody(null)
    }

}
