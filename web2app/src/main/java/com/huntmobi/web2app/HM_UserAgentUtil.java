package com.huntmobi.web2app;

import android.content.Context;
import android.os.Build;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class HM_UserAgentUtil {

    /**
     * 获取设备的 WebView User-Agent 字符串
     *
     * @param context 当前应用的上下文
     * @return 当前设备的 WebView User-Agent 字符串，若获取失败则返回空字符串
     */
    public static String getUserAgent(Context context) {
        String userAgent = ""; // 默认空字符串
        WebView webView = null;

        if (context != null) {
            try {
                // 优先使用 WebSettings.getDefaultUserAgent（API 17+）
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    userAgent = WebSettings.getDefaultUserAgent(context);
                } else {
                    // 低于 API 17，创建 WebView 获取 User-Agent
                    webView = new WebView(context);
                    userAgent = webView.getSettings().getUserAgentString();
                }
            } catch (Exception e) {
                e.printStackTrace();
                // 如果获取失败，返回空字符串
                userAgent = "";
            } finally {
                destroyWebView(webView); // 清理 WebView
            }
        }
        return userAgent;
    }

    /**
     * 清理 WebView，避免内存泄漏
     *
     * @param webView 需要销毁的 WebView
     */
    private static void destroyWebView(WebView webView) {
        if (webView != null) {
            webView.destroy();
        }
    }
}
