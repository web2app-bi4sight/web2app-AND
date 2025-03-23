package com.huntmobi.web2app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;

import org.json.JSONObject;

import java.util.Locale;
import java.util.TimeZone;

public class HM_DeviceData {
    private static final String HM_SharedPreferences_Info = "HM_SharedPreferences_Info";

    private static HM_DeviceData instance;
    private Context context;

    private HM_DeviceData(Context context) {
        this.context = context;
    }

    public static HM_DeviceData getInstance(Context context) {
        if (instance == null) {
            instance = new HM_DeviceData(context);
        }
        return instance;
    }

    // 保存设备信息
    public void saveDeviceInfo() {
        try {
            JSONObject deviceInfo = new JSONObject();
            deviceInfo.put("pgname", getPackageName() != null ? getPackageName() : "");
            deviceInfo.put("appversion", getAppVersionName() != null ? getAppVersionName() : "");
            deviceInfo.put("appver", getAppVersionCode());
            deviceInfo.put("osversion", Build.VERSION.RELEASE != null ? Build.VERSION.RELEASE : "");
            deviceInfo.put("model", Build.MODEL != null ? Build.MODEL : "");
            deviceInfo.put("timezone", getTimeZone() != null ? getTimeZone() : "");
            deviceInfo.put("ss_w", getScreenWidth());
            deviceInfo.put("ss_h", getScreenHeight());
            deviceInfo.put("screensize", getScreenDensity());
            deviceInfo.put("cpu", Runtime.getRuntime().availableProcessors());
            deviceInfo.put("manufacturername", Build.MANUFACTURER != null ? Build.MANUFACTURER : "Android");
            deviceInfo.put("networkconnectionstatus", getNetworkConnectionStatus());
            deviceInfo.put("networktype", getNetworkType());
            deviceInfo.put("systemlanguage", Locale.getDefault().getLanguage() != null ? Locale.getDefault().getLanguage() : "");
            deviceInfo.put("systemcountry", Locale.getDefault().getCountry() != null ? Locale.getDefault().getCountry() : "");
            deviceInfo.put("idfv", "");
            deviceInfo.put("advertiser_id", getAdvertiserId());
            deviceInfo.put("android_id", getAndroidId());

            SharedPreferences prefs = context.getSharedPreferences("HM_Device_Data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("HM_Device_Data", deviceInfo.toString());
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 保存WA设备信息
    public void saveWADeviceInfo() {
        try {
            JSONObject deviceInfo = new JSONObject();
            deviceInfo.put("pgname", getPackageName() != null ? getPackageName() : "");
            deviceInfo.put("appversion", getAppVersionName() != null ? getAppVersionName() : "");
            deviceInfo.put("appver", String.valueOf(getAppVersionCode()));
            deviceInfo.put("osversion", Build.VERSION.RELEASE != null ? Build.VERSION.RELEASE : "");
            deviceInfo.put("model", Build.MODEL != null ? Build.MODEL : "");
            deviceInfo.put("timezoon", getTimeZone() != null ? getTimeZone() : "");
            deviceInfo.put("phinfo", "Android");  // 设备平台信息
            deviceInfo.put("ss_w",  String.valueOf(getScreenWidth()));
            deviceInfo.put("ss_h", String.valueOf(getScreenHeight()));
            deviceInfo.put("screensize", String.valueOf(getScreenDensity()));
            deviceInfo.put("cpu",  String.valueOf(Runtime.getRuntime().availableProcessors()));
            deviceInfo.put("brand", Build.MANUFACTURER != null ? Build.MANUFACTURER : "Android");
            deviceInfo.put("language", Locale.getDefault().getLanguage() != null ? Locale.getDefault().getLanguage() : "");
            deviceInfo.put("systemcountry", Locale.getDefault().getCountry() != null ? Locale.getDefault().getCountry() : "");
            deviceInfo.put("idfv", "");
            deviceInfo.put("advertiser_id", getAdvertiserId());
            deviceInfo.put("android_id", getAndroidId());
            deviceInfo.put("sdk", "3.0.0"); // SDK版本

            SharedPreferences prefs = context.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            String str = deviceInfo.toString();
            editor.putString("HM_WADevice_Data", deviceInfo.toString());
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getPackageName() {
        return context.getPackageName();
    }

    private String getAppVersionName() {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            return null;
        }
    }

    private int getAppVersionCode() {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            return -1;
        }
    }

    private String getTimeZone() {
        return TimeZone.getDefault().getID();
    }

    private int getScreenWidth() {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    private int getScreenHeight() {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

    private float getScreenDensity() {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.density;
    }

    private String getAndroidId() {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private String getAdvertiserId() {
        //
        SharedPreferences sharedPreferences = context.getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        return sharedPreferences.getString("HM_Google_ADS", "");
    }

    private String getNetworkConnectionStatus() {
        // 获取网络连接状态
        return "";
    }

    private String getNetworkType() {
        // 获取网络类型
        return "";
    }
}

