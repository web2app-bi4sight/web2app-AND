package com.huntmobi.web2app;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

public class HM_ClipboardUtil {
    static Application mApplication;
    private static PasteDataCallback pasteDataCallback; // 回调接口
    private static Handler handler;

    public static void getClipboardText(Application ap, PasteDataCallback callback) {
        mApplication = ap;
        pasteDataCallback = callback;
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                try {
                    ClipboardManager clipboard = (ClipboardManager) mApplication.getSystemService(Context.CLIPBOARD_SERVICE);
                    String pasteData = "";
                    //判断剪切板是否有数据
                    if (clipboard.hasPrimaryClip()) {
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
        };
        // 发送延迟消息，以确保在 Android 10 中正确访问剪贴板数据
        handler.sendEmptyMessageDelayed(100, 500);
    }

    public interface PasteDataCallback {
        void onSuccess(String pasteData);
    }
}

