package com.prm392_sp26.prm392_kitchen_mobile.model.response;

import com.google.gson.annotations.SerializedName;

public class MomoCallbackResponse {

    @SerializedName("orderId")
    private String orderId;

    @SerializedName("paymentId")
    private String paymentId;

    @SerializedName("paymentStatus")
    private String paymentStatus;

    @SerializedName("orderStatus")
    private String orderStatus;

    @SerializedName("momoTransId")
    private String momoTransId;

    @SerializedName("resultCode")
    private int resultCode;

    @SerializedName("resultMessage")
    private String resultMessage;

    public String getOrderId() {
        return orderId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public String getMomoTransId() {
        return momoTransId;
    }

    public int getResultCode() {
        return resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }
}
