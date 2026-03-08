package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import com.prm392_sp26.prm392_kitchen_mobile.model.request.CancelOrderRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderHistoryResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {

    private static final String STATUS_CREATED = "CREATED";
    private static final int SORT_NEWEST = 0;
    private static final int SORT_OLDEST = 1;
    private final int pageSize = 10;

    private PrefsManager prefsManager;
    private RecyclerView recyclerOrders;
    private ProgressBar progressOrders;
    private ProgressBar progressLoadMore;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private OrderHistoryAdapter adapter;
    private View btnCreateCustomOrder;
    private Spinner spinnerSortTime;
    private final List<OrderHistoryResponse.OrderItem> cachedOrders = new ArrayList<>();
    private int currentSort = SORT_NEWEST;
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
        spinnerSortTime = findViewById(R.id.spinnerSortTime);

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
        adapter.setOnCancelClickListener(this::showCancelReasonDialog);

        setupSortSpinner();
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
                                    cachedOrders.clear();
                                }
                                if (orders != null) {
                                    cachedOrders.addAll(orders);
                                }
                                applySortAndRender();
                                currentPage = baseResponse.getData().getOrders().getPageNumber();
                                isLastPage = baseResponse.getData().getOrders().isLast();
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

    private void setupSortSpinner() {
        if (spinnerSortTime == null) {
            return;
        }
        List<String> options = new ArrayList<>();
        options.add("Mới nhất");
        options.add("Cũ nhất");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortTime.setAdapter(adapter);
        spinnerSortTime.setSelection(currentSort, false);
        spinnerSortTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (currentSort != position) {
                    currentSort = position;
                    applySortAndRender();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void applySortAndRender() {
        List<OrderHistoryResponse.OrderItem> sorted = new ArrayList<>(cachedOrders);
        Collections.sort(sorted, (left, right) -> {
            long leftTime = getCreatedAtMillis(left);
            long rightTime = getCreatedAtMillis(right);
            if (currentSort == SORT_NEWEST) {
                return Long.compare(rightTime, leftTime);
            }
            return Long.compare(leftTime, rightTime);
        });
        adapter.setItems(sorted);
        tvEmpty.setVisibility(sorted.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private long getCreatedAtMillis(OrderHistoryResponse.OrderItem item) {
        if (item == null) {
            return 0L;
        }
        String createdAt = item.getCreatedAt();
        if (createdAt == null || createdAt.trim().isEmpty()) {
            return 0L;
        }
        Date parsed = parseIsoDate(createdAt);
        return parsed == null ? 0L : parsed.getTime();
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

    private void showCancelReasonDialog(OrderHistoryResponse.OrderItem item) {
        if (item == null || item.getOrderId() == null || item.getOrderId().trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ID đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        EditText input = new EditText(this);
        input.setHint("Nhập lý do hủy");
        input.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
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
                            Toast.makeText(CartActivity.this, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
                            currentPage = 0;
                            isLastPage = false;
                            loadOrders(0, pageSize, false);
                            return;
                        }
                        String msg = "Hủy đơn thất bại";
                        if (response.body() != null && response.body().getMessage() != null) {
                            msg = response.body().getMessage();
                        }
                        Toast.makeText(CartActivity.this, msg, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<OrderResponse>> call, @NonNull Throwable t) {
                        Toast.makeText(CartActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
