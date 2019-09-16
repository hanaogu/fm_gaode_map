import 'package:flutter/material.dart';
import 'package:uuid/uuid.dart';
import '../../types.dart';
import './base.dart';

// 线标记
class FmMapOverlaysText extends FmMapOverlays {@override
  String get selfId => id;
  FmMapOverlaysText({
    this.id,
    this.layer = "0",
    this.visible = true,
    this.zIndex,
    this.point,
    this.text,
    int bgColor,
    this.rotate,
    this.fontSize,
    this.fontColor,
  }) {
    if (id == null) {
      var uuid = new Uuid();
      id = uuid.v1();
    }
  }
  String id;
  String layer;
  bool visible;
  int zIndex;
  FmMapPoint point;
  String text;
  Color bgColor;
  double rotate;
  int fontSize;
  Color fontColor;

  /// 删除标注
  @override
  Future remove() async {
    if (map != null) {
      await map.removeOverlays(id: id, layer: layer);
    }
  }

  @override
  Future setVisible(bool visible) async {
    if (map != null) {
      await map.setOverlaysVisible(id: id, layer: layer, visible: visible);
    }
  }

  @override
  Future setZIndex(int zIndex) async {
    if (map != null) {
      await map.setOverlaysZIndex(id: id, layer: layer, zIndex: zIndex);
    }
  }

  @override
  void fromMap(Map m) {
    if (!m.containsKey("id")) {
      var uuid = new Uuid();
      id = uuid.v1();
    }
    layer = m["layer"] ?? "0";
    visible = m["visible"] ?? true;
    zIndex = m["zIndex"];
    point = FmMapPoint(latitude: m["latitude"], longitude: m["longitude"]);
    text = m["text"];
    bgColor = Color(m["bgColor"]);
    rotate = m["rotate"];
    fontSize = m["fontSize"];
    fontColor = Color(m["fontColor"]);
  }

  // 转json
  @override
  Map toMap() {
    Map option = {
      "id": id,
      "type": "text",
      "layer": layer,
      "visible": visible,
      "text": text,
      "latitude": point.latitude,
      "longitude": point.longitude,
    };
    if (zIndex != null) {
      option["zIndex"] = zIndex;
    }
    if (bgColor != null) {
      option["bgColor"] = bgColor.value;
    }
    if (fontColor != null) {
      option["fontColor"] = fontColor.value;
    }
    if (rotate != null) {
      option["rotate"] = rotate;
    }
    if (fontSize != null) {
      option["fontSize"] = fontSize;
    }
    return option;
  }
}
