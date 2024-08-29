package com.huntmobi.web2app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class HM_RequestManager {
    private static final String REQUEST_QUEUE_PREFS = "request_queue_prefs";
    private static final String REQUEST_QUEUE_KEY = "request_queue";
    private static SharedPreferences sharedPreferences;
    private static HM_RequestManager instance;
    private static final Queue<HttpRequest> requestQueue = new LinkedList<>();
    private static boolean isSending = false;

    private HM_RequestManager(Context context) {
        sharedPreferences = context.getSharedPreferences(REQUEST_QUEUE_PREFS, Context.MODE_PRIVATE);
        loadRequestQueue();
    }

    public static synchronized HM_RequestManager getInstance(Context context) {
        if (instance == null) {
            instance = new HM_RequestManager(context.getApplicationContext());
        }
        return instance;
    }

    private static void loadRequestQueue() {
        String requestQueueJson = sharedPreferences.getString(REQUEST_QUEUE_KEY, null);
        if (requestQueueJson != null) {
            try {
                JSONArray jsonArray = new JSONArray(requestQueueJson);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    HttpRequest httpRequest = new HttpRequest(
                            jsonObject.getString("url"),
                            jsonObject.getString("requestBody"),
                            jsonObject.getString("deviceID"),
                            jsonObject.getBoolean("isHasCallback"),
                            null);
                    requestQueue.add(httpRequest);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private static void saveRequestQueue() {
        JSONArray jsonArray = new JSONArray();
        for (HttpRequest request : requestQueue) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("url", request.urlString);
                jsonObject.put("requestBody", request.requestBody);
                jsonObject.put("deviceID", request.deviceID);
                jsonObject.put("isHasCallback", request.isHasCallback);
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(REQUEST_QUEUE_KEY, jsonArray.toString());
        editor.apply();
    }

    public static void sendHttpPostRequest(final String urlString, final String requestBody, final String deviceID, final Boolean isHasCallBack, final HttpRequest.Callback callback) {
        HttpRequest request = new HttpRequest(urlString, requestBody, deviceID, isHasCallBack, callback);
        synchronized (requestQueue) {
            requestQueue.add(request);
            saveRequestQueue();
            if (!isSending) {
                sendNextRequest();
            }
        }
    }

    public static void sendHttpGetRequest(final String urlString, final String eid) {
        HttpRequest request = new HttpRequest(urlString, eid, false, null);
        synchronized (requestQueue) {
            requestQueue.add(request);
            saveRequestQueue();
            if (!isSending) {
                sendNextRequest();
            }
        }
    }

    private static void sendNextRequest() {
        synchronized (requestQueue) {
            if (!requestQueue.isEmpty()) {
                isSending = true;
                HttpRequest request = requestQueue.peek(); // 只获取下一个请求，不删除
                if (request != null) {
                    request.send();
                } else {
                    isSending = false; // 处理request为null的情况
                }
            } else {
                isSending = false;
            }
        }
    }

    static class HttpRequest {
        private final String urlString;
        private final String requestBody;
        private final String deviceID;
        private final String eid;
        private final Callback callback;
        private final Boolean isHasCallback;

        // 用于POST请求的构造函数
        public HttpRequest(String urlString, String requestBody, String deviceID, Boolean isHasCallback, Callback callback) {
            this.urlString = urlString;
            this.requestBody = requestBody;
            this.deviceID = deviceID;
            this.eid = null; // POST请求不使用eid
            this.callback = callback;
            this.isHasCallback = isHasCallback;
        }

        // 用于GET请求的构造函数
        public HttpRequest(String urlString, String eid, Boolean isHasCallback, Callback callback) {
            this.urlString = urlString;
            this.requestBody = null; // GET请求不使用requestBody
            this.deviceID = null; // GET请求不使用deviceID
            this.eid = eid;
            this.callback = callback;
            this.isHasCallback = isHasCallback;
        }

        public interface Callback {
            void onSuccess(Map<String, String> response);
            void onFailure(int errorCode, String errorMessage);
        }

        public void send() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection connection = null;
                    try {
                        URL url = new URL(urlString);
                        connection = (HttpURLConnection) url.openConnection();
                        if (requestBody == null) {
                            connection.setRequestMethod("GET");
                            connection.setRequestProperty("eid", eid);
                            String HM_W2a_Data = sharedPreferences.getString("HM_W2a_Data", "");
                            String deviceID = sharedPreferences.getString("HM_Device_Id", "");
                            connection.setRequestProperty("__hm_uuid__", deviceID);
                            connection.setRequestProperty("w2a_data_encrypt", HM_W2a_Data);
                        } else {
                            connection.setRequestMethod("POST");
                            connection.setRequestProperty("Content-Type", "application/json");
                            connection.setRequestProperty("__hm_uuid__", deviceID);
                            connection.setDoOutput(true);
                            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                            outputStream.writeBytes(requestBody);
                            outputStream.flush();
                            outputStream.close();
                            Log.d("HM_RequestBody", urlString + requestBody);
                        }
                        int responseCode = connection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            StringBuilder response = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }
                            reader.close();
                            Map<String, String> responseMap = new HashMap<>();
                            responseMap.put("response", response.toString());
                            Log.d("HM_ResponseBody", urlString + response);

                            synchronized (requestQueue) {
                                requestQueue.remove(HttpRequest.this);
                                saveRequestQueue();
                            }
                            if (callback != null) {
                                callback.onSuccess(responseMap);
                            }
                        } else {
                            if (callback != null) {
                                callback.onFailure(responseCode, "请求失败，响应码：" + responseCode);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (callback != null) {
                            callback.onFailure(-1, e.getMessage());
                        } else {
                            if (HttpRequest.this.isHasCallback) {
                                requestQueue.add(HttpRequest.this);
                                requestQueue.remove(HttpRequest.this);
                                saveRequestQueue();
                            }
                        }
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                        synchronized (requestQueue) {
                            isSending = false;
                            sendNextRequest();
                        }
                    }
                }
            }).start();
        }
    }
}
