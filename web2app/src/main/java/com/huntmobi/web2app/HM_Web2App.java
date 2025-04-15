package com.huntmobi.web2app;

import android.app.Application;
import android.content.Context;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Looper;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Handler;

import android.app.Activity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.annotation.NonNull;

public class HM_Web2App {
    private static final String HM_SharedPreferences_Info = "HM_SharedPreferences_Info";
    private static HM_Web2App sharedInstance;
    public String deviceTrackID;
    private static String cbcString;
    private static String fromString;
    public String appname;
    private static Application mApplication;
    private static HM_Network hmNetwork;
    static String baseURL = "https://cdn.bi4sight.com";
//    static String baseURL = "https://capi.bi4sight.com";
    public  String Uid;
    public boolean isLaunchReadCut = false;
    public boolean isGetGAID = false;
    public  String pasteboardString;
    static int callbackNum = 0;
    private static attibuteCallback attCallback;
    public static synchronized HM_Web2App getInstance(Application ap) {
        if (sharedInstance == null) {
            sharedInstance = new HM_Web2App();
            cbcString = "";
            fromString = "";
            sharedInstance.deviceTrackID = "";
            sharedInstance.appname = "";
            mApplication = ap;
            sharedInstance.Uid = "";
            sharedInstance.pasteboardString = "";
            hmNetwork = HM_Network.getInstance(mApplication);
            hmNetwork.setRequestURL(baseURL);
        }
        return sharedInstance;
    }

    public interface attibuteCallback {
        void onSuccess(JSONObject data);
    }

