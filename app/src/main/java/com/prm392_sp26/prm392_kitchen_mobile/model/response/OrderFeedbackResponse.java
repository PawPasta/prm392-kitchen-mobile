package com.prm392_sp26.prm392_kitchen_mobile.model.response;

import com.google.gson.annotations.SerializedName;

public class OrderFeedbackResponse {

    @SerializedName("feedbackId")
    private long feedbackId;

    @SerializedName("orderId")
    private String orderId;

    @SerializedName("orderStatus")
    private String orderStatus;

    @SerializedName("userId")
    private String userId;

    @SerializedName("userDisplayName")
    private String userDisplayName;

    @SerializedName("rating")
    private int rating;

    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    @SerializedName("adminReply")
    private String adminReply;

    @SerializedName("adminReplyAt")
    private String adminReplyAt;

    @SerializedName("adminReplyByUserId")
    private String adminReplyByUserId;

    @SerializedName("adminReplyByDisplayName")
    private String adminReplyByDisplayName;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    public long getFeedbackId() {
        return feedbackId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public int getRating() {
        return rating;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAdminReply() {
        return adminReply;
    }

    public String getAdminReplyAt() {
        return adminReplyAt;
    }

    public String getAdminReplyByUserId() {
        return adminReplyByUserId;
    }

    public String getAdminReplyByDisplayName() {
        return adminReplyByDisplayName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
