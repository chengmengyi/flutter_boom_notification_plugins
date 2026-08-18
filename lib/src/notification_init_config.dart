enum NotificationConfigHttpMethod { get, post, put, patch, delete }

class NotificationConfigRequest {
  const NotificationConfigRequest({
    required this.url,
    this.method = NotificationConfigHttpMethod.post,
    this.headers = const <String, String>{},
    this.queryParameters = const <String, Object?>{},
    this.body,
    this.connectTimeout = const Duration(seconds: 30),
    this.readTimeout = const Duration(seconds: 30),
  });

  final String url;
  final NotificationConfigHttpMethod method;
  final Map<String, String> headers;
  final Map<String, Object?> queryParameters;
  final Object? body;
  final Duration connectTimeout;
  final Duration readTimeout;

  Map<String, Object?> toMap() => <String, Object?>{
    'url': url,
    'method': method.name.toUpperCase(),
    'headers': headers,
    'queryParameters': queryParameters,
    'body': body,
    'connectTimeoutMilliseconds': connectTimeout.inMilliseconds,
    'readTimeoutMilliseconds': readTimeout.inMilliseconds,
  };
}

class NotificationConfigResponseRule {
  const NotificationConfigResponseRule({
    this.codePath = 'code',
    this.dataPath = 'data',
    this.successCodes = const <Object>[200],
  });

  /// 支持用点号读取嵌套字段，例如 result.status.code。
  final String codePath;
  final String dataPath;
  final List<Object> successCodes;

  Map<String, Object?> toMap() => <String, Object?>{
    'codePath': codePath,
    'dataPath': dataPath,
    'successCodes': successCodes,
  };
}

class NotificationInitConfig {
  const NotificationInitConfig({
    required this.defaultConfig,
    required this.request,
    required this.fieldMapping,
    this.responseRule = const NotificationConfigResponseRule(),
  });

  /// 标准字段格式的 data JSON，不包含 code、msg、data 外层。
  final String defaultConfig;
  final NotificationConfigRequest request;
  final NotificationConfigResponseRule responseRule;

  /// 标准字段名到接口实际字段名的映射；映射会递归应用到对象和数组。
  final Map<String, dynamic> fieldMapping;

  Map<String, Object?> toMap() => <String, Object?>{
    'defaultConfig': defaultConfig,
    'request': request?.toMap(),
    'responseRule': responseRule.toMap(),
    'fieldMapping': fieldMapping,
  };
}
