package com.hhwy.fm_gaode_map;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.Circle;
import com.amap.api.maps2d.model.CircleOptions;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.maps2d.model.Polyline;
import com.amap.api.maps2d.model.PolylineOptions;
import com.amap.api.maps2d.model.Text;
import com.amap.api.maps2d.model.TextOptions;
import com.hhwy.fmpermission.FmPermission;

public class FmMapView{
    private FmToolsBase _ftb;
    protected final PluginRegistry.Registrar _registrar;
    private MapView _view;
    private AMap _bmp;

    private final FmMapViewFactory _factory;
    private final HashMap<String, FmOverlay>_overlays = new HashMap<>();
    class FmOverlayItem{
        Object overlay;
        String id;
        String entityId;// 图形实体id
        JSONObject config;
    }
    class FmOverlay{
        final private HashMap <String,FmOverlayItem>_list= new HashMap<>();
        void add(String id,String entityId,Object overlay,JSONObject config){
            FmOverlayItem item = new  FmOverlayItem();
            item.id = id;
            item.entityId = entityId;
            item.config = config;
            item.overlay = overlay;
            _list.put(id,item);
        }

        /**
         * 获取一个对象
         * @param id
         * @return
         */
        FmOverlayItem get(String id){
            return _list.get(id);
        }

        /**
         * 根据实体id查找
         * @param entityId
         * @return
         */
        FmOverlayItem getByEntityId(String entityId){
            for (Map.Entry<String,FmOverlayItem> it:_list.entrySet()) {
                if ( it.getValue().entityId==entityId){
                    return  it.getValue();
                }
            }
            return  null;
        }
        /**
         * 移除一个对象
         * @param id
         */
        boolean remove(String id){
            if ( !_list.containsKey(id) ){
                return false;
            }
            FmOverlayItem item = _list.get(id);
            FmToolsBase.callMethod(item.overlay,"remove");
            _list.remove(id);
            return  true;
        }

        /**
         * 全部移除
         */
        void removeAll(){
            for (Map.Entry<String,FmOverlayItem> it:_list.entrySet()) {
                FmToolsBase.callMethod(it.getValue().overlay,"remove");
            }
            _list.clear();
        }

        /**
         * 设置显示顺序
         * @param id
         * @param index
         * @return
         */
        boolean setIndex(String id, float index){
            if ( !_list.containsKey(id) ){
                return false;
            }
            FmOverlayItem item = _list.get(id);
            FmToolsBase.callMethods(item.overlay,"setZIndex",index);
//            item.overlay.setZIndex(index);
            return  true;
        }

        /**
         * 设置所有元素的显示顺序
         * @param index
         */
        void  setIndexAll(float index){
            for (Map.Entry<String,FmOverlayItem> it:_list.entrySet()) {
                FmToolsBase.callMethods(it.getValue().overlay,"setZIndex",index);
//                it.getValue().overlay.setZIndex(index);
            }
            _list.clear();
        }
        /**
         * 设置显示或隐藏
         * @param id
         * @param visible
         * @return
         */
        boolean setVisible(String id, boolean visible){
            if ( !_list.containsKey(id) ){
                return false;
            }
            FmOverlayItem item = _list.get(id);
            FmToolsBase.callMethods(item.overlay,"setVisible",visible);
//            item.overlay.setVisible(visible);
            return  true;
        }

        /**
         * 设置所有元素的显示或隐藏
         * @param visible
         */
        void  setVisibleAll(boolean visible){
            for (Map.Entry<String,FmOverlayItem> it:_list.entrySet()) {
                FmToolsBase.callMethods(it.getValue().overlay,"setVisible",visible);
//                it.getValue().overlay.setVisible(visible);
            }
            _list.clear();
        }
    }

