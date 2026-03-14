package com.prm392_sp26.prm392_kitchen_mobile.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class NotificationItem {

    @SerializedName("notificationUserId")
    private long notificationUserId;

    @SerializedName("notificationId")
    private long notificationId;

    @SerializedName("title")
    private String title;

    @SerializedName("body")
    private String body;

    @SerializedName("type")
    private String type;

    @SerializedName("data")
    private Map<String, Object> data;

    @SerializedName("isRead")
    private boolean isRead;

    @SerializedName("status")
    private String status;

    @SerializedName("readAt")
    private String readAt;

    @SerializedName("createdAt")
    private String createdAt;

    public long getNotificationUserId() {
        return notificationUserId;
    }

    public long getNotificationId() {
        return notificationId;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public boolean isRead() {
        return isRead;
    }

    public String getStatus() {
        return status;
    }

    public String getReadAt() {
        return readAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
