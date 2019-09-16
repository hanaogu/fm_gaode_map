import 'package:flutter/foundation.dart';
import 'package:fm_fit/fm_fit.dart';
import '../../../fm_gaode_map.dart';
import '../base.dart';
import './base.dart';
import './line.dart';

// 杆元素
class BsDevMapInfoPole extends BsDevMapInfo<FmMapOverlaysMark> {
  BsDevMapInfoPole(
    FmMapController controller, {
    overlay,
    Map config,
    this.line,
    this.point,
    this.title,
    this.snippet,
    @required this.style,
  }) : super(controller, overlay: overlay, config: config);
  // 样式
  final FmdrStyle style;
  // 坐标点
  FmMapPoint point;
  // 前导线
  BsDevMapInfoLine line;
  // 前杆
  BsDevMapInfoPole _before;
  // 后杆
  BsDevMapInfoPole _after;
  String title;
  String snippet;

  // 是否被选中
  bool _selected = false;

  // 获取点
  @override
  FmMapPoint getPoint() {
    return overlay?.point;
  }

  // 选中
  Future select(bool sel) async {
    if (_selected == sel || overlay == null) {
      return;
    }
    _selected = sel;
    overlay.icon = sel ? style.selectedIcon : style.normalIcon;
    await overlay.update();
  }

  Future draw() async {
    if (overlay != null) {
      await overlay.remove();
      overlay = null;
    }
    overlay = await controller.drawer.addOverlay(FmMapOverlaysMark(
      point: point,
      layer: "pole",
      icon: style.normalIcon,
      text: config != null ? config["number"] : null,
      textSize: fit.t(54).ceil(),
      textColor: style.textColor,
      zIndex: 100,
      config: config,
      title: title,
      snippet: snippet,
    ));
  }

  // 创建一个杆塔
  static Future<BsDevMapInfoPole> create(
    FmMapPoint point,
    FmMapController controller,
    FmdrStyle style, {
    Map config,
    bool isDraw = true,
  }) async {
    BsDevMapInfoPole item = BsDevMapInfoPole(
      controller,
      point: point,
      config: config,
      style: style,
    );
    if (isDraw) {
      await item.draw();
    }
    return item;
  }

  Future showInfowindo() async {
    await controller.drawer.showInfoWindow(
      overlay.selfId,
      title: title,
      snippet: snippet,
    );
  }

  // 杆与前杆进行关联
  Future linkBefore(
    BsDevMapInfoPole p1, {
    Map config,
    bool once: false,
  }) async {
    // 处理导线
    if (line == null) {
      line = await BsDevMapInfoLine.create(
        [p1.getPoint(), getPoint()],
        controller,
        style,
        config: config,
      );
    } else {
      line.overlay.points[0] = p1.getPoint();
      await line.overlay.update();
    }
    if (_before != null && !once) {
      await p1.linkBefore(_before, once: true);
    }
    _before = p1;
    // 处理后杆
    if (p1._after != null && !once) {
      p1._after.linkBefore(this, once: true);
    }
    p1._after = this;
  }

  // 移杆
  Future move(FmMapPoint pt) async {
    overlay.point = pt;
    await overlay.update();
    // 线
    if (line != null) {
      line.overlay.points[1] = pt;
      await line.overlay.update();
    }
    // 处理后杆
    if (_after != null && _after.line != null) {
      _after.line.overlay.points[0] = pt;
      await _after.line.overlay.update();
    }
  }

  // 删除本杆
  Future remove() async {
    // 更新关系
    if (_before != null) {
      _before._after = null;
    }
    if (_after != null) {
      _after._before = null;
    }
    if (_after != null && _before != null) {
      await _after.linkBefore(_before);
    }
    // 删除标注
    if (overlay != null) {
      await overlay.remove();
      overlay = null;
    }
    // 删除线
    if (line != null) {
      await line.overlay.remove();
      line = null;
    }
  }
}
