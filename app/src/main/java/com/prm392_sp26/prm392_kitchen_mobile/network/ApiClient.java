package com.prm392_sp26.prm392_kitchen_mobile.network;

import com.prm392_sp26.prm392_kitchen_mobile.KitchenApplication;
import com.prm392_sp26.prm392_kitchen_mobile.util.Constants;
import com.prm392_sp26.prm392_kitchen_mobile.util.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton class để tạo và quản lý Retrofit instance
 * Nơi làm việc với RESTful API
 */
public class ApiClient {

    private static ApiClient instance;
    private final ApiService apiService;

    private ApiClient() {
        // Logging interceptor để debug API calls
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // OkHttp client với timeout và logging
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(90, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(chain -> {
                    Response response = chain.proceed(chain.request());
                    if (response.code() == 401) {
                        String path = response.request().url().encodedPath();
                        boolean isAuthEndpoint = path.contains("/api/auth/login")
                                || path.contains("/api/auth/refresh-token");
                        if (!isAuthEndpoint) {
                            SessionManager.handleUnauthorized(KitchenApplication.getInstance());
                        }
                    }
                    return response;
                })
                .build();

        // Tạo Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    /**
     * Lấy ApiService để gọi API
     */
    public ApiService getApiService() {
        return apiService;
    }
}
