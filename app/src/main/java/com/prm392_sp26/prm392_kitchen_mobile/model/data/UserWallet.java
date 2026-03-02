package com.prm392_sp26.prm392_kitchen_mobile.model.data;

import com.google.gson.annotations.SerializedName;

public class UserWallet {

    @SerializedName("walletId")
    private long walletId;

    @SerializedName("balance")
    private double balance;

    @SerializedName("updatedAt")
    private String updatedAt;

    public long getWalletId() {
        return walletId;
    }

    public void setWalletId(long walletId) {
        this.walletId = walletId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
