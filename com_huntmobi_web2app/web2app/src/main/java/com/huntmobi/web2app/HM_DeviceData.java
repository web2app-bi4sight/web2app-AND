package com.huntmobi.web2app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.DisplayMetrics;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class HM_DeviceData {

    private static HM_DeviceData instance;

    public static synchronized HM_DeviceData getInstance() {
        if (instance == null) {
            instance = new HM_DeviceData();
        }
        return instance;
    }

    public void saveDeviceInfo(Context context) {
        Map<String, String> deviceInfo = new HashMap<>();
        deviceInfo.put("pgname", getAppPackageName(context));
        deviceInfo.put("appversion", getAppVersionName(context));
        deviceInfo.put("appver", getAppVersionCode(context));
        deviceInfo.put("osversion", Build.VERSION.RELEASE);
        deviceInfo.put("model", Build.MODEL);
        deviceInfo.put("timezoon", TimeZone.getDefault().getID());
        deviceInfo.put("ss_w", getScreenWidth(context));
        deviceInfo.put("ss_h", getScreenHeight(context));
        deviceInfo.put("screensize", getScreenDensity(context));
        deviceInfo.put("cpu", String.valueOf(Runtime.getRuntime().availableProcessors()));
        deviceInfo.put("manufacturername", Build.MANUFACTURER);
        deviceInfo.put("networkconnectionstatus", ""); // Placeholder
        deviceInfo.put("networktype", ""); // Placeholder
        deviceInfo.put("systemlanguage", Locale.getDefault().getLanguage());
        deviceInfo.put("systemcountry", Locale.getDefault().getCountry());
        deviceInfo.put("idfv", ""); // Placeholder for Android ID
        deviceInfo.put("advertiser_id", ""); // Placeholder
        deviceInfo.put("android_id", ""); // Placeholder

        SharedPreferences sharedPreferences = context.getSharedPreferences("HM_Device_Data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (Map.Entry<String, String> entry : deviceInfo.entrySet()) {
            editor.putString(entry.getKey(), entry.getValue());
        }
        editor.apply();
    }

    private String getAppPackageName(Context context) {
        return context.getPackageName();
    }

    private String getAppVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "";
        }
    }

    private String getAppVersionCode(Context context) {
        try {
            return String.valueOf(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode);
        } catch (Exception e) {
            return "";
        }
    }

    private String getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return String.format(Locale.getDefault(), "%.2f", displayMetrics.widthPixels / displayMetrics.density);
    }

    private String getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return String.format(Locale.getDefault(), "%.2f", displayMetrics.heightPixels / displayMetrics.density);
    }

    private String getScreenDensity(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return String.format(Locale.getDefault(), "%.2f", displayMetrics.density);
    }
}
