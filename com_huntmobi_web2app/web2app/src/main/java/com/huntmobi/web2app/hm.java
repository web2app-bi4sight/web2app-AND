package com.huntmobi.web2app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class hm {
    static Application mApplication;
    private static final String TAG = "HuntMobiLog";
    static String baseURL = "https://cdn.bi4sight.com";
//    private static InitCallback initCallback; // 回调接口
    private static UpdateW2aDataCallback updateW2aDataCallback; // 回调接口
    static boolean isShowLog = false;

    private static final String HM_SharedPreferences_Info = "HM_SharedPreferences_Info";

    static boolean isUseFingerPrinting = true;

    public static void useFingerPrinting(boolean isUse) {
        isUseFingerPrinting = isUse;
    }

    static int callbackNum = 0;

    // <editor-fold desc="">

    // </editor-fold>

    /**
     * 初始化方法，用于初始化应用程序并执行相关操作。
     *
     * @param ap              应用程序的Application对象。
     * @param gateway         网关信息，用于初始化应用程序的网关设置。
     * @param installEventName 安装事件名称，用于跟踪应用程序的安装事件。
     * @param isNewUser       是否是新用户的标志，用于执行特定于新用户的操作。
     * @param appName         应用程序名称，用于标识应用程序。
     * @param successBlock    初始化成功后的回调函数，用于处理成功初始化后的操作。
     */
    public static void Init(Application ap, String gateway, String installEventName, boolean isNewUser, String appName, InitCallback successBlock) {
        mApplication = ap;
        baseURL = gateway;
        callbackNum += 1;
        if (isGatewayEmpty(gateway)) {
            if (callbackNum > 0) {
                successBlock.onSuccess(null); // 网关为空，直接给null（空对象）
                callbackNum -= 1;
            }
            return;
        }
        HM_RequestManager requestManager = HM_RequestManager.getInstance(mApplication);
        HM_DeviceInfoHelper deviceInfoHelper = new HM_DeviceInfoHelper(mApplication);
        String brand = deviceInfoHelper.getBrand();
        if (brand.equalsIgnoreCase("HUAWEI")) {
            init(ap, gateway, installEventName, appName, isNewUser, successBlock);
        } else {
            checkGoogleAdsId(new GoogleAdsIdCallback() {
                @Override
                public void onGoogleAdsIdChecked(boolean isGoogleAdsIdNull) {
                    try {
                        if (isGoogleAdsIdNull) {
                            fetchAndInit(ap, gateway, installEventName, appName, isNewUser, successBlock);
                        } else {
                            init(ap, gateway, installEventName, appName, isNewUser, successBlock);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        init(ap, gateway, installEventName, appName, isNewUser, successBlock);
                    }
                }
            });
        }
    }

    /**
     * 初始化方法，用于初始化应用程序并执行相关操作。
     *
     * @param ap              应用程序的Application对象。
     * @param gateway         网关信息，用于初始化应用程序的网关设置。
     * @param installEventName 安装事件名称，用于跟踪应用程序的安装事件。
     * @param isNewUser       是否是新用户的标志，用于执行特定于新用户的操作。
     * @param appName         应用程序名称，用于标识应用程序。
     * @param clipboardData   APP端使用剪切板后传值给SDK初始化，SDK不再获取剪切板操作
     * @param successBlock    初始化成功后的回调函数，用于处理成功初始化后的操作。
     */
    public static void Init(Application ap, String gateway, String installEventName, boolean isNewUser, String appName, String clipboardData, InitCallback successBlock) {
        mApplication = ap;
        baseURL = gateway;
        callbackNum += 1;
        if (isGatewayEmpty(gateway)) {
            if (callbackNum > 0) {
                successBlock.onSuccess(null); // 网关为空，直接给null（空对象）
                callbackNum -= 1;
            }
            return;
        }
        HM_RequestManager requestManager = HM_RequestManager.getInstance(mApplication);
        HM_DeviceInfoHelper deviceInfoHelper = new HM_DeviceInfoHelper(mApplication);
        String brand = deviceInfoHelper.getBrand();
        if (brand.equalsIgnoreCase("HUAWEI")) {
            init(ap, gateway, installEventName, appName, isNewUser, successBlock);
        } else {
            checkGoogleAdsId(new GoogleAdsIdCallback() {
                @Override
                public void onGoogleAdsIdChecked(boolean isGoogleAdsIdNull) {
                    try {
                        if (isGoogleAdsIdNull) {
                            fetchAndInit(ap, gateway, installEventName, appName, isNewUser, clipboardData, successBlock);
                        } else {
                            init(ap, gateway, installEventName, appName, isNewUser, successBlock);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        init(ap, gateway, installEventName, appName, isNewUser, successBlock);
                    }
                }
            });
        }
    }

    private static boolean isGatewayEmpty(String gateway) {
        return TextUtils.isEmpty(gateway);
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

    private static void fetchAndInit(Application ap, String gateway, String installEventName, String appName, boolean isNewUser, InitCallback successBlock) {
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
                    init(ap, gateway, installEventName, appName, isNewUser, successBlock);
                }

                @Override
                public void onPermissionDenied() {
                    // 用户拒绝了权限，处理逻辑
                    SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("HM_Google_ADS", "");
                    editor.apply();
                    init(ap, gateway, installEventName, appName, isNewUser, successBlock);
                }

                @Override
                public void onExceptionOccurred(Exception e) {
                    // 获取广告 ID 过程中发生了异常，处理逻辑
                    SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("HM_Google_ADS", "");
                    editor.apply();
                    init(ap, gateway, installEventName, appName, isNewUser, successBlock);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            init(ap, gateway, installEventName, appName, isNewUser, successBlock);
        }
    }

    private static void fetchAndInit(Application ap, String gateway, String installEventName, String appName, boolean isNewUser, String clipboardData, InitCallback successBlock) {
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
                    init(ap, gateway, installEventName, appName, isNewUser,clipboardData, successBlock);
                }

                @Override
                public void onPermissionDenied() {
                    // 用户拒绝了权限，处理逻辑
                    SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("HM_Google_ADS", "");
                    editor.apply();
                    init(ap, gateway, installEventName, appName, isNewUser, clipboardData, successBlock);
                }

                @Override
                public void onExceptionOccurred(Exception e) {
                    // 获取广告 ID 过程中发生了异常，处理逻辑
                    SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("HM_Google_ADS", "");
                    editor.apply();
                    init(ap, gateway, installEventName, appName, isNewUser, clipboardData, successBlock);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            init(ap, gateway, installEventName, appName, isNewUser, successBlock);
        }
    }
    interface GoogleAdsIdCallback {
        void onGoogleAdsIdChecked(boolean isGoogleAdsIdNull);
    }

    private static void init(Application mApplication, String gateway, String installEventName, String appName, boolean isNewUser, InitCallback successBlock) {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("HM_Gateway", gateway); // 一个基于Https://开头加上域名构成的网关URL，不包含结尾的 /
        editor.putString("HM_InstallEventName", TextUtils.isEmpty(installEventName) ? "CompleteRegistration" : installEventName); // 完成注册的事件名称，如果不传默认为：CompleteRegistration
        editor.putString("HM_App_Name", appName);
        editor.apply();

        String isFirstInsert = sharedPreferences.getString("HM_isFirstInsert", "1");

        if (!isNewUser) { // 不是第一次安装
            String HM_W2a_Data = sharedPreferences.getString("HM_W2a_Data", "");
            if (!TextUtils.isEmpty(HM_W2a_Data)) { // 是web2App用户
                hmRequestOnAttribute(appName, successBlock);
            } else { // 不是则回调空对象
                hmRequestOnAttribute(appName, successBlock);
            }
        } else { // 判断 第一次安装
            if ("0".equals(isFirstInsert)) {
                String HM_W2a_Data = sharedPreferences.getString("HM_W2a_Data", "");
                if (!TextUtils.isEmpty(HM_W2a_Data)) { // 是web2App用户
                    hmRequestOnAttribute(appName, successBlock);
                } else { // 不是则回调空对象
                    hmRequestOnAttribute(appName, successBlock);
                }
            } else {
                editor.putString("HM_isFirstInsert", "0");
                editor.apply();
                HM_ClipboardUtil.getClipboardText(mApplication, new HM_ClipboardUtil.PasteDataCallback() {
                    @Override
                    public void onSuccess(String pasteData) {
                        boolean isNeedRequest = true;
                        String w2aStr = pasteData;
                        if (!TextUtils.isEmpty(w2aStr) && w2aStr.startsWith("w2a_data:")) {
                            String preStr = "w2a_data:";
                            if (w2aStr.startsWith(preStr)) {// 剪切板有包含w2a_data:开头的数据
                                editor.putString("HM_W2a_Data", w2aStr);
                                editor.putString("HM_Attribution_Type", "cut");
                                editor.putBoolean("HM_IsAttribution", true);
                                editor.putString("HM_User_Type", "0");
                                editor.apply();
                                // 调用网关【新装API】，并将获取到的adv_data[]写入本地
                                isNeedRequest = false;
                                hmRequestNewUser(successBlock);
                            } else { // 无符合条件数据
                                isNeedRequest = false;
                                editor.putString("HM_User_Type", "2");
                                editor.apply();
                                hmGetWebViewInfo(mApplication, appName, successBlock);
                            }
                        } else {
                            isNeedRequest = false;
                            editor.putString("HM_User_Type", "2");
                            editor.apply();
                            hmGetWebViewInfo(mApplication, appName, successBlock);
                        }
                        if (isNeedRequest) {
                            hmGetWebViewInfo(mApplication, appName, successBlock);
                        }
                    }
                });

            }
        }
    }

    private static void init(Application mApplication, String gateway, String installEventName, String appName, boolean isNewUser, String clipboardData, InitCallback successBlock) {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("HM_Gateway", gateway); // 一个基于Https://开头加上域名构成的网关URL，不包含结尾的 /
        editor.putString("HM_InstallEventName", TextUtils.isEmpty(installEventName) ? "CompleteRegistration" : installEventName); // 完成注册的事件名称，如果不传默认为：CompleteRegistration
        editor.putString("HM_App_Name", appName);
        editor.apply();

        String isFirstInsert = sharedPreferences.getString("HM_isFirstInsert", "1");

        if (!isNewUser) { // 不是第一次安装
            String HM_W2a_Data = sharedPreferences.getString("HM_W2a_Data", "");
            if (!TextUtils.isEmpty(HM_W2a_Data)) { // 是web2App用户
                hmRequestOnAttribute(appName, successBlock);
            } else { // 不是则回调空对象
                hmRequestOnAttribute(appName, successBlock);
            }
        } else { // 判断 第一次安装
            if ("0".equals(isFirstInsert)) {
                String HM_W2a_Data = sharedPreferences.getString("HM_W2a_Data", "");
                if (!TextUtils.isEmpty(HM_W2a_Data)) { // 是web2App用户
                    hmRequestOnAttribute(appName, successBlock);
                } else { // 不是则回调空对象
                    hmRequestOnAttribute(appName, successBlock);
                }
            } else {
                editor.putString("HM_isFirstInsert", "0");
                editor.apply();
                if (clipboardData.startsWith("w2a_data:")) {
                    String preStr = "w2a_data:";
                    if (clipboardData.startsWith(preStr)) {// 剪切板有包含w2a_data:开头的数据
                        editor.putString("HM_W2a_Data", clipboardData);
                        editor.putString("HM_Attribution_Type", "cut");
                        editor.putBoolean("HM_IsAttribution", true);
                        editor.putString("HM_User_Type", "0");
                        editor.apply();
                        // 调用网关【新装API】，并将获取到的adv_data[]写入本地
                        hmRequestNewUser(successBlock);
                    } else { // 无符合条件数据
                        editor.putString("HM_User_Type", "2");
                        editor.apply();
                        hmGetWebViewInfo(mApplication, appName, successBlock);
                    }
                } else {
                    editor.putString("HM_User_Type", "2");
                    editor.apply();
                    hmGetWebViewInfo(mApplication, appName, successBlock);
                }
            }
        }
    }

    private static void hmRequestOnAttribute(String appName, InitCallback successBlock) {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        String gateway = sharedPreferences.getString("HM_Gateway", "");

        if (gateway.isEmpty()) {
            return;
        }

        String url = baseURL + HM_UrlConfig.ATTIBUTE;
        JSONObject fingerprintDataJson = new JSONObject();
        String jsonString = sharedPreferences.getString("HM_WebView_Fingerprint", "");
        if (!jsonString.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                fingerprintDataJson.put("ca", jsonObject.optString("ca"));
                fingerprintDataJson.put("wg", jsonObject.optString("wg"));
                fingerprintDataJson.put("pi", jsonObject.optString("pi"));
                fingerprintDataJson.put("ao", jsonObject.optString("ao"));
                fingerprintDataJson.put("se", jsonObject.optString("se"));
                fingerprintDataJson.put("ft", jsonObject.optString("ft"));
                fingerprintDataJson.put("ua", jsonObject.optString("ua"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        HM_DeviceInfoHelper deviceInfoHelper = new HM_DeviceInfoHelper(mApplication);
        JSONObject deviceInfo = deviceInfoHelper.getDeviceInfo();

        JSONObject device_id = new JSONObject();
        String HM_Google_ADS = sharedPreferences.getString("HM_Google_ADS", "");
        String AndroidId = getAndroidId();

        String HM_W2a_Data = sharedPreferences.getString("HM_W2a_Data", "");

        // Building request body
        JSONObject requestBody = new JSONObject();
        try {
            fingerprintDataJson.put("app_name", appName);

            device_id.put("advertiser_ID", HM_Google_ADS);
            device_id.put("android_ID", AndroidId);

            requestBody.put("fingerprint_data", fingerprintDataJson);
            requestBody.put("device_id", device_id);
            requestBody.put("device_info", deviceInfo);
            requestBody.put("w2a_data_encrypt", HM_W2a_Data);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Get device ID from SharedPreferences, assuming it's stored under "HM_Device_Id"
        String deviceID = sharedPreferences.getString("HM_Device_Id", "");
        HM_RequestManager.HttpRequest.Callback callback = new HM_RequestManager.HttpRequest.Callback() {
            @Override
            public void onSuccess(Map<String, String> response) {
                // 解析 JSON 数据
                try {
                    JSONObject jsonResponse = new JSONObject(response.get("response"));
                    int code = jsonResponse.getInt("code");
                    JSONObject data = jsonResponse.getJSONObject("data");

                    if (code == 0) {
                        // 请求成功，获取数据
                        String w2aDataEncrypt = data.optString("w2a_data_encrypt");
                        JSONArray advData = data.optJSONArray("adv_data");
                        String externalId = data.optString("external_id");
                        String userType = data.optString("user_type");

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("HM_External_Id", externalId);
                        editor.putString("HM_User_Type", userType);
                        String advDataString = advData != null ? advData.toString() : null;
                        editor.putString("HM_Adv_Data", advDataString);
                        editor.apply();
                        isSendW2A(w2aDataEncrypt);
                        updataInfo();
                        if (callbackNum > 0) {
                            successBlock.onSuccess(advData);
                            callbackNum -= 1;
                        }
                    } else {
                        // 请求失败，打印消息
                        String message = jsonResponse.getString("message");
                        Log.e(TAG, "Request failed with message: " + message);
                        if (callbackNum > 0) {
                            successBlock.onSuccess(null);
                            callbackNum -= 1;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    // JSON 解析出错，打印异常信息
                    Log.e(TAG, "Error parsing JSON response: " + e.getMessage());
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
                Log.e(TAG, "Request failed with error code: " + errorCode + ", message: " + errorMessage);
            }
        };
        HM_RequestManager.sendHttpPostRequest(url, requestBody.toString(), deviceID, true, callback);
    }
    private static void hmRequestNewUser(InitCallback successBlock) {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        String w2aDataEncrypt = sharedPreferences.getString("HM_W2a_Data", "");
        if (w2aDataEncrypt.isEmpty()) {
            return;
        }

        String gateway = sharedPreferences.getString("HM_Gateway", "");
        if (gateway.isEmpty()) {
            return;
        }

        String url = baseURL + HM_UrlConfig.INSTALL;
        String eventName = sharedPreferences.getString("HM_InstallEventName", "");
        String eventId = getGUID();
        HM_DeviceInfoHelper deviceInfoHelper = new HM_DeviceInfoHelper(mApplication);
        JSONObject deviceInfo = deviceInfoHelper.getDeviceInfo();

        JSONObject device_id = new JSONObject();
        String HM_Google_ADS = sharedPreferences.getString("HM_Google_ADS", "");
        String AndroidId = getAndroidId();
        JSONObject customData = new JSONObject();
        try {
            device_id.put("advertiser_ID", HM_Google_ADS);
            device_id.put("android_ID", AndroidId);
            customData.put("event_name", eventName);
            customData.put("event_id", eventId);
            customData.put("event_time", getCurrentUTCTimestamp());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Building request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("device_info", deviceInfo);
            requestBody.put("device_id", device_id);
            requestBody.put("customData", customData);
            requestBody.put("w2a_data_encrypt", w2aDataEncrypt);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String deviceID = sharedPreferences.getString("HM_Device_Id", "");
        HM_RequestManager.HttpRequest.Callback callback = new HM_RequestManager.HttpRequest.Callback() {
            @Override
            public void onSuccess(Map<String, String> response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response.get("response"));
                    String code = jsonResponse.optString("code");
                    if ("0".equals(code)) {
                        JSONObject data = jsonResponse.optJSONObject("data");
                        String w2aDataEncrypt = data.optString("w2a_data_encrypt");
                        String externalId = data.optString("external_id");
                        String user_type = data.optString("user_type");
                        JSONArray dataArray = data.optJSONArray("adv_data");

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("HM_W2a_Data", w2aDataEncrypt);
                        editor.putString("HM_External_Id", externalId);
                        editor.putString("HM_User_Type", user_type);
                        String advDataString = dataArray != null ? dataArray.toString() : null;
                        editor.putString("HM_Adv_Data", advDataString);
                        editor.apply();
                        if (callbackNum > 0) {
                            successBlock.onSuccess(dataArray);
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
                    Log.e(TAG, "Error parsing JSON response: " + e.getMessage());
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
                Log.e(TAG, "Request failed with error code: " + errorCode + ", message: " + errorMessage);
            }
        };
        HM_RequestManager.sendHttpPostRequest(url, requestBody.toString(), deviceID, true, callback);
    }

    private static void hmGetWebViewInfo(Context context, String appName, InitCallback successBlock) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
            String string = sharedPreferences.getString("HM_WebView_Fingerprint", null);
            if (string != null && !string.isEmpty()) {
                hmReuqestRegisterInfo(appName, successBlock);
            } else {
                if (isUseFingerPrinting) {
                    HM_WebView webView = new HM_WebView(context);
                    HM_WebView.GetFingerprint(new HM_WebView.FingerprintCallback() {
                        @Override
                        public void onCallback(String Fingerprint) {
                            hmReuqestRegisterInfo(appName, successBlock);
                        }
                    });
                } else {
                    hmReuqestRegisterInfo(appName, successBlock);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            hmReuqestRegisterInfo(appName, successBlock);
        }
    }

    private static void hmReuqestRegisterInfo(String appName ,InitCallback successBlock) {

        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        String gateway = sharedPreferences.getString("HM_Gateway", "");

        if (gateway.isEmpty()) {
            return;
        }

        String url = baseURL + HM_UrlConfig.LANDINGPAGEREAD;
        JSONObject requestData = new JSONObject();
        HM_DeviceInfoHelper deviceInfoHelper = new HM_DeviceInfoHelper(mApplication);
        JSONObject deviceInfo = deviceInfoHelper.getDeviceInfo();
        try {
            requestData.put("app_name", appName);

            String jsonString = sharedPreferences.getString("HM_WebView_Fingerprint", "");
            if (!jsonString.isEmpty()) {
                JSONObject jsonObject = new JSONObject(jsonString);
                requestData.put("ca", jsonObject.optString("ca"));
                requestData.put("wg", jsonObject.optString("wg"));
                requestData.put("pi", jsonObject.optString("pi"));
                requestData.put("ao", jsonObject.optString("ao"));
                requestData.put("se", jsonObject.optString("se"));
                requestData.put("ft", jsonObject.optString("ft"));
                requestData.put("ua", jsonObject.optString("ua"));
            }
            requestData.put("device_info", deviceInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String deviceID = sharedPreferences.getString("HM_Device_Id", "");
        HM_RequestManager.HttpRequest.Callback callback = new HM_RequestManager.HttpRequest.Callback() {
            @Override
            public void onSuccess(Map<String, String> response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response.get("response"));
                    String code = jsonResponse.optString("code");
                    if ("0".equals(code)) {
                        JSONObject data = jsonResponse.optJSONObject("data");
                        String w2aDataEncrypt = data.optString("w2a_data_encrypt");
                        String externalId = data.optString("external_id");
                        String attributionType = data.optString("attribution_type");

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("HM_W2a_Data", w2aDataEncrypt);
                        editor.putString("HM_External_Id", externalId);
                        editor.putString("HM_Attribution_Type", attributionType);
                        editor.putBoolean("HM_IsAttribution", !w2aDataEncrypt.isEmpty());
                        editor.apply();

                        if (!w2aDataEncrypt.isEmpty()) {
                            hmRequestNewUser(successBlock);
                        } else {
                            if (callbackNum > 0) {
                                successBlock.onSuccess(null);
                                callbackNum -= 1;
                            }
                        }
                    } else {
                        if (callbackNum > 0) {
                            successBlock.onSuccess(null);
                            callbackNum -= 1;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error parsing JSON response: " + e.getMessage());
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
                Log.e(TAG, "Request failed with error code: " + errorCode + ", message: " + errorMessage);
            }
        };
        HM_RequestManager.sendHttpPostRequest(url, requestData.toString(), deviceID, true, callback);
    }

    /**
     * 执行购买操作，并触发购买事件。
     *
     * @param event_name   事件名称，用于标识购买事件。默认使用“Purchase”
     * @param currency     货币类型，表示购买所使用的货币。
     * @param value        购买价值，表示购买的价值或金额。
     * @param content_type 内容类型，表示购买的内容类型。
     * @param content_ids  内容标识符，用于标识购买的内容。
     * @param po_id        订单标识符，用于标识购买的订单。
     */
    public static void Purchase( String event_name, String currency, String value, String content_type, String content_ids, String po_id) {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        String w2a_data_encrypt = sharedPreferences.getString("HM_W2a_Data", "");
        if (w2a_data_encrypt.length() < 1) {
            return;
        }

        String Gateway = sharedPreferences.getString("HM_Gateway", "");
        if (Gateway.length() < 1) {
            return;
        }

        String url = baseURL + HM_UrlConfig.PURCHASE;
        JSONObject requestBody = new JSONObject();

        JSONObject device_id = new JSONObject();
        String HM_Google_ADS = sharedPreferences.getString("HM_Google_ADS", "");
        String AndroidId =  getAndroidId();
        JSONObject custom_data = new JSONObject();
        try {
            device_id.put("advertiser_ID", HM_Google_ADS);
            device_id.put("android_ID", AndroidId);
            custom_data.put("event_name", event_name.length() > 0 ? event_name : "Purchase");
            custom_data.put("currency", currency.length() > 0 ? currency : "USD");
            custom_data.put("value", value.length() > 0 ? value : "0.00");
            custom_data.put("event_id", getGUID());
            custom_data.put("event_time", getCurrentUTCTimestamp());
            if (content_type.length() > 0) {
                custom_data.put("content_type", content_type);
                if (content_ids.length() > 0) {
                    String[] idsArray = content_ids.split(",");
                    if (idsArray.length > 0) {
                        JSONArray idsJSONArray = new JSONArray(Arrays.asList(idsArray));
                        custom_data.put("content_ids", idsJSONArray);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Building request body
        try {
            requestBody.put("device_id", device_id);
            requestBody.put("custom_data", custom_data);
            requestBody.put("w2a_data_encrypt", w2a_data_encrypt);
            requestBody.put("po_id", po_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String deviceID = sharedPreferences.getString("HM_Device_Id", "");
        HM_RequestManager.HttpRequest.Callback callback = new HM_RequestManager.HttpRequest.Callback() {
            @Override
            public void onSuccess(Map<String, String> response) {

            }
            @Override
            public void onFailure(int errorCode, String errorMessage) {
                // Handle failure response
                Log.e(TAG, "Request failed with error code: " + errorCode + ", message: " + errorMessage);
            }
        };
        HM_RequestManager.sendHttpPostRequest(url, requestBody.toString(), deviceID, false, callback);
    }

    /**
     * 发送事件数据到服务器。
     *
     * @param event_ID   事件ID，建议使用GUID确保唯一性，如果客户有自己的追踪的ID 时候，可以保持一致方便通过事件ID追踪数据链路，若无，可以给空字符串，SDK 将自己生成GUID；每次调用该函数将生成一个新的GUID；
     * @param event_name 事件名称，建议配置"AddToCart"
     * @param currency   货币单位，使用国际标准货币代码，如：USD，代表美元；INR 代表印度卢比
     * @param value      货币价值，使用浮点小数；如果传入非数字将强制默认为0
     * @param typeStr    类型字符串，固定传入"product"
     * @param idsStr     ID字符串，根据品类不同，传入相关商品唯一标记，请使用字符串数组；
     */
    public static void EventPost(String event_ID, String event_name, String currency, String value, String typeStr, String idsStr) {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        String w2a_data_encrypt = sharedPreferences.getString("HM_W2a_Data", "");
        if (w2a_data_encrypt.length() < 1) {
            return;
        }

        String Gateway = sharedPreferences.getString("HM_Gateway", "");
        if (Gateway.length() < 1) {
            return;
        }

        String url = baseURL + HM_UrlConfig.EVENTPOST;
        JSONObject requestBody = new JSONObject();

        JSONObject device_id = new JSONObject();
        String HM_Google_ADS = sharedPreferences.getString("HM_Google_ADS", "");
        String AndroidId = getAndroidId();
        JSONObject custom_data = new JSONObject();
        try {
            device_id.put("advertiser_ID", HM_Google_ADS);
            device_id.put("android_ID", AndroidId);
            custom_data.put("event_name", event_name.length() > 0 ? event_name : "Purchase");
            custom_data.put("currency", currency.length() > 0 ? currency : "USD");
            custom_data.put("value", value.length() > 0 ? value : "0.00");
            custom_data.put("event_id", event_ID.isEmpty() ? getGUID() : event_ID);
            custom_data.put("event_time", getCurrentUTCTimestamp());
            if (typeStr.length() > 0) {
                custom_data.put("content_type", typeStr);

            }
            if (idsStr.length() > 0) {
                String[] idsArray = idsStr.split(",");
                if (idsArray.length > 0) {
                    JSONArray idsJSONArray = new JSONArray(Arrays.asList(idsArray));
                    custom_data.put("content_ids", idsJSONArray);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            requestBody.put("device_id", device_id);
            requestBody.put("custom_data", custom_data);
            requestBody.put("w2a_data_encrypt", w2a_data_encrypt);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String deviceID = sharedPreferences.getString("HM_Device_Id", "");
        HM_RequestManager.HttpRequest.Callback callback = new HM_RequestManager.HttpRequest.Callback() {
            @Override
            public void onSuccess(Map<String, String> response) {

            }
            @Override
            public void onFailure(int errorCode, String errorMessage) {
                // Handle failure response
                Log.e(TAG, "Request failed with error code: " + errorCode + ", message: " + errorMessage);
            }
        };
        HM_RequestManager.sendHttpPostRequest(url, requestBody.toString(), deviceID, false, callback);
    }

    /**
     * 发送用户数据更新事件到服务器。
     *
     * @param emStr       电子邮件字符串，表示用户的电子邮件地址。
     * @param Fb_login_id Facebook登录ID，表示用户的Facebook登录标识。
     * @param Phone       电话号码，表示用户的电话号码。
     * @param Zipcode     邮政编码，表示用户所在地的邮政编码。
     * @param City        城市，表示用户所在的城市。
     * @param State       州/省，表示用户所在的州或省。
     * @param Gender      性别，表示用户的性别。
     * @param Fn          名字，表示用户的名字。
     * @param Ln          姓氏，表示用户的姓氏。
     * @param DateBirth   出生日期，表示用户的出生日期。
     * @param Country     国家，表示用户所在的国家。
     * @param block       可运行的块，用于处理用户数据更新事件完成后的操作。
     */
    public static void UserDataUpdateEvent(String emStr, String Fb_login_id, String Phone, String Zipcode, String City, String State, String Gender, String Fn, String Ln, String DateBirth, String Country,  UserDataUpdateCallback block) {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        String w2a_data_encrypt = sharedPreferences.getString("HM_W2a_Data", "");
        if (w2a_data_encrypt.length() < 1) {
            return;
        }

        String Gateway = sharedPreferences.getString("HM_Gateway", "");
        if (Gateway.length() < 1) {
            return;
        }

        String url = baseURL + HM_UrlConfig.USERDATAUPDATE;
        JSONObject requestBody = new JSONObject();

        JSONObject device_id = new JSONObject();
        String HM_Google_ADS = sharedPreferences.getString("HM_Google_ADS", "");
        String AndroidId = getAndroidId();
        try {
            device_id.put("advertiser_ID", HM_Google_ADS);
            device_id.put("android_ID", AndroidId);
            requestBody.put("device_id", device_id);
            requestBody.put("w2a_data_encrypt", w2a_data_encrypt);
            requestBody.put("em", emStr);
            requestBody.put("fb_login_id", Fb_login_id);
            requestBody.put("ph", Phone);
            requestBody.put("zp", Zipcode);
            requestBody.put("ct", City);
            requestBody.put("st", State);
            requestBody.put("ge", Gender);
            requestBody.put("fn", Fn);
            requestBody.put("ln", Ln);
            requestBody.put("db", DateBirth);
            requestBody.put("country", Country);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String deviceID = sharedPreferences.getString("HM_Device_Id", "");
        HM_RequestManager.HttpRequest.Callback callback = new HM_RequestManager.HttpRequest.Callback() {
            @Override
            public void onSuccess(Map<String, String> response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response.get("response"));
                    String code = jsonResponse.optString("code");
                    if ("0".equals(code)) {
                        JSONObject data = jsonResponse.optJSONObject("data");
                        String w2aDataEncrypt = data.optString("w2a_data_encrypt");
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                        editor.putString("HM_W2a_Data", w2aDataEncrypt);
//                        editor.apply();
                        isSendW2A(w2aDataEncrypt);
                    }
                    block.onSuccess();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int errorCode, String errorMessage) {
                // Handle failure response
                Log.e(TAG, "Request failed with error code: " + errorCode + ", message: " + errorMessage);
            }
        };
        HM_RequestManager.sendHttpPostRequest(url, requestBody.toString(), deviceID, true, callback);
    }

    public static void updataInfo() {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        String w2a_data_encrypt = sharedPreferences.getString("HM_W2a_Data", "");
        if (w2a_data_encrypt.length() < 1) {
            return;
        }
        String Gateway = sharedPreferences.getString("HM_Gateway", "");
        if (Gateway.length() < 1) {
            return;
        }

        String url = baseURL + HM_UrlConfig.SESSION;
        JSONObject device_id = new JSONObject();
        String HM_Google_ADS = sharedPreferences.getString("HM_Google_ADS", "");
        String AndroidId = getAndroidId();
        JSONObject requestBody = new JSONObject();
        try {
            device_id.put("advertiser_ID", HM_Google_ADS);
            device_id.put("android_ID", AndroidId);
            requestBody.put("device_id", device_id);
            requestBody.put("w2a_data_encrypt", w2a_data_encrypt);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String deviceID = sharedPreferences.getString("HM_Device_Id", "");
        HM_RequestManager.HttpRequest.Callback callback = new HM_RequestManager.HttpRequest.Callback() {
            @Override
            public void onSuccess(Map<String, String> response) {

            }
            @Override
            public void onFailure(int errorCode, String errorMessage) {
                // Handle failure response
                Log.e(TAG, "Request failed with error code: " + errorCode + ", message: " + errorMessage);
            }
        };
        HM_RequestManager.sendHttpPostRequest(url, requestBody.toString(), deviceID, false, callback);
    }

    public static void GetPageData(final PageDataCallback pagecallback) {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        String gateway = sharedPreferences.getString("HM_Gateway", "");
        if (TextUtils.isEmpty(gateway)) {
            pagecallback.onDataReceived(null);
            return;
        }
        String url = baseURL + HM_UrlConfig.RELOADPAGEDATA;
        JSONObject requestData = new JSONObject();
        String appName = sharedPreferences.getString("HM_App_Name", "");
        try {
            requestData.put("app_name", appName);
            String jsonString = sharedPreferences.getString("HM_WebView_Fingerprint", "");
            if (!jsonString.isEmpty()) {
                JSONObject jsonObject = new JSONObject(jsonString);
                requestData.put("ca", jsonObject.optString("ca"));
                requestData.put("wg", jsonObject.optString("wg"));
                requestData.put("pi", jsonObject.optString("pi"));
                requestData.put("ao", jsonObject.optString("ao"));
                requestData.put("se", jsonObject.optString("se"));
                requestData.put("ft", jsonObject.optString("ft"));
                requestData.put("ua", jsonObject.optString("ua"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("fingerprint_data", requestData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String deviceID = sharedPreferences.getString("HM_Device_Id", "");

        HM_RequestManager.HttpRequest.Callback callback = new HM_RequestManager.HttpRequest.Callback() {
            @Override
            public void onSuccess(Map<String, String> response) {

                try {
                    JSONObject jsonResponse = new JSONObject(response.get("response"));
                    int code = jsonResponse.getInt("code");
                    JSONObject data = jsonResponse.getJSONObject("data");
                    if (code == 0) {
                        // 请求成功，获取数据
                        JSONArray advData = data.optJSONArray("adv_data");
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        String advDataString = advData != null ? advData.toString() : "";
                        editor.putString("HM_Adv_Data", advDataString);
                        editor.apply();
                    } else {
                        // 请求失败，打印消息
                        String message = jsonResponse.getString("message");
                        Log.e(TAG, "Request failed with message: " + message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    // JSON 解析出错，打印异常信息
                    Log.e(TAG, "Error parsing JSON response: " + e.getMessage());
                }
                pagecallback.onDataReceived(AdvDataRead());
            }
            @Override
            public void onFailure(int errorCode, String errorMessage) {
                // Handle failure response
                pagecallback.onDataReceived(null);
                Log.e(TAG, "Request failed with error code: " + errorCode + ", message: " + errorMessage);
            }
        };
        HM_RequestManager.sendHttpPostRequest(url, requestBody.toString(), deviceID, true, callback);
    }

    public interface PageDataCallback {
        void onDataReceived(JSONArray data);
    }

    public interface InitCallback {
        void onSuccess(JSONArray list);
    }

    public interface UserDataUpdateCallback {
        void onSuccess();
    }

    // 更新W2A数据事件的注册方法
    public static void UpdateW2aDataEvent(UpdateW2aDataCallback callback) {
        updateW2aDataCallback = callback;
    }

    // 回调接口
    public interface UpdateW2aDataCallback {
        void onCallback(JSONArray adv_data, String HM_W2a_Data); // 回调方法，接收数组和字符串参数
    }

    public static void SetDeviceID(Application ap, String deviceID) {
        SharedPreferences sharedPreferences = ap.getSharedPreferences("HM_SharedPreferences_Info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (deviceID.length() > 50) {
            deviceID = deviceID.substring(0, 50);
        }
        editor.putString("HM_Device_Id", deviceID);
        editor.apply();
    }

    public static String getAndroidId() {
        try {
            return Settings.Secure.getString(mApplication.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } catch (SecurityException e) {
            // 权限问题或其他安全异常
            Log.e("getAndroidId", "SecurityException occurred: " + e.getMessage());
        } catch (Exception ex) {
            // 其他异常
            Log.e("getAndroidId", "Exception occurred: " + ex.getMessage());
        }
        return ""; // 无法获取 Android ID，返回空字符串
    }

    public static String GetW2AEncrypt() {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        return sharedPreferences.getString("HM_W2a_Data", "");
    }

    public static void SetW2AEncrypt(String w2a) {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("HM_W2a_Data", w2a);
        editor.apply();
    }

    public static void getAttributionInfo(AttributionInfoCallback callback) {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        String externalId = sharedPreferences.getString("HM_External_Id", "");
        String attributionType = sharedPreferences.getString("HM_Attribution_Type", "");
        boolean isAttribution = sharedPreferences.getBoolean("HM_IsAttribution", false);
        String userType = sharedPreferences.getString("HM_User_Type", "");
        if (isAttribution) {
            callback.onAttributionInfoReceived(isAttribution, attributionType, externalId, userType);
        } else {
            callback.onAttributionInfoReceived(isAttribution, null, externalId, userType);
        }
    }

    public interface AttributionInfoCallback {
        void onAttributionInfoReceived(boolean isAttribution, String attributionType, String externalId, String userType);
    }

    public static JSONArray AdvDataRead() {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        String HM_Adv_Data = sharedPreferences.getString("HM_Adv_Data", "");
        JSONArray advDataArray = new JSONArray();
        if (!HM_Adv_Data.trim().isEmpty()) {
            try {
                advDataArray = new JSONArray(HM_Adv_Data);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("AdvDataRead", "Failed to parse HM_Adv_Data as JSON array: " + HM_Adv_Data, e);
            }
        } else {
            Log.d("AdvDataRead", "HM_Adv_Data is null or empty");
        }
        return advDataArray;
    }

    public static void isSendW2A(String w2a) {
        try {
            SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String HM_W2a_Data = sharedPreferences.getString("HM_W2a_Data", "");
            if (!HM_W2a_Data.equals(w2a)) {
                editor.putString("HM_W2a_Data", w2a);
                editor.apply();
                if (updateW2aDataCallback != null) {
                    updateW2aDataCallback.onCallback(AdvDataRead(), w2a);
                }
            }
        } catch (Exception e) {
            // 在这里处理异常，可以记录日志或者采取其他适当的措施
            e.printStackTrace(); // 打印异常信息到控制台
        }
    }

    public static String getGUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static String getCurrentUTCTimestamp() {
        Date currentDate = new Date();
        long utcTimestamp = currentDate.getTime() / 1000; // Convert milliseconds to seconds
        return String.valueOf(utcTimestamp);
    }

    public static void EventKey(String eid) {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        String gateway = sharedPreferences.getString("HM_Gateway", "");
        if (TextUtils.isEmpty(gateway)) {
            return;
        }
        String url = baseURL + HM_UrlConfig.eventkey;
        HM_RequestManager.HttpRequest.Callback callback = new HM_RequestManager.HttpRequest.Callback() {
            @Override
            public void onSuccess(Map<String, String> response) {
            }
            @Override
            public void onFailure(int errorCode, String errorMessage) {
                // Handle failure response
                Log.e(TAG, "Request failed with error code: " + errorCode + ", message: " + errorMessage);
            }
        };
        HM_RequestManager.sendHttpGetRequest(url, eid);
    }
}
