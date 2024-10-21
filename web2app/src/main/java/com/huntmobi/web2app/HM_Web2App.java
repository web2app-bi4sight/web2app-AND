package com.huntmobi.web2app;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import android.app.Activity;
import android.os.Bundle;
public class HM_Web2App {
    private static final String HM_SharedPreferences_Info = "HM_SharedPreferences_Info";
    private static HM_Web2App sharedInstance;
    public String deviceTrackID;
    private String[] eventNamesArray;
    private static String atcString;
    private static String cbcString;
    private boolean isAddRequest;
    public static String appname;
    private static Application mApplication;
    static String baseURL = "https://cdn.bi4sight.com";
    public  String Uid;
    static int callbackNum = 0;
    private static attibuteCallback attCallback;
    private static boolean isFirst = true;
    public static synchronized HM_Web2App getInstance(Application ap) {
        if (sharedInstance == null) {
            sharedInstance = new HM_Web2App();
            sharedInstance.isAddRequest = false;
            cbcString = "";
            atcString = "";
            sharedInstance.deviceTrackID = "";
            appname = "";
            mApplication = ap;
            sharedInstance.Uid = "";
            registerLifecycleCallbacks();
            isFirst = true;
        }
        return sharedInstance;
    }

    private static void registerLifecycleCallbacks() {
        mApplication.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
                // 应用从后台回到前台时触发此方法
                HM_Web2App.sharedInstance.onAppForeground();
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }

    private void onAppForeground() {
        if (isFirst) {
            isFirst = false;
        } else {
            attibute(attCallback);
        }
    }

