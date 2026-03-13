package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.adapters.CartDishAdapter;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.UpdateOrderDishesRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private View btnDeleteMode;
    private View layoutDeleteActions;
    private CheckBox cbSelectAll;
    private MaterialButton btnDeleteSelected;

    private final List<OrderResponse.OrderDishDetail> currentDishes = new ArrayList<>();
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
        tvEmpty = findViewById(R.id.tvEmptyOrders);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        btnCreateCustomOrder = findViewById(R.id.btnCreateCustomOrder);
        btnDeleteMode = findViewById(R.id.btnDeleteMode);
        layoutDeleteActions = findViewById(R.id.layoutDeleteActions);
        cbSelectAll = findViewById(R.id.cbSelectAll);
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

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

        if (btnDeleteMode != null) {
            btnDeleteMode.setVisibility(View.VISIBLE);
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
                if (currentDishes.isEmpty()) {
                    Toast.makeText(CartActivity.this, "Giỏ hàng đang trống", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                startActivity(intent);
            });
        }

        if (layoutDeleteActions != null) {
            layoutDeleteActions.setVisibility(View.GONE);
        }

        dishAdapter = new CartDishAdapter();
        dishAdapter.setOnSelectionChangeListener((selectedCount, totalCount, allSelected) ->
                updateDeleteActionUi(selectedCount, allSelected));
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setAdapter(dishAdapter);

        if (cbSelectAll != null) {
            cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (ignoreSelectAllChange || !isDeleteMode) {
                    return;
                }
                dishAdapter.setSelectAll(isChecked);
            });
        }

        if (btnDeleteSelected != null) {
            btnDeleteSelected.setOnClickListener(v -> deleteSelectedDishes());
        }

        setupRefresh();
        loadCurrentOrder();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                                currentDishes.clear();
                                if (dishes != null) {
                                    currentDishes.addAll(dishes);
                                }
                                if (currentDishes.isEmpty()) {
                                    if (isDeleteMode) {
                                        exitDeleteMode();
                                    }
                                    showEmpty();
                                    return;
                                }
                                showDishes(currentDishes);
                                return;
                            }
                        }

                        currentDishes.clear();
                        if (isDeleteMode) {
                            exitDeleteMode();
                        }
                        showEmpty();
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<OrderResponse>> call,
                                          @NonNull Throwable t) {
                        setLoading(false);
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
        dishAdapter.setSelectionMode(isDeleteMode);
        recyclerOrders.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        if (isDeleteMode) {
            updateDeleteActionUi(dishAdapter.getSelectedCount(), dishAdapter.isAllSelected());
        }
    }

    private void showEmpty() {
        dishAdapter.setItems(new ArrayList<>());
        dishAdapter.setSelectionMode(false);
        recyclerOrders.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
    }

    private void enterDeleteMode() {
        if (currentDishes.isEmpty()) {
            Toast.makeText(this, "Không có món để xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        isDeleteMode = true;
        dishAdapter.setSelectionMode(true);

        if (btnCreateCustomOrder != null) {
            btnCreateCustomOrder.setVisibility(View.GONE);
        }
        if (layoutDeleteActions != null) {
            layoutDeleteActions.setVisibility(View.VISIBLE);
        }
        updateDeleteActionUi(dishAdapter.getSelectedCount(), dishAdapter.isAllSelected());
    }

    private void exitDeleteMode() {
        isDeleteMode = false;
        dishAdapter.setSelectionMode(false);

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

    private void deleteSelectedDishes() {
        Set<Integer> selectedDishKeys = dishAdapter.getSelectedDishKeys();
        if (selectedDishKeys.isEmpty()) {
            Toast.makeText(this, "Chưa chọn món nào", Toast.LENGTH_SHORT).show();
            return;
        }

        List<OrderResponse.OrderDishDetail> remainingDishes = new ArrayList<>();
        for (int i = 0; i < currentDishes.size(); i++) {
            OrderResponse.OrderDishDetail dish = currentDishes.get(i);
            int dishKey = getDishKey(dish, i);
            if (!selectedDishKeys.contains(dishKey)) {
                remainingDishes.add(dish);
            }
        }

        updateOrderDishes(remainingDishes, selectedDishKeys.size());
    }

    private int getDishKey(OrderResponse.OrderDishDetail dish, int position) {
        if (dish == null) {
            return -1 - position;
        }
        if (dish.getOrderDishId() > 0) {
            return dish.getOrderDishId();
        }
        return -1 - position;
    }

    private void updateOrderDishes(List<OrderResponse.OrderDishDetail> remainingDishes, int deletedCount) {
        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu token. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }

        UpdateOrderDishesRequest request = buildUpdateOrderDishesRequest(remainingDishes);

        if (btnDeleteSelected != null) {
            btnDeleteSelected.setEnabled(false);
        }
        if (cbSelectAll != null) {
            cbSelectAll.setEnabled(false);
        }
        if (btnDeleteMode != null) {
            btnDeleteMode.setEnabled(false);
        }

        ApiClient.getInstance()
                .getApiService()
                .updateOrderDishes("Bearer " + token, request)
                .enqueue(new Callback<BaseResponse<OrderResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<OrderResponse>> call,
                                           @NonNull Response<BaseResponse<OrderResponse>> response) {
                        restoreDeleteActionState();

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {
                            currentDishes.clear();
                            currentDishes.addAll(remainingDishes);

                            exitDeleteMode();
                            if (currentDishes.isEmpty()) {
                                showEmpty();
                            } else {
                                showDishes(currentDishes);
                            }

                            Toast.makeText(CartActivity.this,
                                    "Đã xóa " + deletedCount + " món",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String message = "Không thể cập nhật giỏ hàng";
                        if (response.body() != null && response.body().getMessage() != null) {
                            message = response.body().getMessage();
                        }
                        Toast.makeText(CartActivity.this, message, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<OrderResponse>> call,
                                          @NonNull Throwable t) {
                        restoreDeleteActionState();
                        Toast.makeText(CartActivity.this,
                                "Lỗi mạng khi cập nhật giỏ hàng.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void restoreDeleteActionState() {
        if (btnDeleteSelected != null) {
            btnDeleteSelected.setEnabled(true);
        }
        if (cbSelectAll != null) {
            cbSelectAll.setEnabled(true);
        }
        if (btnDeleteMode != null) {
            btnDeleteMode.setEnabled(true);
        }
    }

    private UpdateOrderDishesRequest buildUpdateOrderDishesRequest(
            List<OrderResponse.OrderDishDetail> dishes) {
        List<UpdateOrderDishesRequest.DishInput> requestDishes = new ArrayList<>();
        if (dishes != null) {
            for (OrderResponse.OrderDishDetail dish : dishes) {
                if (dish == null) {
                    continue;
                }
                requestDishes.add(mapDishInput(dish));
            }
        }
        return new UpdateOrderDishesRequest(requestDishes);
    }

    private UpdateOrderDishesRequest.DishInput mapDishInput(OrderResponse.OrderDishDetail dish) {
        int quantity = Math.max(1, dish.getQuantity());

        List<OrderResponse.OrderDishItemDetail> customItems = dish.getCustomItems();
        boolean hasCustomItems = customItems != null && !customItems.isEmpty();
        boolean isCustomDish = "CUSTOM".equalsIgnoreCase(safeText(dish.getDishStatus()));

        if ((isCustomDish || hasCustomItems) && hasCustomItems) {
            UpdateOrderDishesRequest.CustomDish customDish = mapCustomDish(dish, customItems);
            return UpdateOrderDishesRequest.DishInput.fromCustom(customDish, quantity);
        }

        if (dish.getDishId() > 0) {
            return UpdateOrderDishesRequest.DishInput.fromDish(dish.getDishId(), quantity);
        }

        UpdateOrderDishesRequest.CustomDish fallbackCustomDish =
                new UpdateOrderDishesRequest.CustomDish(
                        safeText(dish.getDishName()).isEmpty() ? "Custom dish" : dish.getDishName().trim(),
                        "",
                        new ArrayList<>());
        return UpdateOrderDishesRequest.DishInput.fromCustom(fallbackCustomDish, quantity);
    }

    private UpdateOrderDishesRequest.CustomDish mapCustomDish(
            OrderResponse.OrderDishDetail dish,
            List<OrderResponse.OrderDishItemDetail> customItems) {
        Map<Integer, List<OrderResponse.OrderDishItemDetail>> groupedByStep = new LinkedHashMap<>();
        for (OrderResponse.OrderDishItemDetail item : customItems) {
            if (item == null) {
                continue;
            }
            int stepId = item.getStepId();
            if (stepId <= 0) {
                continue;
            }
            groupedByStep.computeIfAbsent(stepId, key -> new ArrayList<>()).add(item);
        }

        List<UpdateOrderDishesRequest.Step> steps = new ArrayList<>();
        for (Map.Entry<Integer, List<OrderResponse.OrderDishItemDetail>> entry : groupedByStep.entrySet()) {
            int stepId = entry.getKey();
            List<OrderResponse.OrderDishItemDetail> stepItems = entry.getValue();

            List<UpdateOrderDishesRequest.StepItem> mappedItems = new ArrayList<>();

            for (OrderResponse.OrderDishItemDetail stepItem : stepItems) {
                if (stepItem == null || stepItem.getItemId() <= 0) {
                    continue;
                }
                mappedItems.add(new UpdateOrderDishesRequest.StepItem(
                        stepItem.getItemId(),
                        stepItem.getQuantity(),
                        stepItem.getNote()));
            }

            steps.add(new UpdateOrderDishesRequest.Step(
                    stepId,
                    new ArrayList<>(),
                    mappedItems));
        }

        String customName = safeText(dish.getDishName()).isEmpty()
                ? "Custom dish"
                : dish.getDishName().trim();
        return new UpdateOrderDishesRequest.CustomDish(customName, "", steps);
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}
