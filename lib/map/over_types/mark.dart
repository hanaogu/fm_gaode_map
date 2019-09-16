import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:uuid/uuid.dart';
import '../../types.dart';
import './base.dart';

// 标记
class FmMapOverlaysMark extends FmMapOverlays {
  @override
  String get selfId => id;
  FmMapOverlaysMark({
    this.id,
    this.layer = "0",
    this.visible = true,
    this.zIndex,
    this.point,
    this.icon,
    this.draggable = false,
    this.title,
    this.text,
    this.textSize = 16,
    this.textColor = const Color(0xFF000000),
    this.rotate = 0.0,
    this.anchorX = 0.5,
    this.anchorY = 0.5,
    this.scale = 1.0,
    this.config,
    this.snippet,
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
  // 图标--flutter资源中的
  String icon;
  // 是否可拖拽
  bool draggable;
  // 信息-标题
  String title;
  // 信息-内容
  String snippet;
  String text;
  int textSize;
  Color textColor;
  double rotate;
  double anchorX;
  double anchorY;
  double scale;
  Map<dynamic, dynamic> config;

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
  // 显示信息
  Future showInfoWindow() async {
    if (map != null) {
      await map.showInfoWindow(id, title: title, snippet: snippet);
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
    draggable = m["draggable"] ?? false;
    if (m.containsKey("icon")) {
      icon = m["icon"];
    }
    if (m.containsKey("title")) {
      title = m["title"];
    }
    if (m.containsKey("title")) {
      title = m["title"];
    }
    textSize = m["textSize"] ?? 16;
    textColor = Color(m["textColor"] ?? 0xFF000000);
    rotate = m["rotate"] ?? 0.0;
    anchorX = m["anchorX"] ?? 0.5;
    anchorY = m["anchorY"] ?? 0.5;
    scale = m["scale"] ?? 1.0;
    if (m["config"] != null) {
      config = json.decode(m["config"]);
    } else {
      config = null;
    }
  }

  // 转json
  @override
  Map toMap() {
    Map option = {
      "id": id,
      "type": "mark",
      "layer": layer,
      "visible": visible,
      "latitude": point.latitude,
      "longitude": point.longitude,
      "icon": icon,
      "draggable": draggable,
      "anchorX": anchorX,
      "anchorY": anchorY,
      "scale": scale,
      "textSize": textSize,
      "textColor": textColor.value,
    };
    if (zIndex != null) {
      option["zIndex"] = zIndex;
    }
    if (title != null) {
      option["title"] = title;
    }
    if (snippet != null) {
      option["snippet"] = snippet;
    }
    if (text != null) {
      option["text"] = text;
    }
    if (rotate != null) {
      option["rotate"] = rotate;
    }
    if (config != null) {
      option["config"] = json.encode(config);
    }
    return option;
  }
}
