package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.adapters.OrderHistoryAdapter;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.CancelOrderRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderHistoryResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity {

    private PrefsManager prefsManager;
    private RecyclerView recyclerOrders;
    private ProgressBar progressOrders;
    private ProgressBar progressLoadMore;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout chipContainer;
    private OrderHistoryAdapter adapter;
    private boolean isLoading;
    private boolean isLastPage;
    private int currentPage;
    private final int pageSize = 10;
    private String selectedStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.orderHistoryMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefsManager = PrefsManager.getInstance(this);
        recyclerOrders = findViewById(R.id.recyclerOrders);
        progressOrders = findViewById(R.id.progressOrders);
        progressLoadMore = findViewById(R.id.progressLoadMore);
        tvEmpty = findViewById(R.id.tvEmptyOrders);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        chipContainer = findViewById(R.id.chipContainer);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        adapter = new OrderHistoryAdapter();
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setAdapter(adapter);
        adapter.setOnItemClickListener(this::openOrderDetail);
        adapter.setOnCancelClickListener(this::showCancelReasonDialog);
        adapter.setOnFeedbackClickListener(this::openOrderDetailForFeedback);

        setupChips();
        setupRefresh();
        setupPagination();

        loadOrders(0, pageSize, null, false);
    }

    private void loadOrders(int page, int size, String status, boolean isLoadMore) {
        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu token. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true, isLoadMore);
        ApiClient.getInstance()
                .getApiService()
                .getOrderHistory("Bearer " + token, page, size, status)
                .enqueue(new Callback<BaseResponse<OrderHistoryResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<OrderHistoryResponse>> call,
                            @NonNull Response<BaseResponse<OrderHistoryResponse>> response) {
                        setLoading(false, isLoadMore);

                        if (response.isSuccessful() && response.body() != null) {
                            BaseResponse<OrderHistoryResponse> baseResponse = response.body();
                            if (baseResponse.isSuccess() && baseResponse.getData() != null
                                    && baseResponse.getData().getOrders() != null) {
                                List<OrderHistoryResponse.OrderItem> orders = baseResponse.getData().getOrders()
                                        .getContent();
                                List<OrderHistoryResponse.OrderItem> visibleOrders = filterOutCreated(orders);
                                if (page == 0) {
                                    adapter.setItems(visibleOrders);
                                } else {
                                    adapter.addItems(visibleOrders);
                                }
                                currentPage = baseResponse.getData().getOrders().getPageNumber();
                                isLastPage = baseResponse.getData().getOrders().isLast();
                                boolean isEmpty = adapter.getItemCount() == 0;
                                tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                                return;
                            }
                            Toast.makeText(OrderHistoryActivity.this,
                                    baseResponse.getMessage() != null ? baseResponse.getMessage()
                                            : "Không thể tải lịch sử đơn.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        Toast.makeText(OrderHistoryActivity.this,
                                "Server error: " + response.code(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<OrderHistoryResponse>> call,
                            @NonNull Throwable t) {
                        setLoading(false, isLoadMore);
                        Toast.makeText(OrderHistoryActivity.this,
                                "Lỗi mạng khi tải lịch sử đơn.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean loading, boolean isLoadMore) {
        isLoading = loading;
        if (isLoadMore) {
            progressLoadMore.setVisibility(loading ? View.VISIBLE : View.GONE);
            return;
        }
        if (!swipeRefresh.isRefreshing()) {
            progressOrders.setVisibility(loading ? View.VISIBLE : View.GONE);
            recyclerOrders.setVisibility(loading ? View.GONE : View.VISIBLE);
        }
        swipeRefresh.setRefreshing(false);
    }

    private void setupRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            currentPage = 0;
            isLastPage = false;
            loadOrders(0, pageSize, selectedStatus, false);
        });
    }

    private void setupPagination() {
        recyclerOrders.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy <= 0) {
                    return;
                }
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) {
                    return;
                }
                int totalItemCount = layoutManager.getItemCount();
                int lastVisible = layoutManager.findLastVisibleItemPosition();
                if (!isLoading && !isLastPage && lastVisible >= totalItemCount - 2) {
                    loadOrders(currentPage + 1, pageSize, selectedStatus, true);
                }
            }
        });
    }

    private void setupChips() {
        String[] statuses = new String[] { "ALL", "CONFIRMED", "PROCESSING", "READY", "COMPLETED", "CANCELLED" };
        for (String status : statuses) {
            TextView chip = new TextView(this);
            chip.setText(status.equals("ALL") ? "Tất cả" : status);
            chip.setTag(status);
            chip.setTextSize(12f);
            chip.setTextColor(getResources().getColor(R.color.colorTextPrimary));
            int paddingH = (int) (12 * getResources().getDisplayMetrics().density);
            int paddingV = (int) (6 * getResources().getDisplayMetrics().density);
            chip.setPadding(paddingH, paddingV, paddingH, paddingV);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd((int) (8 * getResources().getDisplayMetrics().density));
            chip.setLayoutParams(params);

            chip.setBackgroundResource(R.drawable.bg_chip);
            chip.setOnClickListener(v -> {
                String tag = (String) v.getTag();
                selectedStatus = "ALL".equals(tag) ? null : tag;
                updateChipStyles();
                currentPage = 0;
                isLastPage = false;
                loadOrders(0, pageSize, selectedStatus, false);
            });

            chipContainer.addView(chip);
        }
        updateChipStyles();
    }

    private void updateChipStyles() {
        for (int i = 0; i < chipContainer.getChildCount(); i++) {
            View view = chipContainer.getChildAt(i);
            if (!(view instanceof TextView)) {
                continue;
            }
            TextView chip = (TextView) view;
            String tag = (String) chip.getTag();
            boolean selected = (selectedStatus == null && "ALL".equals(tag))
                    || (selectedStatus != null && selectedStatus.equals(tag));
            chip.setBackgroundResource(selected ? R.drawable.bg_chip_selected : R.drawable.bg_chip);
        }
    }

    private List<OrderHistoryResponse.OrderItem> filterOutCreated(
            List<OrderHistoryResponse.OrderItem> orders) {
        List<OrderHistoryResponse.OrderItem> filtered = new ArrayList<>();
        if (orders == null || orders.isEmpty()) {
            return filtered;
        }
        for (OrderHistoryResponse.OrderItem item : orders) {
            if (item == null) {
                continue;
            }
            String status = item.getStatus();
            if (status == null || status.trim().isEmpty()) {
                filtered.add(item);
                continue;
            }
            if (!"CREATED".equalsIgnoreCase(status.trim())) {
                filtered.add(item);
            }
        }
        return filtered;
    }


    private void openOrderDetail(OrderHistoryResponse.OrderItem item) {
        String json = new Gson().toJson(item);
        android.content.Intent intent = new android.content.Intent(this, OrderDetailActivity.class);
        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, item.getOrderId());
        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_JSON, json);
        startActivity(intent);
    }

    private void openOrderDetailForFeedback(OrderHistoryResponse.OrderItem item) {
        String json = new Gson().toJson(item);
        android.content.Intent intent = new android.content.Intent(this, OrderDetailActivity.class);
        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, item.getOrderId());
        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_JSON, json);
        intent.putExtra(OrderDetailActivity.EXTRA_SCROLL_TO_FEEDBACK, true);
        startActivity(intent);
    }

    private void showCancelReasonDialog(OrderHistoryResponse.OrderItem item) {
        if (item == null || item.getOrderId() == null || item.getOrderId().trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ID đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        EditText input = new EditText(this);
        input.setHint("Nhập lý do hủy");
        input.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout container = new LinearLayout(this);
        container.setPadding(padding, padding / 2, padding, 0);
        container.addView(input);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Lý do hủy đơn")
                .setView(container)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String reason = input.getText().toString().trim();
                    if (reason.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập lý do hủy", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    cancelOrder(item.getOrderId(), reason);
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void cancelOrder(String orderId, String reason) {
        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu token. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }

        ApiClient.getInstance()
                .getApiService()
                .cancelOrder("Bearer " + token, orderId, new CancelOrderRequest(reason))
                .enqueue(new Callback<BaseResponse<OrderResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<OrderResponse>> call,
                            @NonNull Response<BaseResponse<OrderResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(OrderHistoryActivity.this, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
                            currentPage = 0;
                            isLastPage = false;
                            loadOrders(0, pageSize, selectedStatus, false);
                            return;
                        }
                        String msg = "Hủy đơn thất bại";
                        if (response.body() != null && response.body().getMessage() != null) {
                            msg = response.body().getMessage();
                        }
                        Toast.makeText(OrderHistoryActivity.this, msg, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<OrderResponse>> call,
                            @NonNull Throwable t) {
                        Toast.makeText(OrderHistoryActivity.this,
                                "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
