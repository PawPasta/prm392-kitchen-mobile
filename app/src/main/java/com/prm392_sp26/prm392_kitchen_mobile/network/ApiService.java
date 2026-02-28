package com.prm392_sp26.prm392_kitchen_mobile.network;

import com.prm392_sp26.prm392_kitchen_mobile.model.data.UserProfile;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.LoginRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.LoginResponse;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Retrofit API Service interface
 * Định nghĩa các API endpoints để gọi đến backend
 */
public interface ApiService {

    /**
     * Đăng nhập với Firebase ID Token
     * POST /api/auth/login
     * @param request chứa idToken từ Firebase
     * @return BaseResponse<LoginResponse> với thông tin user và tokens
     */
    @POST("api/auth/login")
    Call<BaseResponse<LoginResponse>> login(@Body LoginRequest request);

    /**
     * Lấy profile của user đang đăng nhập
     * GET /api/users/me
     * @param authHeader Authorization header dạng "Bearer <token>"
     * @return BaseResponse<UserProfile>
     */
    @GET("api/users/me")
    Call<BaseResponse<UserProfile>> getCurrentUserProfile(@Header("Authorization") String authHeader);
}
