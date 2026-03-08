package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.adapters.OrderHistoryAdapter;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderHistoryResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {

    private static final String STATUS_CREATED = "CREATED";
    private final int pageSize = 10;

    private PrefsManager prefsManager;
    private RecyclerView recyclerOrders;
    private ProgressBar progressOrders;
    private ProgressBar progressLoadMore;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private OrderHistoryAdapter adapter;
    private View btnCreateCustomOrder;
    private boolean isLoading;
    private boolean isLastPage;
    private int currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cartMain), (v, insets) -> {
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
        btnCreateCustomOrder = findViewById(R.id.btnCreateCustomOrder);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        if (btnCreateCustomOrder != null) {
            btnCreateCustomOrder.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(
                        CartActivity.this,
                        com.prm392_sp26.prm392_kitchen_mobile.activities.CreateCustomOrderActivity.class);
                startActivity(intent);
            });
        }

        adapter = new OrderHistoryAdapter();
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setAdapter(adapter);
        adapter.setOnItemClickListener(this::openOrderDetail);

        setupRefresh();
        setupPagination();

        loadOrders(0, pageSize, false);
    }

    private void loadOrders(int page, int size, boolean isLoadMore) {
        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu token. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true, isLoadMore);
        ApiClient.getInstance()
                .getApiService()
                .getOrderHistory("Bearer " + token, page, size, STATUS_CREATED)
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
                                if (page == 0) {
                                    adapter.setItems(orders);
                                } else {
                                    adapter.addItems(orders);
                                }
                                currentPage = baseResponse.getData().getOrders().getPageNumber();
                                isLastPage = baseResponse.getData().getOrders().isLast();
                                tvEmpty.setVisibility(adapter.getItemCount() == 0
                                        ? View.VISIBLE
                                        : View.GONE);
                                return;
                            }
                            Toast.makeText(CartActivity.this,
                                    baseResponse.getMessage() != null ? baseResponse.getMessage()
                                            : "Không thể tải giỏ hàng.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        Toast.makeText(CartActivity.this,
                                "Server error: " + response.code(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<OrderHistoryResponse>> call, @NonNull Throwable t) {
                        setLoading(false, isLoadMore);
                        Toast.makeText(CartActivity.this,
                                "Lỗi mạng khi tải giỏ hàng.", Toast.LENGTH_LONG).show();
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
            loadOrders(0, pageSize, false);
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
                    loadOrders(currentPage + 1, pageSize, true);
                }
            }
        });
    }

    private void openOrderDetail(OrderHistoryResponse.OrderItem item) {
        String json = new Gson().toJson(item);
        android.content.Intent intent = new android.content.Intent(this, OrderDetailActivity.class);
        intent.putExtra("order_json", json);
        startActivity(intent);
    }
}