    /**
     * 根据实体id查找
     * @param entityId
     * @return
     */
    FmOverlayItem getByEntityId(String entityId){
        for (Map.Entry<String,FmOverlay>item:_overlays.entrySet()){
            FmOverlayItem it = item.getValue().getByEntityId(entityId);
            if (  it!= null ){
               return it;
            }
        }
        return null;
    }
    /**
     * 根据id查找
     * @param id
     * @return
     */
    FmOverlayItem getById(String id){
        for (Map.Entry<String,FmOverlay>item:_overlays.entrySet()){
            FmOverlayItem it = item.getValue().get(id);
            if (  it!= null ){
                return it;
            }
        }
        return null;
    }
    void _clickOverlay(Object overlay){
        if ( overlay == null ){
            return;
        }
        String id = (String)FmToolsBase.callMethod(overlay,"getId");
        if ( id == null ){
            return;
        }
        FmOverlayItem item = getByEntityId(id);
        if ( item != null ){
            _ftb.invokeMethod("click_overlay",_ftb.JsonObject2HashMap(item.config));
        }
    }
    /**
     * 构造函数
     * @param registrar
     */
    FmMapView(String name,final PluginRegistry.Registrar registrar, final FmMapViewFactory factory){
        _registrar = registrar;
        _ftb = new FmToolsBase(this, name, registrar);
        _factory = factory;

        _view=new MapView(registrar.activity());
        _view.onCreate(new Bundle());
        _bmp = _view.getMap();
        _bmp.moveCamera(CameraUpdateFactory.zoomTo(17f));
        UiSettings uiSettings = _bmp.getUiSettings();

        //设置log位置
        uiSettings.setScaleControlsEnabled(true);//控制比例尺控件是否显示
        uiSettings.setCompassEnabled(true); //控制指南针的显示和隐藏。
        uiSettings.setZoomControlsEnabled(true); //缩放控件
        uiSettings.setMyLocationButtonEnabled(false);//定位按钮

        System.out.println("====");
        System.out.println(_bmp);

        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);
        _bmp.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        _bmp.setMyLocationEnabled(true);

        FmPermission.getPermission(new Observer<Boolean>() {
           @Override
           public void onSubscribe(Disposable d) {
               System.out.println("onSubscribe: " + d.toString());
           }

           @Override
           public void onNext(Boolean aBoolean) {

//               _view=new MapView(registrar.activity());
//               _bmp = _view.getMap();
//               _bmp.setMyLocationEnabled(true);
           }

            @Override
            public void onError(Throwable e) {
                System.out.println("onError:"+e.toString());
            }

            @Override
            public void onComplete() {
               System.out.println("onComplete: success");
            }
        },
            FmPermission.FmPermissionType.LOCATION,
            FmPermission.FmPermissionType.STORAGE,
            FmPermission.FmPermissionType.PHONE,
            FmPermission.FmPermissionType.STORAGE
        );

