package com.huntmobi.web2app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;

public class HM_AdvertisingIdHelper {

    private static final int TIMEOUT_MILLISECONDS = 5000; // 5秒超时
    private static AdvertisingIdCallback advertisingIdCallback; // 回调接口

    // 声明超时处理的 Runnable
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static boolean timeoutOccurred = false; // 超时发生标志
    private static final Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            // 设置超时发生标志
            timeoutOccurred = true;
            // 超时，取消获取广告ID
            advertisingIdCallback.onAdvertisingIdObtained("");
        }
    };

    public interface AdvertisingIdCallback {
        void onAdvertisingIdObtained(String advertisingId);
        void onPermissionDenied();
        void onExceptionOccurred(Exception e);
    }

    public static void getAdvertisingId(Context context, AdvertisingIdCallback callback) {
        advertisingIdCallback = callback;
        int playServicesAvailability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (playServicesAvailability == GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)) {
            // 权限已授予，在支线程中获取广告 ID
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                        if (!adInfo.isLimitAdTrackingEnabled()) {
                            String advertisingId = adInfo.getId();
                            if (!timeoutOccurred) {
                                // 取消超时处理
                                mainHandler.removeCallbacks(timeoutRunnable);
                                callback.onAdvertisingIdObtained(advertisingId);
                            }
                        } else {
                            // 用户未授权跟踪广告 ID
                            if (!timeoutOccurred) {
                                // 取消超时处理
                                mainHandler.removeCallbacks(timeoutRunnable);
                                callback.onPermissionDenied();
                            }
                        }
                    } catch (IOException | GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException e) {
                        e.printStackTrace();
                        if (!timeoutOccurred) {
                            // 取消超时处理
                            mainHandler.removeCallbacks(timeoutRunnable);
                            callback.onExceptionOccurred(e);
                        }
                    }
                }
            }).start();
            // 超时处理
            mainHandler.postDelayed(timeoutRunnable, TIMEOUT_MILLISECONDS);
        } else {
            // Google Play 服务不可用
            callback.onExceptionOccurred(new Exception("Google Play 服务不可用"));
        }
    }
}
