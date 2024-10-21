package com.huntmobi.web2app;

import android.content.Context;

import org.json.JSONObject;

import java.util.Map;

public class HM_Event {

    private static final String baseURL = "https://sl.bi4sight.com";
    private static final String slattibute = "slattibute";

    private static final String baseWAURL = "https://capi.bi4sight.com";
    private static final String attribute = "w2a/attribute";
    private static final String eventpost = "w2a/eventpost";
    private static final String customerinfo = "w2a/customerinfo";

    private static HM_Event sharedInstance;
    private Context context;

    private HM_Event(Context context) {
        this.context = context;
    }

    public static synchronized HM_Event getInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new HM_Event(context.getApplicationContext());
        }
        return sharedInstance;
    }

    public void event(String eventName, Map<String, Object> values, final HM_RequestManager.HttpRequest.Callback callback) {
        String url = setUrlWithEvent(eventName);
        JSONObject requestBody = new JSONObject(values);

        HM_RequestManager.sendHttpPostRequest(url, requestBody.toString(), null, true, new HM_RequestManager.HttpRequest.Callback() {
            @Override
            public void onSuccess(Map<String, String> response) {
                if (callback != null) {
                    callback.onSuccess(response);
                }
            }

            @Override
            public void onFailure(int errorCode, String errorMessage) {
                if (callback != null) {
                    callback.onFailure(errorCode, errorMessage);
                }
            }
        });
    }

    private String setUrlWithEvent(String eventName) {
        String path = "";
        if ("CompleteRegistration".equals(eventName)) {
            path = slattibute;
        }
        return baseURL + "/" + path;
    }

    // 发送 W2A 事件
    public void WAEvent(String eventName, Map<String, Object> values, final HM_RequestManager.HttpRequest.Callback callback) {
        String url = setWAUrlWithEvent(eventName);
        JSONObject requestBody = new JSONObject(values);

        HM_RequestManager.sendHttpPostRequest(url, requestBody.toString(), null, true, new HM_RequestManager.HttpRequest.Callback() {
            @Override
            public void onSuccess(Map<String, String> response) {
                if (callback != null) {
                    callback.onSuccess(response);
                }
            }

            @Override
            public void onFailure(int errorCode, String errorMessage) {
                if (callback != null) {
                    callback.onFailure(errorCode, errorMessage);
                }
            }
        });
    }

    private String setWAUrlWithEvent(String eventName) {
        String path = "";
        if ("CompleteRegistration".equals(eventName)) {
            path = attribute;
        } else if ("EventPost".equals(eventName)) {
            path = eventpost;
        } else if ("UpDateUserInfo".equals(eventName)) {
            path = customerinfo;
        }
        return baseWAURL + "/" + path;
    }
}
