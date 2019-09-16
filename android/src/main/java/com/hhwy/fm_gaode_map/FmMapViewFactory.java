package com.hhwy.fm_gaode_map;

import android.content.Context;
import android.view.View;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MessageCodec;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

public class FmMapViewFactory extends PlatformViewFactory {
    final private HashMap<String, FmMapView> _list= new HashMap<>();

    final private PluginRegistry.Registrar _registrar;
    public FmMapViewFactory(MessageCodec<Object> createArgsCodec,PluginRegistry.Registrar registrar) {
        super(createArgsCodec);
        _registrar = registrar;
    }
    /**
     * 创建一个视图
     * @param name 名字
     */
    public com.amap.api.maps2d.MapView createView(String name){
        if ( _list.containsKey(name)){
//            System.out.println("has mapview："+name);
            return  _list.get(name).view();
        }
        FmMapView view = new FmMapView(name,_registrar,this);
        _list.put(name,view);
        return  view.view();
    }

    /**
     * 移除一个视图
     * @param name
     */
    void remove(String name, boolean onlyRemove){
        System.out.println("remove view："+name);
        if ( onlyRemove ) {
            _list.remove(name);
            return;
        }
        FmMapView view = _list.get(name);
        if ( view ==null ){
            System.out.println("dispose view empty:"+name);
            return;
        }
        view.dispose();
        _list.remove(name);
    }

    @Override
    public PlatformView create(final Context context, int i, final Object obj) {
        return new PlatformView() {
            @Override
            public View getView() {
                Map<String,Object> m = (Map)obj;
                return  createView(m.get("name").toString());
            }

            @Override
            public void dispose() {
                Map<String,Object> m = (Map)obj;
                remove(m.get("name").toString(),false);
            }
        };
    }
}
