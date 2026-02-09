package com.huntmobi.web2app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HM_Network {
    private static final String TAG = "HMNetwork";
    private static final String REQUEST_FILE = "HM_RequestQueue.json";
    private static final long RETRY_DELAY_MS = 60_000;
    private static final long PERSIST_WINDOW_MS = 1000L;
    private static final long PERSIST_MAX_DELAY_MS = 5000L;
    private static final int PERSIST_CHANGE_THRESHOLD = 20;

    private static HM_Network instance;
    private final ExecutorService requestQueue = Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final List<JSONObject> savedRequests = new ArrayList<>();
    private final Map<String, NetworkCallback> callbackMap = new HashMap<>();
    private final Lock requestLock = new ReentrantLock();
    private final File requestsFile;
    private boolean hasLoadedRequests = false;
    private boolean isEnableLog = false;
    private String requestURL = "";
    private final Context context;
    private long nextScheduledAt = -1L;
    private final Object persistScheduleLock = new Object();
    private ScheduledFuture<?> persistFuture = null;
    private long nextPersistAt = -1L;
    private long firstDirtyAt = -1L;
    private int dirtyCount = 0;

    public interface NetworkCallback {
        void onSuccess(String response);
        void onFailure(Exception e);
    }

    private HM_Network(Context context) {
        this.context = context.getApplicationContext();
        requestsFile = new File(this.context.getFilesDir(), REQUEST_FILE);
        requestQueue.execute(this::loadSavedRequests);
    }

    public static synchronized HM_Network getInstance(Context context) {
        if (instance == null) {
            instance = new HM_Network(context);
        }
        return instance;
    }

    // region 配置方法
    public void setLogEnabled(boolean enabled) {
        isEnableLog = enabled;
    }

    public void setRequestURL(String url) {
        this.requestURL = url;
    }
    // endregion

    // region 请求管理
    public boolean addRequest(String method,
                              String relativePath,
                              Map<String, Object> params,
                              NetworkCallback callback) {
        if (!params.containsKey("eid")) {
            notifyFailure(callback, new IllegalArgumentException("eid参数缺失"));
            return false;
        }

        String eid = params.get("eid").toString();
        if (eid.isEmpty()) {
            notifyFailure(callback, new IllegalArgumentException("eid不能为空"));
            return false;
        }

        requestLock.lock();
        try {
            if (isDuplicateEid(eid)) {
                return false;
            }

            JSONObject requestData = buildRequestData(method, relativePath, params);
            JSONObject entry = new JSONObject(Collections.singletonMap(eid, requestData));
            savedRequests.add(entry);
            if (callback != null) {
                callbackMap.put(eid, callback);
            }
            schedulePersist();

            scheduleProcessQueue(0L);
            return true;
        } catch (JSONException e) {
            notifyFailure(callback, e);
            return false;
        } finally {
            requestLock.unlock();
        }
    }
    // endregion

    // region 网络请求执行
    private void executeRequest(String method,
                                String relativePath,
                                Map<String, Object> params,
                                String eid,
                                NetworkCallback callback) {
        HttpURLConnection connection = null;
        try {
            if (!isNetworkAvailable()) {
                throw new IOException("网络不可用");
            }
            String fullUrl = requestURL + relativePath;
            String paramsString = processParams(params);
            if (method.equals("GET")) {
                fullUrl = fullUrl + "?p=" + URLEncoder.encode(paramsString, "UTF-8");
            }
            URL url = new URL(fullUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            SharedPreferences prefs = getSharedPreferences();
            connection.setRequestProperty("User-Agent", prefs.getString("HM_WebView_UA", ""));
            connection.setRequestProperty("__hm_uuid__", prefs.getString("__hm_uuid__", ""));
            connection.setRequestProperty("__an__", prefs.getString("HM_AppName", ""));
            connection.setRequestProperty("__sv__", HM_UrlConfig.versionString);
            Object eventName = params.get("event_name");
            if (eventName != null) {
                connection.setRequestProperty("__en__", eventName.toString());
            }
            if (method.equals("POST")) {
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "text/plain");
                connection.setDoOutput(true);

                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(paramsString);
                outputStream.flush();
                outputStream.close();
                if (isEnableLog) {
                    Log.d("HM_RequestBody", fullUrl + "\n" + paramsString);
                }
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = readInputStream(connection.getInputStream());
                if (isEnableLog) {
                    Log.d("HM_Response", fullUrl + "\n" + response);
                }
                handleSuccess(eid, response, callback);
            } else {
                String errorBody = readErrorStream(connection);
                throw new IOException("HTTP错误码: " + responseCode + ", errorBody: " + errorBody);
            }
        } catch (Exception e) {
            handleFailure(eid, e, callback);
        } finally {
            if (connection != null) connection.disconnect();
        }
    }
    // endregion

    // region 数据处理
    public String processParams(Map<String, Object> params) {
        String paramsString = "";
        Object dataArrayObj = params.get("dataArray");
        List<String> dataArray = convertToListOfStrings(dataArrayObj);
        if (dataArray != null && dataArray.size() > 0) {
            for (int i = 0; i < dataArray.size(); i++) {
                String string = dataArray.get(i);
                dataArray.set(i, optimizedEscapePipeInString(string));
            }
            paramsString = String.join("|", dataArray);
        }
        return paramsString;
    }

    /**
     * 将各种类型的数组/列表安全转换为 List<String>
     * 支持：List<String>, List<Object>, JSONArray, 数组类型等
     */
    private List<String> convertToListOfStrings(Object obj) {
        if (obj == null) {
            return null;
        }

        List<String> result = new ArrayList<>();

        // 处理 List<String>
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            for (Object item : list) {
                result.add(item != null ? item.toString() : "");
            }
            return result;
        }

        // 处理 JSONArray
        if (obj instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) obj;
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    Object item = jsonArray.get(i);
                    result.add(item != null ? item.toString() : "");
                }
            } catch (JSONException e) {
                Log.e(TAG, "转换 JSONArray 失败", e);
            }
            return result;
        }

        // 处理数组类型
        if (obj.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                Object item = java.lang.reflect.Array.get(obj, i);
                result.add(item != null ? item.toString() : "");
            }
            return result;
        }

        // 其他类型，转换为单个元素的列表
        result.add(obj.toString());
        return result;
    }

    public String optimizedEscapePipeInString(String inputString) {
        return inputString != null ? inputString.replace("|", "\\|") : null;
    }
    // endregion

    // region 持久化处理
    private void loadSavedRequests() {
        if (hasLoadedRequests) return;

        requestLock.lock();
        try {
            if (!requestsFile.exists()) return;

            StringBuilder json = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(requestsFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
            }

            JSONArray jsonArray = new JSONArray(json.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject entry = jsonArray.getJSONObject(i);
                savedRequests.add(entry);
            }
            hasLoadedRequests = true;
        } catch (Exception e) {
            Log.e(TAG, "加载持久化请求失败", e);
        } finally {
            requestLock.unlock();
        }
        scheduleProcessQueue(0L);
    }

    private void persistRequests() {
        requestLock.lock();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(requestsFile))) {
            JSONArray jsonArray = new JSONArray(savedRequests);
            writer.write(jsonArray.toString());
        } catch (Exception e) {
            Log.e(TAG, "持久化请求失败", e);
        } finally {
            requestLock.unlock();
        }
    }
    // endregion

    // region 工具方法
    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences("HM_SharedPreferences_Info", Context.MODE_PRIVATE);
    }

    private boolean isExpired(String timestamp) {
        try {
            long time = Long.parseLong(timestamp);
            return (System.currentTimeMillis()/1000 - time) > 48*3600;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = json.get(key);
            if (value instanceof JSONObject) {
                value = jsonToMap((JSONObject) value);
            } else if (value instanceof JSONArray) {
                // 将 JSONArray 转换为 List<Object>，以便后续处理
                value = jsonArrayToList((JSONArray) value);
            }
            map.put(key, value);
        }
        return map;
    }

    /**
     * 将 JSONArray 转换为 List<Object>
     */
    private List<Object> jsonArrayToList(JSONArray jsonArray) throws JSONException {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Object item = jsonArray.get(i);
            if (item instanceof JSONObject) {
                list.add(jsonToMap((JSONObject) item));
            } else if (item instanceof JSONArray) {
                list.add(jsonArrayToList((JSONArray) item));
            } else {
                list.add(item);
            }
        }
        return list;
    }

    private String readInputStream(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }
    // endregion

    // region 回调处理
    private void handleSuccess(String eid, String response, NetworkCallback callback) {
        removeRequest(eid);
        notifySuccess(callback, response);
    }

    private void handleFailure(String eid, Exception e, NetworkCallback callback) {
        Log.e(TAG, "请求处理失败", e);
        updateRequestOnFailure(eid, e);
        notifyFailure(callback, e);
    }

    private void notifySuccess(NetworkCallback callback, String response) {
        if (callback == null) return;
        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(response));
    }

    private void notifyFailure(NetworkCallback callback, Exception e) {
        if (callback == null) return;
        new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(e));
    }
    // endregion

    // region 辅助方法
    private boolean isDuplicateEid(String eid) throws JSONException {
        for (JSONObject entry : savedRequests) {
            if (entry.has(eid)) return true;
        }
        return false;
    }

    private JSONObject buildRequestData(String method,
                                        String path,
                                        Map<String, Object> params)
            throws JSONException {
        JSONObject data = new JSONObject();
        data.put("method", method);
        data.put("relativePath", path);
        data.put("params", new JSONObject(params));
        data.put("nextRetryAt", 0L);
        data.put("retryCount", 0);
        data.put("lastError", "");
        return data;
    }

    private void removeRequest(String eid) {
        requestLock.lock();
        try {
            Iterator<JSONObject> it = savedRequests.iterator();
            while (it.hasNext()) {
                JSONObject entry = it.next();
                if (entry.has(eid)) {
                    it.remove();
                    break;
                }
            }
            callbackMap.remove(eid);
            schedulePersist();
        } finally {
            requestLock.unlock();
        }
    }
    // endregion

    // region 重试调度
    private void scheduleProcessQueue(long delayMs) {
        long safeDelayMs = Math.max(0L, delayMs);
        long scheduleAt = System.currentTimeMillis() + safeDelayMs;
        requestLock.lock();
        try {
            if (nextScheduledAt != -1L && nextScheduledAt <= scheduleAt) {
                return;
            }
            nextScheduledAt = scheduleAt;
        } finally {
            requestLock.unlock();
        }
        scheduler.schedule(() -> requestQueue.execute(this::processQueue), safeDelayMs, TimeUnit.MILLISECONDS);
    }

    private void processQueue() {
        NextRequest next;
        Long nextAt;
        requestLock.lock();
        try {
            nextScheduledAt = -1L;
            next = findNextDueRequestLocked(System.currentTimeMillis());
            if (next == null) {
                nextAt = findEarliestRetryAtLocked();
            } else {
                nextAt = null;
            }
        } finally {
            requestLock.unlock();
        }

        if (next != null) {
            try {
                executeRequest(
                        next.method,
                        next.relativePath,
                        next.params,
                        next.eid,
                        next.callback
                );
            } catch (Exception e) {
                Log.e(TAG, "执行请求失败", e);
            }
            scheduleProcessQueue(0L);
            return;
        }

        if (nextAt != null) {
            long delayMs = Math.max(0L, nextAt - System.currentTimeMillis());
            scheduleProcessQueue(delayMs);
        }
    }

    private NextRequest findNextDueRequestLocked(long now) {
        JSONObject bestEntry = null;
        String bestEid = null;
        long bestRetryAt = Long.MAX_VALUE;
        for (JSONObject entry : savedRequests) {
            Iterator<String> keys = entry.keys();
            if (!keys.hasNext()) continue;
            String eid = keys.next();
            JSONObject data = entry.optJSONObject(eid);
            if (data == null) continue;
            long retryAt = data.optLong("nextRetryAt", 0L);
            if (retryAt <= now && retryAt <= bestRetryAt) {
                bestRetryAt = retryAt;
                bestEntry = data;
                bestEid = eid;
            }
        }
        if (bestEntry == null || bestEid == null) {
            return null;
        }
        try {
            return new NextRequest(
                    bestEid,
                    bestEntry.getString("method"),
                    bestEntry.getString("relativePath"),
                    jsonToMap(bestEntry.getJSONObject("params")),
                    callbackMap.get(bestEid)
            );
        } catch (JSONException e) {
            Log.e(TAG, "解析待发送请求失败", e);
            return null;
        }
    }

    private Long findEarliestRetryAtLocked() {
        Long earliest = null;
        for (JSONObject entry : savedRequests) {
            Iterator<String> keys = entry.keys();
            if (!keys.hasNext()) continue;
            String eid = keys.next();
            JSONObject data = entry.optJSONObject(eid);
            if (data == null) continue;
            long retryAt = data.optLong("nextRetryAt", 0L);
            if (earliest == null || retryAt < earliest) {
                earliest = retryAt;
            }
        }
        return earliest;
    }

    private void updateRequestOnFailure(String eid, Exception e) {
        requestLock.lock();
        try {
            for (JSONObject entry : savedRequests) {
                if (!entry.has(eid)) continue;
                JSONObject data = entry.optJSONObject(eid);
                if (data == null) return;
                int retryCount = data.optInt("retryCount", 0) + 1;
                data.put("retryCount", retryCount);
                data.put("nextRetryAt", System.currentTimeMillis() + RETRY_DELAY_MS);
                data.put("lastError", e.getMessage() != null ? e.getMessage() : "unknown error");
                schedulePersist();
                return;
            }
        } catch (JSONException jsonException) {
            Log.e(TAG, "更新失败请求失败", jsonException);
        } finally {
            requestLock.unlock();
        }
    }

    private String readErrorStream(HttpURLConnection connection) {
        try {
            InputStream errorStream = connection.getErrorStream();
            if (errorStream == null) return "";
            return readInputStream(errorStream);
        } catch (Exception e) {
            return "";
        }
    }

    private boolean isNetworkAvailable() {
        try {
            android.net.ConnectivityManager cm =
                    (android.net.ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return true;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                android.net.Network network = cm.getActiveNetwork();
                if (network == null) return false;
                android.net.NetworkCapabilities caps = cm.getNetworkCapabilities(network);
                return caps != null && (caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)
                        || caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR)
                        || caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET)
                        || caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_BLUETOOTH));
            } else {
                android.net.NetworkInfo info = cm.getActiveNetworkInfo();
                return info != null && info.isConnected();
            }
        } catch (Exception e) {
            return true;
        }
    }

    private static class NextRequest {
        private final String eid;
        private final String method;
        private final String relativePath;
        private final Map<String, Object> params;
        private final NetworkCallback callback;

        private NextRequest(String eid,
                            String method,
                            String relativePath,
                            Map<String, Object> params,
                            NetworkCallback callback) {
            this.eid = eid;
            this.method = method;
            this.relativePath = relativePath;
            this.params = params;
            this.callback = callback;
        }
    }
    // endregion

    // region 资源释放
    public void shutdown() {
        requestQueue.shutdown();
        scheduler.shutdown();
    }
    // endregion

    // region 持久化调度
    private void schedulePersist() {
        long now = System.currentTimeMillis();
        long targetDelay;
        synchronized (persistScheduleLock) {
            if (dirtyCount == 0) {
                firstDirtyAt = now;
            }
            dirtyCount += 1;

            if (dirtyCount >= PERSIST_CHANGE_THRESHOLD) {
                targetDelay = 0L;
            } else {
                long maxDelay = Math.max(0L, (firstDirtyAt + PERSIST_MAX_DELAY_MS) - now);
                targetDelay = Math.min(PERSIST_WINDOW_MS, maxDelay);
            }

            long targetAt = now + targetDelay;
            if (persistFuture != null && !persistFuture.isDone()) {
                if (nextPersistAt != -1L && nextPersistAt <= targetAt) {
                    return;
                }
                persistFuture.cancel(false);
            }
            nextPersistAt = targetAt;
            persistFuture = scheduler.schedule(() ->
                    requestQueue.execute(this::runPersist), targetDelay, TimeUnit.MILLISECONDS);
        }
    }

    private void runPersist() {
        synchronized (persistScheduleLock) {
            dirtyCount = 0;
            firstDirtyAt = -1L;
            nextPersistAt = -1L;
            persistFuture = null;
        }
        persistRequests();
    }
    // endregion
}