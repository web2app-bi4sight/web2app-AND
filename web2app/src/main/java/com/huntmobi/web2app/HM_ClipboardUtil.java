package com.huntmobi.web2app;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

import android.app.Application;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.List;

public class HM_ClipboardUtil {
    private static final long CLIPBOARD_RETRY_INTERVAL_MS = 500L;
    private static final long CLIPBOARD_MAX_WAIT_MS = 5000L;

    public static void getClipboardText(Application ap, PasteDataCallback callback) {
        if (callback == null || ap == null) {
            return;
        }
        final Application application = ap;
        final PasteDataCallback pasteDataCallback = callback;
        final long deadline = System.currentTimeMillis() + CLIPBOARD_MAX_WAIT_MS;
        Handler handler = new Handler(Looper.getMainLooper());
        // 发送延迟任务，以确保在 Android 10 中正确访问剪贴板数据
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!isAppInForeground(application)) {
                        if (System.currentTimeMillis() >= deadline) {
                            pasteDataCallback.onSuccess("");
                            return;
                        }
                        handler.postDelayed(this, CLIPBOARD_RETRY_INTERVAL_MS);
                        return;
                    }
                    ClipboardManager clipboard = (ClipboardManager) application.getSystemService(Context.CLIPBOARD_SERVICE);
                    String pasteData = "";
                    //判断剪切板是否有数据
                    if (clipboard != null && clipboard.hasPrimaryClip()) {
                        // 获取剪贴板中当前主要剪贴板数据的描述信息，确保不为空
                        if (clipboard.getPrimaryClipDescription() != null) {
                            // 剪贴板中的数据描述包含纯文本类型的 MIME 类型
                            if (clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)) {
                                ClipData clipData = clipboard.getPrimaryClip();
                                // 剪贴板数据对象不为空
                                if (clipData != null) {
                                    ClipData.Item item = clipData.getItemAt(0);
                                    // 数据项不为空且数据项中的文本不为空
                                    if (item != null && item.getText() != null) {
                                        // 获取文本数据
                                        pasteData = item.getText().toString();
                                    }
                                }
                            }
                        }
                    }
                    pasteDataCallback.onSuccess(pasteData);
                } catch (Exception e) {
                    e.printStackTrace();
                    pasteDataCallback.onSuccess("");
                }
            }
        }, CLIPBOARD_RETRY_INTERVAL_MS);
    }

    public interface PasteDataCallback {
        void onSuccess(String pasteData);
    }

    private static boolean isAppInForeground(Context context) {
        try {
            ActivityManager activityManager =
                    (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager == null) {
                return false;
            }
            List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
            if (processes == null) {
                return false;
            }
            String packageName = context.getPackageName();
            for (ActivityManager.RunningAppProcessInfo process : processes) {
                if (process != null
                        && process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                        && process.processName != null
                        && process.processName.equals(packageName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}

