package com.prm392_sp26.prm392_kitchen_mobile.model.request;

import com.google.gson.annotations.SerializedName;

/**
 * Request body cho API /api/auth/login
 * Gửi Firebase ID Token đến backend để xác thực
 */
public class LoginRequest {

    @SerializedName("idToken")
    private String idToken;

    public LoginRequest() {
    }

    public LoginRequest(String idToken) {
        this.idToken = idToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}

