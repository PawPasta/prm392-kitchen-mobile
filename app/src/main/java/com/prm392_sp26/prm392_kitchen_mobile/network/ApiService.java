package com.prm392_sp26.prm392_kitchen_mobile.network;

import com.prm392_sp26.prm392_kitchen_mobile.model.request.LoginRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.RefreshTokenRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.LoginResponse;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.DishResponse;
import com.prm392_sp26.prm392_kitchen_mobile.shared.PageResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.DishDetailResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.ItemResponse;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit API Service interface
 * Định nghĩa các API endpoints để gọi đến backend
 */
public interface ApiService {

    @POST("api/auth/login")
    Call<BaseResponse<LoginResponse>> login(@Body LoginRequest request);

    /**
     * Refresh access token khi hết hạn
     * POST /api/auth/refresh-token
     */
    @POST("api/auth/refresh-token")
    Call<BaseResponse<LoginResponse>> refreshToken(@Body RefreshTokenRequest request);

    @GET("api/dishes")
    Call<BaseResponse<PageResponse<DishResponse>>> getDishes(
        @Header("Authorization") String token,
        @Query("page") int page,
        @Query("size") int size
    );
    @GET("api/dishes/{dishId}")
    Call<BaseResponse<DishDetailResponse>> getDishDetail(
        @Header("Authorization") String token,
        @Path("dishId") int dishId
    );
    /** Lấy items của 1 step trong 1 dish */
    @GET("api/dishes/{dishId}/steps/{stepId}/items")
    Call<BaseResponse<PageResponse<ItemResponse>>> getItemsByDishStep(
        @Header("Authorization") String token,
        @Path("dishId") int dishId,
        @Path("stepId") int stepId,
        @Query("page") int page,
        @Query("size") int size
    );
    
}
