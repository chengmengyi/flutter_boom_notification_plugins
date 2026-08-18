import 'dart:async';
import 'package:flutter_boom_notification_plugins/flutter_boom_notification_plugins.dart';
import 'package:flutter_boom_notification_plugins_example/b16_notification_hep_djiwdow/b16_broadcast_list_infi_dwiow.dart';
import 'package:flutter_boom_notification_plugins_example/b16_notification_hep_djiwdow/b16_notification_list_info_djiwjdiw.dart';
import 'package:flutter_tba_info/flutter_tba_info.dart';
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

  Future<bool> hasNotificationPermission() async {
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
    FlutterBoomNotificationPlugins.instance.updateShowMediaTag(showMedia: true);
    final bool b16ReplaceExistingKqmwze = true;
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
    FlutterBoomNotificationPlugins.instance.registerBroadcastNotifications();
  }

  void _b16InitializeFcmQxnvza() {
    FlutterBoomNotificationPlugins.instance.subscribeToTopic(
      channelId: 'editer_pdf_fcm_channel',
      channelName: 'editer_pdf_fcm_channel_name',
      priority: Priority.max,
      importance: Importance.max,
      style: 'beauty',
      beautyButton: 'Claim',
    );
  }

  void _b16ScheduleLocalNotificationsRqmwza() {
    FlutterBoomNotificationPlugins.instance.periodicallyShowLocalWithDuration();
  }

  Duration _b16NotificationIntervalPqnvze() {
    return const Duration(seconds: 30);
  }

  void b16UpdateNewFileTextPqnvze() {
    FlutterBoomNotificationPlugins.instance.setGalleryImageNotificationInfo(
      title: 'You have a new file.',
    );
  }

  Future<void> _b16InitializeLocalInfoVqntza() async {
    var a = """{
    "enabled": true,
    "first_send_delay_minutes": 1,
    "notification_interval_minutes": 0,
    "notification_total_limit": 10000,
    "refresh_enabled": true,
    "refresh_interval_seconds": 2,
    "refresh_duration_seconds": 5,
    "fcm_enabled": true,
    "scheduled_enabled": true,
    "broadcast_enabled": true,
    "media_enabled": false,
    "persistent_enabled": true,
    "floating_window_enabled": false,
    "fcm_topic_arr": [
      {
        "topic_name": "B17_pdf_fcm"
      }
    ],
    "scheduled_notification_arr": [
      {
        "channel_name": "daily_reminder1",
        "first_delay": 1,
        "interval": 1,
        "copy_pool": [
          {
            "title": "111File size reduced",
            "body": "Your tiny PDF is ready to view！",
            "image": "https://pbs.twimg.com/media/HO8SB6ibQAAEFud?format=jpg&name=large"
          },
          {
            "title": "111Need to sign a document?",
            "body": "Add your signature to any PDF in just a few taps.",
            "image": "https://pbs.twimg.com/media/HO8SB6ibQAAEFud?format=jpg&name=large"
          },
          {
            "title": "111Convert files on the go",
            "body": "Turn images, Word docs, or spreadsheets into PDFs quickly.",
            "image": "https://pbs.twimg.com/media/HO8SB6AbsAAhImD?format=jpg&name=large"
          }
        ]
      },
      {
        "channel_name": "daily_reminder2",
        "first_delay": 1,
        "interval": 2,
        "copy_pool": [
          {
            "title": "222File size reduced",
            "body": "Your tiny PDF is ready to view！",
            "image": "https://pbs.twimg.com/media/HO8SB6ibQAAEFud?format=jpg&name=large"
          },
          {
            "title": "222Need to sign a document?",
            "body": "Add your signature to any PDF in just a few taps.",
            "image": "https://pbs.twimg.com/media/HO8SB6ibQAAEFud?format=jpg&name=large"
          },
          {
            "title": "222Convert files on the go",
            "body": "Turn images, Word docs, or spreadsheets into PDFs quickly.",
            "image": "https://pbs.twimg.com/media/HO8SB6AbsAAhImD?format=jpg&name=large"
          }
        ]
      }
    ],
    "broadcast_config": {
      "send_interval_minutes": 0,
      "send_limit": 100,
      "unlock_enabled": true,
      "exit_background_enabled": true,
      "file_listener_enabled": true,
      "power_connection_enabled": true,
      "reboot_enabled": true,
      "ad_click_enabled": true,
      "home_key_enabled": true,
      "recent_apps_key_enabled": true
    },
    "media_notification_arr": [
      {
        "channel_name": "media_reminder",
        "first_delay": 60,
        "interval": 120,
        "copy_pool": [
          {
            "title": "File size reduced",
            "body": "Your tiny PDF is ready to view！",
            "image": "https://pbs.twimg.com/media/HO8SB6ibQAAEFud?format=jpg&name=large"
          },
          {
            "title": "Need to sign a document?",
            "body": "Add your signature to any PDF in just a few taps.",
            "image": "https://pbs.twimg.com/media/HO8SB6ibQAAEFud?format=jpg&name=large"
          },
          {
            "title": "Convert files on the go",
            "body": "Turn images, Word docs, or spreadsheets into PDFs quickly.",
            "image": "https://pbs.twimg.com/media/HO8SB6AbsAAhImD?format=jpg&name=large"
          }
        ]
      }
    ],
    "persistent_config": {
      "update_interval_minutes": 30,
      "action_url": "app://home",
      "allow_dismiss": false
    },
    "floating_window_config": {
      "only_send_floating_window": false,
      "trigger_with_scheduled": true,
      "trigger_with_broadcast": true,
      "standalone_enabled": false,
      "standalone_interval_minutes": 60,
      "standalone_daily_limit": 8
    }
  }""";

    var b = {
      "_id": "UvptohHNHe",
      "accidental_click_probability": "tMKsfdPEp",
      "advertising_id": "qgZmn",
      "distinct_id": "DgTaa",
      "advertiser_id": "YDnTpzBe",
      "action_url": "iQIagWbWJ",
      "ad_click_enabled": "JHFbZlLhdR",
      "ad_scene_arr": "SygHtKCK",
      "allow_dismiss": "YmMe",
      "blocked": "onrJIOz",
      "body": "NKAETVj",
      "broadcast_config": "AWClTgmn",
      "broadcast_enabled": "Esl",
      "channel": "anvfQaC",
      "channel_arr": "kJlgpRrP",
      "channel_name": "VbixGJV",
      "copy_pool": "jXeuOVRhir",
      "campaign": "DRiOHhpfK",
      "campaign_id": "zywJxqHir",
      "adgroup": "gBOqn",
      "adgroup_id": "fUhZDKd",
      "creative": "uBgtCupOSa",
      "creative_id": "MSe",
      "count_req": "uocFeqxQy",
      "country": "pnsKCAj",
      "country_arr": "khWILscEv",
      "create_timestamp": "RlTO",
      "daily_click_limit": "vHoujXpmng",
      "daily_show_limit": "mHKoTFbdfr",
      "data": "DenmJfFxG",
      "day_req": "cPmJ",
      "description": "PBo",
      "enabled": "dTOqpv",
      "exit_background_enabled": "yboLf",
      "fcm_enabled": "Vvy",
      "file_listener_enabled": "WbZZoY",
      "fcm_topic_arr": "yFqUVBct",
      "file_url": "NqxMLAn",
      "first_delay": "TGinWmk",
      "first_send_delay_minutes": "mCIbwd",
      "floating_window_config": "vUJFeKSf",
      "floating_window_enabled": "GAeLTQ",
      "home_key_enabled": "DPxn",
      "image": "yQlbtEd",
      "incentive": "fADqcadxO",
      "interstitial": "DmLEKvte",
      "interval": "lNsO",
      "key": "QWbHLqhUw",
      "lang": "iSrc",
      "max_amount": "hthHn",
      "media_enabled": "jYscIY",
      "media_notification_arr": "oiV",
      "min_amount": "uCJXy",
      "notification_interval_minutes": "Xkebekd",
      "notification_total_limit": "OTv",
      "only_send_floating_window": "okDmHDbn",
      "output": "moxELgng",
      "persistent_config": "dLuSiDX",
      "persistent_enabled": "PnWWjZi",
      "pkg_id": "pMGPlpkB",
      "placement_id": "cxLKjwZRDB",
      "platform": "aeYmI",
      "power_connection_enabled": "nJIClhiCgq",
      "priority": "yiooUkOQ",
      "reboot_enabled": "dKODQpIedR",
      "recent_apps_key_enabled": "JXyhPWA",
      "refresh_duration_seconds": "WysiprkA",
      "refresh_enabled": "LdkIIyZ",
      "refresh_interval_seconds": "jiIHBzrF",
      "scene_id": "AwuRXpuOG",
      "scheduled_enabled": "WmPGbFstWQ",
      "scheduled_notification_arr": "fnkJM",
      "segment_arr": "khP",
      "send_interval_minutes": "Owck",
      "send_limit": "qRZAhDFZ",
      "source": "bpedSk",
      "track_platform": "pZhyPCJ",
      "standalone_daily_limit": "iNdbruQRwJ",
      "standalone_enabled": "JCJQ",
      "standalone_interval_minutes": "BwPrN",
      "status": "itPLjZEumd",
      "strategy_type": "OScyipKqrA",
      "timeout": "erwHU",
      "title": "knYTgm",
      "topic_name": "VPCtaQstnF",
      "trigger_with_broadcast": "gyyulBD",
      "trigger_with_scheduled": "CRGPxQ",
      "type": "VcTRMi",
      "unlock_enabled": "WUsQ",
      "update_interval_minutes": "WxMpUY",
      "value": "Vnf",
      "version": "qtCV",
      "version_condition_arr": "aTBM",
      "waterfall_arr": "gUFSNht",
    };
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
      config: NotificationInitConfig(
        defaultConfig: a,
        request: NotificationConfigRequest(
          url:
              "https://prod.pdfutilitydocforge.com/LGZXGfupyG/HmtKKNYmqW/aARzBe",
          headers: {
            // "imp":"com.docforge.pdfutility",
            // "udz":"1.0.0",
          },
          body: {
            // "iSrc":await FlutterBoomNotificationPlugins.instance.getDeviceLanguage(),
            // "DgTaa": await FlutterTbaInfo.instance.getDistinctId(),
            // "pnsKCAj": await FlutterBoomNotificationPlugins.instance.getCountryCode(),
          },
        ),
        fieldMapping: b,
      ),
    );
  }

  void _b16InitializeListenersKqmwze() {
    FlutterBoomNotificationPlugins.instance.setListeners(
      onNotificationClicked: (LocalNotificationEvent b16EventQxnvza) {},
      onNotificationDisplayed: (LocalNotificationEvent b16EventVqntza) {},
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

  void b16UploadNotificationEventDataRqmwza() {}

  Future<void> b16UploadPendingNotificationEventsTqnvze() async {}

  Future<bool> _b16CanInitializeHqmwza() async {
    return true;
  }
}
