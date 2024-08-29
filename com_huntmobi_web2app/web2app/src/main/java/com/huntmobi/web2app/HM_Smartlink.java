package com.huntmobi.web2app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HM_Smartlink {
    static Application mApplication;
    private static String sCode = "";
    private static String from = "";
    private static final String TAG = "HuntMobiLog";
    private static JSONArray sCodes = new JSONArray();
    private static final String HM_SharedPreferences_Info = "HM_SharedPreferences_Info";
    private static String cbc = "";
    private static String atc = "";
    private static String deviceTrackIDString = "";
    static String baseURL = "https://sl.bi4sight.com";

    static int callbackNum = 0;

    public static void setFrom(String link) {
        from = link;
    }

    public static void setdeviceTrackID(String deviceTrackID) {
        deviceTrackIDString = deviceTrackID;
    }

    public interface attributeCallback {
        void onSuccess(JSONObject data);
    }

    public static void setSCodeArray(JSONArray array) { sCodes = array; }
    public static void init(Application ap, String scode) {
        mApplication = ap;
        sCode = scode;
        HM_DeviceData.getInstance().saveDeviceInfo(ap.getApplicationContext());

        checkGoogleAdsId(new GoogleAdsIdCallback() {
            @Override
            public void onGoogleAdsIdChecked(boolean isGoogleAdsIdNull) {
                try {
                    if (isGoogleAdsIdNull) {
                        fetchAndInit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void start(attributeCallback attributeBlock) {
        Log.d(TAG, "333333333333333333333333333333333333333");
        callbackNum += 1;
        HM_RequestManager requestManager = HM_RequestManager.getInstance(mApplication);
        HM_DeviceInfoHelper deviceInfoHelper = new HM_DeviceInfoHelper(mApplication);
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String isFirstInsert = sharedPreferences.getString("HM_isFirstInsert", "1");

        if ("0".equals(isFirstInsert)) {
            atc = "launch";
            slAttibute(attributeBlock);
        } else {
            editor.putString("HM_isFirstInsert", "0");
            editor.apply();
            atc = "add";
            HM_ClipboardUtil.getClipboardText(mApplication, new HM_ClipboardUtil.PasteDataCallback() {
                @Override
                public void onSuccess(String pasteData) {
                    if (!TextUtils.isEmpty(pasteData)) {
                        if(!matchesInString(pasteData).isEmpty())
                            cbc = matchesInString(pasteData);
                    }
                    slAttibute(attributeBlock);
                }
            });
        }

    }

    private static void slAttibute(attributeCallback attributeBlock) {
        String url = baseURL + HM_UrlConfig.slattibuteString;
        String eid = getGUID();
        long timestamp = System.currentTimeMillis()/1000;

        SharedPreferences sharedPreferences = mApplication.getSharedPreferences("HM_Device_Data", Context.MODE_PRIVATE);
        String aid = sharedPreferences.getString("HM_SMART_AID", "");
        String dtid = sharedPreferences.getString("HM_SMART_DTID", "");
        long ats = sharedPreferences.getLong("HM_SMART_ATS", 0);

        Map<String, ?> deviceInfo = sharedPreferences.getAll();
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("cbc", cbc);
            requestBody.put("scode", sCode);
            requestBody.put("eid", eid);
            requestBody.put("scodes", sCodes);
            requestBody.put("ts", timestamp);
            requestBody.put("atc", atc);
            requestBody.put("from", from);
            JSONObject deviceJson = new JSONObject();
            for (Map.Entry<String, ?> entry : deviceInfo.entrySet()) {
                // 确保将每个值转换为合适的 JSON 类型
                Object value = entry.getValue();
                if (value instanceof String) {
                    deviceJson.put(entry.getKey(), (String) value);
                } else if (value instanceof Integer) {
                    deviceJson.put(entry.getKey(), (Integer) value);
                } else if (value instanceof Boolean) {
                    deviceJson.put(entry.getKey(), (Boolean) value);
                } else if (value instanceof Double) {
                    deviceJson.put(entry.getKey(), (Double) value);
                } else {
                    // 其他类型根据需要处理
                    deviceJson.put(entry.getKey(), value.toString());
                }
            }
            requestBody.put("device", deviceJson);
            requestBody.put("ua", "");
            requestBody.put("aid", aid.isEmpty() ? "" : aid);
            requestBody.put("dtid", atc.equals("add") ? deviceTrackIDString : (dtid.isEmpty() ? "" : dtid));
            requestBody.put("ats", ats != 0 ? ats : 0L);
            from = "";
            cbc = "";
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HM_RequestManager.HttpRequest.Callback callback = new HM_RequestManager.HttpRequest.Callback() {
            @Override
            public void onSuccess(Map<String, String> response) {
                try {
                    JSONObject responseObject = new JSONObject(Objects.requireNonNull(response.get("response")));
                    String code = responseObject.getString("code");
                    JSONObject data = responseObject.getJSONObject("data");
                    if ("0".equals(code)) {
                        String aid = data.optString("aid");
                        String dtid = data.optString("dtid");
                        long ats = data.optLong("ats");
                        if (!aid.isEmpty()) {
                            SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("HM_SMART_AID", aid);
                            editor.putString("HM_SMART_DTID", dtid);
                            editor.putLong("HM_SMART_ATS", ats);
                            editor.apply(); // 保存修改
                        }
                    }
                    if (callbackNum > 0) {
                        attributeBlock.onSuccess(responseObject);
                        callbackNum -= 1;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (callbackNum > 0) {
                        attributeBlock.onSuccess(createDefaultJSONObject());
                        callbackNum -= 1;
                    }
                }
            }
            @Override
            public void onFailure(int errorCode, String errorMessage) {
                // Handle failure response
                Log.e(TAG, "Request failed with error code: " + errorCode + ", message: " + errorMessage);
                if (callbackNum > 0) {
                    attributeBlock.onSuccess(createDefaultJSONObject());
                    callbackNum -= 1;
                }
            }
        };
        HM_RequestManager.sendHttpPostRequest(url, requestBody.toString(), "", true, callback);
    }

    private static JSONObject createDefaultJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("code", 0);
            JSONObject dataObject = new JSONObject();
            dataObject.put("dpv", JSONObject.NULL);
            dataObject.put("cid", JSONObject.NULL);
            dataObject.put("tid", JSONObject.NULL);
            dataObject.put("aid", JSONObject.NULL);
            dataObject.put("dtid", JSONObject.NULL);

            jsonObject.put("data", dataObject);
            jsonObject.put("message", "success");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    interface GoogleAdsIdCallback {
        void onGoogleAdsIdChecked(boolean isGoogleAdsIdNull);
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

    private static void fetchAndInit() {
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
                }

                @Override
                public void onPermissionDenied() {
                    // 用户拒绝了权限，处理逻辑
                    SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("HM_Google_ADS", "");
                    editor.apply();
                }

                @Override
                public void onExceptionOccurred(Exception e) {
                    // 获取广告 ID 过程中发生了异常，处理逻辑
                    SharedPreferences sharedPreferences = mApplication.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("HM_Google_ADS", "");
                    editor.apply();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getGUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static String matchesInString(String input) {
        String pattern = "[BISGHT][A-Za-z0-9]{2}(L|K)[A-Za-z0-9]{7}[SMARL]";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(input);
        StringBuilder resultBuilder = new StringBuilder();
        while (matcher.find()) {
            resultBuilder.append(matcher.group()).append("");
        }
        if (resultBuilder.length() > 0) {
            resultBuilder.setLength(resultBuilder.length());
        }
        return resultBuilder.toString();
    }

}
