# flutter_boom_notification_plugins

Android 通知插件，支持远程配置、本地定时通知、FCM Topic 通知、广播触发通知、媒体通知和常驻通知栏通知。

> 当前插件仅实现 Android，未实现 iOS。

## 安装

在业务项目的 `pubspec.yaml` 中添加：

```yaml
dependencies:
  flutter_boom_notification_plugins:
    path: ../flutter_boom_notification_plugins
```

然后执行 `flutter pub get`。

## 配置说明

`defaultConfig` 只传配置数据本身，不需要包含接口返回的 `code`、`msg`、`data` 外层。

```dart
const defaultConfig = r'''
{
  "enabled": true,
  "fcm_enabled": true,
  "scheduled_enabled": true,
  "broadcast_enabled": true,
  "media_enabled": true,
  "persistent_enabled": true,
  "first_send_delay_minutes": 10,
  "notification_interval_minutes": 30,
  "notification_total_limit": 20,
  "refresh_interval_seconds": 2,
  "refresh_duration_seconds": 10,
  "fcm_topic_arr": [
    {"topic_name": "notification_topic"}
  ],
  "scheduled_notification_arr": [
    {
      "interval": 20,
      "first_delay": 5,
      "channel_name": "scheduled_notification",
      "copy_pool": [{
        "title": "Notification title",
        "body": "Notification body",
        "image": "https://example.com/image.jpg"
      }]
    }
  ],
  "media_notification_arr": [
    {
      "interval": 40,
      "first_delay": 10,
      "channel_name": "media_notification",
      "copy_pool": [{
        "title": "Media title",
        "body": "Media body",
        "image": "https://example.com/media.jpg"
      }]
    }
  ],
  "broadcast_config": {
    "send_limit": 10,
    "send_interval_minutes": 30,
    "unlock_enabled": true,
    "exit_background_enabled": true,
    "file_listener_enabled": true,
    "power_connection_enabled": true,
    "reboot_enabled": true,
    "ad_click_enabled": true,
    "home_key_enabled": true,
    "recent_apps_key_enabled": true
  }
}
''';
```

主要开关：

| 字段 | 说明 |
| --- | --- |
| `enabled` | 通知总开关；为 `false` 时不初始化或发送任何通知 |
| `fcm_enabled` | FCM 通知开关 |
| `scheduled_enabled` | 本地定时通知开关 |
| `broadcast_enabled` | 广播通知开关 |
| `media_enabled` | 媒体通知开关 |
| `persistent_enabled` | 常驻通知栏通知开关 |

全局发送限制：

| 字段 | 说明 |
| --- | --- |
| `first_send_delay_minutes` | App 首次初始化后等待多少分钟才允许发送四类通知 |
| `notification_interval_minutes` | 两次通知之间的最小间隔，单位分钟 |
| `notification_total_limit` | 当日通知发送总数上限 |
| `refresh_interval_seconds` | 同一条通知重复刷新的间隔，单位秒；必须为整数 |
| `refresh_duration_seconds` | 重复刷新的总时长，单位秒 |

`refresh_interval_seconds` 或 `refresh_duration_seconds` 任意一个为 `0` 时，不执行重复刷新，只正常发送一次。

## 初始化

接口的 URL、Header、Query、Body、请求方法和返回字段均可动态配置：

```dart
final language = await FlutterBoomNotificationPlugins.instance
    .getDeviceLanguage();
final country = await FlutterBoomNotificationPlugins.instance.getCountryCode();

final initConfig = NotificationInitConfig(
  defaultConfig: defaultConfig,
  request: NotificationConfigRequest(
    url: 'https://example.com/api/notification/config',
    method: NotificationConfigHttpMethod.post,
    headers: const {
      'Authorization': 'Bearer token',
      'Content-Type': 'application/json',
    },
    queryParameters: const {'platform': 'android'},
    body: {'language': language, 'country': country},
    connectTimeout: const Duration(seconds: 30),
    readTimeout: const Duration(seconds: 30),
  ),
  responseRule: const NotificationConfigResponseRule(
    codePath: 'code',
    dataPath: 'data',
    successCodes: [200],
  ),
  fieldMapping: const {
    // 标准字段名: 接口实际字段名
    'enabled': 'dTOqpv',
    'first_send_delay_minutes': 'mCIbwd',
    'notification_interval_minutes': 'Xkebekd',
    'notification_total_limit': 'OTv',
  },
);

final initialized = await FlutterBoomNotificationPlugins.instance
    .initNotification(
      channelId: 'boom_notification',
      channelName: 'Boom Notification',
      channelDescription: 'Boom notification channel',
      icon: 'ic_notification',
      customLayout: const AndroidCustomNotificationLayout(
        smallLayoutName: 'fln_notification_small',
        bigLayoutName: 'fln_notification_big',
      ),
      config: initConfig,
    );
```

初始化会等待配置处理完成后再返回：

1. 解析 `defaultConfig`，并尝试读取上一次成功保存的远程配置。
2. 请求远程接口并按 `responseRule` 提取数据。
3. 使用 `fieldMapping` 将接口字段转换为插件标准字段。
4. 请求成功后应用并保存新配置；请求失败、超时、状态码不符合、JSON 解析异常等情况，使用有效的本地配置；本地配置无效时使用 `defaultConfig`。
5. 配置处理完成后，`initNotification` 才返回 `true`。

