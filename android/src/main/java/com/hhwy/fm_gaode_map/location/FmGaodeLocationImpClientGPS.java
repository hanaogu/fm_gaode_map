package com.hhwy.fm_gaode_map.location;

import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.hhwy.fmpermission.FmPermission;

import java.util.HashMap;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class FmGaodeLocationImpClientGPS extends FmGaodeLocationImpClient {

    private LocationManager _locationManager;

    LocationListener _listener;
    GpsStatus.Listener _gpsListener;

    // 是否正在定位
    boolean locating = false;

    public FmGaodeLocationImpClientGPS(String name, PluginRegistry.Registrar registrar){
        super(name,registrar);
        _registrar = registrar;
        FmPermission.activity = registrar.activity();
        _locationManager = (LocationManager) registrar.activeContext().getSystemService(registrar.activeContext().LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setSpeedRequired(true);
        criteria.setAltitudeRequired(true);
        criteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setCostAllowed(false);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
    }

    @Override
    public void start(final MethodChannel.Result result) {
        locating = false;
        final HashMap rt=new HashMap();
        FmPermission.getPermission(new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println("onSubscribe: "+d.toString());
            }

            @Override
            public void onNext(Boolean aBoolean) {
                if(!aBoolean){
                    rt.put("status",false);
                    rt.put("msg", "未获取到权限");
                    result.success(rt);
                    return;
                }
                if (isStarted()) {
                    return;
                }
                _listener = new LocationListener(){
                    @Override
                    public void onLocationChanged(android.location.Location bdLocation) {
                        HashMap<String, Object> jsonObject = new HashMap();
                        jsonObject.put("coordType", "GPS");
                        jsonObject.put("time", System.currentTimeMillis());
                        jsonObject.put("speed", bdLocation.getSpeed());
                        jsonObject.put("altitude", bdLocation.getAltitude());
                        jsonObject.put("latitude", bdLocation.getLatitude());
                        jsonObject.put("longitude", bdLocation.getLongitude());
                        jsonObject.put("bearing", bdLocation.getBearing());
                        _ftb.invokeMethod("onLocation", jsonObject);
                    }
                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                };
                _gpsListener = new GpsStatus.Listener(){
                    @Override
                    public void onGpsStatusChanged(int event) {
                        if(event == 1){
                            locating = true;
                        }
                        if(event == 2) {
                            locating = false;
                        }
                        GpsStatus gpsStatus = _locationManager.getGpsStatus(null);
                        if (gpsStatus != null) {
                            switch (event) {
                                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                                    int validNumber = 0;
                                    int visibleNumber = 0;
                                    for (GpsSatellite gpsSatellite : gpsStatus.getSatellites()) {
                                        if (gpsSatellite.usedInFix()) {
                                            validNumber++;
                                        }
                                        visibleNumber++;
                                    }
                                    _ftb.invokeMethod("onGpsStatus", String.valueOf(validNumber));
                                    break;
                            }
                        }
                    }
                };
                _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, _listener, _registrar.activeContext().getMainLooper());
                _locationManager.addGpsStatusListener(_gpsListener);
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

    @Override
    public void stop(MethodChannel.Result result) {
        stopGps();
    }

    private void stopGps(){
        if (_locationManager != null) {
            _locationManager.removeUpdates(_listener);
            _locationManager.removeGpsStatusListener(_gpsListener);
            _listener = null;
            _gpsListener = null;
            locating = false;
        }
    }

    @Override
    public void isLocating(MethodChannel.Result result) {
        HashMap rt=new HashMap();
        rt.put("status",true);
        rt.put("data", isStarted());
        result.success(rt);
    }

    @Override
    public void isOpenGPS(MethodChannel.Result result) {
        HashMap rt=new HashMap();
        rt.put("status",true);
        rt.put("data", isGPSAvailable());
        result.success(rt);
    }

    public boolean isStarted() {
        return locating;
    }

    @Override
    public void dispose() {
        super.dispose();
        stopGps();
        if(_locationManager != null) {
            _locationManager = null;
        }
    }
}