    public void attibuteWithAppname(String AppName, attibuteCallback successBlock) {
//        Log.d("HMLOG", "attibuteWithAppname: ");
        checkGoogleAdsId(new GoogleAdsIdCallback() {
            @Override
            public void onGoogleAdsIdChecked(boolean isGoogleAdsIdNull) {
                try {
                    if (isGoogleAdsIdNull) {
                        fetchAndInit(successBlock);
                    } else {
                        isGetGAID = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    isGetGAID = true;
                }
            }
        });
        appname = AppName;
        attCallback = successBlock;
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("HM_AppName", appname);
        editor.putString("__hm_uuid__", Uid);
        String isFirstInsert = sharedPreferences.getString("HM_isFirstInsert", "1");
        if (!"0".equals(isFirstInsert)) {//首次启动，获取归因
            editor.putString("HM_isFirstInsert", "0");
            editor.apply();
            getAllInfo(successBlock);
        } else {
            onsession();
        }
    }

    public void applinksStar(Uri data, attibuteCallback successBlock) {
        if (data != null) {
            String url = data.toString();
//            Log.d("HMLOG", "applinksStar: " + url);
            if (HM_W2ADataValidator.isW2ADataString(url)) {
                fromString = url;
            }
        }
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        String isFirstInsert = sharedPreferences.getString("HM_isFirstInsert", "1");
        if ("0".equals(isFirstInsert)) {//非首次启动，获取归因
            if (HM_W2ADataValidator.isW2ADataString(pasteboardString)) {
                if (HM_W2ADataValidator.isW2ADataString(pasteboardString)) {
                    cbcString = pasteboardString;
                }
                if (fromString.length() > 0 || cbcString.length() > 0) {
                    launch(successBlock);
                }
            } else if (isLaunchReadCut) {
                HM_ClipboardUtil.getClipboardText(mApplication, new HM_ClipboardUtil.PasteDataCallback() {
                    @Override
                    public void onSuccess(String pasteData) {
                        if (!TextUtils.isEmpty(pasteData) && HM_W2ADataValidator.isW2ADataString(pasteData)) {
                            cbcString = pasteData;
                        }
                        if (fromString.length() > 0 || cbcString.length() > 0) {
                            launch(successBlock);
                        }
                    }
                });
            } else if (fromString.length() > 0) {
                launch(successBlock);
            }
        } else {
            if (appname.length() > 0 ) {
//                Log.d("HMLOG", "applinksStar: Att");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("HM_isFirstInsert", "0");
                editor.putString("HM_AppName", appname);
                editor.apply();
                getAllInfo(successBlock);
            }
        }
    }

    private  void getAllInfo(attibuteCallback successBlock) {
        if (pasteboardString.length() > 0) {
            if (HM_W2ADataValidator.isW2ADataString(pasteboardString)) {
                cbcString = pasteboardString;
            }
            hmGetWebViewInfo(successBlock);
        } else {
            HM_ClipboardUtil.getClipboardText(mApplication, new HM_ClipboardUtil.PasteDataCallback() {
                @Override
                public void onSuccess(String pasteData) {
                    if (!TextUtils.isEmpty(pasteData) && HM_W2ADataValidator.isW2ADataString(pasteData)) {
                        cbcString = pasteData;
                    }
                    hmGetWebViewInfo(successBlock);
                }
            });
        }
    }


    private void hmGetWebViewInfo(attibuteCallback successBlock) {
//        Log.d("HMLOG", "hmGetWebViewInfo: ");
        try {
            SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
            String string = sharedPreferences.getString("HM_WebView_UA", null);
            if (string != null && !string.isEmpty()) {
                attibute(successBlock);
            } else {
                String userAgent = HM_UserAgentUtil.getUserAgent(mApplication);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("HM_WebView_UA", userAgent);
                editor.apply();
                attibute(successBlock);
            }
        } catch (Exception e) {
            e.printStackTrace();
            attibute(successBlock);
        }
    }

    private void attibute(attibuteCallback successBlock) {
//        Log.d("HMLOG", "attibute: ");
        if (successBlock != null) {
            callbackNum += 1;
        }
        Map<String, Object> dic = setAttibuteRequestInfo();
        cbcString = "";
        pasteboardString = "";
        fromString = "";
        hmNetwork.addRequest(
                "POST",
                HM_UrlConfig.W2A_10_ATTRIBUTE,
                dic,
                new HM_Network.NetworkCallback() {
                    @Override
                    public void onSuccess(String response) {
                        returnData(response, successBlock);
                    }
                    @Override
                    public void onFailure(Exception e) {
                        if (callbackNum > 0) {
                            successBlock.onSuccess(null);
                            callbackNum -= 1;
                        }
                    }
                }
        );
    }

    private void launch(attibuteCallback successBlock) {
//        Log.d("HMLOG", "launch: ");
        if (successBlock != null) {
            callbackNum += 1;
        }
        Map<String, Object> dic = setRequestInfo(Arrays.asList(cbcString, fromString));
        cbcString = "";
        pasteboardString = "";
        fromString = "";
        hmNetwork.addRequest(
                "POST",
                HM_UrlConfig.W2A_10_LAUNCH,
                dic,
                new HM_Network.NetworkCallback() {
                    @Override
                    public void onSuccess(String response) {
                        returnData(response, successBlock);
                    }
                    @Override
                    public void onFailure(Exception e) {
                        if (callbackNum > 0) {
                            successBlock.onSuccess(null);
                            callbackNum -= 1;
                        }
                    }
                }
        );
    }

    private void returnData(String response, attibuteCallback successBlock) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            String code = jsonResponse.optString("code");
            if ("0".equals(code)) {
                JSONObject data = jsonResponse.optJSONObject("data");
                assert data != null;
                String w2aDataEncrypt = data.optString("w2akey");
                String dtid = data.optString("dtid");
                SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (w2aDataEncrypt.length() > 0) {
                    editor.putString("HM_W2a_Data", w2aDataEncrypt);
                }
                editor.putString("HM_WEB2APP_DTID", dtid);
                editor.apply();
                uploadDeviceInfo();
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
    private void uploadDeviceInfo() {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        String w2akey = sharedPreferences.getString("HM_W2a_Data", "");
        if (isGetGAID && w2akey.length() > 0) {
            Map<String, Object>  data = setRequestInfo(HM_DeviceData.getInstance(mApplication).getDeviceInfo());
            hmNetwork.addRequest(
                    "POST",
                    HM_UrlConfig.W2A_10_SETDEVICE,
                    data,
                    new HM_Network.NetworkCallback() {
                        @Override
                        public void onSuccess(String response) {
                        }
                        @Override
                        public void onFailure(Exception e) {
                        }
                    }
            );
            onsession();
        }
    }

    private void onsession() {
        if (shouldReportTodayAndUpdate()) {
            Map<String, Object> dic = setRequestInfo(null);
            cbcString = "";
            pasteboardString = "";
            hmNetwork.addRequest(
                    "GET",
                    HM_UrlConfig.W2A_10_SESSION,
                    dic,
                    new HM_Network.NetworkCallback() {
                        @Override
                        public void onSuccess(String response) {
                        }
                        @Override
                        public void onFailure(Exception e) {
                        }
                    }
            );
        }
    }

    public void eventPostWithEventInfo(HM_EventInfoModel eventInfoModel) {
        String path = HM_UrlConfig.W2A_10_EVENTPOST;
        String method = "POST";
        String eventName = eventInfoModel.getEventData().eventName();
        if (eventName.equals("BI_Purchase") || eventName.equals("Purchase") || eventName.equals("CompletePayment")) {
            path = HM_UrlConfig.W2A_10_PRUCHASE;
        } else {
            method = "GET";
        }
        Map<String, Object>  data = setRequestInfo(eventInfoModel.toArray());
        hmNetwork.addRequest(
                method,
                path,
                data,
                new HM_Network.NetworkCallback() {
                    @Override
                    public void onSuccess(String response) {
                    }
                    @Override
                    public void onFailure(Exception e) {
                    }
                }
        );
    }

    public void updateUserInfo(HM_UserInfoModel userInfoModel) {
        Map<String, Object>  data = setRequestInfo(userInfoModel.toArray());
        hmNetwork.addRequest(
                "POST",
                HM_UrlConfig.W2A_10_SETUSERDATA,
                data,
                new HM_Network.NetworkCallback() {
                    @Override
                    public void onSuccess(String response) {
                    }
                    @Override
                    public void onFailure(Exception e) {
                    }
                }
        );
    }

    public static boolean shouldReportTodayAndUpdate() {
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        Calendar calendar = Calendar.getInstance();
        String currentDateString = DateFormat.format("yyyy-MM-dd", calendar).toString();
        String lastSessionDate = sharedPreferences.getString("lastSessionDate", null);
        if (currentDateString.equals(lastSessionDate)) {
            return false;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lastSessionDate", currentDateString);
        editor.apply();
        return true;
    }

    public Map<String, Object> setAttibuteRequestInfo() {
        Map<String, Object> map = new HashMap<>();
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        String eid = getGUID();
        String an = sharedPreferences.getString("HM_AppName", appname);
        String ua = sharedPreferences.getString("HM_WebView_UA", "");
        String dtid = sharedPreferences.getString("HM_WEB2APP_DTID", deviceTrackID);
        List<String> dataArray = Arrays.asList(eid, an, cbcString, ua, dtid, fromString);
        map.put("eid", eid);
        map.put("dataArray", dataArray);
        return map;
    }

    public Map<String, Object> setRequestInfo(List<Object> array) {
        Map<String, Object> resultMap = new HashMap<>();
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        String eid = getGUID();
        String an = sharedPreferences.getString("HM_AppName", "");
        String w2akey = sharedPreferences.getString("HM_W2a_Data", "");
        List<Object> dataArray = new ArrayList<>(Arrays.asList(eid, an, w2akey));
        if (array != null) {
            dataArray.addAll(array);
        }
        resultMap.put("eid", eid);
        resultMap.put("dataArray", dataArray);
        return resultMap;
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
                    isGetGAID = true;
                    uploadDeviceInfo();
                }

                @Override
                public void onPermissionDenied() {
                    // 用户拒绝了权限，处理逻辑
                    SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("HM_Google_ADS", "");
                    editor.apply();
                    isGetGAID = true;
                    uploadDeviceInfo();
                }

                @Override
                public void onExceptionOccurred(Exception e) {
                    // 获取广告 ID 过程中发生了异常，处理逻辑
                    SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("HM_Google_ADS", "");
                    editor.apply();
                    isGetGAID = true;
                    uploadDeviceInfo();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            isGetGAID = true;
            uploadDeviceInfo();
        }
    }

    interface GoogleAdsIdCallback {
        void onGoogleAdsIdChecked(boolean isGoogleAdsIdNull);
    }
}
