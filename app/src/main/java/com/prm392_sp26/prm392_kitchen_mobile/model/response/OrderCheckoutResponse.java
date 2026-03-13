package com.prm392_sp26.prm392_kitchen_mobile.model.response;

import com.google.gson.annotations.SerializedName;

public class OrderCheckoutResponse {

    @SerializedName("orderId")
    private String orderId;

    @SerializedName("promotionId")
    private Integer promotionId;

    @SerializedName("paymentMethodId")
    private Integer paymentMethodId;

    @SerializedName("paymentMethodName")
    private String paymentMethodName;

    @SerializedName("orderStatus")
    private String orderStatus;

    @SerializedName("paymentId")
    private String paymentId;

    @SerializedName("paymentStatus")
    private String paymentStatus;

    @SerializedName("totalPrice")
    private double totalPrice;

    @SerializedName("discountAmount")
    private double discountAmount;

    @SerializedName("finalAmount")
    private double finalAmount;

    @SerializedName("paymentUrl")
    private String paymentUrl;

    public String getOrderId() {
        return orderId;
    }

    public Integer getPromotionId() {
        return promotionId;
    }

    public Integer getPaymentMethodId() {
        return paymentMethodId;
    }

    public String getPaymentMethodName() {
        return paymentMethodName;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public double getFinalAmount() {
        return finalAmount;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }
}
