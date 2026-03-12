package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
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

import com.google.android.material.button.MaterialButton;
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
    private static final String DELETE_REASON = "Xóa khỏi giỏ hàng";
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
    private View btnDeleteMode;
    private View layoutDeleteActions;
    private CheckBox cbSelectAll;
    private MaterialButton btnDeleteSelected;
    private final List<OrderHistoryResponse.OrderItem> cachedOrders = new ArrayList<>();
    private int currentSort = SORT_NEWEST;
    private boolean isLoading;
    private boolean isLastPage;
    private int currentPage;
    private boolean isDeleteMode;
    private boolean ignoreSelectAllChange;

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
        btnDeleteMode = findViewById(R.id.btnDeleteMode);
        layoutDeleteActions = findViewById(R.id.layoutDeleteActions);
        cbSelectAll = findViewById(R.id.cbSelectAll);
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        if (btnDeleteMode != null) {
            btnDeleteMode.setOnClickListener(v -> {
                if (isDeleteMode) {
                    exitDeleteMode();
                } else {
                    enterDeleteMode();
                }
            });
        }
        if (btnCreateCustomOrder != null) {
            btnCreateCustomOrder.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(
                        CartActivity.this,
                        com.prm392_sp26.prm392_kitchen_mobile.activities.CreateCustomOrderActivity.class);
                startActivity(intent);
            });
        }

        adapter = new OrderHistoryAdapter();
        adapter.setCancelEnabled(false);
        adapter.setOnItemClickListener(this::openOrderDetail);
        adapter.setOnSelectionChangeListener((selectedCount, totalCount, allSelected) ->
                updateDeleteActionUi(selectedCount, allSelected));
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setAdapter(adapter);

        if (cbSelectAll != null) {
            cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (ignoreSelectAllChange || !isDeleteMode) {
                    return;
                }
                adapter.setSelectAll(isChecked);
            });
        }
        if (btnDeleteSelected != null) {
            btnDeleteSelected.setOnClickListener(v -> deleteSelectedOrders());
        }

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
                                List<OrderHistoryResponse.OrderItem> orders =
                                        baseResponse.getData().getOrders().getContent();
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
                    public void onFailure(@NonNull Call<BaseResponse<OrderHistoryResponse>> call,
                                          @NonNull Throwable t) {
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
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                options);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortTime.setAdapter(spinnerAdapter);
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

    private void enterDeleteMode() {
        if (adapter.getItemCount() == 0) {
            Toast.makeText(this, "Không có đơn để xóa", Toast.LENGTH_SHORT).show();
            return;
        }
        isDeleteMode = true;
        adapter.setSelectionMode(true);
        if (btnCreateCustomOrder != null) {
            btnCreateCustomOrder.setVisibility(View.GONE);
        }
        if (layoutDeleteActions != null) {
            layoutDeleteActions.setVisibility(View.VISIBLE);
        }
        updateDeleteActionUi(0, false);
    }

    private void exitDeleteMode() {
        isDeleteMode = false;
        adapter.setSelectionMode(false);
        if (layoutDeleteActions != null) {
            layoutDeleteActions.setVisibility(View.GONE);
        }
        if (btnCreateCustomOrder != null) {
            btnCreateCustomOrder.setVisibility(View.VISIBLE);
        }
        if (cbSelectAll != null) {
            ignoreSelectAllChange = true;
            cbSelectAll.setChecked(false);
            ignoreSelectAllChange = false;
        }
    }

    private void updateDeleteActionUi(int selectedCount, boolean allSelected) {
        if (!isDeleteMode) {
            return;
        }
        if (cbSelectAll != null) {
            ignoreSelectAllChange = true;
            cbSelectAll.setChecked(allSelected);
            ignoreSelectAllChange = false;
        }
        if (btnDeleteSelected != null) {
            btnDeleteSelected.setText("Xóa (" + selectedCount + ")");
            btnDeleteSelected.setEnabled(selectedCount > 0);
        }
    }

    private void deleteSelectedOrders() {
        List<String> selectedIds = adapter.getSelectedOrderIds();
        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Chưa chọn đơn nào", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu token. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }

        if (btnDeleteSelected != null) {
            btnDeleteSelected.setEnabled(false);
        }
        if (cbSelectAll != null) {
            cbSelectAll.setEnabled(false);
        }
        if (btnDeleteMode != null) {
            btnDeleteMode.setEnabled(false);
        }

        final int total = selectedIds.size();
        final int[] completed = {0};
        final int[] success = {0};

        for (String orderId : selectedIds) {
            ApiClient.getInstance()
                    .getApiService()
                    .cancelOrder("Bearer " + token, orderId, new CancelOrderRequest(DELETE_REASON))
                    .enqueue(new Callback<BaseResponse<OrderResponse>>() {
                        @Override
                        public void onResponse(@NonNull Call<BaseResponse<OrderResponse>> call,
                                               @NonNull Response<BaseResponse<OrderResponse>> response) {
                            if (response.isSuccessful()
                                    && response.body() != null
                                    && response.body().isSuccess()) {
                                success[0]++;
                            }
                            onDeleteRequestDone(completed, success, total);
                        }

                        @Override
                        public void onFailure(@NonNull Call<BaseResponse<OrderResponse>> call,
                                              @NonNull Throwable t) {
                            onDeleteRequestDone(completed, success, total);
                        }
                    });
        }
    }

    private void onDeleteRequestDone(int[] completed, int[] success, int total) {
        completed[0]++;
        if (completed[0] < total) {
            return;
        }

        if (btnDeleteSelected != null) {
            btnDeleteSelected.setEnabled(true);
        }
        if (cbSelectAll != null) {
            cbSelectAll.setEnabled(true);
        }
        if (btnDeleteMode != null) {
            btnDeleteMode.setEnabled(true);
        }

        if (success[0] > 0) {
            Toast.makeText(this,
                    "Đã xóa " + success[0] + "/" + total + " đơn",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,
                    "Không thể xóa đơn đã chọn",
                    Toast.LENGTH_SHORT).show();
        }

        currentPage = 0;
        isLastPage = false;
        exitDeleteMode();
        loadOrders(0, pageSize, false);
    }
}
