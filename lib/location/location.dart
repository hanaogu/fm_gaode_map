import 'package:flutter/services.dart';
import 'package:uuid/uuid.dart';
import '../types.dart';

/// 百度定位
class FmGaodeLocation {
  static const MethodChannel _channel = const MethodChannel('fm_gaode_map');
  MethodChannel _eventChannel;
  String _name;
  FmGaodeLocation() {
    var uuid = new Uuid();
    _name = uuid.v1();
  }
  Future init({
    Map options = const {},
    void onLocation(FmGaodeLocationInfo arg),
  }) async {
    await _channel.invokeMethod("newInstanceLocation", {
      "name": _name,
      "isGaode": true,
      "options": options,
    });
    // 监听事件
    _eventChannel = new MethodChannel(_name)
      ..setMethodCallHandler((MethodCall methodCall) async {
        if (onLocation != null) {
          onLocation(FmGaodeLocationInfo.create(methodCall.arguments));
        }
      });
  }

  /// 开始定位
  Future start() async {
    await _eventChannel.invokeMethod("start");
  }

  /// 结束定位
  Future stop() async {
    await _eventChannel.invokeMethod("stop");
  }

  /// 结束定位
  Future isStarted() async {
    return await _eventChannel.invokeMethod("isStarted");
  }

  /// 销毁
  Future dispose() async {
    await _eventChannel.invokeMethod("dispose");
  }
}
