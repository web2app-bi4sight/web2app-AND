package com.huntmobi.web2app;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HM_EventDataModel {

    private String poid;
    private String eventId;
    private String eventName;
    private String currency;
    private String value;
    private String contentType;
    private List<String> contentIds;
    private String eventTime;

    // Constructor
    public HM_EventDataModel(String poid, String eventId, String eventName, String currency, String value, String contentType, List<String> contentIds) {
        this.poid = (poid != null && !poid.isEmpty()) ? poid : "";
        this.eventId = (eventId != null && !eventId.isEmpty()) ? eventId : generateGUID();
        this.eventName = (eventName != null && !eventName.isEmpty()) ? eventName : "";
        this.currency = (currency != null && !currency.isEmpty()) ? currency : "";
        this.value = (value != null && !value.isEmpty()) ? value : "";
        this.contentType = (contentType != null && !contentType.isEmpty()) ? contentType : "";
        this.contentIds = (contentIds != null && !contentIds.isEmpty()) ? contentIds : Collections.emptyList();
        setTimestamp();
    }

    /**
     * 生成GUID
     */
    private String generateGUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 设置时间戳
     */
    public void setTimestamp() {
        this.eventTime = String.valueOf(System.currentTimeMillis() / 1000L);  // 秒级时间戳
    }

    /**
     * 将对象转换为 Map
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("po_id", poid);
        map.put("event_id", eventId);
        map.put("event_name", eventName);
        map.put("currency", currency);
        map.put("value", value);
        map.put("content_type", contentType);
        map.put("content_ids", contentIds.isEmpty() ? Collections.emptyList() : contentIds);
        map.put("event_time", eventTime);
        return Collections.unmodifiableMap(map);
    }
}
