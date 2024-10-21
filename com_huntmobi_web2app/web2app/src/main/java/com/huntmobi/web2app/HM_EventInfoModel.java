package com.huntmobi.web2app;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HM_EventInfoModel {

    /**
     * 是否是关键事件
     */
    private boolean isEventKey;

    /**
     * 是否需要延迟上报
     */
    private boolean isDelay;

    /**
     * 事件内容
     */
    private HM_EventDataModel eventData;

    // Constructor
    public HM_EventInfoModel(boolean isEventKey, boolean isDelay, HM_EventDataModel eventData) {
        this.isEventKey = isEventKey;
        this.isDelay = isDelay;
        this.eventData = (eventData != null) ? eventData : new HM_EventDataModel("", "", "", "", "", "", null);  // 防止 null
    }

    // Getter methods
    public boolean isEventKey() {
        return isEventKey;
    }

    public void setEventKey(boolean isEventKey) {
        this.isEventKey = isEventKey;
    }

    public boolean isDelay() {
        return isDelay;
    }

    public void setDelay(boolean isDelay) {
        this.isDelay = isDelay;
    }

    public HM_EventDataModel getEventData() {
        return eventData;
    }

    public void setEventData(HM_EventDataModel eventData) {
        this.eventData = eventData;
    }

    /**
     * 将对象转换为 Map
     */
    public JSONObject toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("is_event", isEventKey);
        map.put("is_delay", isDelay);
        map.put("event_data", eventData.toMap());

        // 创建一个不可变的 Map 并将其转换为 JSONObject
        return new JSONObject(Collections.unmodifiableMap(map));
    }
}
