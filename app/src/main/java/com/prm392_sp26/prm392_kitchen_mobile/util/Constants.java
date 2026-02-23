package com.prm392_sp26.prm392_kitchen_mobile.util;

/**
 * Chứa các hằng số sử dụng trong app
 */
public class Constants {

    // Base URL của backend API
    public static final String BASE_URL = "http://10.0.2.2:8080/"; // localhost cho emulator

    // SharedPreferences keys
    public static final String PREFS_NAME = "prm392_kitchen_prefs";
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_REFRESH_TOKEN = "refresh_token";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";

    // API Endpoints
    public static final String API_AUTH_LOGIN = "api/auth/login";
}
