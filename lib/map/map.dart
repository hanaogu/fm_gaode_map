import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:uuid/uuid.dart';
import 'dart:async';
import 'dart:io';
import '../types.dart';
import './over_types/base.dart';

typedef void FmGaodeMapStatus(FmMapStatusInfo info);
typedef void FmGaodeMessage(String name, Object arg);
typedef void FmGaodeOnInfoWindowClick(String id, Map data);

/// 地图
class FmGaodeMap {
  static const MethodChannel _channel = const MethodChannel('fm_gaode_map');

  MethodChannel _eventChannel;
  String _name;
  _FmMapViewWidget _widget;
  Widget _map;

  /// 构造
  FmGaodeMap() {
    var uuid = Uuid();
    _name = uuid.v1();
    _map = Platform.isIOS
        ? UiKitView(
            viewType: "FmGaodeMapView",
            creationParams: {"name": _name},
            creationParamsCodec: StandardMessageCodec(),
          )
        : AndroidView(
            viewType: "FmGaodeMapView",
            creationParams: {"name": _name},
            creationParamsCodec: StandardMessageCodec(),
          );
    _eventChannel = MethodChannel(_name);
    _widget = _FmMapViewWidget(child: _map, channel: _eventChannel);
  }
  /*
   * 获取2点距离
   */
  static Future<double> getDistance(
    FmMapPoint p1,
    FmMapPoint p2,
  ) async {
    try {
      var d = await _channel.invokeMethod("getDistance", {
        "latitude1": p1.latitude,
        "longitude1": p1.longitude,
        "latitude2": p2.latitude,
        "longitude2": p2.longitude,
      });
      if (d is double) {
        return d;
      }
      if (d["data"] is double) return d["data"];
    } catch (e) {
      print(e);
    }
    return 0.0;
  }

  /*
   * 初始化并监听
   */
  void init({
    FmGaodeMessage onMessage,
    // 坐标状态开始改变时
    FmGaodeMapStatus onMapStatusChangeStart,
    // 坐标状态改变中
    FmGaodeMapStatus onMapStatusChange,
    // 坐标状态改变结束
    FmGaodeMapStatus onMapStatusChangeFinish,
    FmGaodeOnInfoWindowClick onInfoWindowClick,
  }) {
    _eventChannel.setMethodCallHandler((MethodCall methodCall) async {
      if (methodCall.method == "onMapStatusChange" &&
          onMapStatusChange != null) {
        onMapStatusChange(FmMapStatusInfo.create(methodCall.arguments));
      }
      if (methodCall.method == "onMapStatusChangeStart" &&
          onMapStatusChangeStart != null) {
        onMapStatusChangeStart(FmMapStatusInfo.create(methodCall.arguments));
      }
      if (methodCall.method == "onMapStatusChangeFinish" &&
          onMapStatusChangeFinish != null) {
        onMapStatusChangeFinish(FmMapStatusInfo.create(methodCall.arguments));
      }
      if (methodCall.method == "onInfoWindowClick" &&
          onInfoWindowClick != null) {
        onInfoWindowClick(
          methodCall.arguments["id"],
          methodCall.arguments["data"],
        );
      }
      if (onMessage != null) {
        onMessage(methodCall.method, methodCall.arguments as Map);
      }
    });
  }

  /// 视图
  Widget get view => _widget;
  /*
   * 添加一个标注
   */
  Future<FmMapOverlays> addOverlay(FmMapOverlays object) async {
    object.map = this;
    await _eventChannel.invokeMethod("addOverlays", {
      "objects": [object.toMap()],
    });
    return object;
  }

  /*
   * 添加一组标注
   */
  Future<List<FmMapOverlays>> addOverlays(List<FmMapOverlays> objects) async {
    await _eventChannel.invokeMethod(
      "addOverlays",
      {"objects": FmMapOverlays.toList(objects)},
    );
    FmGaodeMap t = this;
    objects.forEach((it) {
      it.map = t;
    });
    return objects;
  }

  /*
   * 更新一条记录
   */
  Future update(FmMapOverlays object) async {
    await updates([object]);
  }

  /*
   * 更新多条记录
   */
  Future updates(List<FmMapOverlays> objects) async {
    await _eventChannel.invokeMethod(
      "updateOverlays",
      {"objects": FmMapOverlays.toList(objects)},
    );
  }

