import 'package:flutter/material.dart';
import './page.dart';
import 'view_test.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'fm_gaode',
      theme: new ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: Home(),
    );
  }
}

class Home extends StatefulWidget {
  @override
  _HomeState createState() => _HomeState();
}

class _HomeState extends State<Home> {
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('高德地图测试'),
        centerTitle: true,
      ),
      body: Center(
          child: Column(
        children: <Widget>[
          OutlineButton(
            child: Text("进入"),
            onPressed: () {
              Navigator.push(
                context,
                new MaterialPageRoute(builder: (context) => PageTest()),
              );
            },
          ),
          OutlineButton(
            child: Text("进入test"),
            onPressed: () {
              Navigator.push(
                context,
                new MaterialPageRoute(builder: (context) => ViewText()),
              );
            },
          ),
        ],
      )),
    );
  }
}
