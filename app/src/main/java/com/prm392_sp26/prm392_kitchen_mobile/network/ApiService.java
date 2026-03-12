package com.prm392_sp26.prm392_kitchen_mobile.network;

import com.prm392_sp26.prm392_kitchen_mobile.model.data.UserProfile;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.CancelOrderRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.CreateOrderRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.LoginRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.RefreshTokenRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.UpdateProfileRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderHistoryResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.LoginResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderResponse;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.DishResponse;
import com.prm392_sp26.prm392_kitchen_mobile.shared.PageResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.DishDetailResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.ItemResponse;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

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

    /**
     * Lấy đơn hàng hiện tại ở trạng thái CREATED của user đang đăng nhập
     * GET /api/orders/current
     */
    @GET("api/orders/current")
    Call<BaseResponse<OrderResponse>> getCurrentOrder(
            @Header("Authorization") String authHeader);

    /**
     * Refresh access token khi hết hạn
     * POST /api/auth/refresh-token
     */
    @POST("api/auth/refresh-token")
    Call<BaseResponse<LoginResponse>> refreshToken(@Body RefreshTokenRequest request);

    /**
     * Hủy đơn hàng
     * PATCH /api/orders/{orderId}/cancel
     */
    @PATCH("api/orders/{orderId}/cancel")
    Call<BaseResponse<OrderResponse>> cancelOrder(
            @Header("Authorization") String authHeader,
            @Path("orderId") String orderId,
            @Body CancelOrderRequest request);

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

    /**
     * Lấy items theo step (không cần dishId)
     * GET /api/items/step/{stepId}
     */
    @GET("api/items/step/{stepId}")
    Call<BaseResponse<PageResponse<ItemResponse>>> getItemsByStep(
        @Header("Authorization") String token,
        @Path("stepId") int stepId,
        @Query("page") int page,
        @Query("size") int size
    );

    // ==================== ITEM MANAGEMENT APIs ====================

    /**
     * Lấy tất cả items với phân trang
     * GET /api/items
     * Items bao gồm thông tin stepName (nhóm thành phần: carb, protein, sauce, etc.)
     *
     * @param authHeader Authorization header dạng "Bearer <token>"
     * @param page       trang (0-based)
     * @param size       số phần tử mỗi trang
     * @return BaseResponse<PageResponse<ItemResponse>> với danh sách items
     */
    @GET("api/items")
    Call<BaseResponse<PageResponse<ItemResponse>>> getAllItems(
        @Header("Authorization") String authHeader,
        @Query("page") int page,
        @Query("size") int size
    );

    /**
     * Lấy thông tin chi tiết của một item theo ID
     * GET /api/items/{itemId}
     *
     * @param authHeader Authorization header dạng "Bearer <token>"
     * @param itemId     ID của item
     * @return BaseResponse<ItemResponse>
     */
    @GET("api/items/{itemId}")
    Call<BaseResponse<ItemResponse>> getItemById(
        @Header("Authorization") String authHeader,
        @Path("itemId") int itemId
    );

    /**
     * Lấy thông tin chi tiết của item bao gồm cả nutrients
     * GET /api/items/{itemId}/detail
     *
     * @param authHeader Authorization header dạng "Bearer <token>"
     * @param itemId     ID của item
     * @return BaseResponse<ItemDetailResponse> (bao gồm nutrients)
     */
    @GET("api/items/{itemId}/detail")
    Call<BaseResponse<ItemResponse>> getItemDetailWithNutrients(
        @Header("Authorization") String authHeader,
        @Path("itemId") int itemId
    );

    /**
     * Tìm kiếm items theo tên
     * GET /api/items/search
     *
     * @param authHeader Authorization header dạng "Bearer <token>"
     * @param name       Tên item cần tìm
     * @param page       trang (0-based)
     * @param size       số phần tử mỗi trang
     * @return BaseResponse<PageResponse<ItemResponse>>
     */
    @GET("api/items/search")
    Call<BaseResponse<PageResponse<ItemResponse>>> searchItemsByName(
        @Header("Authorization") String authHeader,
        @Query("name") String name,
        @Query("page") int page,
        @Query("size") int size
    );

    /**
     * Lấy items theo trạng thái (ENABLE, DISABLE, EXPIRED)
     * GET /api/items/status/{status}
     *
     * @param authHeader Authorization header dạng "Bearer <token>"
     * @param status     ENABLE, DISABLE, hoặc EXPIRED
     * @param page       trang (0-based)
     * @param size       số phần tử mỗi trang
     * @return BaseResponse<PageResponse<ItemResponse>>
     */
    @GET("api/items/status/{status}")
    Call<BaseResponse<PageResponse<ItemResponse>>> getItemsByStatus(
        @Header("Authorization") String authHeader,
        @Path("status") String status,
        @Query("page") int page,
        @Query("size") int size
    );

    // ==================== ORDER MANAGEMENT APIs ====================

    @POST
    Call<BaseResponse<OrderResponse>> createOrder(
            @Url String url,
            @Header("Authorization") String authHeader,
            @Body CreateOrderRequest request);


}
