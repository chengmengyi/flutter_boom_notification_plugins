import 'dart:convert';

import 'package:flutter_boom_notification_plugins/flutter_boom_notification_plugins.dart';

class B16BroadcastConfigHepVqntza {
  const B16BroadcastConfigHepVqntza._();

  static List<BroadcastNotificationConfig> b16BuildConfigsKqmwze() {
    int b16ActionIntervalQxnvza = 30;
    int b16BatteryIntervalVqntza = 600;
    return <BroadcastNotificationConfig>[
      BroadcastNotificationConfig(
        payload: LocalNotificationPayload.userPresent,
        interval: Duration(seconds: 1),
      ),
      BroadcastNotificationConfig(
        payload: LocalNotificationPayload.actionPowerConnected,
        interval: Duration(seconds: b16ActionIntervalQxnvza),
      ),
      BroadcastNotificationConfig(
        payload: LocalNotificationPayload.actionPowerDisconnected,
        interval: Duration(seconds: b16ActionIntervalQxnvza),
      ),
      BroadcastNotificationConfig(
        payload: LocalNotificationPayload.batteryChanged,
        interval: Duration(seconds: b16BatteryIntervalVqntza),
      ),
      BroadcastNotificationConfig(
        payload: LocalNotificationPayload.screenOn,
        interval: Duration(seconds: b16ActionIntervalQxnvza),
      ),
      BroadcastNotificationConfig(
        payload: LocalNotificationPayload.screenOff,
        interval: Duration(seconds: b16ActionIntervalQxnvza),
      ),
      BroadcastNotificationConfig(
        payload: LocalNotificationPayload.packageAdded,
        interval: Duration(seconds: b16ActionIntervalQxnvza),
      ),
      BroadcastNotificationConfig(
        payload: LocalNotificationPayload.packageRemoved,
        interval: Duration(seconds: b16ActionIntervalQxnvza),
      ),
      BroadcastNotificationConfig(
        payload: LocalNotificationPayload.packageReplaced,
        interval: Duration(seconds: b16ActionIntervalQxnvza),
      ),
      BroadcastNotificationConfig(
        payload: LocalNotificationPayload.closeSystemDialogs,
        interval: Duration(seconds: b16ActionIntervalQxnvza),
      ),
      BroadcastNotificationConfig(
        payload: LocalNotificationPayload.configurationChanged,
        interval: Duration(seconds: b16ActionIntervalQxnvza),
      ),
    ];
  }
}
