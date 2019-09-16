package com.hhwy.fm_gaode_map.location;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import org.json.JSONObject;

import java.util.HashMap;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import com.hhwy.fmpermission.*;

/**
 * 百度监听类
 */
public class FmGaodeLocationImpClientGaode extends FmGaodeLocationImpClient {
    // 控制监听
    AMapLocationClient _client;
    // 监听消息
    AMapLocationListener _listener;
    // 构造函数
    public FmGaodeLocationImpClientGaode(String name, PluginRegistry.Registrar registrar, JSONObject options) {
        super(name, registrar);
        _registrar = registrar;
        FmPermission.activity = registrar.activity();
        _client = new AMapLocationClient(registrar.activity());
        // 设置
        AMapLocationClientOption locationClientOption = new AMapLocationClientOption();
        //单位是毫秒，默认30000毫秒，超时时间
        locationClientOption.setHttpTimeOut(5000);
        locationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
        locationClientOption.setInterval(1000);
        //设置是否返回地址信息（默认返回地址信息）
        locationClientOption.setNeedAddress(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        locationClientOption.setMockEnable(false);
        //关闭缓存机制
        locationClientOption.setLocationCacheEnable(false);
        _client.setLocationOption(locationClientOption);

        _listener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
//                System.out.println("==============");
                if ( aMapLocation.getErrorCode() == 0 ) {
                    HashMap<String, Object> jsonObject = new HashMap();
                    jsonObject.put("coordType", "Gaode");
                    jsonObject.put("time", System.currentTimeMillis());
                    jsonObject.put("speed", aMapLocation.getSpeed());
                    jsonObject.put("altitude", aMapLocation.getAltitude());
                    jsonObject.put("latitude", aMapLocation.getLatitude());
                    jsonObject.put("longitude", aMapLocation.getLongitude());
                    jsonObject.put("bearing", aMapLocation.getBearing());
//                    System.out.println(jsonObject);
                    _ftb.invokeMethod("onLocation", jsonObject);
                }else{
                    System.out.println(aMapLocation.getErrorCode());
                    System.out.println(aMapLocation.getErrorInfo().toString());
                }
            }
        };
        _client.setLocationListener(_listener);

    }

    @Override
    public void start(final MethodChannel.Result result) {
        final HashMap rt=new HashMap();
        FmPermission.getPermission(new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println("onSubscribe: "+d.toString());
            }

            @Override
            public void onNext(Boolean aBoolean) {
                System.out.println(aBoolean);
                if(!aBoolean){
                    rt.put("status",false);
                    rt.put("msg", "未获取到权限");
                    result.success(rt);
                    return;
                }
//                if (_client.isStarted()) {
//                    return;
//                }
                System.out.println(_client.isStarted());
                System.out.println("started!");
                _client.startLocation();
                rt.put("status",true);
                rt.put("data", "成功");
                result.success(rt);
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("onError:"+e.toString());
                rt.put("status",false);
                rt.put("msg", e.toString());
                result.success(rt);
            }

            @Override
            public void onComplete() {
                System.out.println("onComplete: success");
            }
        }, FmPermission.FmPermissionType.LOCATION, FmPermission.FmPermissionType.STORAGE);
    }

    public boolean isStarted() {
        return _client.isStarted();
    }

    @Override
    public void isOpenGPS(MethodChannel.Result result) {
        HashMap rt=new HashMap();
        rt.put("status",true);
        rt.put("data", isGPSAvailable());
        result.success(rt);
    }

    @Override
    public void isLocating(MethodChannel.Result result) {
        HashMap rt=new HashMap();
        rt.put("status",true);
        rt.put("data", isStarted());
        result.success(rt);
    }

    @Override
    public void stop(MethodChannel.Result result) {
        if(_client != null){
            _client.stopLocation();
            _client.onDestroy();
        }
        final HashMap rt=new HashMap();
        rt.put("status",true);
        rt.put("data", "成功");
        result.success(rt);
    }

    @Override
    public void dispose() {
        if(_client != null) {
            _client.stopLocation();
            _client.onDestroy();
            _client.unRegisterLocationListener(_listener);
            _listener = null;
        }
        _client = null;
        super.dispose();
    }
}
