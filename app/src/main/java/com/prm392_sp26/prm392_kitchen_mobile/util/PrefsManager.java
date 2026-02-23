package com.prm392_sp26.prm392_kitchen_mobile.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.prm392_sp26.prm392_kitchen_mobile.model.response.LoginResponse;

/**
 * Quản lý SharedPreferences để lưu trữ thông tin user session
 */
public class PrefsManager {

    private static PrefsManager instance;
    private final SharedPreferences prefs;

    private PrefsManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized PrefsManager getInstance(Context context) {
        if (instance == null) {
            instance = new PrefsManager(context);
        }
        return instance;
    }

    /**
     * Lưu thông tin đăng nhập sau khi login thành công
     */
    public void saveLoginResponse(LoginResponse response) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.KEY_ACCESS_TOKEN, response.getAccessToken());
        editor.putString(Constants.KEY_REFRESH_TOKEN, response.getRefreshToken());
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    /**
     * Kiểm tra user đã đăng nhập chưa
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
    }

    /**
     * Lấy access token để gọi API
     */
    public String getAccessToken() {
        return prefs.getString(Constants.KEY_ACCESS_TOKEN, null);
    }

    /**
     * Lấy refresh token
     */
    public String getRefreshToken() {
        return prefs.getString(Constants.KEY_REFRESH_TOKEN, null);
    }


    /**
     * Xóa toàn bộ session khi logout
     */
    public void clearSession() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
}
