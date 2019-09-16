package com.hhwy.fm_gaode_map.location;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;

import com.hhwy.fm_gaode_map.FmToolsBase;

import java.util.ArrayList;
import java.util.HashMap;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

public abstract class FmGaodeLocationImpClient{
    FmToolsBase _ftb;
    // flutter通道名称
    String _name;

    PluginRegistry.Registrar _registrar;

    private SensorManager sensorManager;
    private float[] magneticValues = new float[3]; //地磁传感器数据
    private float[] accelerometerValues = new float[3]; //重力传感器数据
    private float lastOrientation = 0;
    private ArrayList<Float> orientateValues = new ArrayList(); //方向缓存
    // 监听消息
    SensorEventListener _listener;

    FmGaodeLocationImpClient(String name,PluginRegistry.Registrar registrar){
        _registrar = registrar;
        _ftb = new FmToolsBase(this, name, registrar);
    }

    /**
     * 开始定位
     */
    public abstract void start(MethodChannel.Result result);

    /**
     * 结束定位
     */
    public abstract void stop(MethodChannel.Result result);

    /**
     * 是否打开gps
     */
    public abstract void isOpenGPS(MethodChannel.Result result);
    /**
     * 是否开始定位
     */
    public abstract void isLocating(MethodChannel.Result result);

    public void stopOrientation(MethodChannel.Result result){
        if (sensorManager != null) {
            sensorManager.unregisterListener(_listener);
            sensorManager = null;
        }
        final HashMap rt=new HashMap();
        rt.put("status",true);
        rt.put("data", "成功");
        result.success(rt);
    }
    /**
     * 获取方向
     */
    public void startOrientation(MethodChannel.Result result){
        sensorManager = (SensorManager) _registrar.activeContext().getSystemService(Context.SENSOR_SERVICE);
        Sensor magnetic;
        Sensor accelerometer;
//        Sensor gyroscope;
        if (sensorManager != null) {
            magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            _listener = new SensorEventListener(){
                @Override
                public void onSensorChanged(SensorEvent event) {
                    switch (event.sensor.getType()) {
                        case Sensor.TYPE_MAGNETIC_FIELD:
                            magneticValues = event.values;
                            break;
                        case Sensor.TYPE_ACCELEROMETER:
                            accelerometerValues = event.values;
                            break;
//                        case Sensor.TYPE_GYROSCOPE:
//                            break;
                        default:
                            break;

                    }
                    calculateDegrees();
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                }
            };
            sensorManager.registerListener(_listener, magnetic, SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(_listener, accelerometer, SensorManager.SENSOR_DELAY_GAME);
//            sensorManager.registerListener(_listener, gyroscope, SensorManager.SENSOR_DELAY_GAME);
        }
        final HashMap rt=new HashMap();
        rt.put("status",true);
        rt.put("data", "成功");
        result.success(rt);
    }

    private void calculateDegrees() {
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticValues);
        SensorManager.getOrientation(R, values);
        float degree = (float) Math.toDegrees(values[0]);
        if(orientateValues.size() >= 200){
            float curDegree = 0;
            boolean isPlus = true;
            for(int i = 0; i < orientateValues.size(); ++i){
                isPlus = orientateValues.get(i) >= 0;
                curDegree += Math.abs(orientateValues.get(i));
            }
            curDegree = curDegree / orientateValues.size();

            if (!isPlus) {
                curDegree = 0 - curDegree;
                curDegree += 360;
            }
            if (curDegree > 360) {
                curDegree -= 360;
            }
            if (Math.abs(lastOrientation - curDegree) > 1) {
                lastOrientation = curDegree;
                _ftb.invokeMethod("onOrientation", lastOrientation);
//            onResult("orientationChanged", degree);
            }
            orientateValues.clear();
            return;
        }
        orientateValues.add(degree);
    }

    /**
     * gps是否可用
     *
     * @return 检测结果
     */
    public boolean isGPSAvailable() {
        LocationManager lManager = (LocationManager) _registrar.activeContext().getSystemService(Context.LOCATION_SERVICE);
        boolean ok = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return ok;
    }

    /**
     * 销毁
     */
    public void dispose() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(_listener);
            sensorManager = null;
        }
        _ftb.dispose();
    };
}
