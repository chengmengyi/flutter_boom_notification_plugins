package com.boom.notification.flutter_boom_notification_plugins

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.mockito.Mockito
import kotlin.test.Test

internal class FlutterBoomNotificationPluginsPluginTest {
    @Test
    fun onMethodCall_unknownMethod_returnsNotImplementedWithoutNotificationState() {
        val plugin =
            Mockito.mock(
                FlutterBoomNotificationPluginsPlugin::class.java,
                Mockito.CALLS_REAL_METHODS,
            )

        val call = MethodCall("unknownMethod", null)
        val mockResult: MethodChannel.Result = Mockito.mock(MethodChannel.Result::class.java)
        plugin.onMethodCall(call, mockResult)

        Mockito.verify(mockResult).notImplemented()
    }
}
