import 'package:flutter/material.dart';
import 'package:fm_gaode_map/fm_gaode_map.dart';

import 'view_test.dart';

class PageTest extends StatefulWidget {
  @override
  _PageTestState createState() => _PageTestState();
}

class _PageTestState extends State<PageTest> {
  FmMapController _controller;
  FmdrLinePole _cj;
  final FmdrStyle style = FmdrStyle(
    normalIcon: "lib/images/circle3.png",
    selectedIcon: "lib/images/circle4.png",
    lineColor: Colors.blue,
  );
  @override
  void initState() {
    super.initState();
    _controller = FmMapController(onMessage: (method, config) {
      if (method == "click_overlay") {
        _cj.onClick(config);
      }
    });
    _cj = FmdrLinePole(controller: _controller, style: style, closed: true);
    _cj.begin();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: Text("定位测试"),
          centerTitle: true,
        ),
        body: Column(
          children: <Widget>[
            Expanded(child: FmMapView(controller: _controller)),
            Container(
              height: 100,
              child: Wrap(
                children: <Widget>[
                  OutlineButton(
                    child: Text("测试"),
                    onPressed: () async {
                      Navigator.push(
                        context,
                        new MaterialPageRoute(builder: (context) => ViewText()),
                      );
                    },
                  ),
                  OutlineButton(
                      child: Text("绘"),
                      onPressed: () async {
                        if (_controller.centerPoint == null) {
                          return;
                        }
                        await _cj.sure(config: {
                          "number": _cj.nextNumber(),
                        });
                      }),
                  OutlineButton(
                    child: Text("删"),
                    onPressed: () {
                      _cj.remove(_cj.selectedIndex());
                    },
                  ),
                  OutlineButton(
                    child: Text("前"),
                    onPressed: () {
                      _cj.setInstertDirect(FmdrBaseInsertDirect.before);
                    },
                  ),
                  OutlineButton(
                    child: Text("后"),
                    onPressed: () {
                      _cj.setInstertDirect(FmdrBaseInsertDirect.after);
                    },
                  ),
                  OutlineButton(
                    child: Text("不选"),
                    onPressed: () {
                      _cj.cancelSelect();
                    },
                  ),
                  OutlineButton(
                    child: Text("开始"),
                    onPressed: () {
                      _cj.begin();
                    },
                  ),
                  OutlineButton(
                    child: Text("结束"),
                    onPressed: () {
                      _cj.end();
                    },
                  ),
                  // OutlineButton(
                  //   child: Text("多页面"),
                  //   onPressed: () {
                  //     Navigator.push(
                  //       context,
                  //       new MaterialPageRoute(builder: (context) => PageTest()),
                  //     );
                  //   },
                  // )
                ],
              ),
            ),
            // Expanded(
            //   child: _map2.map,
            // )
          ],
        ));
  }
}
