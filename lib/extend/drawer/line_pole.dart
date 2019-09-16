import '../../fm_gaode_map.dart';
import './base.dart';
import './drtypes/pole.dart';

class FmdrLinePole {
  FmdrLinePole({
    this.controller,
    this.style,
    this.closed = false,
    this.clickMark,
  });
  FmdrStyle style;
  bool closed;
  var clickMark;
  // 操控地图
  FmMapController controller;
  // 已绘制的杆
  List<BsDevMapInfoPole> _poles = [];
  // 杆信息
  List<BsDevMapInfoPole> get poles => _poles;
  // 从坐标点转成图
  fromPoints(List<FmMapPoint> data, {Map Function(int index) bind}) async {
    await removeAll();
    for (int i = 0; i < data.length; ++i) {
      Map config = {};
      if (bind != null) {
        config = bind(i);
      }
      config["number"] = nextNumber();
      _poles.add(await BsDevMapInfoPole.create(
        data[i],
        controller,
        style,
        config: config,
        isDraw: false,
      ));
    }
    await drawAll();
  }

  // 转坐标
  List<FmMapPoint> toPoints() {
    List<FmMapPoint> arr = [];
    _poles.forEach((it) {
      arr.add(it.point);
    });
    return arr;
  }

  // 插入方向
  FmdrBaseInsertDirect _direct = FmdrBaseInsertDirect.none;

  bool _editing = false;
  // 当前选中的
  int _selected = -1;
  int _currentIndex = -1;
  Future _endInsert(int i) async {
    if (closed && _poles.length > 2 && (i == 0 || i == _poles.length - 1)) {
      int i1 = i == 0 ? _poles.length - 1 : i - 1;
      int i2 = i == 0 ? i + 1 : 0;
      if (_selected == -1) {
        i1 = _poles.length - 1;
        i2 = 0;
      }
      await controller.updateShirr(
        p1: _poles[i1].overlay.point,
        p3: _poles[i2].overlay.point,
      );
    } else {
      await controller.updateShirr(p1: _poles[i].overlay.point);
    }
  }

  Future setCurrent(int i) async {
    _currentIndex = i;
    if (i == -1) {
      await controller.stopShirr();
      return;
    }
    // 正常插
    if (_direct == FmdrBaseInsertDirect.none) {
      if ((i - 1) < 0 || (i + 1) > _poles.length - 1) {
        if (!controller.isShirr()) {
          await controller.startShirr(
            _poles[i].overlay.point,
            color: style.tmplineColor,
          );
        } else {
          await _endInsert(i);
        }
        return;
      }
      // 更新2杆之间
      await controller.updateShirr(
        p1: _poles[i - 1].overlay.point,
        p3: _poles[i + 1].overlay.point,
      );
      return;
    }
    // 前插
    if (_direct == FmdrBaseInsertDirect.before) {
      if (i == 0) {
        if (closed) {
          await controller.updateShirr(
            p1: _poles[i].overlay.point,
            p3: _poles[_poles.length - 1].overlay.point,
          );
        } else {
          await controller.updateShirr(p1: _poles[i].overlay.point);
        }
        return;
      }
      // 更新2杆之间
      await controller.updateShirr(
        p1: _poles[i - 1].overlay.point,
        p3: _poles[i].overlay.point,
      );
      return;
    }
    // 后插
    if (_direct == FmdrBaseInsertDirect.after) {
      if ((i + 1) > _poles.length - 1) {
        if (closed && _poles.length > 2) {
          await controller.updateShirr(
            p1: _poles[i].overlay.point,
            p3: _poles[0].overlay.point,
          );
        } else {
          await controller.updateShirr(p1: _poles[i].overlay.point);
        }
        return;
      }
      // 更新2杆之间
      await controller.updateShirr(
        p1: _poles[i].overlay.point,
        p3: _poles[i + 1].overlay.point,
      );
      return;
    }
  }

  // 设置当前插入方向
  // @override
  Future setInstertDirect(FmdrBaseInsertDirect direct) async {
    _direct = direct;
    await setCurrent(_currentIndex);
  }

  // @override
  Future remove(int index) async {
    if (index == -1) {
      return;
    }
    // 当前选中的不选了
    if (_selected != index && _selected != -1) {
      await unselect();
    }
    _selected = -1;
    await _poles[index].remove();
    _poles.removeAt(index);
    _direct = FmdrBaseInsertDirect.none;
    await setCurrent(_poles.length - 1);
  }

  // @override
  void onClick(Map config) async {
    if (clickMark != null) {
      clickMark(config);
    }
    if (!_editing) {
      return;
    }
    if (config["type"] == "line") {
      return;
    }
    int i = _poles.indexWhere((it) {
      return it.overlay.id == config["id"];
    });
    if (i == -1) {
      return;
    }
    if (_selected == i) {
      await cancelSelect();
    } else {
      await unselect();
      await select(i);
    }
  }

