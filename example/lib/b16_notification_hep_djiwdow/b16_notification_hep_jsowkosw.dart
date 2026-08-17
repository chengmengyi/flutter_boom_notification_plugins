import 'dart:async';
import 'package:flutter_boom_notification_plugins/flutter_boom_notification_plugins.dart';
import 'package:flutter_boom_notification_plugins_example/b16_notification_hep_djiwdow/b16_broadcast_list_infi_dwiow.dart';
import 'package:flutter_boom_notification_plugins_example/b16_notification_hep_djiwdow/b16_notification_list_info_djiwjdiw.dart';
import 'package:permission_handler/permission_handler.dart';

class B16NotificationHepPqnvze {
  B16NotificationHepPqnvze._();
  static final B16NotificationHepPqnvze instance = B16NotificationHepPqnvze._();

  bool _b16InitializedQxnvza = false;

  Future<void> b16InitializeNotificationsQxnvza({
    bool b16RequestPermissionKqmwze = false,
  }) async {
    if (_b16InitializedQxnvza) {
      return;
    }
    final bool b16CanInitializeVqntza = await _b16CanInitializeHqmwza();
    if (!b16CanInitializeVqntza) {
      return;
    }
    _b16InitializeListenersKqmwze();
    await _b16InitializeLocalInfoVqntza();
    b16UpdateNewFileTextPqnvze();
    _b16ScheduleLocalNotificationsRqmwza();
    _b16InitializeFcmQxnvza();
    _b16InitializeBroadcastsVqntza();
    b16InitializeMediaNotificationVqntza();
    _b16InitializeShortcutNotificationHqmwza();
    _b16InitializedQxnvza = true;
    await Permission.notification.request();
  }

  Future<bool> hasNotificationPermission()async{
    var permissionStatus = await Permission.notification.status;
    var isGranted = permissionStatus.isGranted || permissionStatus.isLimited;
    return isGranted;
  }

  void _b16InitializeShortcutNotificationHqmwza() {
    FlutterBoomNotificationPlugins.instance.showPersistentShortcutNotification(
      homeText: 'Home',
      mergeText: 'Scan',
      importText: 'Word to PDF',
      convertText: 'Image to PDF',
      homeIcon: 'b16_func_home',
      mergeIcon: 'b16_func_scan',
      importIcon: 'b16_func_word',
      convertIcon: 'b16_func_pdf',
    );
  }

  Future<void> b16InitializeMediaNotificationVqntza() async {
    final bool b16CanInitializeQxnvza = await _b16CanInitializeHqmwza();
    if (!b16CanInitializeQxnvza) {
      return;
    }
    FlutterBoomNotificationPlugins.instance.updateShowMediaTag(
      showMedia: true,
    );
    final bool b16ReplaceExistingKqmwze =
        true;
    FlutterBoomNotificationPlugins.instance.periodicallyShowMediaWithDuration(
      id: 9010,
      repeatDurationInterval: _b16NotificationIntervalPqnvze(),
      title: 'PDF Edit',
      body: 'PDF Edit Body',
      reflectionConfig: MediaReflectionConfig(
        secret: "B16secretKeyKhuwi",
        mediaSessionClass:
            "v1:oU8ZqyeQlnoNde34:0orxX/bgQC9XO9PLtV+SsmNZPpAHpLbV3SfBxRK0gresMuUGBV/H+x3W7fjhvMVYOvdSRKpduxZQOaH52kC0kB2ptw==",
        mediaSessionTokenClass:
            "v1:1ghYSzOmYdMjCqgW:IzaL6++IQ+j6Qv3F5Q7cTzqxGJyowFcWKrr1b1KQoGd68Tq04KprbBbtI6SnlPNlPiokPXKNk+kXswY/IU3KLwhNMhKJgfi8SQ==",
        mediaSessionTag:
            "v1:eCw+dvOZO3AnU7Hs:vB7TpcWkeiwUJrklfdFpdKslMa305hbPGdAhmiAREg==",
        playbackStateClass:
            "v1:N2EiKtH+/7Vfw+dU:LZw4h8ZDnNTSYm2HhdkR3+82C7qyp0xtV2V0TyWIM7ToHGlGm5kVSzPG6BTnlvsrxPkioksaDwMKDBZsLgVZ/2/MmD4=",
        playbackStateBuilderClass:
            "v1:OZP9NZV/bvAjDUxQ:dp8cRwnwhXF7wWYStIwzQDw7POT3aDpYMKH7ervpftCtk1xjebSlXsqbT5k5XLscGhvK6qhYgUoIKoQvIPFcyECFbF99SZZm0dWVjQ==",
        mediaStyleClass:
            "v1:eKeEeYrn6bwoVMhh:hzDhXiNH1gUUa02DEo8meZJqWICBT7GWxVpL6hY0CT7Zzt3vWjDR4wpc/4ewR3Vx/OIMOPyuXfiLhqhDWqpK4g==",
        setFlagsMethod: "v1:zsrp2wU/tzBJv8L/:YKdCJc6mq/+QpoSxfEYURNf1+sW2njhv",
        setActiveMethod:
            "v1:Ns4ghyUZXwlYd9HG:RO921bMjgHtWcsn1VjP1pqNjQtIl41r0OA==",
        setPlaybackStateMethod:
            "v1:DDvP5FNkzrWPPUHD:8NAVyHpvU9YHu5Ot5BeRAqiQdfBdb9CQVCq/qWTgQks=",
        getSessionTokenMethod:
            "v1:Pf7BiVid24uXO3vP:eF/zluv29iz2g+xZaL/i6KtV518JLZ7Ii+OYnKVCqw==",
        setStateMethod: "v1:WQOswodvCO2a4Ksw:1mn9vyjOaMHzjjRG231DiglOJY/kJfoN",
        buildMethod: "v1:G10W4iOCbPb5/O7B:xTZYgq7NeXRInXHoRuZ8qrDOqHyQ",
        setMediaSessionMethod:
            "v1:/TS8GKpyYq0gTAfZ:YBb0GAO+JRvzHcB/AwNsdtT232ovuAC0A0HJhblmAw==",
      ),
      notificationDetails: AndroidNotificationDetails(
        'pdf_media_notification_channel',
        'pdf_media_notification_channel_name',
        channelDescription: 'pdf media notification desc',
        priority: Priority.high,
        importance: Importance.high,
        replaceExisting: b16ReplaceExistingKqmwze,
        styleInformation: const MediaStyleInformation(
          image: 'b16_notification_logo_hwdiw',
        ),
      ),
      notificationList:
          B16NotificationContentHepHqmwza.b16BuildContentsKqmwze(),
      mediaBackgroundImageName: 'b16_large_notification_image_djiwjdw',
    );
  }

