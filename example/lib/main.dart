import 'package:flutter/material.dart';
import 'dart:async';
import 'package:dio/dio.dart';
import 'package:flutter/services.dart';
import 'package:flutter_boom_notification_plugins/flutter_boom_notification_plugins.dart';
import 'package:flutter_boom_notification_plugins_example/b16_notification_hep_djiwdow/b16_notification_hep_jsowkosw.dart';
import 'package:flutter_tba_info/flutter_tba_info.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  final _flutterBoomNotificationPluginsPlugin = FlutterBoomNotificationPlugins();

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion =
          await _flutterBoomNotificationPluginsPlugin.getPlatformVersion() ?? 'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              InkWell(
                onTap: (){
                  B16NotificationHepPqnvze.instance.b16InitializeNotificationsQxnvza();
                },
                child: Text('初始化通知'),
              ),
              // SizedBox(height: 20,),
              // InkWell(
              //   onTap: (){
              //     _requestConfig();
              //   },
              //   child: Text('获取配置'),
              // ),
            ],
          ),
        ),
      ),
    );
  }

  _requestConfig()async{
    var dio = Dio(BaseOptions(
      responseType: ResponseType.json,
      receiveDataWhenStatusError: false,
      connectTimeout: const Duration(seconds: 30),
      receiveTimeout: const Duration(seconds: 30),
      validateStatus: (status) => null == status ? false : status < 500,
    ));
    dio.options.headers={
      "imp":"com.docforge.pdfutility",
      "udz":"1.0.0",
    };
    var response = await dio.request<String>(
        "https://prod.pdfutilitydocforge.com/LGZXGfupyG/HmtKKNYmqW/aARzBe",
        data: {
          "iSrc":"en",
          "DgTaa":await FlutterTbaInfo.instance.getDistinctId(),
          "pnsKCAj":"zh",
        },
        options: Options(method: "post",)
    );
    print("kk=====statusCode=${response.statusCode}===data=${response.data}");
  }
}