        _bmp.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                _clickOverlay(marker);
                return true;
            }
        });
        _bmp.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                _onMapStatus("onMapStatusChangeStart",cameraPosition);
            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                _onMapStatus("onMapStatusChangeFinish",cameraPosition);
            }
        });
        _bmp.setOnInfoWindowClickListener(new AMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                FmOverlayItem item = getByEntityId(marker.getId());
                if ( item != null ) {
                    HashMap<String,Object> m = new HashMap<>();
                    m.put("data",item.config);
                    m.put("id",item.id);
                    _ftb.invokeMethod("onInfoWindowClick", m);
                }
            }
        });
    }
    private  void _onMapStatus(String name,CameraPosition cameraPosition){
        HashMap<String,Object> m = new HashMap<>();
        m.put("latitude",cameraPosition.target.latitude);
        m.put("longitude",cameraPosition.target.longitude);
        m.put("zoom",cameraPosition.zoom);
        m.put("overlook",cameraPosition.bearing);
//        m.put("rotate",cameraPosition.rotate);
//        m.put("screenX",cameraPosition.targetScreen.x);
//        m.put("screenY",cameraPosition.targetScreen.y);
        _ftb.invokeMethod(name,m);
    }

    /**
     * 设置地图定位点
     * @param obj
     * String name, 对象名称
     * double latitude, 经度
     * double longitude, 纬度
     * float direction, 方向
     * float accuracy 圈
     */
    public  void setCurrentPoint(final JSONObject obj, MethodChannel.Result result) {
        HashMap rt=new HashMap();
        try {
            setCurrentPointImp(
                    obj.getDouble("latitude"),
                    obj.getDouble("longitude"),
                    (float) obj.getDouble("direction"),
                    (float)obj.getDouble("accuracy")
            );
            rt.put("status",true);
            rt.put("data", obj.toString());
            result.success(rt);
        } catch (JSONException e) {
            e.printStackTrace();
            rt.put("status",false);
            rt.put("msg", e.toString());
            result.success(rt);
        }
    }

    /**
     * 显示标注内容
     * @param obj
     * @param result
     */
    public  void showInfoWindow(final JSONObject obj, MethodChannel.Result result) {
        try {
            FmOverlayItem item = getById(obj.getString("id"));
            if ( item == null ){
                result.success(FmToolsBase.suc(false));
                return;
            }
            Marker mk = (Marker)item.overlay;
            if ( mk != null ){
                if ( obj.has("title") ){
                    mk.setTitle(obj.getString("title"));
                }
                if ( obj.has("snippet") ){
                    mk.setSnippet(obj.getString("snippet"));
                }
                if ( obj.getBoolean("visible") ) {
                    mk.showInfoWindow();
                }else{
                    mk.hideInfoWindow();
                }
            }
            result.success(FmToolsBase.suc(true));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /**
     * 设置地图定位到指定点
     * @param obj
     * latitude
     * longitude
     * overlook
     * rotate
     * zoom
     */
    public  void setCenter(final JSONObject obj, MethodChannel.Result result) {
        HashMap rt=new HashMap();
        try {
            setCenterImp(
                    new LatLng(obj.getDouble("latitude"),obj.getDouble("longitude")),
                    (float) obj.getDouble("overlook"),
                    (float)obj.getDouble("rotate"),
                    obj.has("zoom")?(float)obj.getDouble("zoom"):_bmp.getCameraPosition().zoom,
                    null
            );
            rt.put("status",true);
            rt.put("data", obj.toString());
            result.success(rt);
        } catch (JSONException e) {
            e.printStackTrace();
            rt.put("status",false);
            rt.put("msg", e.toString());
            result.success(rt);
        }
    }
    /**
     * 设置定位点
     * @param latitude 经度
     * @param longitude 纬度
     * @param direction 方向
     * @param accuracy 圈大小
     */
    void setCurrentPointImp(double latitude, double longitude,float direction, float accuracy){
//        MyLocationData myLocationData = new MyLocationData.Builder()
//                .direction(direction )
//                .accuracy(accuracy )
//                .latitude(latitude)
//                .longitude(longitude)
//                .build();
//        _bmp.setMyLocationData(myLocationData);
    }

    /**
     * 获取内部view
     * @return MapView
     */
    com.amap.api.maps2d.MapView view(){return _view;}

    /**
     * 设置地图定位到指定点
     * @param latLng
     * @param overlook
     * @param rotate
     * @param zoom
     * @param point
     */
    void setCenterImp(final LatLng latLng,
                   final float overlook,
                   final float rotate,
                   final float zoom,
                   final Point point) {

        System.out.println("center begin");
        if (_view == null) {
            return;
        }
        _bmp.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng
                , zoom, overlook, rotate)));

        System.out.println("center end");
//        setStatus(new LatLng(request.getDouble("latitude", 0), request.getDouble("longitude", 0)),
//                1, 360, gaodeMap.getCameraPosition().zoom, null);

