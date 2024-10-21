package com.huntmobi.web2app;

import android.os.Build;
import android.util.DisplayMetrics;
import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.TimeZone;

public class HM_DeviceInfoHelper {

    private static Context context;

    public HM_DeviceInfoHelper(Context context) {
        this.context = context;
    }

    public JSONObject getDeviceInfo() {
        JSONObject deviceInfo = new JSONObject();
        try {
            deviceInfo.put("brand", getBrand());
            deviceInfo.put("model", getModel());
            deviceInfo.put("language", getLanguage());
            deviceInfo.put("phInfo", getPhInfo());
            deviceInfo.put("osVersion", getOsVersion());
            deviceInfo.put("screenSize", String.valueOf(getScreenDensityDpi()));
            deviceInfo.put("ss_h", String.valueOf(getScreenHeight()));
            deviceInfo.put("ss_w", String.valueOf(getScreenWidth()));
            deviceInfo.put("timezone", getTimezone());
            deviceInfo.put("cpu", String.valueOf(getCpuCoreCount()));
            deviceInfo.put("pgname", getPackageName());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return deviceInfo;
    }

    public String getBrand() {
        return Build.BRAND;
    }

    private String getModel() {
        return Build.MODEL;
    }

    private String getLanguage() {
        // 获取默认的本地化对象
        Locale defaultLocale = Locale.getDefault();

        // 获取语言的缩写
        String languageCode = defaultLocale.getLanguage();

        return languageCode;
    }

    private String getPhInfo() {
        return "ANDROID"; // Assuming it's always Android
    }

    private String getOsVersion() {
        return Build.VERSION.RELEASE;
    }

    public static int getScreenDensityDpi() {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float density = displayMetrics.density;
        return Math.round(density);
    }

    private String getScreenSize() {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.widthPixels + "x" + metrics.heightPixels;
    }

    private int getScreenHeight() {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }

    private int getScreenWidth() {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    private String getTimezone() {
        TimeZone tz = TimeZone.getDefault();
        return tz.getID();
    }

    private int getCpuCoreCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    private String getPackageName() {
        return context.getPackageName();
    }
}
