import '../../types.dart';
import '../map.dart';
import './base.dart';

// 控制器
class FmGaodeMapController {
  FmGaodeMapController({map}) {
    _map = map;
  }
  FmGaodeMap _map;
  set map(m) {
    _map = m;
  }

  get map => _map;

  // 删除一个图层
  Future removeLayer(String layer) async {
    if (_map != null) {
      await _map.removeOverlays(layer: layer);
    }
  }

  /// 更新一个标注
  Future<FmMapOverlays> updateOverlay(FmMapOverlays object) async {
    if (_map != null) {
      return await _map.update(object);
    }
    return null;
  }

  /// 添加一个标注
  Future<FmMapOverlays> addOverlay(FmMapOverlays object) async {
    if (_map != null) {
      return await _map.addOverlay(object);
    }
    return null;
  }

  /// 添加一组标注
  Future<List<FmMapOverlays>> addOverlays(List<FmMapOverlays> objects) async {
    if (_map != null) {
      return await _map.addOverlays(objects);
    }
    return null;
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
    Map m = {"visible": visible};
    if (id != null) {
      m["id"] = id;
    }
    if (title != null) {
      m["title"] = title;
    }
    if (snippet != null) {
      m["snippet"] = snippet;
    }
    await _map.showInfoWindow(
      id,
      title: title,
      snippet: snippet,
      visible: visible,
    );
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
    double zoom = 0,
  }) async {
    if (_map != null) {
      return await _map.setCenter(
        point,
        overlook: overlook,
        rotate: rotate,
        zoom: zoom,
      );
    }
  }
}
