package com.hhwy.fm_gaode_map;


import android.content.Intent;
import com.amap.api.maps2d.AMapUtils;

import com.amap.api.maps2d.model.LatLng;
import com.hhwy.fm_gaode_map.location.FmGaodeLocationImpClient;
import com.hhwy.fm_gaode_map.location.FmGaodeLocationImpClientGaode;
import com.hhwy.fm_gaode_map.location.FmGaodeLocationImpClientGPS;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.StandardMessageCodec;

public class FmMapPluginImp {
    final private PluginRegistry.Registrar _registrar;

    FmMapPluginImp(PluginRegistry.Registrar registrar){
        _registrar = registrar;
        registrar.platformViewRegistry().registerViewFactory("FmGaodeMapView",
                new FmMapViewFactory(new StandardMessageCodec(),_registrar));


    }
    /**
     * 新增定位实例
     * @param obj
     */
    public void newInstanceLocation(final JSONObject obj, MethodChannel.Result result){
        HashMap rt=new HashMap();
        try {
            // 获取名字
            String name = obj.getString("name");
            boolean isGaode = obj.getBoolean("isGaode");
            System.out.println(isGaode);
            // 新增定位
            FmGaodeLocationImpClient client = isGaode ?
                    new FmGaodeLocationImpClientGaode(name,_registrar,obj.getJSONObject("options")):
                    new FmGaodeLocationImpClientGPS(name,_registrar);
            System.out.println("newInstance,name:"+name);
            rt.put("status",true);
            rt.put("data", true);
            result.success(rt);
        } catch (JSONException e) {
            e.printStackTrace();
            rt.put("status",false);
            rt.put("msg", e.toString());
            result.success(rt);
        }
    }
    /**
     * 新增轨迹实例
     * @param obj
     */
//    public void newInstanceTrace(final JSONObject obj, MethodChannel.Result result){
//        HashMap rt=new HashMap();
//        try {
////            LocationService.context = _registrar.activity();
////            Intent intent = new Intent(_registrar.activity(), LocationService.class);
////            LocationService.mtIntent = intent;
////            _registrar.activity().startService(intent);
//
//
//            // 获取名字
//            String name = obj.getString("name");
//            // 新增定位
////            FmGaodeTrace client = new FmGaodeTrace(name,_registrar);
//            System.out.println("newInstance,name:"+name);
//            rt.put("status",true);
//            rt.put("data", true);
//            result.success(rt);
//        } catch (JSONException e) {
//            e.printStackTrace();
//            rt.put("status",false);
//            rt.put("msg", e.toString());
//            result.success(rt);
//        }
//    }
    /**
     * 获取距离
     * @param obj
     * @return
     */
    public void getDistance(JSONObject obj, MethodChannel.Result result){
        HashMap rt=new HashMap();
        double dis = getDistanceInner(obj);
        if(dis == -1.0){
            rt.put("status",false);
            rt.put("msg", "计算失败");
            result.success(rt);
            return;
        }
        rt.put("status",true);
        rt.put("data", dis);
        result.success(rt);
    }


    /**
     * 获取距离
     * @param obj
     * @return
     */
    private double getDistanceInner(final JSONObject obj){
        try {
            float distance = AMapUtils.calculateLineDistance(
                new LatLng(obj.getDouble("latitude1"),
                obj.getDouble("longitude1")), new LatLng(obj.getDouble("latitude2"),
                obj.getDouble("longitude2")));
            return  distance;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1.0;
    }
}