//        MapStatus.Builder builder = new MapStatus.Builder();
//        if (latLng != null) {
//            builder.target(latLng);
//        }
//        if (overlook >= -45 && overlook <= 0) {
//            builder.overlook(overlook);
//        }
//        if (rotate >= -180 && rotate <= 180) {
//            builder.rotate(rotate);
//        }
//        if (zoom >= 1 && zoom <= 21) {
//            builder.zoom(zoom);
//        }
//        if (point != null) {
//            builder.targetScreen(point);
//        }
//        _bmp.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }

    /**
     * 绘制文字覆盖物
     * @param obj
     */
    void addOverlays(final JSONObject obj, MethodChannel.Result result) {
        HashMap rt=new HashMap();
        try {
            JSONArray arr = obj.getJSONArray("objects");
            for( int i=0; i<arr.length();++i){
                JSONObject item = arr.getJSONObject(i);
                _createOptions(item);
            }
            rt.put("status",true);
            rt.put("data", arr.toString());
            result.success(rt);
        } catch (JSONException e) {
            e.printStackTrace();
            rt.put("status",false);
            rt.put("msg", e.toString());
            result.success(rt);
        }
    }
    /**
     * 移除覆盖物
     * @param obj
     */
    void removeOverlays(final JSONObject obj, MethodChannel.Result result){
        HashMap rt=new HashMap();
        try {
            // 先取图层
            if ( obj.has("layer") ){
                FmOverlay item = _overlays.get(obj.getString("layer"));
                // 无id时，删除所有图层元素
                if ( obj.has("id")){
                    item.remove(obj.getString("id"));
                }else{
                    item.removeAll();
                    _overlays.remove(obj.getString("layer"));
                }
            }else{
                if ( !obj.has("id")) {
                    System.out.println("removeOverlays error:need id or layer");
                    return;
                }
                // 查找id，进行删除
                for (Map.Entry<String,FmOverlay>item:_overlays.entrySet()){
                    if ( item.getValue().remove(obj.getString("id")) ){
                        break;
                    }
                }
            }
            rt.put("status",true);
            rt.put("data", obj.toString());
            result.success(rt);
        } catch (JSONException e) {
            e.printStackTrace();
            rt.put("status",false);
            rt.put("msg", e.toString());
            result.success(rt);
        }
    }
    /**
     * 设置显示顺序
     * @param obj
     */
    void setOverlaysIndex(final JSONObject obj, MethodChannel.Result result){
        HashMap rt=new HashMap();
        try {
            // 先取图层
            if ( obj.has("layer") ){
                FmOverlay item = _overlays.get(obj.getString("layer"));
                // 无id时，设置所有图层元素
                if ( obj.has("id")){
                    item.setIndex(obj.getString("id"),obj.getInt("zIndex"));
                }else{
                    item.setIndexAll(obj.getInt("zIndex"));
                }
            }else{
                if ( !obj.has("id")) {
                    System.out.println("setOverlaysIndex error:need id or layer");
                    return;
                }
                // 查找id
                for (Map.Entry<String,FmOverlay>item:_overlays.entrySet()){
                    if ( item.getValue().setIndex(obj.getString("id"),obj.getInt("zIndex")) ){
                        break;
                    }
                }
            }
            rt.put("status",true);
            rt.put("data", obj.toString());
            result.success(rt);
        } catch (JSONException e) {
            e.printStackTrace();
            rt.put("status",false);
            rt.put("msg", e.toString());
            result.success(rt);
        }
    }
    /**
     * 设置显示或隐藏
     * @param obj
     */
    void setOverlaysVisible(final JSONObject obj, MethodChannel.Result result){
        HashMap rt=new HashMap();
        try {
            // 先取图层
            if ( obj.has("layer") ){
                FmOverlay item = _overlays.get(obj.getString("layer"));
                // 无id时，设置所有图层元素
                if ( obj.has("id")){
                    item.setVisible(obj.getString("id"),obj.getBoolean("visible"));
                }else{
                    item.setVisibleAll(obj.getBoolean("visible"));
                }
            }else{
                if ( !obj.has("id")) {
                    System.out.println("setOverlaysVisible error:need id or layer");
                    return;
                }
                // 查找id
                for (Map.Entry<String,FmOverlay>item:_overlays.entrySet()){
                    if ( item.getValue().setVisible(obj.getString("id"),obj.getBoolean("visible")) ){
                        break;
                    }
                }
            }
            rt.put("status",true);
            rt.put("data", obj.toString());
            result.success(rt);
        } catch (JSONException e) {
            e.printStackTrace();
            rt.put("status",false);
            rt.put("msg", e.toString());
            result.success(rt);
        }
    }

    /**
     * 更新已有实体元素
     * @param obj
     */
    void updateOverlays(final JSONObject obj, MethodChannel.Result result){
        HashMap rt=new HashMap();
        try {
            JSONArray arr = obj.getJSONArray("objects");
            for( int i=0; i<arr.length();++i){
                JSONObject item = arr.getJSONObject(i);
                for (Map.Entry<String,FmOverlay>it:_overlays.entrySet()){
                    // 移除成功后再加一遍
                    if ( it.getValue().remove(item.getString("id")) ){
                        _createOptions(item);
                        break;
                    }
                }
            }
            rt.put("status",true);
            rt.put("data", arr.toString());
            result.success(rt);
        } catch (JSONException e) {
            e.printStackTrace();
            rt.put("status",false);
            rt.put("msg", e.toString());
            result.success(rt);
        }
    }

    /**
     * 创建一个标记配置
     * @param obj
     * @return
     */
    private Object _createOptions(final JSONObject obj){
        try {
            String type = obj.getString("type");
            if ( type.equalsIgnoreCase("circle")) {
                // 圆心位置
                LatLng center = new LatLng(obj.getDouble("latitude"), obj.getDouble("longitude"));
                // 构造CircleOptions对象
                CircleOptions c = new CircleOptions().center(center)
                        .radius(obj.getInt("radius"));
                // 填充颜色
                if (obj.has("fillColor")) {
                    c.fillColor(obj.getInt("fillColor"));
                }
                // 边框宽和边框颜色
                if (obj.has("strokeWidth") && obj.has("strokeColor")) {
                    c.strokeColor(obj.getInt("strokeColor"));
                    c.strokeWidth(obj.getInt("strokeWidth"));
                }
                if ( obj.has("zIndex") ){
                    c.zIndex(obj.getInt("zIndex"));
                }
                if ( obj.has("visible") ){
                    c.visible(obj.getBoolean("visible"));
                }
                // 在地图上显示
                Circle entity = _bmp.addCircle(c);
                _addOver(obj, entity,entity.getId());
                return entity;
            }else if(type.equalsIgnoreCase("line")){
                JSONArray points = obj.getJSONArray("points");
                List<LatLng> pts = new ArrayList<LatLng>();
                for( int i=0; i<points.length();++i){
                    JSONObject item = points.getJSONObject(i);
                    pts.add(new LatLng(item.getDouble("latitude"),item.getDouble("longitude")));
                }
                PolylineOptions lin = new PolylineOptions().addAll(pts);
                if ( obj.has("color")){
                    lin.color(obj.getInt("color"));
                }
                if ( obj.has("width")){
                    lin.width(obj.getInt("width"));
                }
                if ( obj.has("dottedLine")){
                    lin.setDottedLine(obj.getBoolean("dottedLine"));
                }
                if ( obj.has("zIndex") ){
                    lin.zIndex(obj.getInt("zIndex"));
                }
                if ( obj.has("visible") ){
                    lin.visible(obj.getBoolean("visible"));
                }
                // 在地图上显示
                Polyline entity = _bmp.addPolyline(lin);
                _addOver(obj, entity,entity.getId());
                return entity;
            }else if( type.equalsIgnoreCase("mark")){
                LatLng center = new LatLng(obj.getDouble("latitude"), obj.getDouble("longitude"));
                MarkerOptions mk = new MarkerOptions().position(center);
//                Bitmap bitmap = BitmapDescriptorFactory.fromAsset(obj.getString("icon")).getBitmap();
                AssetManager assetManager = _ftb._registrar.context().getAssets();
                String key = _ftb._registrar.lookupKeyForAsset(obj.getString("icon"));

                Bitmap bitmap = null;
                try {
                    bitmap = BitmapFactory.decodeStream(assetManager.open(key));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if ( obj.has("scale") && obj.getDouble("scale")!=1.0 ){
                    bitmap = _ftb.imageScale(bitmap,(float)obj.getDouble("scale"),(float)obj.getDouble("scale"));
                }
                if (obj.has("text")) {
                    // 在图片上绘制文字
                    float textSize =-1;
                    if ( obj.has("textSize")){
                        textSize = (float)obj.getInt("textSize");
                    }
                    int textColor = Color.BLACK;
                    if ( obj.has("textColor")){
                        textColor = obj.getInt("textColor");
                    }
                    bitmap = _ftb.textBitmap(bitmap,obj.getString("text"),textSize, textColor);
                }
                mk.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                if ( obj.has("draggable") ){
                    mk.draggable(obj.getBoolean("draggable"));
                }
                if ( obj.has("title") ){
                    mk.title(obj.getString("title"));
                }
                if ( obj.has("snippet") ){
                    mk.snippet(obj.getString("snippet"));
                }
                if ( obj.has("zIndex") ){
                    mk.zIndex(obj.getInt("zIndex"));
                }
                if ( obj.has("visible") ){
                    mk.visible(obj.getBoolean("visible"));
                }
                if ( obj.has("anchorX") && obj.has("anchorY")){
                    mk.anchor((float)obj.getDouble("anchorX"),(float)obj.getDouble("anchorY"));
                }else{
                    mk.anchor(0.5f,0.5f);
                }

                // 在地图上显示
                Marker entity = _bmp.addMarker(mk);
                if ( obj.has("showInfoWindow") && obj.getBoolean("showInfoWindow")){
                    entity.showInfoWindow();
                }
                _addOver(obj, entity,entity.getId());
                return  entity;
                //System.out.println(option);
            }else if( type.equalsIgnoreCase("text")){
                LatLng llText = new LatLng(obj.getDouble("latitude"), obj.getDouble("longitude"));
                //构建TextOptions对象
                TextOptions txt = new TextOptions()
                        // 文字内容
                        .text(obj.getString("text"))
                        // 坐标
                        .position(new LatLng(obj.getDouble("latitude"),obj.getDouble("longitude")));

                if ( obj.has("bgColor") ){
                    txt.backgroundColor(obj.getInt("bgColor"));
                }
                if ( obj.has("rotate") ){
                    txt.rotate(obj.getInt("rotate"));
                }
                if ( obj.has("fontSize") ){
                    txt.fontSize(obj.getInt("fontSize"));
                }
                if ( obj.has("fontColor") ){
                    txt.fontColor(obj.getInt("fontColor"));
                }
                if ( obj.has("zIndex") ){
                    txt.zIndex(obj.getInt("zIndex"));
                }
                if ( obj.has("visible") ){
                    txt.visible(obj.getBoolean("visible"));
                }

                // 在地图上显示
                Text entity = _bmp.addText(txt);
                _addOver(obj, entity,null);
                return entity;
            }
            return  null;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  null;
    }

    /**
     * 添加覆盖物
     * @param obj 配置
     * @param it 覆盖物
     */
    private void _addOver(final JSONObject obj,Object it, String entityId){
        try {
            FmOverlay item;
            if ( !_overlays.containsKey(obj.getString("layer"))){
                item = new FmOverlay();
                _overlays.put(obj.getString("layer"),item);
            }else{
                item = _overlays.get(obj.getString("layer"));
            }
            //Bundle bundle = new Bundle();
            //bundle.putString("id",obj.getString("id"));
//            bundle.putString("layer",obj.getString("layer"));
//            it.setExtraInfo(bundle);
            item.add(obj.getString("id"),entityId, it, obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /**
     * 根据地域名称绘制区域
     * @param obj
     */
    public void addPolygonByName(final JSONObject obj, final MethodChannel.Result result){
        final HashMap rt = new HashMap();
//        _registrar.activity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                final DistrictSearch mDistrictSearch = DistrictSearch.newInstance();
//                OnGetDistricSearchResultListener listener = new OnGetDistricSearchResultListener() {
//                    @Override
//                    public void onGetDistrictResult(DistrictResult districtResult) {
//                        System.out.println(districtResult.error);
//                        if (null != districtResult && districtResult.error == SearchResult.ERRORNO.NO_ERROR) {
//                            //获取边界坐标点，并展示
//                            if (districtResult.error == SearchResult.ERRORNO.NO_ERROR) {
//                                List<List<LatLng>> polyLines = districtResult.getPolylines();
//                                if (polyLines == null) {
//                                    rt.put("status",false);
//                                    rt.put("msg", "失败");
//                                    result.success(rt);
//                                    return;
//                                }
//                                try {
//                                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
//                                    for (List<LatLng> polyline : polyLines) {
//                                        if ( obj.getBoolean("showLine") ) {
//                                            OverlayOptions ooPolyline11 = new PolylineOptions().width(obj.getInt("linewidth"))
//                                                    .points(polyline).dottedLine(true).color(obj.getInt("lineColor"));
//                                            _bmp.addOverlay(ooPolyline11);
//                                        }
//                                        if ( obj.getBoolean("showPolygon") ) {
//                                            OverlayOptions ooPolygon = new PolygonOptions().points(polyline)
//                                                    .stroke(new Stroke(obj.getInt("borderWidth"), obj.getInt("borderColor"))).fillColor(obj.getInt("fillColor"));
//                                            _bmp.addOverlay(ooPolygon);
//                                        }
////                                        for (LatLng latLng : polyline) {
////                                            builder.include(latLng);
////                                        }
//                                    }
//                                    if ( obj.getBoolean("fitScreen")) {
//                                        _bmp.setMapStatus(MapStatusUpdateFactory.newLatLngBounds(builder.build()));
//                                    }
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                    rt.put("status",false);
//                                    rt.put("msg", e.toString());
//                                    result.success(rt);
//                                }
//
//                            }
//                        }
//                        mDistrictSearch.destroy();
//                        rt.put("status",true);
//                        rt.put("data", obj.toString());
//                        result.success(rt);
//                    }
//                };
//                mDistrictSearch.setOnDistrictSearchListener(listener);
//                try {
//                    mDistrictSearch.searchDistrict(new DistrictSearchOption().cityName(obj.getString("name")));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    rt.put("status",false);
//                    rt.put("msg", e.toString());
//                    result.success(rt);
//                }
//            }
//        });
    }

    /**
     * 设置地图类型
     * @param obj
     */
    public void setMapType(final JSONObject obj, MethodChannel.Result result){
        HashMap rt=new HashMap();
        try {
            int type = obj.getInt("type");
            _bmp.setMapType(type);
            rt.put("status",true);
            rt.put("data", type);
            result.success(rt);
        } catch (JSONException e) {
            e.printStackTrace();
            rt.put("status",false);
            rt.put("msg", e.toString());
            result.success(rt);
        }
    }
    /**
     * 销毁
     */
    public void dispose(){
        _ftb.dispose();
        _bmp = null;
        _view = null;
    }
}
