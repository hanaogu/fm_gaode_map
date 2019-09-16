package com.hhwy.fm_gaode_map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

/**
 * 工具类，用于通信
 */
public class FmToolsBase {
    /**
     * 插件对象
     */
    protected final PluginRegistry.Registrar _registrar;

    // 与flutter通信
    private MethodChannel _channel;
    // flutter通道名称
    final String _name;

    /**
     * 构造函数
     * @param name 名称
     * @param registrar flutter初始类
     */
    public FmToolsBase(final Object imp, String name, PluginRegistry.Registrar registrar){
        _name = name;
        _registrar = registrar;
        _channel = new MethodChannel(_registrar.messenger(),_name);
        _channel.setMethodCallHandler(new MethodChannel.MethodCallHandler(){
            @Override
            public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
                if ( imp != null ) {
                    FmToolsBase.onMethodCall(imp, methodCall, result);
                }
            }
        });
    }

    /**
     * 给flutter发送消息
     * @param method 方法名称
     * @param arguments 参数
     */
    public void invokeMethod(String method, Object arguments){
        if ( _channel ==null ){return;}
        _channel.invokeMethod(method, arguments);
    }

    /**
     * 销毁
     */
    public void dispose(){
        if ( _channel != null ) {
            _channel.setMethodCallHandler(null);
            _channel = null;
        }
    }
    static public  HashMap suc(Object obj){
        HashMap map = new HashMap();
        map.put("data",obj);
        return map;
    }
    /**
     * 调用无参数的方法
     * @param imp
     * @param method
     */
    static public  Object callMethod(Object imp, String method){
        Class<?> clazz = imp.getClass();
        try {
            Method ms = clazz.getDeclaredMethod(method);
            ms.setAccessible(true);
            return ms.invoke(imp);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
    static Method getMethod(Object imp, String method){
        Class<?> clazz = imp.getClass();
        Method[] ms = clazz.getDeclaredMethods();
        for (Method it : ms) {
            if (it.getName().equals(method)) {
                return it;
            }
        }
        return null;
    }
    /**
     * 调用n个参数的方法
     * @param imp
     * @param method
     */
    static public  Object callMethods(Object imp, String method,Object ...obj){
        Class<?> clazz = imp.getClass();
        try {
            Method[] ms = clazz.getDeclaredMethods();
            for (Method it : ms) {
                if (it.getName().equals(method)) {
                    return it.invoke(imp, obj);
                }
            }
        }catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 通过反射调用实例方法
     * @param imp
     * @param call
     * @param result
     */
    static public void onMethodCall(Object imp, MethodCall call, MethodChannel.Result result) {
        if ( imp ==null ){
            return;
        }
        Method m = getMethod(imp,call.method);
        if ( m== null ){
            result.notImplemented();
            return;
        }
        if ( m != null ){
            try {
                int len = m.getParameterTypes().length;
                if (len == 0 ){
                    m.invoke(imp);
                }
                else if ( len==1 ){
                    m.invoke(imp,result);
                }else {
                    m.invoke(imp, call.arguments == null ? new JSONObject() : new JSONObject((Map) call.arguments), result);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
//        callMethods(imp,call.method,call.arguments,result);
//        Class<?> clazz = imp.getClass();
//        try {
//            // 获取所有方法
//            Method[] ms = clazz.getDeclaredMethods();
//            ArrayList names = new ArrayList();
//            for(Method name : ms){
//                names.add(name.getName());
//            }
//            int index = names.indexOf(call.method);
//            // 判断是否含有被调用方法
//            if(index == -1){
//                HashMap rt=new HashMap();
//                rt.put("status",false);
//                rt.put("msg", "无此方法");
//                result.success(rt);
//                System.out.println("无此方法: "+call.method);
//                return;
//            }
//            // 获取方法参数
//            Class[] paramTypes = ms[index].getParameterTypes();
//            //是否存在Json参数
//            boolean jsonPara = false;
//            //是否存在Result参数
//            boolean resultPara = false;
//            if(paramTypes.length == 0){
//                jsonPara = false;
//                resultPara = false;
//            }else if(paramTypes.length == 1){
//                jsonPara = paramTypes[0].equals(JSONObject.class);
//                resultPara = paramTypes[0].equals(MethodChannel.Result.class);
//            }else if(paramTypes.length == 2){
//                jsonPara = paramTypes[0].equals(JSONObject.class);
//                resultPara = paramTypes[1].equals(MethodChannel.Result.class);
//            }
//            Method method = ms[index];
//            if(jsonPara && resultPara){
////                Method method = clazz.getDeclaredMethod(call.method, JSONObject.class, MethodChannel.Result.class);
//                method.setAccessible(true);
//                method.invoke(imp, new JSONObject((Map) call.arguments), result);
//            }else if(jsonPara && !resultPara){
////                Method method = clazz.getDeclaredMethod(call.method, JSONObject.class);
//                method.setAccessible(true);
//                method.invoke(imp, new JSONObject((Map) call.arguments));
//            }else if(!jsonPara && resultPara){
////                Method method = clazz.getDeclaredMethod(call.method, MethodChannel.Result.class);
//                method.setAccessible(true);
//                method.invoke(imp, result);
//            }else if(!jsonPara && !resultPara){
////                Method method = clazz.getDeclaredMethod(call.method);
//                method.setAccessible(true);
//                method.invoke(imp);
//            }

//            if (call.arguments != null) {
//                Method method = clazz.getDeclaredMethod(call.method, JSONObject.class);
//                method.setAccessible(true);
//                Object r = method.invoke(imp, new JSONObject((Map) call.arguments));
//                if(r == null){
//                    return;
//                }
//                result.success(r);
//            } else {
//                Method method = clazz.getDeclaredMethod(call.method);
//                method.setAccessible(true);
//                Object r = method.invoke(imp);
//                if(r == null){
//                    return;
//                }
//                result.success(r);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            result.notImplemented();
//        }
    }
    /**
     * 调整图片大小
     *
     * @param bitmap
     *            源
     * @param scale_w
     *            输出宽度
     * @param scale_h
     *            输出高度
     * @return
     */
    public static Bitmap imageScale(Bitmap bitmap, float scale_w, float scale_h) {
        int src_w = bitmap.getWidth();
        int src_h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scale_w, scale_h);
        Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix,
                true);
        return dstbmp;
    }
    public static Bitmap textBitmap(Bitmap bitmap, String text, float textSize, int textColor) {
        if (bitmap != null && !text.isEmpty()) {
            Bitmap temp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(temp);
            canvas.drawBitmap(bitmap, 0, 0, null);
            Paint paint = new Paint();
            if ( textSize > 0 ) {
                paint.setTextSize(textSize);
            }
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setColor(textColor);
            Rect rect = new Rect();
            paint.getTextBounds(text, 0, text.length(), rect);
//            float x = (bitmap.getWidth() - rect.width()) / 2f-1.0f;
            float y = (bitmap.getHeight() + rect.height()) / 2f-1.0f;
//            canvas.drawText(text, x, y, paint);
            canvas.drawText(text, bitmap.getWidth()/2.0f-1.0f, y, paint);
            bitmap.recycle();
            return temp;
        }
        return bitmap;
    }
    public HashMap JsonObject2HashMap(JSONObject jo) {
        HashMap<String,Object> hm = new HashMap<>();
        for (Iterator<String> keys = jo.keys(); keys.hasNext();) {
            try {
                String key1 = keys.next();
//                if (jo.get(key1) instanceof JSONObject) {
//                    JsonObject2HashMap((JSONObject) jo.get(key1));
//                    continue;
//                }
                if(key1.equals("icon")){
                    continue;
                }
                hm.put(key1, jo.get(key1).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return hm;
    }
}
