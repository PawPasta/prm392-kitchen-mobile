package com.prm392_sp26.prm392_kitchen_mobile.model.request;

import com.google.gson.annotations.SerializedName;

public class RefreshTokenRequest {

    @SerializedName("refreshToken")
    private String refreshToken;

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
