package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.os.Bundle;
import android.view.View;
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

import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.adapters.CartDishAdapter;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {

    private PrefsManager prefsManager;
    private RecyclerView recyclerOrders;
    private ProgressBar progressOrders;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private CartDishAdapter dishAdapter;
    private View btnCreateCustomOrder;

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
        tvEmpty = findViewById(R.id.tvEmptyOrders);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        btnCreateCustomOrder = findViewById(R.id.btnCreateCustomOrder);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        View btnDeleteMode = findViewById(R.id.btnDeleteMode);
        if (btnDeleteMode != null) {
            btnDeleteMode.setVisibility(View.GONE);
        }

        View layoutDeleteActions = findViewById(R.id.layoutDeleteActions);
        if (layoutDeleteActions != null) {
            layoutDeleteActions.setVisibility(View.GONE);
        }

        Spinner spinnerSortTime = findViewById(R.id.spinnerSortTime);
        if (spinnerSortTime != null) {
            View sortRow = (View) spinnerSortTime.getParent();
            if (sortRow != null) {
                sortRow.setVisibility(View.GONE);
            } else {
                spinnerSortTime.setVisibility(View.GONE);
            }
        }

        ProgressBar progressLoadMore = findViewById(R.id.progressLoadMore);
        if (progressLoadMore != null) {
            progressLoadMore.setVisibility(View.GONE);
        }

        if (btnCreateCustomOrder != null) {
            btnCreateCustomOrder.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(
                        CartActivity.this,
                        com.prm392_sp26.prm392_kitchen_mobile.activities.CreateCustomOrderActivity.class);
                startActivity(intent);
            });
        }

        dishAdapter = new CartDishAdapter();
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setAdapter(dishAdapter);

        setupRefresh();
        loadCurrentOrder();
    }

    private void setupRefresh() {
        swipeRefresh.setOnRefreshListener(this::loadCurrentOrder);
    }

    private void loadCurrentOrder() {
        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu token. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);

        ApiClient.getInstance()
                .getApiService()
                .getCurrentOrder("Bearer " + token)
                .enqueue(new Callback<BaseResponse<OrderResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<OrderResponse>> call,
                                           @NonNull Response<BaseResponse<OrderResponse>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            BaseResponse<OrderResponse> baseResponse = response.body();
                            if (baseResponse.isSuccess() && baseResponse.getData() != null) {
                                List<OrderResponse.OrderDishDetail> dishes = baseResponse.getData().getDishes();
                                if (dishes == null || dishes.isEmpty()) {
                                    showEmpty();
                                    return;
                                }
                                showDishes(dishes);
                                return;
                            }
                        }

                        showEmpty();
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<OrderResponse>> call,
                                          @NonNull Throwable t) {
                        setLoading(false);
                        showEmpty();
                        Toast.makeText(CartActivity.this,
                                "Lỗi mạng khi tải giỏ hàng.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        if (!swipeRefresh.isRefreshing()) {
            progressOrders.setVisibility(loading ? View.VISIBLE : View.GONE);
            if (loading) {
                recyclerOrders.setVisibility(View.GONE);
            }
        }
        swipeRefresh.setRefreshing(false);
    }

    private void showDishes(List<OrderResponse.OrderDishDetail> dishes) {
        dishAdapter.setItems(dishes);
        recyclerOrders.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
    }

    private void showEmpty() {
        dishAdapter.setItems(new ArrayList<>());
        recyclerOrders.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
    }
}