    public interface attibuteCallback {
        void onSuccess(JSONObject data);
    }
    public void attibuteWithAppname(String AppName, attibuteCallback successBlock) {
        callbackNum += 1;
        appname = AppName;
        attCallback = successBlock;
        HM_RequestManager requestManager = HM_RequestManager.getInstance(mApplication);
        HM_DeviceInfoHelper deviceInfoHelper = new HM_DeviceInfoHelper(mApplication);
        String brand = deviceInfoHelper.getBrand();
        if (brand.equalsIgnoreCase("HUAWEI")) {
            init(successBlock);
        } else {
            checkGoogleAdsId(new GoogleAdsIdCallback() {
                @Override
                public void onGoogleAdsIdChecked(boolean isGoogleAdsIdNull) {
                    try {
                        if (isGoogleAdsIdNull) {
                            fetchAndInit(successBlock);
                        } else {
                            init(successBlock);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        init(successBlock);
                    }
                }
            });
        }
    }

    private void init(attibuteCallback successBlock) {
        HM_DeviceData.getInstance(mApplication).saveWADeviceInfo();

        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("HM_Device_Id", Uid);

        String isFirstInsert = sharedPreferences.getString("HM_isFirstInsert", "1");
        if ("0".equals(isFirstInsert)) {
            atcString = "launch";
            attibute(successBlock);
        } else {
            editor.putString("HM_isFirstInsert", "0");
            editor.apply();
            HM_ClipboardUtil.getClipboardText(mApplication, new HM_ClipboardUtil.PasteDataCallback() {
                @Override
                public void onSuccess(String pasteData) {
                    if (!TextUtils.isEmpty(pasteData) && pasteData.startsWith("w2a_data:")) {
                        String preStr = "w2a_data:";
                        if (pasteData.startsWith(preStr)) {// 剪切板有包含w2a_data:开头的数据
                            cbcString = pasteData;
                        }
                    }
                    atcString = "add";
                    hmGetWebViewInfo(successBlock);
                }
            });
        }

    }

    private void hmGetWebViewInfo(attibuteCallback successBlock) {
        try {
            SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
            String string = sharedPreferences.getString("HM_WebView_Fingerprint", null);
            if (string != null && !string.isEmpty()) {
                attibute(successBlock);
            } else {
                HM_WebView webView = new HM_WebView(mApplication);
                HM_WebView.GetFingerprint(new HM_WebView.FingerprintCallback() {
                    @Override
                    public void onCallback(String Fingerprint) {
                        attibute(successBlock);
                    }
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
            attibute(successBlock);
        }
    }

    private void attibute(attibuteCallback successBlock) {
//        Log.d("HM_Web2App", "222222222222222222222222222");
        JSONObject dic = setRequestInfo();
        cbcString = "";

        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        String url = baseURL + HM_UrlConfig.W2A_ATTRIBUTE;
        String deviceID = sharedPreferences.getString("HM_Device_Id", "");
        HM_RequestManager.HttpRequest.Callback callback = new HM_RequestManager.HttpRequest.Callback() {
            @Override
            public void onSuccess(Map<String, String> response) {
                try {
                    JSONObject jsonResponse = new JSONObject(Objects.requireNonNull(response.get("response")));
                    String code = jsonResponse.optString("code");
                    if ("0".equals(code)) {
                        JSONObject data = jsonResponse.optJSONObject("data");
                        assert data != null;
                        String w2aDataEncrypt = data.optString("w2akey");
                        String dtid = data.optString("dtid");
                        data.put("isAttribution", w2aDataEncrypt.length() > 0);

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("HM_W2a_Data", w2aDataEncrypt);
                        editor.putString("HM_WEB2APP_DTID", dtid);
                        editor.apply();
                            if (callbackNum > 0) {
                                successBlock.onSuccess(data);
                                callbackNum -= 1;
                            }
                    } else {
                        if (callbackNum > 0) {
                            successBlock.onSuccess(null);
                            callbackNum -= 1;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (callbackNum > 0) {
                        successBlock.onSuccess(null);
                        callbackNum -= 1;
                    }
                }
            }
            @Override
            public void onFailure(int errorCode, String errorMessage) {
                // Handle failure response
                if (callbackNum > 0) {
                    successBlock.onSuccess(null);
                    callbackNum -= 1;
                }
            }
        };
        HM_RequestManager.sendHttpPostRequest(url, dic.toString(), deviceID, true, callback);
    }

    public void eventPostWithEventInfo(HM_EventInfoModel eventInfoModel) {
        JSONObject data = setEventRequestInfo(eventInfoModel.toMap());
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        String url = baseURL + HM_UrlConfig.W2A_EVENTPOST;
        String deviceID = sharedPreferences.getString("HM_Device_Id", "");
        HM_RequestManager.HttpRequest.Callback callback = new HM_RequestManager.HttpRequest.Callback() {
            @Override
            public void onSuccess(Map<String, String> response) {
            }
            @Override
            public void onFailure(int errorCode, String errorMessage) {
            }
        };
        HM_RequestManager.sendHttpPostRequest(url, data.toString(), deviceID, true, callback);
    }

    public void updateUserInfo(HM_UserInfoModel userInfoModel) {
        JSONObject data = setUserRequestInfo(userInfoModel.toMap());
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        String url = baseURL + HM_UrlConfig.W2A_CUSTOMERINFO;
        String deviceID = sharedPreferences.getString("HM_Device_Id", "");
        HM_RequestManager.HttpRequest.Callback callback = new HM_RequestManager.HttpRequest.Callback() {
            @Override
            public void onSuccess(Map<String, String> response) {
            }
            @Override
            public void onFailure(int errorCode, String errorMessage) {
            }
        };
        HM_RequestManager.sendHttpPostRequest(url, data.toString(), deviceID, true, callback);
    }

    private JSONObject setRequestInfo() {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        String ua = "";
        try {
            String jsonString = sharedPreferences.getString("HM_WebView_Fingerprint", "");
            if (!jsonString.isEmpty()) {
                JSONObject jsonObject = new JSONObject(jsonString);
                ua = jsonObject.optString("ua");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        long timestamp = System.currentTimeMillis() / 1000L;
        String guid = getGUID();
        String dtid = sharedPreferences.getString("HM_WEB2APP_DTID", "");

        String deviceInfoString = sharedPreferences.getString("HM_WADevice_Data", null);
        JSONObject deviceInfo = new JSONObject();
        if (deviceInfoString != null) {
            try {
                deviceInfo = new JSONObject(deviceInfoString);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            HM_DeviceData.getInstance(mApplication).saveWADeviceInfo();
        }
        String w2akey = sharedPreferences.getString("HM_W2a_Data", "");
        JSONObject dic = new JSONObject();
        try {
            dic.put("device", deviceInfo);
            dic.put("cbc", cbcString);
            dic.put("ua", ua);
            dic.put("ts", timestamp);
            dic.put("option", atcString);
            dic.put("eid", guid);
            dic.put("action", "attribute");
            dic.put("app_name", appname);
            dic.put("w2akey", TextUtils.isEmpty(w2akey) ? "" : w2akey);
            dic.put("dt_id", "add".equals(atcString) ? deviceTrackID : (TextUtils.isEmpty(dtid) ? "" : dtid));

            dic.put("w2a_data_encrypt", TextUtils.isEmpty(w2akey) ? "" : w2akey);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dic;
    }

    private JSONObject setEventRequestInfo(JSONObject dic) {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        JSONObject newDic = dic != null ? dic : new JSONObject();  // 确保 dic 不为 null
        String deviceInfoString = sharedPreferences.getString("HM_WADevice_Data", null);
        JSONObject deviceInfo = new JSONObject();

        if (deviceInfoString != null) {
            try {
                deviceInfo = new JSONObject(deviceInfoString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            // 如果没有设备信息，则调用方法保存设备信息
            HM_DeviceData.getInstance(mApplication).saveWADeviceInfo();
        }

        // 获取 W2A key，如果为空则使用空字符串
        String w2akey = sharedPreferences.getString("HM_W2a_Data", "");

        try {
            // 更新 newDic 内容
            newDic.put("device", deviceInfo);
            newDic.put("app_name", appname);
            newDic.put("w2akey", TextUtils.isEmpty(w2akey) ? "" : w2akey);
            newDic.put("eid", getGUID());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newDic;
    }


    private JSONObject setUserRequestInfo(JSONObject dic) {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);

        // 确保 dic 不为 null，如果为 null 则创建新的 JSONObject
        JSONObject newDic = dic != null ? dic : new JSONObject();

        String deviceInfoString = sharedPreferences.getString("HM_WADevice_Data", null);
        JSONObject deviceInfo = new JSONObject();

        if (deviceInfoString != null) {
            try {
                deviceInfo = new JSONObject(deviceInfoString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            HM_DeviceData.getInstance(mApplication).saveWADeviceInfo();
        }

        // 获取 W2A key
        String w2akey = sharedPreferences.getString("HM_W2a_Data", "");

        try {
            // 更新 newDic 中的内容
            newDic.put("device", deviceInfo);
            newDic.put("app_name", appname);  // appname 需要确保已定义
            newDic.put("w2akey", TextUtils.isEmpty(w2akey) ? "" : w2akey);
            newDic.put("eid", getGUID());  // getGUID() 方法需要确保存在
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newDic;
    }

    public static String getGUID() {
        return UUID.randomUUID().toString();
    }

    private static void checkGoogleAdsId(GoogleAdsIdCallback callback) {
        try {
            SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
            String HM_Google_ADS = sharedPreferences.getString("HM_Google_ADS", "");
            callback.onGoogleAdsIdChecked(TextUtils.isEmpty(HM_Google_ADS));
        } catch (Exception e) {
            e.printStackTrace();
            callback.onGoogleAdsIdChecked(false);
        }
    }

    private void fetchAndInit(attibuteCallback successBlock) {
        try {
            HM_AdvertisingIdHelper.getAdvertisingId(mApplication, new HM_AdvertisingIdHelper.AdvertisingIdCallback() {
                @Override
                public void onAdvertisingIdObtained(String advertisingId) {
                    SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (advertisingId != null) {
                        // 获取到广告 ID，处理逻辑
                        editor.putString("HM_Google_ADS", advertisingId);
                    } else {
                        // 获取广告 ID 失败，处理逻辑
                        editor.putString("HM_Google_ADS", "");
                    }
                    editor.apply();
                    init(successBlock);
                }

                @Override
                public void onPermissionDenied() {
                    // 用户拒绝了权限，处理逻辑
                    SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("HM_Google_ADS", "");
                    editor.apply();
                    init(successBlock);
                }

                @Override
                public void onExceptionOccurred(Exception e) {
                    // 获取广告 ID 过程中发生了异常，处理逻辑
                    SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("HM_Google_ADS", "");
                    editor.apply();
                    init(successBlock);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            init(successBlock);
        }
    }
    interface GoogleAdsIdCallback {
        void onGoogleAdsIdChecked(boolean isGoogleAdsIdNull);
    }
}
