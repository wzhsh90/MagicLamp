package com.rebo.bulb.utils;


import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wzhsh90 on 2016/5/7.
 */
public class EventBusUtil {

    public static void postEvent(String code, String content) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject();
            jsonObject.put("code", code);
            jsonObject.put("content", content);
            EventBus.getDefault().post(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public static void registerEvent(Object obj){
        EventBus.getDefault().register(obj);
    }
    public static void unRegisterEvent(Object obj){
        EventBus.getDefault().unregister(obj);
    }
}
