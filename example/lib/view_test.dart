import 'package:flutter/material.dart';

import 'package:fm_gaode_map/fm_gaode_map.dart';

class ViewText extends StatefulWidget {
  ViewText({Key key}) : super(key: key);
  @override
  _ViewTextState createState() => _ViewTextState();
}

class _ViewTextState extends State<ViewText> {
  // final FmGaodeMap _view = FmGaodeMap();
  final FmGaodeLocation _gaodeLocation = FmGaodeLocation();
  @override
  void initState() {
    super.initState();
    // _view.init();
    _gaodeLocation.init(onLocation: (FmGaodeLocationInfo arg) {
      print("${arg.point.latitude},${arg.point.longitude}");
    }).then((v) {
      _gaodeLocation.start();
    });
  }

  @override
  void dispose() {
    _gaodeLocation.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: Text("定位测试"),
          centerTitle: true,
        ),
        body: FlatButton(
          child: Text("push"),
          onPressed: () {
            Navigator.push(
              context,
              new MaterialPageRoute(builder: (context) => ViewText()),
            );
          },
        )
        // body: _view.view,
        );
  }
}
