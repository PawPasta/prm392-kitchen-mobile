package com.prm392_sp26.prm392_kitchen_mobile.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NotificationListResponse {

    @SerializedName("unreadCount")
    private long unreadCount;

    @SerializedName("notifications")
    private List<NotificationItem> notifications;

    public long getUnreadCount() {
        return unreadCount;
    }

    public List<NotificationItem> getNotifications() {
        return notifications;
    }
}