  // 定位第几个标注
  // @override
  Future location(int index) async {
    if (index < 0 ||
        index >= _poles.length ||
        _poles[index].overlay == null ||
        !_poles[index].overlay.point.isValid()) {
      return;
    }
    await controller.drawer.setCenter(_poles[index].overlay.point);
  }

  // 不选择
  Future unselect() async {
    // 先取消选择
    if (_selected != -1) {
      await _poles[_selected].select(false);
    }
    _selected = -1;
  }

  // 选中第几个标注
  // @override
  Future select(int index) async {
    if (index < 0 || index >= _poles.length) {
      return;
    }
    // 再设置选择
    _selected = index;
    await _poles[index].select(true);
    // 处理橡皮线
    setCurrent(index);
  }

  // 修改某个
  // @override
  Future modify(int index, {FmMapPoint pt, Map config}) async {
    if (index < 0 || index >= _poles.length) {
      return;
    }
    if (pt != null) {
      await _poles[index].move(pt);
      await _endInsert(index);
    }
    if (config != null) {
      if (_poles[index].config == null) {
        _poles[index].config = config;
      } else {
        _poles[index].config.addAll(config);
      }
    }
  }

  // @override
  int selectedIndex() {
    return _selected;
  }

  // 获取数量
  // @override
  int count() {
    return _poles.length;
  }

  // @override
  Future sure({Map config}) async {
    if (!_editing) {
      return;
    }
    // 如果没有选择，则绘制
    if (_selected == -1) {
      await draw(controller.centerPoint, style, config: config);
      return;
    }
    if (_direct == FmdrBaseInsertDirect.none) {
      await modify(selectedIndex(), pt: controller.centerPoint);
    } else {
      await draw(controller.centerPoint, style,
          config: config ?? {"number": nextNumber()});
    }
  }

  BsDevMapInfoPole getAt(int index) {
    return _poles[index];
  }

  // 获取一个新编号
  String nextNumber() {
    return "${_poles.length + 1}";
  }

  // 取消选择
  // @override
  Future cancelSelect() async {
    if (_selected != -1) {
      await _poles[_selected].select(false);
    }
    if (!_editing) {
      controller.stopShirr();
      return;
    }
    _direct = FmdrBaseInsertDirect.none;
    _selected = -1;
    await setCurrent(_poles.length - 1);
  }

  // 绘制一个点状物
  // @override
  Future<int> draw(FmMapPoint pt, FmdrStyle style, {Map config}) async {
    // 创建一个杆
    var item = await BsDevMapInfoPole.create(
      pt,
      controller,
      style,
      config: config,
    );
    // 正常插
    if (_direct == FmdrBaseInsertDirect.none || _poles.length < 1) {
      if (_poles.length != 0) {
        item.linkBefore(_poles.last);
      }
      _poles.add(item);
      await unselect();
      await setCurrent(_poles.length - 1);
      return _poles.length - 1;
    }
    int currentSelected = _selected;
    if (currentSelected == -1) {
      currentSelected = _currentIndex;
    }
    await unselect();
    int index = 0;
    if (_direct == FmdrBaseInsertDirect.before) {
      // 前插
      if (currentSelected == -1) {
        return -1;
      }
      await _poles[currentSelected].linkBefore(item);
      _poles.insert(currentSelected, item);
      index = currentSelected;
    } else {
      // 后插
      if (currentSelected == -1) {
        return -1;
      }
      await item.linkBefore(_poles[currentSelected]);
      _poles.insert(currentSelected + 1, item);
      index = currentSelected + 1;
    }
    // 设置当前插的杆选中
    await select(index);
    await setCurrent(index);
    return index;
  }

  // @override
  Future drawAll() async {
    for (int i = 0; i < _poles.length; ++i) {
      await _poles[i].draw();
      if (i != 0) {
        await _poles[i].linkBefore(_poles[i - 1]);
      }
    }
    if (closed && _poles.length > 2) {
      await _poles[0].linkBefore(_poles[_poles.length - 1]);
    }
  }

  // @override
  end([bool wantClosed = false]) async {
    _editing = false;
    _direct = FmdrBaseInsertDirect.none;
    // 如果大于3连
    if ((closed || wantClosed) && _poles.length > 2) {
      var it = _poles[0];
      if (it.line == null) {
        await it.linkBefore(_poles[_poles.length - 1]);
      }
    }
    await unselect();
    await controller.stopShirr();
  }

  // @override
  Future begin() async {
    _editing = true;
    _direct = FmdrBaseInsertDirect.none;
    if (_poles.length > 0) {
      if (closed) {
        if (_poles.length > 1) {
          await controller.startShirr(
            _poles[0].overlay.point,
            p3: _poles[_poles.length - 1].overlay.point,
            color: style.tmplineColor,
          );
        }
      } else {
        await controller.startShirr(
          _poles[_poles.length - 1].overlay.point,
          color: style.tmplineColor,
        );
      }
    }
  }

  // @override
  bool isBegin() {
    return _editing;
  }

  // @override
  Future removeAll() async {
    for (int i = 0; i < _poles.length; ++i) {
      await _poles[i].remove();
    }
    _poles.clear();
  }
}
