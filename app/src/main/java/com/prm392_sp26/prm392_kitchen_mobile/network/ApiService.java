package com.prm392_sp26.prm392_kitchen_mobile.network;

import com.prm392_sp26.prm392_kitchen_mobile.model.data.UserProfile;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.LoginRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.UpdateProfileRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderHistoryResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.LoginResponse;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 * Retrofit API Service interface
 * Định nghĩa các API endpoints để gọi đến backend
 */
public interface ApiService {

    /**
     * Đăng nhập với Firebase ID Token
     * POST /api/auth/login
     * 
     * @param request chứa idToken từ Firebase
     * @return BaseResponse<LoginResponse> với thông tin user và tokens
     */
    @POST("api/auth/login")
    Call<BaseResponse<LoginResponse>> login(@Body LoginRequest request);

    /**
     * Lấy profile của user đang đăng nhập
     * GET /api/users/me
     * 
     * @param authHeader Authorization header dạng "Bearer <token>"
     * @return BaseResponse<UserProfile>
     */
    @GET("api/users/me")
    Call<BaseResponse<UserProfile>> getCurrentUserProfile(@Header("Authorization") String authHeader);

    /**
     * Cập nhật profile của user đang đăng nhập
     * PUT /api/users/me
     * 
     * @param authHeader Authorization header dạng "Bearer <token>"
     * @param request    body chứa displayName và imageUrl
     * @return BaseResponse<UserProfile>
     */
    @PUT("api/users/me")
    Call<BaseResponse<UserProfile>> updateCurrentUserProfile(
            @Header("Authorization") String authHeader,
            @Body UpdateProfileRequest request);

    /**
     * Lấy lịch sử đơn hàng của user đang đăng nhập
     * GET /api/orders/history
     * 
     * @param authHeader Authorization header dạng "Bearer <token>"
     * @param page       trang (0-based)
     * @param size       số phần tử mỗi trang
     * @param status     lọc theo trạng thái (tuỳ chọn)
     * @return BaseResponse<OrderHistoryResponse>
     */
    @GET("api/orders/history")
    Call<BaseResponse<OrderHistoryResponse>> getOrderHistory(
            @Header("Authorization") String authHeader,
            @Query("page") int page,
            @Query("size") int size,
            @Query("status") String status);
}
