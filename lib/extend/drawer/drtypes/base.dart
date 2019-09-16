
// 设备基类
import '../../../fm_gaode_map.dart';

abstract class BsDevMapInfo<T extends FmMapOverlays> {
  BsDevMapInfo(this.controller, {this.overlay, this.config});
  // 地图元素
  T overlay;
  // 内部数据
  Map config;
  // 地图操控
  FmMapController controller;
  // 获取设备点
  FmMapPoint getPoint() {
    return null;
  }
  // 绘制
  // Future draw({Map cfg});
}
