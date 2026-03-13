package com.prm392_sp26.prm392_kitchen_mobile.model.response;

import com.google.gson.annotations.SerializedName;

public class PaymentMethodResponse {

    @SerializedName("paymentMethodId")
    private int paymentMethodId;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("isActive")
    private boolean isActive;

    @SerializedName("walletBalance")
    private Double walletBalance;

    public int getPaymentMethodId() {
        return paymentMethodId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return isActive;
    }

    public Double getWalletBalance() {
        return walletBalance;
    }
}
