package com.prm392_sp26.prm392_kitchen_mobile.model.response;

import com.google.gson.annotations.SerializedName;

public class WalletTopUpResponse {

    @SerializedName("paymentId")
    private String paymentId;

    @SerializedName("paymentMethodId")
    private long paymentMethodId;

    @SerializedName("paymentMethodName")
    private String paymentMethodName;

    @SerializedName("amount")
    private double amount;

    @SerializedName("paymentStatus")
    private String paymentStatus;

    @SerializedName("paymentUrl")
    private String paymentUrl;

    public String getPaymentId() {
        return paymentId;
    }

    public long getPaymentMethodId() {
        return paymentMethodId;
    }

    public String getPaymentMethodName() {
        return paymentMethodName;
    }

    public double getAmount() {
        return amount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }
}
