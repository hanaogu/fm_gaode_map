// import 'dart:async';

// import 'package:flutter/services.dart';

// class FmGaodeMap {
//   static const MethodChannel _channel =
//       const MethodChannel('fm_gaode_map');

//   static Future<String> get platformVersion async {
//     final String version = await _channel.invokeMethod('getPlatformVersion');
//     return version;
//   }
// }
export './map/map.dart';
export './location/location.dart';
export './extend/mapview.dart';
export './extend/map_controller.dart';
export './types.dart';
export './map/over_types/base.dart';
export './map/over_types/line.dart';
export './map/over_types/text.dart';
export './map/over_types/mark.dart';
export './map/over_types/controller.dart';

export './extend/drawer/base.dart';
export './extend/drawer/drtypes/base.dart';
export './extend/drawer/drtypes/line.dart';
export './extend/drawer/drtypes/pole.dart';
export './extend/drawer/line_pole.dart';