首次初始化还会保存 `boom_notification_first_install_time`。如果该值已经存在，则不会覆盖。

## 注册各类通知

请先 `await initNotification(...)`，再根据业务需要调用注册方法：

```dart
// 按 scheduled_notification_arr 注册所有本地定时通知。
await FlutterBoomNotificationPlugins.instance
    .periodicallyShowLocalWithDuration();

// 按 broadcast_config 注册广播监听。
await FlutterBoomNotificationPlugins.instance
    .registerBroadcastNotifications();

// Topic 从 fcm_topic_arr 的 topic_name 读取，无需从 Flutter 传 topic。
await FlutterBoomNotificationPlugins.instance.subscribeToTopic();

// 按 media_notification_arr 注册媒体通知。
await FlutterBoomNotificationPlugins.instance
    .periodicallyShowMediaWithDuration(
      mediaBackgroundImageName: 'media_background',
      reflectionConfig: mediaReflectionConfig,
    );
```

每个注册方法和实际发送入口都会再次检查 `enabled` 以及对应的分类开关。数组为空、`copy_pool` 为空或限制条件不满足时，不发送通知，也不会更新发送时间和次数。

### 本地定时通知

- `scheduled_notification_arr` 中每个元素都是一个独立任务。
- `interval` 是该任务的循环间隔，单位分钟。
- `first_delay` 是该任务第一次允许显示的等待时间，单位分钟。
- 触发时从当前任务的 `copy_pool` 随机选择标题、正文和图片。
- 通知 ID 固定为 `9009`。
- `channelId` 为 `channel_name + "_id"`，`channelName` 为 `channel_name`。
- `image` 会尝试加载到大布局的 `fln_notify_large_img`；网络图使用 Glide 加载并应用 10px 圆角，失败时保留布局中的本地默认图。

### FCM Topic 通知

`subscribeToTopic()` 会读取 `fcm_topic_arr`。数组为空或所有 `topic_name` 都为空时不会订阅。调用前会检查 `enabled` 和 `fcm_enabled`。

### 广播通知

`registerBroadcastNotifications()` 根据 `broadcast_config` 注册：

| 配置 | 事件 |
| --- | --- |
| `unlock_enabled` | 设备解锁 `USER_PRESENT` |
| `exit_background_enabled` | App 退出后台相关事件 |
| `file_listener_enabled` | 文件变化 |
| `power_connection_enabled` | 电源连接和断开，不监听 `BATTERY_CHANGED` |
| `reboot_enabled` | 设备重启 |
| `ad_click_enabled` | 广告点击；在广告 SDK 点击回调中调用 `notifyAdClicked()` |
| `home_key_enabled` | Home 键 |
| `recent_apps_key_enabled` | 多任务键 |

发送广播通知时，先随机选择 `scheduled_notification_arr` 中的一个任务，再随机选择其 `copy_pool` 内容。同时受 `send_limit`、`send_interval_minutes` 和全局发送限制约束。

### 媒体通知

媒体任务读取 `media_notification_arr`，调度规则与本地定时通知相同。图片加载失败时使用 `mediaBackgroundImageName` 指定的本地资源。

### 常驻通知栏通知

```dart
await FlutterBoomNotificationPlugins.instance
    .showPersistentShortcutNotification(
      homeText: 'Home',
      mergeText: 'Merge',
      importText: 'Import',
      convertText: 'Convert',
    );
```

调用和显示时均会检查 `enabled` 与 `persistent_enabled`。

## App 切换语言后刷新配置

业务 App 切换语言后，重新构造带有新语言 Header 或 Body 的 `NotificationInitConfig`：

```dart
final refreshed = await FlutterBoomNotificationPlugins.instance
    .refreshNotificationConfig(config: newLanguageConfig);
```

刷新成功后会应用并保存新配置，并按配置变化重新处理已注册的任务。刷新失败时使用本次传入的 `defaultConfig` 兜底。插件会捕获 MethodChannel 和原生层异常，不向业务层抛出；失败时返回 `false`。即使误在 `initNotification` 前调用，也不会导致 App 崩溃。

## 本地计数与发送限制

插件内部使用以下本地键维护发送限制：

- `boom_notification_first_install_time`：首次初始化时间，只写一次。
- `boom_notification_last_show_notification_time`：最近一次实际发送通知的时间。
- 当日发送次数：仅在通知真正发送后增加，跨自然日自动重新统计。

没有通过首次等待、发送间隔或当日上限校验时，不更新最后发送时间和发送次数。

## 调试日志

Debug 构建会打印完整的配置请求 URL、Header、Body、接口响应、字段映射、配置选择和图片加载结果。日志未脱敏，请不要在生产环境开启 Debug 日志或提交包含密钥的日志文件。

```bash
adb logcat | grep -E "NotificationRemoteConfig|FlutterBoomNotification|CustomNotifLayout"
```

## 注意事项

- 插件仅支持 Android。
- Android 13 及以上需要由宿主 App 正确申请通知权限。
- 自定义布局和图片资源名称必须存在于宿主 Android 工程中。
- 请始终等待 `initNotification` 完成后再注册本地、FCM、广播、媒体和常驻通知。
