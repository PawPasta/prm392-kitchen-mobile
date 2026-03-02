package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderHistoryResponse;

import java.text.NumberFormat;
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

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

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
        tvOrderId.setText("Order #" + shortId(orderId));
        tvStatus.setText(order.getStatus() == null ? "" : order.getStatus());

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
            chip.setTextColor(getResources().getColor(R.color.colorTextPrimary));
            chip.setPadding(dp(10), dp(6), dp(10), dp(6));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd(dp(6));
            chip.setLayoutParams(params);
            boolean isActive = status.equalsIgnoreCase(currentStatus);
            chip.setBackgroundResource(isActive
                    ? R.drawable.bg_status_chip_active
                    : R.drawable.bg_status_chip_inactive);
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
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        return format.format(amount);
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
