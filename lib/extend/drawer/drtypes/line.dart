import '../../../fm_gaode_map.dart';
import './base.dart';

class BsDevMapInfoLine extends BsDevMapInfo<FmMapOverlaysLine> {
  BsDevMapInfoLine(
    FmMapController controller, {
    this.style,
    this.points,
    config,
  }) : super(controller, config: config);
  // 样式
  List<FmMapPoint> points;
  final FmdrStyle style;
  Future draw() async {
    if (overlay != null) {
      await overlay.remove();
      overlay = null;
    }
    overlay = await controller.drawer.addOverlay(
      FmMapOverlaysLine(
        layer: "line",
        zIndex: 2,
        color: style.lineColor,
        points: points,
      ),
    );
  }

  // 创建一条线
  static Future<BsDevMapInfoLine> create(
    List<FmMapPoint> points,
    FmMapController controller,
    FmdrStyle style, {
    Map config,
    bool isDraw = true,
  }) async {
    BsDevMapInfoLine item = BsDevMapInfoLine(
      controller,
      style: style,
      points: points,
      config: config,
    );
    if (isDraw) {
      await item.draw();
    }
    return item;
  }
}
