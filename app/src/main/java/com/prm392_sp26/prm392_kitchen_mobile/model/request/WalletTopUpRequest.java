package com.prm392_sp26.prm392_kitchen_mobile.model.request;

import com.google.gson.annotations.SerializedName;

public class WalletTopUpRequest {

    @SerializedName("amount")
    private double amount;

    @SerializedName("paymentMethodId")
    private int paymentMethodId;

    public WalletTopUpRequest(double amount, int paymentMethodId) {
        this.amount = amount;
        this.paymentMethodId = paymentMethodId;
    }

    public double getAmount() {
        return amount;
    }

    public int getPaymentMethodId() {
        return paymentMethodId;
    }
}
