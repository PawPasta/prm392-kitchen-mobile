package com.prm392_sp26.prm392_kitchen_mobile.model.request;

import com.google.gson.annotations.SerializedName;

public class OrderCheckoutRequest {

    @SerializedName("orderId")
    private final String orderId;

    @SerializedName("paymentMethodId")
    private final Integer paymentMethodId;

    @SerializedName("promotionId")
    private final Integer promotionId;

    public OrderCheckoutRequest(String orderId, Integer paymentMethodId, Integer promotionId) {
        this.orderId = orderId;
        this.paymentMethodId = paymentMethodId;
        this.promotionId = promotionId;
    }

    public String getOrderId() {
        return orderId;
    }

    public Integer getPaymentMethodId() {
        return paymentMethodId;
    }

    public Integer getPromotionId() {
        return promotionId;
    }
}
