package com.prm392_sp26.prm392_kitchen_mobile.model.response;

import com.google.gson.annotations.SerializedName;

/**
 * Response data từ API /api/auth/login
 * Chứa accessToken và refreshToken sau khi đăng nhập thành công
 */
public class LoginResponse {

    @SerializedName("accessToken")
    private String accessToken;

    @SerializedName("refreshToken")
    private String refreshToken;

    public LoginResponse() {
    }

    public LoginResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

