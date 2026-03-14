package com.prm392_sp26.prm392_kitchen_mobile.model.request;

import com.google.gson.annotations.SerializedName;

/**
 * Request body cho API /api/auth/login
 * Gửi Firebase ID Token đến backend để xác thực
 */
public class LoginRequest {

    @SerializedName("idToken")
    private String idToken;

    @SerializedName("fcmToken")
    private String fcmToken;

    public LoginRequest() {
    }

    public LoginRequest(String idToken) {
        this.idToken = idToken;
    }

    public LoginRequest(String idToken, String fcmToken) {
        this.idToken = idToken;
        this.fcmToken = fcmToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}