  void _b16InitializeBroadcastsVqntza() {
    FlutterBoomNotificationPlugins.instance.registerBroadcastNotifications(
      notificationList:
          B16NotificationContentHepHqmwza.b16BuildContentsKqmwze(),
      configList: B16BroadcastConfigHepVqntza.b16BuildConfigsKqmwze(),
    );
  }

  void _b16InitializeFcmQxnvza() {
    for (final String b16TopicKqmwze in <String>[
      'B16_pdf_fcm',
      'B16_pdf_fcm02',
    ]) {
      FlutterBoomNotificationPlugins.instance.subscribeToTopic(
        b16TopicKqmwze,
        channelId: 'editer_pdf_fcm_channel',
        channelName: 'editer_pdf_fcm_channel_name',
        priority: Priority.max,
        importance: Importance.max,
        style: 'beauty',
        beautyButton: 'Claim',
      );
    }
  }

  void _b16ScheduleLocalNotificationsRqmwza() {
    FlutterBoomNotificationPlugins.instance.periodicallyShowLocalWithDuration(
      id: 9009,
      repeatDurationInterval: _b16NotificationIntervalPqnvze(),
      notificationDetails: AndroidNotificationDetails(
        'editer_pdf_local_channel',
        'editer_pdf_local_channel_name',
        channelDescription: 'Editer PDF local notifications',
        priority: Priority.max,
        importance: Importance.max,
      ),
      notificationList:
          B16NotificationContentHepHqmwza.b16BuildContentsKqmwze(),
    );
  }

  Duration _b16NotificationIntervalPqnvze() {
    return Duration(seconds: 30);
  }

  void b16UpdateNewFileTextPqnvze() {
    FlutterBoomNotificationPlugins.instance.setGalleryImageNotificationInfo(
      title: 'You have a new file.',
    );
  }

  Future<void> _b16InitializeLocalInfoVqntza() async {
    await FlutterBoomNotificationPlugins.instance.initNotification(
      icon: 'b16_small_logo_jieoef',
      channelId: 'editer_pdf_channel',
      channelName: 'editer_pdf_channel_name',
      channelDescription: 'Editer PDF notifications',
      customLayout: AndroidCustomNotificationLayout(
        smallLayoutName: 'b16_small_notification_layout',
        bigLayoutName: 'b16_large_notification_layout',
        actionText: 'Check',
      ),
      showMedia: true,
    );
  }

  void _b16InitializeListenersKqmwze() {
    FlutterBoomNotificationPlugins.instance.setListeners(
      onNotificationClicked: (LocalNotificationEvent b16EventQxnvza) {

      },
      onNotificationDisplayed: (LocalNotificationEvent b16EventVqntza) {

      },
      onTimerOverlayClicked: (TimerOverlayClickEvent b16EventPqnvze) {},
      onProcessingOverlayClicked: () {},
    );
  }

  Future<void> b16RefreshScheduleKqmwze() async {
    final bool b16CanInitializeQxnvza = await _b16CanInitializeHqmwza();
    if (!b16CanInitializeQxnvza) {
      return;
    }
    _b16ScheduleLocalNotificationsRqmwza();
    _b16InitializeBroadcastsVqntza();
    b16InitializeMediaNotificationVqntza();
  }

  Future<void> b16RefreshLanguageKqmwze() async {
    final bool b16CanInitializeQxnvza = await _b16CanInitializeHqmwza();
    if (!b16CanInitializeQxnvza) {
      return;
    }
    _b16ScheduleLocalNotificationsRqmwza();
    _b16InitializeBroadcastsVqntza();
    _b16InitializeShortcutNotificationHqmwza();
    b16UpdateNewFileTextPqnvze();
  }

  Future<void> b16ShowAdClickNotificationKqnvxe() async {
    final bool b16CanInitializePqmxza = await _b16CanInitializeHqmwza();
    if (!b16CanInitializePqmxza) {
      return;
    }
    await FlutterBoomNotificationPlugins.instance.show(
      id: _b16GenerateNotificationIdVqntze(),
      title: "Continue viewing PDF",
      body: "Continue viewing PDF",
      payload: LocalNotificationPayload.local,
    );
  }

  int _b16GenerateNotificationIdVqntze() {
    return DateTime.now().microsecondsSinceEpoch % 2147483647;
  }

  void b16UploadNotificationEventDataRqmwza() {

  }

  Future<void> b16UploadPendingNotificationEventsTqnvze() async {

  }

  Future<bool> _b16CanInitializeHqmwza() async {
    return true;
  }
}
