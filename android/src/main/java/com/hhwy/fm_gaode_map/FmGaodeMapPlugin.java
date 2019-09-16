package com.hhwy.fm_gaode_map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** FmGaodeMapPlugin */
public class FmGaodeMapPlugin implements MethodCallHandler {

  private  static  FmMapPluginImp _imp;
  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "fm_gaode_map");
    channel.setMethodCallHandler(new FmGaodeMapPlugin());
    _imp= new FmMapPluginImp(registrar);
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    FmToolsBase.onMethodCall(_imp,call,result);
  }
}
