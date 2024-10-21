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
    static boolean isShowLog = false;

    private static final String HM_SharedPreferences_Info = "HM_SharedPreferences_Info";

    static boolean isUseFingerPrinting = true;

    public static void useFingerPrinting(boolean isUse) {
        isUseFingerPrinting = isUse;
    }

    static int callbackNum = 0;

    private static String atcString;
    private static String cbcString;
    public static String appname;
    public  String deviceTrackID;

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
    public void Init(Application ap, String gateway, String installEventName, boolean isNewUser, String appName, InitCallback successBlock) {
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
    public void Init(Application ap, String gateway, String installEventName, boolean isNewUser, String appName, String clipboardData, InitCallback successBlock) {
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

    private void fetchAndInit(Application ap, String gateway, String installEventName, String appName, boolean isNewUser, InitCallback successBlock) {
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

    private void fetchAndInit(Application ap, String gateway, String installEventName, String appName, boolean isNewUser, String clipboardData, InitCallback successBlock) {
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

    private void init(Application mApplication, String gateway, String installEventName, String appName, boolean isNewUser, InitCallback successBlock) {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("HM_App_Name", appName);
        editor.apply();
        appname = appName;
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
                    String w2aStr = pasteData;
                    if (!TextUtils.isEmpty(w2aStr) && w2aStr.startsWith("w2a_data:")) {
                        String preStr = "w2a_data:";
                        if (w2aStr.startsWith(preStr)) {// 剪切板有包含w2a_data:开头的数据
                            cbcString = w2aStr;
                        }
                    }
                    atcString = "add";
                    hmGetWebViewInfo(successBlock);
                }
            });
        }
    }

    private void init(Application mApplication, String gateway, String installEventName, String appName, boolean isNewUser, String clipboardData, InitCallback successBlock) {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("HM_App_Name", appName);
        editor.apply();
        appname = appName;
        String isFirstInsert = sharedPreferences.getString("HM_isFirstInsert", "1");
        if ("0".equals(isFirstInsert)) {
            atcString = "launch";
            attibute(successBlock);
        } else {
            editor.putString("HM_isFirstInsert", "0");
            editor.apply();
            String preStr = "w2a_data:";
            if (clipboardData.startsWith(preStr)) {// 剪切板有包含w2a_data:开头的数据
                cbcString = clipboardData;
            }
            atcString = "add";
            hmGetWebViewInfo(successBlock);
        }
    }

    private void attibute(InitCallback successBlock) {
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
                    JSONObject jsonResponse = new JSONObject(response.get("response"));
                    String code = jsonResponse.optString("code");
                    if ("0".equals(code)) {
                        JSONObject data = jsonResponse.optJSONObject("data");
                        String w2aDataEncrypt = data.optString("w2akey");
                        String externalId = data.optString("external_id");
                        String attributionType = data.optString("attribution_type");
                        JSONArray adv_data = data.optJSONArray("adv_data");
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("HM_W2a_Data", w2aDataEncrypt);
                        editor.putString("HM_External_Id", externalId);
                        editor.putString("HM_Attribution_Type", attributionType);
                        editor.putBoolean("HM_IsAttribution", !w2aDataEncrypt.isEmpty());
                        editor.apply();
                        if (callbackNum > 0) {
                            successBlock.onSuccess(adv_data);
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
            dic.put("dt_id", "add".equals(atcString) ? this.deviceTrackID : (TextUtils.isEmpty(dtid) ? "" : dtid));

            dic.put("w2a_data_encrypt", "");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dic;
    }

    private void hmGetWebViewInfo(InitCallback successBlock) {
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

    private JSONObject setEventRequestInfo(JSONObject dic) {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        JSONObject newDic = new JSONObject();

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
            newDic.put("event_data", dic);
            newDic.put("device", deviceInfo);
            newDic.put("app_name", appname);
            newDic.put("w2akey", TextUtils.isEmpty(w2akey) ? "" : w2akey);
            newDic.put("eid", getGUID());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newDic;
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
    public void Purchase( String event_name, String currency, String value, String content_type, String content_ids, String po_id) {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        String url = baseURL + HM_UrlConfig.W2A_EVENTPOST;
        JSONObject custom_data = new JSONObject();
        try {
            custom_data.put("event_name", event_name.length() > 0 ? event_name : "Purchase");
            custom_data.put("currency", currency.length() > 0 ? currency : "USD");
            custom_data.put("value", value.length() > 0 ? value : "0.00");
//            custom_data.put("event_id", getGUID());
            custom_data.put("event_time", getCurrentUTCTimestamp());
            custom_data.put("po_id", po_id);
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

        JSONObject data = setEventRequestInfo(custom_data);
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
    public void EventPost(String event_ID, String event_name, String currency, String value, String typeStr, String idsStr) {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        String url = baseURL + HM_UrlConfig.W2A_EVENTPOST;
        JSONObject custom_data = new JSONObject();
        try {
            custom_data.put("event_name", event_name.length() > 0 ? event_name : "Purchase");
            custom_data.put("currency", currency.length() > 0 ? currency : "USD");
            custom_data.put("value", value.length() > 0 ? value : "0.00");
//            custom_data.put("event_id", getGUID());
            custom_data.put("event_time", getCurrentUTCTimestamp());
            if (typeStr.length() > 0) {
                custom_data.put("content_type", typeStr);
                if (idsStr.length() > 0) {
                    String[] idsArray = idsStr.split(",");
                    if (idsArray.length > 0) {
                        JSONArray idsJSONArray = new JSONArray(Arrays.asList(idsArray));
                        custom_data.put("content_ids", idsJSONArray);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject data = setEventRequestInfo(custom_data);
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
     */
    public  void UserDataUpdateEvent(String emStr, String Fb_login_id, String Phone, String Zipcode, String City, String State, String Gender, String Fn, String Ln, String DateBirth, String Country) {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        JSONObject requestBody = new JSONObject();
        try {

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
        JSONObject data = setUserRequestInfo(requestBody);
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

    public static void GetPageData(final PageDataCallback pagecallback) {

    }

    public interface PageDataCallback {
        void onDataReceived(JSONArray data);
    }

    public interface InitCallback {
        void onSuccess(JSONArray list);
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
