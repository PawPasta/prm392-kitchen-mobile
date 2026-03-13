package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.adapters.CheckoutDishAdapter;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.CreateOrderFeedbackRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.data.UserProfile;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.CancelOrderRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderFeedbackResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderHistoryResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.CurrencyFormatter;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;
import com.prm392_sp26.prm392_kitchen_mobile.util.StatusColorUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "extra_order_id";
    public static final String EXTRA_ORDER_JSON = "order_json";
    public static final String EXTRA_SCROLL_TO_FEEDBACK = "extra_scroll_to_feedback";

    private TextView tvOrderId;
    private TextView tvStatus;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private TextView tvUserId;
    private TextView tvCreatedAt;
    private TextView tvPickupAt;
    private TextView tvTotalPrice;
    private TextView tvDiscount;
    private TextView tvFinalAmount;
    private TextView tvNote;
    private TextView tvDishesEmpty;
    private ScrollView svOrderDetailContent;
    private LinearLayout timelineContainer;
    private RecyclerView rvDishes;
    private View cardOrderFeedback;
    private RatingBar ratingOrderFeedback;
    private EditText etOrderFeedbackTitle;
    private EditText etOrderFeedbackContent;
    private Button btnSubmitOrderFeedback;
    private Button btnCancelOrder;
    private ProgressBar progressFeedback;
    private ProgressBar progressCancelOrder;
    private ProgressBar progressOrderDetail;

    private CheckoutDishAdapter dishAdapter;
    private String currentOrderId;
    private String currentOrderUserId = "";
    private UserProfile currentUserProfile;
    private boolean shouldOpenFeedbackSection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_detail);
        shouldOpenFeedbackSection = getIntent().getBooleanExtra(EXTRA_SCROLL_TO_FEEDBACK, false);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.orderDetailMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnCancelOrder.setOnClickListener(v -> confirmCancel());
        btnSubmitOrderFeedback.setOnClickListener(v -> submitFeedback());

        String orderId = resolveOrderId();
        if (orderId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        currentOrderId = orderId;
        loadOrderDetail(orderId);
    }

    private void bindViews() {
        tvOrderId = findViewById(R.id.tvOrderDetailId);
        tvStatus = findViewById(R.id.tvOrderDetailStatus);
        tvUserName = findViewById(R.id.tvOrderDetailUserName);
        tvUserEmail = findViewById(R.id.tvOrderDetailUserEmail);
        tvUserId = findViewById(R.id.tvOrderDetailUserId);
        tvCreatedAt = findViewById(R.id.tvOrderDetailCreatedAt);
        tvPickupAt = findViewById(R.id.tvOrderDetailPickupAt);
        tvTotalPrice = findViewById(R.id.tvOrderDetailTotalPrice);
        tvDiscount = findViewById(R.id.tvOrderDetailDiscount);
        tvFinalAmount = findViewById(R.id.tvOrderDetailFinalAmount);
        tvNote = findViewById(R.id.tvOrderDetailNote);
        tvDishesEmpty = findViewById(R.id.tvOrderDetailDishesEmpty);
        svOrderDetailContent = findViewById(R.id.svOrderDetailContent);
        timelineContainer = findViewById(R.id.timelineContainer);
        rvDishes = findViewById(R.id.rvOrderDetailDishes);
        cardOrderFeedback = findViewById(R.id.cardOrderFeedback);
        ratingOrderFeedback = findViewById(R.id.ratingOrderFeedback);
        etOrderFeedbackTitle = findViewById(R.id.etOrderFeedbackTitle);
        etOrderFeedbackContent = findViewById(R.id.etOrderFeedbackContent);
        btnSubmitOrderFeedback = findViewById(R.id.btnSubmitOrderFeedback);
        progressFeedback = findViewById(R.id.progressFeedback);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        progressCancelOrder = findViewById(R.id.progressCancelOrder);
        progressOrderDetail = findViewById(R.id.progressOrderDetail);

        dishAdapter = new CheckoutDishAdapter();
        rvDishes.setLayoutManager(new LinearLayoutManager(this));
        rvDishes.setAdapter(dishAdapter);
    }

    private String resolveOrderId() {
        String orderId = safeText(getIntent().getStringExtra(EXTRA_ORDER_ID));
        if (!orderId.isEmpty()) {
            return orderId;
        }

        String json = getIntent().getStringExtra(EXTRA_ORDER_JSON);
        if (json == null || json.trim().isEmpty()) {
            return "";
        }
        try {
            OrderHistoryResponse.OrderItem order = new Gson().fromJson(json, OrderHistoryResponse.OrderItem.class);
            return order != null && order.getOrderId() != null ? order.getOrderId().trim() : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    private void loadOrderDetail(String orderId) {
        String token = PrefsManager.getInstance(this).getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setOrderLoading(true);
        loadCurrentUserProfile(token);
        ApiClient.getInstance()
                .getApiService()
                .getOrderById("Bearer " + token, orderId)
                .enqueue(new Callback<BaseResponse<OrderResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<OrderResponse>> call,
                                           @NonNull Response<BaseResponse<OrderResponse>> response) {
                        setOrderLoading(false);

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()
                                && response.body().getData() != null) {
                            bindOrder(response.body().getData());
                            return;
                        }

                        String message = "Không tải được chi tiết đơn hàng";
                        if (response.body() != null && response.body().getMessage() != null) {
                            message = response.body().getMessage();
                        }
                        Toast.makeText(OrderDetailActivity.this, message, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<OrderResponse>> call,
                                          @NonNull Throwable t) {
                        setOrderLoading(false);
                        Toast.makeText(OrderDetailActivity.this,
                                "Lỗi kết nối: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadCurrentUserProfile(String token) {
        ApiClient.getInstance()
                .getApiService()
                .getCurrentUserProfile("Bearer " + token)
                .enqueue(new Callback<BaseResponse<UserProfile>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<UserProfile>> call,
                                           @NonNull Response<BaseResponse<UserProfile>> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()
                                && response.body().getData() != null) {
                            currentUserProfile = response.body().getData();
                            bindUserInfo(currentUserProfile, currentOrderUserId);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<UserProfile>> call,
                                          @NonNull Throwable t) {
                        // Keep fallback user info from order detail if profile request fails.
                    }
                });
    }

    private void bindOrder(OrderResponse order) {
        if (order == null) {
            return;
        }

        currentOrderId = safeText(order.getOrderId());
        tvOrderId.setText("Order #" + shortId(currentOrderId));

        String status = safeText(order.getStatus());
        tvStatus.setText(status.isEmpty() ? "--" : status);
        int statusColor = ContextCompat.getColor(this, StatusColorUtil.getStatusColorRes(status));
        tvStatus.setTextColor(statusColor);

        currentOrderUserId = safeText(order.getUserId());
        bindUserInfo(currentUserProfile, currentOrderUserId);
        tvCreatedAt.setText("Ngày tạo: " + formatDateTime(order.getCreatedAt()));

        String pickup = safeText(order.getPickupAt());
        if (pickup.isEmpty()) {
            tvPickupAt.setVisibility(View.GONE);
        } else {
            tvPickupAt.setVisibility(View.VISIBLE);
            tvPickupAt.setText("Nhận: " + formatDateTime(pickup));
        }

        tvTotalPrice.setText(formatCurrency(order.getTotalPrice()));
        tvDiscount.setText("-" + formatCurrency(order.getDiscountAmount()));
        tvFinalAmount.setText(formatCurrency(order.getFinalAmount()));

        String note = safeText(order.getNote());
        tvNote.setText("Ghi chú: " + (note.isEmpty() ? "--" : note));

        renderTimeline(status);
        renderDishes(order);

        if (isCompletedStatus(status)) {
            showFeedbackSection();
            loadOrderFeedback(currentOrderId);
        } else {
            hideFeedbackSection();
        }

        btnCancelOrder.setVisibility(canCancelOrder(status) ? View.VISIBLE : View.GONE);
    }

    private void bindUserInfo(@Nullable UserProfile profile, String fallbackUserId) {
        String fallbackId = fallback(fallbackUserId, "--");
        if (profile == null) {
            tvUserName.setText("Tên: Khách hàng");
            tvUserEmail.setText("Email: --");
            tvUserId.setText("User ID: " + fallbackId);
            return;
        }

        String name = safeText(profile.getDisplayName());
        if (name.isEmpty()) {
            name = "Khách hàng";
        }
        tvUserName.setText("Tên: " + name);

        String email = safeText(profile.getEmail());
        tvUserEmail.setText("Email: " + (email.isEmpty() ? "--" : email));

        String userId = safeText(profile.getUserId());
        tvUserId.setText("User ID: " + (userId.isEmpty() ? fallbackId : userId));
    }

    private void renderTimeline(String currentStatus) {
        timelineContainer.removeAllViews();
        String[] stepStatuses = new String[] { "CONFIRMED", "PROCESSING", "READY", "COMPLETED" };
        String[] stepLabels = new String[] { "Xác nhận", "Đang làm", "Sẵn sàng", "Hoàn tất" };
        int currentIndex = getProgressStepIndex(currentStatus);

        for (int i = 0; i < stepStatuses.length; i++) {
            boolean reached = currentIndex >= i;
            boolean isCurrent = currentIndex == i;

            LinearLayout stepContainer = new LinearLayout(this);
            stepContainer.setOrientation(LinearLayout.VERTICAL);
            stepContainer.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams stepParams = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            stepParams.weight = 1f;
            stepContainer.setLayoutParams(stepParams);

            FrameLayout indicatorRow = new FrameLayout(this);
            LinearLayout.LayoutParams indicatorRowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(24));
            indicatorRow.setLayoutParams(indicatorRowParams);

            if (i > 0) {
                View leftConnector = new View(this);
                FrameLayout.LayoutParams leftParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        dp(3),
                        Gravity.CENTER_VERTICAL);
                leftParams.setMarginEnd(dp(12));
                leftConnector.setLayoutParams(leftParams);
                leftConnector.setBackgroundColor(ContextCompat.getColor(this,
                        currentIndex >= i ? R.color.colorSuccess : R.color.cardStroke));
                indicatorRow.addView(leftConnector);
            }

            if (i < stepStatuses.length - 1) {
                View rightConnector = new View(this);
                FrameLayout.LayoutParams rightParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        dp(3),
                        Gravity.CENTER_VERTICAL);
                rightParams.setMarginStart(dp(12));
                rightConnector.setLayoutParams(rightParams);
                rightConnector.setBackgroundColor(ContextCompat.getColor(this,
                        currentIndex > i ? R.color.colorSuccess : R.color.cardStroke));
                indicatorRow.addView(rightConnector);
            }

            TextView circle = new TextView(this);
            FrameLayout.LayoutParams circleParams = new FrameLayout.LayoutParams(dp(24), dp(24), Gravity.CENTER);
            circle.setLayoutParams(circleParams);
            circle.setGravity(Gravity.CENTER);
            circle.setTypeface(Typeface.DEFAULT_BOLD);
            circle.setTextSize(11f);
            circle.setTextColor(ContextCompat.getColor(this, reached ? R.color.white : R.color.colorTextSecondary));
            if (reached && !isCurrent) {
                circle.setText("✓");
            } else {
                circle.setText(String.valueOf(i + 1));
            }

            GradientDrawable circleBg = new GradientDrawable();
            circleBg.setShape(GradientDrawable.OVAL);
            circleBg.setColor(ContextCompat.getColor(this, reached ? R.color.colorSuccess : R.color.colorSurfaceVariant));
            circle.setBackground(circleBg);
            indicatorRow.addView(circle);
            stepContainer.addView(indicatorRow);

            TextView label = new TextView(this);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            labelParams.topMargin = dp(8);
            label.setLayoutParams(labelParams);
            label.setText(stepLabels[i]);
            label.setTextSize(11f);
            label.setTypeface(isCurrent ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            label.setTextColor(ContextCompat.getColor(this, reached ? R.color.colorTextPrimary : R.color.colorTextSecondary));
            stepContainer.addView(label);

            timelineContainer.addView(stepContainer);
        }
    }

    private int getProgressStepIndex(String status) {
        String normalized = safeText(status).toUpperCase(Locale.US);
        switch (normalized) {
            case "CONFIRMED":
                return 0;
            case "PROCESSING":
                return 1;
            case "READY":
                return 2;
            case "COMPLETED":
                return 3;
            default:
                return -1;
        }
    }

    private void renderDishes(OrderResponse order) {
        if (order == null || order.getDishes() == null || order.getDishes().isEmpty()) {
            dishAdapter.setItems(null);
            rvDishes.setVisibility(View.GONE);
            tvDishesEmpty.setVisibility(View.VISIBLE);
            return;
        }

        tvDishesEmpty.setVisibility(View.GONE);
        rvDishes.setVisibility(View.VISIBLE);
        dishAdapter.setItems(order.getDishes());
    }

    private boolean isCompletedStatus(String status) {
        return "COMPLETED".equalsIgnoreCase(safeText(status));
    }

    private void showFeedbackSection() {
        if (cardOrderFeedback != null) {
            cardOrderFeedback.setVisibility(View.VISIBLE);
        }
    }

    private void hideFeedbackSection() {
        if (cardOrderFeedback != null) {
            cardOrderFeedback.setVisibility(View.GONE);
        }
        if (progressFeedback != null) {
            progressFeedback.setVisibility(View.GONE);
        }
        clearFeedbackForm();
    }

    private void clearFeedbackForm() {
        if (ratingOrderFeedback != null) {
            ratingOrderFeedback.setRating(0f);
        }
        if (etOrderFeedbackTitle != null) {
            etOrderFeedbackTitle.setText("");
        }
        if (etOrderFeedbackContent != null) {
            etOrderFeedbackContent.setText("");
        }
    }

    private void loadOrderFeedback(String orderId) {
        String token = PrefsManager.getInstance(this).getAccessToken();
        if (token == null || token.trim().isEmpty() || orderId == null || orderId.trim().isEmpty()) {
            bindFeedbackData(null, false);
            return;
        }

        setFeedbackLoading(true);
        ApiClient.getInstance()
                .getApiService()
                .getOrderFeedback("Bearer " + token, orderId)
                .enqueue(new Callback<BaseResponse<OrderFeedbackResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<OrderFeedbackResponse>> call,
                                           @NonNull Response<BaseResponse<OrderFeedbackResponse>> response) {
                        setFeedbackLoading(false);
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()
                                && response.body().getData() != null) {
                            bindFeedbackData(response.body().getData(), true);
                            return;
                        }
                        bindFeedbackData(null, false);
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<OrderFeedbackResponse>> call,
                                          @NonNull Throwable t) {
                        setFeedbackLoading(false);
                        bindFeedbackData(null, false);
                    }
                });
    }

    private void bindFeedbackData(@Nullable OrderFeedbackResponse feedback, boolean hasFeedback) {
        if (feedback == null || !hasFeedback) {
            clearFeedbackForm();
            setFeedbackReadOnly(false);
            scrollToFeedbackSectionIfNeeded();
            return;
        }

        int rating = feedback.getRating();
        if (rating < 0) {
            rating = 0;
        }
        if (rating > 5) {
            rating = 5;
        }
        ratingOrderFeedback.setRating((float) rating);
        etOrderFeedbackTitle.setText(safeText(feedback.getTitle()));
        etOrderFeedbackContent.setText(safeText(feedback.getContent()));
        setFeedbackReadOnly(true);
        scrollToFeedbackSectionIfNeeded();
    }

    private void setFeedbackReadOnly(boolean readOnly) {
        ratingOrderFeedback.setIsIndicator(readOnly);
        etOrderFeedbackTitle.setEnabled(!readOnly);
        etOrderFeedbackTitle.setFocusable(!readOnly);
        etOrderFeedbackTitle.setFocusableInTouchMode(!readOnly);
        etOrderFeedbackContent.setEnabled(!readOnly);
        etOrderFeedbackContent.setFocusable(!readOnly);
        etOrderFeedbackContent.setFocusableInTouchMode(!readOnly);
        btnSubmitOrderFeedback.setVisibility(readOnly ? View.GONE : View.VISIBLE);
    }

    private void submitFeedback() {
        if (currentOrderId == null || currentOrderId.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        String token = PrefsManager.getInstance(this).getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        int rating = Math.round(ratingOrderFeedback.getRating());
        String title = safeText(etOrderFeedbackTitle.getText() == null
                ? ""
                : etOrderFeedbackTitle.getText().toString());
        String content = safeText(etOrderFeedbackContent.getText() == null
                ? ""
                : etOrderFeedbackContent.getText().toString());

        if (rating <= 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }
        if (title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }
        if (content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitOrderFeedback.setEnabled(false);
        setFeedbackLoading(true);
        ApiClient.getInstance()
                .getApiService()
                .submitOrderFeedback(
                        "Bearer " + token,
                        currentOrderId,
                        new CreateOrderFeedbackRequest(rating, title, content))
                .enqueue(new Callback<BaseResponse<OrderFeedbackResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<OrderFeedbackResponse>> call,
                                           @NonNull Response<BaseResponse<OrderFeedbackResponse>> response) {
                        btnSubmitOrderFeedback.setEnabled(true);
                        setFeedbackLoading(false);

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()
                                && response.body().getData() != null) {
                            Toast.makeText(OrderDetailActivity.this, "Đã gửi đánh giá", Toast.LENGTH_SHORT).show();
                            bindFeedbackData(response.body().getData(), true);
                            return;
                        }
                        String msg = "Gửi đánh giá thất bại";
                        if (response.body() != null && response.body().getMessage() != null) {
                            msg = response.body().getMessage();
                        }
                        Toast.makeText(OrderDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<OrderFeedbackResponse>> call,
                                          @NonNull Throwable t) {
                        btnSubmitOrderFeedback.setEnabled(true);
                        setFeedbackLoading(false);
                        Toast.makeText(OrderDetailActivity.this,
                                "Lỗi kết nối: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setFeedbackLoading(boolean loading) {
        if (progressFeedback != null) {
            progressFeedback.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void scrollToFeedbackSectionIfNeeded() {
        if (!shouldOpenFeedbackSection || cardOrderFeedback == null || cardOrderFeedback.getVisibility() != View.VISIBLE) {
            return;
        }
        shouldOpenFeedbackSection = false;
        cardOrderFeedback.post(() -> {
            if (svOrderDetailContent != null) {
                svOrderDetailContent.smoothScrollTo(0, cardOrderFeedback.getTop());
            }
        });
    }

    private boolean canCancelOrder(String status) {
        return "CREATED".equalsIgnoreCase(safeText(status));
    }

    private void confirmCancel() {
        int padding = dp(16);
        EditText input = new EditText(this);
        input.setHint("Nhập lý do hủy");
        input.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout container = new LinearLayout(this);
        container.setPadding(padding, padding / 2, padding, 0);
        container.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("Lý do hủy đơn")
                .setView(container)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String reason = input.getText().toString().trim();
                    if (reason.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập lý do hủy", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    cancelOrder(reason);
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void cancelOrder(String reason) {
        if (currentOrderId == null || currentOrderId.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ID đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = PrefsManager.getInstance(this).getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCancelOrder.setEnabled(false);
        progressCancelOrder.setVisibility(View.VISIBLE);

        ApiClient.getInstance().getApiService()
                .cancelOrder("Bearer " + token, currentOrderId, new CancelOrderRequest(reason))
                .enqueue(new Callback<BaseResponse<OrderResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<OrderResponse>> call,
                                           @NonNull Response<BaseResponse<OrderResponse>> response) {
                        progressCancelOrder.setVisibility(View.GONE);
                        btnCancelOrder.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(OrderDetailActivity.this, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
                            loadOrderDetail(currentOrderId);
                            return;
                        }

                        String msg = "Hủy đơn thất bại";
                        if (response.body() != null && response.body().getMessage() != null) {
                            msg = response.body().getMessage();
                        }
                        Toast.makeText(OrderDetailActivity.this, msg, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<OrderResponse>> call,
                                          @NonNull Throwable t) {
                        progressCancelOrder.setVisibility(View.GONE);
                        btnCancelOrder.setEnabled(true);
                        Toast.makeText(OrderDetailActivity.this,
                                "Lỗi kết nối: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setOrderLoading(boolean loading) {
        progressOrderDetail.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private String shortId(String id) {
        if (id == null) {
            return "";
        }
        String trimmed = id.trim();
        if (trimmed.length() <= 8) {
            return trimmed;
        }
        return trimmed.substring(trimmed.length() - 8);
    }

    private String formatCurrency(double amount) {
        return CurrencyFormatter.formatVnd(amount);
    }

    private String formatDateTime(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "--";
        }
        Date date = parseIsoDate(input);
        if (date == null) {
            return input.replace("T", " ").replace("Z", "");
        }
        SimpleDateFormat out = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return out.format(date);
    }

    private Date parseIsoDate(String input) {
        String value = input.trim();
        String[] patterns = new String[] {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                return sdf.parse(value);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    private String safeText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private String fallback(String value, String fallbackValue) {
        String normalized = safeText(value);
        return normalized.isEmpty() ? fallbackValue : normalized;
    }
}
