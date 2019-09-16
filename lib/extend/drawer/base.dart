import 'package:flutter/material.dart';

// 插入方向
enum FmdrBaseInsertDirect { none, before, after }

// 样式
class FmdrStyle {
  FmdrStyle({
    this.normalIcon,
    this.selectedIcon,
    this.lineColor = const Color(0xFFFF0000),
    this.tmplineColor = const Color(0xFFFF0000),
    this.textColor = const Color(0xFFFFFFFF),
  });
  // 线的颜色
  Color lineColor;
  // 临时线的颜色
  Color tmplineColor;
  // 文字颜色
  Color textColor;
  // 点的正常图标
  String normalIcon;
  // 点的选中图标
  String selectedIcon;
}
