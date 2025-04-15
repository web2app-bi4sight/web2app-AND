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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HM_Network {
    private static final String TAG = "HMNetwork";
    private static final String REQUEST_FILE = "HM_RequestQueue.json";

    private static HM_Network instance;
    private final ExecutorService requestQueue = Executors.newSingleThreadExecutor();
    private final List<JSONObject> savedRequests = new ArrayList<>();
    private final Lock requestLock = new ReentrantLock();
    private final File requestsFile;
    private boolean hasLoadedRequests = false;
    private boolean isEnableLog = false;
    private String requestURL = "";
    private final Context context;

    public interface NetworkCallback {
        void onSuccess(String response);
        void onFailure(Exception e);
    }

    private HM_Network(Context context) {
        this.context = context.getApplicationContext();
        requestsFile = new File(this.context.getFilesDir(), REQUEST_FILE);
        loadSavedRequests();
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
            persistRequests();

            requestQueue.execute(() ->
                    executeRequest(method, relativePath, params, eid, callback)
            );
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
            String fullUrl = requestURL + relativePath;
            String paramsString = processParams(params);
            if (method.equals("GET")) {
                fullUrl = fullUrl + "?p=" + paramsString;
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
                Log.d("HM_RequestBody", fullUrl + "\n" + paramsString);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = readInputStream(connection.getInputStream());
                Log.d("HM_Response", fullUrl + "\n" + response);
                handleSuccess(eid, response, callback);
            } else {
                throw new IOException("HTTP错误码: " + responseCode);
            }
        } catch (Exception e) {
            handleFailure(e, callback);
        } finally {
            if (connection != null) connection.disconnect();
        }
    }
    // endregion

    // region 数据处理
    public String processParams(Map<String, Object> params) {
        String paramsString = "";
        List<String> dataArray = (List<String>) params.get("dataArray");
        if (dataArray != null && dataArray.size() > 0) {
            for (int i = 0; i < dataArray.size(); i++) {
                String string = dataArray.get(i);
                dataArray.set(i, optimizedEscapePipeInString(string));
            }
            paramsString = String.join("|", dataArray);
        }
        return paramsString;
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

                Iterator<String> keys = entry.keys();
                if (keys.hasNext()) {
                    String eid = keys.next();
                    JSONObject data = entry.getJSONObject(eid);
                    requestQueue.execute(() ->
                            {
                                try {
                                    executeRequest(
                                            data.getString("method"),
                                            data.getString("relativePath"),
                                            jsonToMap(data.getJSONObject("params")),
                                            eid,
                                            null // 历史请求无回调
                                    );
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                }
            }
            hasLoadedRequests = true;
        } catch (Exception e) {
            Log.e(TAG, "加载持久化请求失败", e);
        } finally {
            requestLock.unlock();
        }
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
            }
            map.put(key, value);
        }
        return map;
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

    private void handleFailure(Exception e, NetworkCallback callback) {
        Log.e(TAG, "请求处理失败", e);
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
            persistRequests();
        } finally {
            requestLock.unlock();
        }
    }
    // endregion
}