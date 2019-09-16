import '../map.dart';
import 'dart:async';

abstract class FmMapOverlays {
  FmGaodeMap _map;
  set map(FmGaodeMap m) => _map = m;
  FmGaodeMap get map => _map;
  String get selfId;
  // 更新地图
  Future update() async {
    if (_map == null) {
      return;
    }
    await _map.update(this);
  }

  Future remove();
  Future setVisible(bool visible);
  Future setZIndex(int zIndex);

  void fromMap(Map m);
  Map toMap();
  // 数组转Map
  static List<Map> toList(List<FmMapOverlays> list) {
    List<Map> m = [];
    list.forEach((it) {
      m.add(it.toMap());
    });
    return m;
  }
}