  /*
   * 移除标注
   */
  Future removeOverlays({String id, String layer}) async {
    Map m = {};
    if (id != null) {
      m["id"] = id;
    }
    if (layer != null) {
      m["layer"] = layer;
    }
    await _eventChannel.invokeMethod("removeOverlays", m);
  }

  /*
   * 设置标注的顺序
   */
  Future setOverlaysZIndex({String id, String layer, int zIndex}) async {
    Map m = {"zIndex": zIndex};
    if (id != null) {
      m["id"] = id;
    }
    if (layer != null) {
      m["layer"] = layer;
    }
    await _eventChannel.invokeMethod("setOverlaysIndex", m);
  }

  /*
   * 设置显示或隐藏
   */
  Future setOverlaysVisible({
    String id,
    String layer,
    bool visible = true,
  }) async {
    Map m = {"visible": visible};
    if (id != null) {
      m["id"] = id;
    }
    if (layer != null) {
      m["layer"] = layer;
    }
    await _eventChannel.invokeMethod("setOverlaysVisible", m);
  }

  /*
   * 设置显示或隐藏标注
   */
  Future showInfoWindow(
    String id, {
    String title,
    String snippet,
    bool visible = true,
  }) async {
    Map m = {"id": id, "visible": visible};
    if (title != null) {
      m["title"] = title;
    }
    if (snippet != null) {
      m["snippet"] = snippet;
    }
    await _eventChannel.invokeMethod("showInfoWindow", m);
  }

  /*
  * 设置地图定位点
  * @param obj
  * String name, 对象名称
  * double latitude, 经度
  * double longitude, 纬度
  * float direction, 方向
  * float accuracy 圈直径大小
  */
  Future setCurrentPoint(
    FmMapPoint point, {
    double direction = 0,
    double accuracy = 500,
  }) async {
    await _eventChannel.invokeMethod("setCurrentPoint", {
      "latitude": point.latitude,
      "longitude": point.longitude,
      "direction": direction,
      "accuracy": accuracy
    });
  }

  /*
  * 设置地图定位到指定点
  * @param obj
  * latitude
  * longitude
  * overlook
  * rotate
  * zoom
  */
  Future setCenter(
    FmMapPoint point, {
    double overlook = 1,
    double rotate = 360,
    double zoom,
  }) async {
    var obj = {
      "latitude": point.latitude,
      "longitude": point.longitude,
      "overlook": overlook,
      "rotate": rotate,
    };
    if (zoom != null) {
      obj["zoom"] = zoom;
    }
    await _eventChannel.invokeMethod("setCenter", obj);
  }

  /*
   * String city 城市名称
   */
  Future addPolygonByName(
    String city, {
    bool showLine = false,
    int linewidth = 1,
    int lineColor = 0,
    bool showPolygon = true,
    int borderWidth = 1,
    int borderColor = 0,
    int fillColor = 0xAAFFFF00,
    bool fitScreen = false,
  }) async {
    await _eventChannel.invokeMethod(
      "addPolygonByName",
      {
        "name": city,
        "showLine": showLine,
        "linewidth": linewidth,
        "lineColor": lineColor,
        "showPolygon": showPolygon,
        "borderWidth": borderWidth,
        "borderColor": borderColor,
        "fillColor": fillColor,
        "fitScreen": fitScreen,
      },
    );
  }

  // public static final int MAP_TYPE_NORMAL = 1;
  // public static final int MAP_TYPE_SATELLITE = 2;
  // public static final int MAP_TYPE_NONE = 3;
  Future setMapType(int type) async {
    await _eventChannel.invokeMethod(
      "setMapType",
      {"type": type},
    );
  }
}

class _FmMapViewWidget extends StatefulWidget {
  _FmMapViewWidget({
    Key key,
    @required this.child,
    @required this.channel,
  }) : super(key: key);
  final Widget child;
  final MethodChannel channel;
  @override
  _FmMapViewWidgetState createState() => _FmMapViewWidgetState();
}

class _FmMapViewWidgetState extends State<_FmMapViewWidget> {
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return widget.child;
  }

  @override
  void dispose() {
    super.dispose();
    if (Platform.isIOS) {
      widget.channel.invokeMethod("handDispose");
    }
  }
}
