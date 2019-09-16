import 'package:flutter/material.dart';

import '../fm_gaode_map.dart';
import '../types.dart';
import 'map_controller.dart';
import 'package:fm_fit/fm_fit.dart';

/// 地图线状采集
class FmMapView extends StatefulWidget {
  FmMapView({
    Key key,
    this.controller,
    this.onInit,
    this.showPlus = true,
    this.onLocation,
    this.onFollow,
    this.follow = true,
    this.maxDistanceFollow = 15.0,
  }) : super(key: key);
  final FmMapController controller;
  final VoidCallback onInit;
  // 是否显示屏幕中间的+号
  final bool showPlus;
  // 是图是否开启跟随模式
  final bool follow;
  // 定位变化响应
  final Function onLocation;
  // 定位变化且为跟随模式响应
  final Function onFollow;
  // 移动地图多少米，停止跟随
  final double maxDistanceFollow;
  @override
  _FmMapViewState createState() => _FmMapViewState();
}

class _FmMapViewState extends State<FmMapView> {
  // 地图组件
  final FmGaodeMap _map = FmGaodeMap();
  // 定位组件
  final FmGaodeLocation _location = FmGaodeLocation();
  // 当前定位点
  FmMapPoint _locationPoint;
  // 当前屏幕中心点
  FmMapPoint _centerPoint;

  bool _following = true;

  bool get isFollow => widget.follow && _following;
  Future _setCenter(FmMapPoint pt) async {
    await _map.setCenter(pt);
    if (widget.follow && mounted) {
      setState(() {
        _following = true;
      });
    }
  }

  @override
  void initState() {
    super.initState();
    if (widget.controller != null) {
      widget.controller.drawer.map = _map;
    }
    // 地图
    _map.init(
      onInfoWindowClick: widget.controller?.onInfoWindowClick,
      onMessage: (String method, Object config) {
        if (widget.controller != null && widget.controller.onMessage != null) {
          widget.controller.onMessage(method, config);
        }
      },
      onMapStatusChange: widget.controller?.onMapStatusChange,
      onMapStatusChangeStart: (FmMapStatusInfo info) {
        if (widget.controller != null) {
          widget.controller.onMapStatusChangeStartPro(info);
        }
        if (widget.controller != null &&
            widget.controller.onMapStatusChangeStart != null) {
          widget.controller.onMapStatusChangeStart(info);
        }
      },
      // 坐标及状态变化
      onMapStatusChangeFinish: (FmMapStatusInfo info) {
        if (!info.point.isValid()) {
          return;
        }
        // 记录当前屏幕中心点
        _centerPoint = info.point;
        if (widget.controller != null) {
          widget.controller.centerPoint = _centerPoint;
        }
        if (widget.controller != null) {
          widget.controller.onMapStatusChangeFinishPro(info);
        }
        if (widget.controller != null &&
            widget.controller.onMapStatusChangeFinish != null) {
          widget.controller.onMapStatusChangeFinish(info);
        }
        // 判断是否停止跟随
        if (!widget.follow) {
          return;
        }
        // 跟随时判断当前点与屏幕中心点的距离，大于x米停止跟随
        FmGaodeMap.getDistance(_centerPoint, _locationPoint).then((v) {
          if (v > widget.maxDistanceFollow) {
            _following = false;
            if (mounted) {
              setState(() {});
            }
          }
        });
      },
    );
    // 定位
    _location.init(onLocation: (FmGaodeLocationInfo event) {
      if (!event.point.isValid()) {
        return;
      }
      // 记录当前定位点
      if (_locationPoint == null) {
        // 定位到当前
        if (mounted) {
          _map.setCenter(event.point);
        }
      }
      _locationPoint = event.point;
      if (widget.controller != null) {
        widget.controller.locationPoint = _locationPoint;
      }
      // 设置地图定位点
      if (mounted) {
        _map.setCurrentPoint(_locationPoint);
      }

      if (widget.onLocation != null) {
        widget.onLocation(event.point);
      }
      // 跟随
      if (isFollow) {
        _setCenter(event.point);

        if (widget.onFollow != null) {
          widget.onFollow(event.point);
        }
      }
    }).then((v) {
      _location.start();
      if (widget.onInit != null) {
        widget.onInit();
      }
    });
  }

  @override
  void dispose() {
    _location.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      child: Stack(
        children: <Widget>[
          _map.view,
          Offstage(
            offstage: !widget.showPlus,
            child: Center(
              child: Text(
                "+",
                style: TextStyle(
                  fontSize: fit.t(80),
                  fontWeight: FontWeight.w300,
                  color: Colors.blue,
                ),
              ),
            ),
          ),
          // 定位按钮
          Positioned(
            bottom: fit.t(100.0),
            left: fit.t(20.0),
            child: GestureDetector(
              child: Container(
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.all(Radius.circular(fit.t(4))),
                ),
                height: fit.t(60),
                width: fit.t(60),
                child: Icon(
                  isFollow ? Icons.local_activity : Icons.my_location,
                  color: Colors.blue,
                  size: fit.t(40),
                ),
              ),
              onTap: () {
                // 定位当前位置
                if (_locationPoint == null || !_locationPoint.isValid()) {
                  return;
                }
                _setCenter(_locationPoint);
              },
            ),
          )
        ],
      ),
    );
  }
}
