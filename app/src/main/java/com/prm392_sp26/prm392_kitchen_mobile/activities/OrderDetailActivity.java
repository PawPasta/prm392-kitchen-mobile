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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.prm392_sp26.prm392_kitchen_mobile.model.request.CancelOrderRequest;
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

    private TextView tvOrderId;
    private TextView tvStatus;
    private TextView tvUserId;
    private TextView tvCreatedAt;
    private TextView tvPickupAt;
    private TextView tvTotalPrice;
    private TextView tvDiscount;
    private TextView tvFinalAmount;
    private TextView tvNote;
    private TextView tvDishesEmpty;
    private LinearLayout timelineContainer;
    private RecyclerView rvDishes;
    private Button btnCancelOrder;
    private ProgressBar progressCancelOrder;
    private ProgressBar progressOrderDetail;

    private CheckoutDishAdapter dishAdapter;
    private String currentOrderId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.orderDetailMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnCancelOrder.setOnClickListener(v -> confirmCancel());

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
        tvUserId = findViewById(R.id.tvOrderDetailUserId);
        tvCreatedAt = findViewById(R.id.tvOrderDetailCreatedAt);
        tvPickupAt = findViewById(R.id.tvOrderDetailPickupAt);
        tvTotalPrice = findViewById(R.id.tvOrderDetailTotalPrice);
        tvDiscount = findViewById(R.id.tvOrderDetailDiscount);
        tvFinalAmount = findViewById(R.id.tvOrderDetailFinalAmount);
        tvNote = findViewById(R.id.tvOrderDetailNote);
        tvDishesEmpty = findViewById(R.id.tvOrderDetailDishesEmpty);
        timelineContainer = findViewById(R.id.timelineContainer);
        rvDishes = findViewById(R.id.rvOrderDetailDishes);
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

        tvUserId.setText("User ID: " + fallback(order.getUserId(), "--"));
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

        btnCancelOrder.setVisibility(canCancelOrder(status) ? View.VISIBLE : View.GONE);
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
            LinearLayout.LayoutParams stepParams = new LinearLayout.LayoutParams(dp(72),
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            stepContainer.setLayoutParams(stepParams);

            TextView circle = new TextView(this);
            LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(dp(24), dp(24));
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
            circleBg.setColor(ContextCompat.getColor(this, reached ? R.color.colorPrimary : R.color.colorSurfaceVariant));
            circle.setBackground(circleBg);
            stepContainer.addView(circle);

            TextView label = new TextView(this);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            labelParams.topMargin = dp(6);
            label.setLayoutParams(labelParams);
            label.setText(stepLabels[i]);
            label.setTextSize(11f);
            label.setTypeface(isCurrent ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            label.setTextColor(ContextCompat.getColor(this, reached ? R.color.colorTextPrimary : R.color.colorTextSecondary));
            stepContainer.addView(label);

            timelineContainer.addView(stepContainer);

            if (i < stepStatuses.length - 1) {
                View connector = new View(this);
                LinearLayout.LayoutParams connectorParams = new LinearLayout.LayoutParams(dp(18), dp(2));
                connectorParams.topMargin = dp(11);
                connector.setLayoutParams(connectorParams);
                connector.setBackgroundColor(ContextCompat.getColor(this,
                        currentIndex > i ? R.color.colorPrimary : R.color.cardStroke));
                timelineContainer.addView(connector);
            }
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
