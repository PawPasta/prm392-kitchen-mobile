package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.app.AlertDialog;
import android.os.Bundle;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.prm392_sp26.prm392_kitchen_mobile.R;
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
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvOrderId;
    private TextView tvStatus;
    private TextView tvCreatedAt;
    private TextView tvPickupAt;
    private TextView tvTotal;
    private TextView tvNote;
    private LinearLayout timelineContainer;
    private LinearLayout dishesContainer;
    private Button btnCancelOrder;
    private ProgressBar progressCancelOrder;
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

        tvOrderId = findViewById(R.id.tvOrderDetailId);
        tvStatus = findViewById(R.id.tvOrderDetailStatus);
        tvCreatedAt = findViewById(R.id.tvOrderDetailCreatedAt);
        tvPickupAt = findViewById(R.id.tvOrderDetailPickupAt);
        tvTotal = findViewById(R.id.tvOrderDetailTotal);
        tvNote = findViewById(R.id.tvOrderDetailNote);
        timelineContainer = findViewById(R.id.timelineContainer);
        dishesContainer = findViewById(R.id.dishesContainer);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        progressCancelOrder = findViewById(R.id.progressCancelOrder);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnCancelOrder.setOnClickListener(v -> confirmCancel());

        String json = getIntent().getStringExtra("order_json");
        if (json == null || json.trim().isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu đơn hàng.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        OrderHistoryResponse.OrderItem order = new Gson().fromJson(json, OrderHistoryResponse.OrderItem.class);
        bindOrder(order);
    }

    private void bindOrder(OrderHistoryResponse.OrderItem order) {
        if (order == null) {
            return;
        }

        String orderId = order.getOrderId() == null ? "" : order.getOrderId();
        currentOrderId = orderId;
        tvOrderId.setText("Order #" + shortId(orderId));

        String status = order.getStatus() == null ? "" : order.getStatus();
        tvStatus.setText(status);
        tvStatus.setTextColor(getColor(StatusColorUtil.getStatusColorRes(status)));

        // Chỉ hiện nút Hủy đơn khi đơn đang ở trạng thái CREATED
        if ("CREATED".equalsIgnoreCase(status)) {
            btnCancelOrder.setVisibility(View.VISIBLE);
        } else {
            btnCancelOrder.setVisibility(View.GONE);
        }

        tvCreatedAt.setText("Ngày tạo: " + formatDateTime(order.getCreatedAt()));
        String pickup = order.getPickupAt();
        if (pickup == null || pickup.trim().isEmpty()) {
            tvPickupAt.setVisibility(View.GONE);
        } else {
            tvPickupAt.setVisibility(View.VISIBLE);
            tvPickupAt.setText("Nhận: " + formatDateTime(pickup));
        }

        double amount = order.getFinalAmount() > 0 ? order.getFinalAmount() : order.getTotalPrice();
        tvTotal.setText("Tổng: " + formatCurrency(amount));

        String note = order.getNote();
        if (note == null || note.trim().isEmpty()) {
            tvNote.setText("Ghi chú: --");
        } else {
            tvNote.setText("Ghi chú: " + note);
        }

        renderTimeline(order.getStatus());
        renderDishes(order.getDishes());
    }

    private void renderTimeline(String currentStatus) {
        timelineContainer.removeAllViews();
        String[] statuses = new String[] { "CONFIRMED", "PROCESSING", "READY", "COMPLETED", "CANCELLED" };
        for (String status : statuses) {
            TextView chip = new TextView(this);
            chip.setText(status);
            chip.setTextSize(11f);
            chip.setPadding(dp(10), dp(6), dp(10), dp(6));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd(dp(6));
            chip.setLayoutParams(params);
            boolean isActive = status.equalsIgnoreCase(currentStatus);
            if (isActive) {
                int statusColor = getColor(StatusColorUtil.getStatusBackgroundColorRes(status));
                chip.setBackgroundResource(R.drawable.bg_status_chip_active);
                if (chip.getBackground() != null) {
                    chip.getBackground().setTint(statusColor);
                }
                chip.setTextColor(getColor(R.color.white));
            } else {
                chip.setBackgroundResource(R.drawable.bg_status_chip_inactive);
                chip.setTextColor(getColor(R.color.colorTextPrimary));
            }
            timelineContainer.addView(chip);
        }
    }

    private void renderDishes(List<OrderHistoryResponse.OrderDish> dishes) {
        dishesContainer.removeAllViews();
        if (dishes == null || dishes.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Không có món.");
            empty.setTextColor(getResources().getColor(R.color.colorTextSecondary));
            dishesContainer.addView(empty);
            return;
        }

        for (OrderHistoryResponse.OrderDish dish : dishes) {
            if (dish == null) {
                continue;
            }
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, dp(4), 0, dp(4));

            ImageView ivDish = new ImageView(this);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(24), dp(24));
            iconParams.setMarginEnd(dp(8));
            ivDish.setLayoutParams(iconParams);
            ivDish.setImageResource(R.drawable.ic_dish);
            ivDish.setColorFilter(getResources().getColor(R.color.colorTextPrimary));

            TextView tvDish = new TextView(this);
            String name = dish.getDishName() == null ? "" : dish.getDishName();
            tvDish.setText(name + " x" + dish.getQuantity() + " - " + formatCurrency(dish.getLineTotal()));
            tvDish.setTextColor(getResources().getColor(R.color.colorTextPrimary));
            tvDish.setTextSize(13f);

            row.addView(ivDish);
            row.addView(tvDish);
            dishesContainer.addView(row);

            List<OrderHistoryResponse.OrderDishItem> customItems = dish.getCustomItems();
            if (customItems == null) {
                continue;
            }
            for (OrderHistoryResponse.OrderDishItem item : customItems) {
                if (item == null) {
                    continue;
                }
                TextView tvItem = new TextView(this);
                String itemName = item.getItemName() == null ? "" : item.getItemName();
                String unit = item.getUnit() == null ? "" : item.getUnit();
                tvItem.setText("- " + itemName + " x" + item.getQuantity() + unit + " ("
                        + formatCurrency(item.getPrice()) + ")");
                tvItem.setTextColor(getResources().getColor(R.color.colorTextSecondary));
                tvItem.setTextSize(12f);
                tvItem.setPadding(dp(12), dp(2), 0, dp(2));
                dishesContainer.addView(tvItem);
            }
        }
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
                .enqueue(new retrofit2.Callback<BaseResponse<OrderResponse>>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<BaseResponse<OrderResponse>> call,
                                           @NonNull retrofit2.Response<BaseResponse<OrderResponse>> response) {
                        progressCancelOrder.setVisibility(View.GONE);
                        btnCancelOrder.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(OrderDetailActivity.this, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
                            btnCancelOrder.setVisibility(View.GONE);
                            tvStatus.setText("CANCELLED");
                            tvStatus.setTextColor(getColor(StatusColorUtil.getStatusColorRes("CANCELLED")));
                        } else {
                            String msg = "Hủy đơn thất bại";
                            if (response.body() != null && response.body().getMessage() != null) {
                                msg = response.body().getMessage();
                            } else if (response.errorBody() != null) {
                                try { msg = response.errorBody().string(); } catch (Exception ignored) {}
                            }
                            Toast.makeText(OrderDetailActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull retrofit2.Call<BaseResponse<OrderResponse>> call,
                                          @NonNull Throwable t) {
                        progressCancelOrder.setVisibility(View.GONE);
                        btnCancelOrder.setEnabled(true);
                        Toast.makeText(OrderDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
}
