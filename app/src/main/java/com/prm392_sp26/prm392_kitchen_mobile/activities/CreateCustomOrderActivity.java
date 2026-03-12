package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.adapters.SelectableItemAdapter;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.CreateOrderRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.DishStepResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.ItemResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.shared.PageResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.Constants;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import android.content.res.ColorStateList;
import android.content.Intent;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.prm392_sp26.prm392_kitchen_mobile.model.request.RefreshTokenRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity để tạo đơn hàng từ custom dish builder
 */
public class CreateCustomOrderActivity extends AppCompatActivity {

    private static final String TAG = "CreateCustomOrderActivity";

    // Views
    private ImageView btnBack;
    private RecyclerView rvItems;
    private EditText etNote;
    private MaterialButton btnCreateOrder;
    private ProgressBar progressCreateOrder;
    private ProgressBar progressSteps;
    private TextView tvStepProgress;
    private TextView tvTotalCalories;
    private TextView tvCurrentStepName;
    private MaterialButton btnSkipStep;
    private MaterialButton btnNextStep;
    private View layoutFinalize;

    // Data
    private int quantity = 1;
    private List<DishStepResponse> steps = new ArrayList<>();
    private DishStepResponse selectedStep = null;
    private int currentStepIndex = 0;
    // One persistent adapter per stepId so selections survive when switching steps
    private Map<Integer, SelectableItemAdapter> stepAdapterMap = new HashMap<>();
    private Map<Integer, List<ItemResponse>> stepItemsMap = new HashMap<>();
    private Set<Integer> skippedSteps = new HashSet<>();
    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_custom_order);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.customOrderRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefsManager = PrefsManager.getInstance(this);
        initializeViews();
        setupListeners();
        loadStepsAndItems();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        rvItems = findViewById(R.id.rvItems);
        etNote = findViewById(R.id.etNote);
        btnCreateOrder = findViewById(R.id.btnCreateOrder);
        progressCreateOrder = findViewById(R.id.progressCreateOrder);
        progressSteps = findViewById(R.id.progressSteps);
        tvStepProgress = findViewById(R.id.tvStepProgress);
        tvTotalCalories = findViewById(R.id.tvTotalCalories);
        tvCurrentStepName = findViewById(R.id.tvCurrentStepName);
        btnSkipStep = findViewById(R.id.btnSkipStep);
        btnNextStep = findViewById(R.id.btnNextStep);
        layoutFinalize = findViewById(R.id.layoutFinalize);

        rvItems.setLayoutManager(new LinearLayoutManager(this));
        if (btnCreateOrder != null) {
            btnCreateOrder.setEnabled(false);
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnCreateOrder != null) {
            btnCreateOrder.setOnClickListener(v -> createOrder());
        }

        if (btnSkipStep != null) {
            btnSkipStep.setOnClickListener(v -> skipCurrentStep());
        }

        if (btnNextStep != null) {
            btnNextStep.setOnClickListener(v -> {
                if (selectedStep == null) {
                    return;
                }
                if (!isStepDone(selectedStep.getStepId())) {
                    Toast.makeText(this, "Hãy chọn nguyên liệu hoặc bấm Bỏ qua bước", Toast.LENGTH_SHORT).show();
                    return;
                }
                moveToNextStep();
                updateProgressAndCalories();
            });
        }
    }

    private void loadStepsAndItems() {
        String token = "Bearer " + prefsManager.getAccessToken();

        // Lấy tất cả items (bao gồm stepName = nhóm thành phần: carb, protein, sauce...)
        ApiClient.getInstance()
                .getApiService()
                .getAllItems(token, 0, 100)
                .enqueue(new Callback<BaseResponse<PageResponse<ItemResponse>>>() {
            @Override
            public void onResponse(Call<BaseResponse<PageResponse<ItemResponse>>> call, Response<BaseResponse<PageResponse<ItemResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<ItemResponse> allItems = response.body().getData().getContent();
                    if (allItems != null && !allItems.isEmpty()) {
                        // Nhóm items theo stepName (carb, protein, sauce, etc.)
                        Map<String, List<ItemResponse>> groupedItems = new HashMap<>();
                        Map<String, Integer> stepNameToId = new HashMap<>();

                        for (ItemResponse item : allItems) {
                            String stepName = item.getStepName();
                            int stepId = item.getStepId();

                            // Bỏ qua item DISABLE / EXPIRED — không đưa vào danh sách chọn
                            if (!"ENABLE".equals(item.getStatus())) continue;

                            if (stepName != null && !stepName.isEmpty()) {
                                if (!groupedItems.containsKey(stepName)) {
                                    groupedItems.put(stepName, new ArrayList<>());
                                    stepNameToId.put(stepName, stepId);
                                }
                                groupedItems.get(stepName).add(item);
                            }
                        }

                        // Tạo steps từ groupedItems
                        steps.clear();
                        for (Map.Entry<String, Integer> entry : stepNameToId.entrySet()) {
                            String stepName = entry.getKey();
                            int stepId = entry.getValue();

                            steps.add(new DishStepResponse() {
                                @Override
                                public int getStepId() { return stepId; }
                                @Override
                                public String getStepName() { return stepName; }
                            });

                            // Lưu items cho step này
                            stepItemsMap.put(stepId, groupedItems.get(stepName));
                        }

                        // Display items for first step
                        if (!steps.isEmpty()) {
                            setCurrentStep(0);
                        }
                        updateProgressAndCalories();
                    } else {
                        Toast.makeText(CreateCustomOrderActivity.this, "Không có nguyên liệu nào", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CreateCustomOrderActivity.this, "Lỗi tải danh sách nguyên liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<PageResponse<ItemResponse>>> call, Throwable t) {
                Toast.makeText(CreateCustomOrderActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadItemsForStep(int stepId) {
        String token = "Bearer " + prefsManager.getAccessToken();

        // API để lấy items cho step
        ApiClient.getInstance()
                .getApiService()
                .getItemsByStep(token, stepId, 0, 50)
                .enqueue(new Callback<BaseResponse<PageResponse<ItemResponse>>>() {
            @Override
            public void onResponse(Call<BaseResponse<PageResponse<ItemResponse>>> call, Response<BaseResponse<PageResponse<ItemResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<ItemResponse> items = response.body().getData().getContent();
                    if (items != null) {
                        stepItemsMap.put(stepId, items);
                        displayItemsForStep(stepId);
                    }
                } else {
                    Toast.makeText(CreateCustomOrderActivity.this, "Không có nguyên liệu cho bước này", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<PageResponse<ItemResponse>>> call, Throwable t) {
                Toast.makeText(CreateCustomOrderActivity.this, "Lỗi tải nguyên liệu: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayItemsForStep(int stepId) {
        List<ItemResponse> items = stepItemsMap.getOrDefault(stepId, new ArrayList<>());
        // Reuse existing adapter so selections are preserved when switching steps
        SelectableItemAdapter adapter = stepAdapterMap.get(stepId);
        if (adapter == null) {
            adapter = new SelectableItemAdapter(items);
            adapter.setOnSelectionChangedListener(this::updateProgressAndCalories);
            stepAdapterMap.put(stepId, adapter);
        }
        if (rvItems != null) {
            rvItems.setAdapter(adapter);
        }
    }

    private void createOrder() {
        if (btnCreateOrder != null) {
            btnCreateOrder.setEnabled(true);
        }

        if (!areAllStepsDone()) {
            Toast.makeText(this, "Vui lòng hoàn tất các bước chọn nguyên liệu", Toast.LENGTH_SHORT).show();
            return;
        }
        quantity = 1;

        // Collect selections from ALL steps
        List<CreateOrderRequest.Step> stepsList = new ArrayList<>();
        for (DishStepResponse step : steps) {
            int stepId = step.getStepId();
            SelectableItemAdapter adapter = stepAdapterMap.get(stepId);
            if (adapter == null) continue;

            List<Integer> selectedItemIds = adapter.getSelectedItemIds();
            if (selectedItemIds.isEmpty()) continue;

            stepsList.add(new CreateOrderRequest.Step(stepId, selectedItemIds));
        }

        if (stepsList.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một nguyên liệu", Toast.LENGTH_SHORT).show();
            return;
        }

        String note = etNote != null ? etNote.getText().toString().trim() : "";

        CreateOrderRequest.CustomDish customDish = new CreateOrderRequest.CustomDish(
                "Custom Dish",
                "Tùy chỉnh bởi người dùng",
                stepsList
        );

        List<CreateOrderRequest.DishInput> dishes = new ArrayList<>();
        dishes.add(CreateOrderRequest.DishInput.fromCustom(customDish, quantity));
        CreateOrderRequest request = new CreateOrderRequest(null, note, dishes);

        // Show loading
        if (progressCreateOrder != null) {
            progressCreateOrder.setVisibility(View.VISIBLE);
        }
        if (btnCreateOrder != null) {
            btnCreateOrder.setEnabled(false);
        }

        String token = "Bearer " + prefsManager.getAccessToken();

        ApiClient.getInstance()
                .getApiService()
                .createOrder(Constants.ORDER_CREATE_URL, token, request)
                .enqueue(new Callback<BaseResponse<OrderResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<OrderResponse>> call, Response<BaseResponse<OrderResponse>> response) {
                if (progressCreateOrder != null) {
                    progressCreateOrder.setVisibility(View.GONE);
                }
                if (btnCreateOrder != null) {
                    btnCreateOrder.setEnabled(true);
                }

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(CreateCustomOrderActivity.this, "Đặt món custom thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (response.code() == 401) {
                    refreshTokenAndRetry(() -> createOrder());
                } else {
                    String errorMsg = "Lỗi tạo đơn hàng (HTTP " + response.code() + ")";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    } else if (response.errorBody() != null) {
                        try { errorMsg = response.errorBody().string(); } catch (Exception ignored) {}
                    }
                    Log.e("CreateCustomOrderActivity", "createOrder error: " + errorMsg);
                    Toast.makeText(CreateCustomOrderActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<OrderResponse>> call, Throwable t) {
                if (progressCreateOrder != null) {
                    progressCreateOrder.setVisibility(View.GONE);
                }
                if (btnCreateOrder != null) {
                    btnCreateOrder.setEnabled(true);
                }
                Log.e("CreateCustomOrderActivity", "createOrder failure", t);
                Toast.makeText(CreateCustomOrderActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshTokenAndRetry(Runnable onSuccess) {
        String refreshToken = prefsManager.getRefreshToken();
        if (refreshToken == null) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            navigateToLogin();
            return;
        }
        ApiClient.getInstance().getApiService()
            .refreshToken(new RefreshTokenRequest(refreshToken))
            .enqueue(new Callback<BaseResponse<LoginResponse>>() {
                @Override
                public void onResponse(Call<BaseResponse<LoginResponse>> call, Response<BaseResponse<LoginResponse>> res) {
                    if (res.isSuccessful() && res.body() != null && res.body().isSuccess()) {
                        prefsManager.saveLoginResponse(res.body().getData());
                        onSuccess.run();
                    } else {
                        Toast.makeText(CreateCustomOrderActivity.this, "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                        navigateToLogin();
                    }
                }
                @Override
                public void onFailure(Call<BaseResponse<LoginResponse>> call, Throwable t) {
                    Toast.makeText(CreateCustomOrderActivity.this, "Lỗi xác thực: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean areAllStepsDone() {
        if (steps.isEmpty()) {
            return false;
        }
        for (DishStepResponse step : steps) {
            int stepId = step.getStepId();
            SelectableItemAdapter adapter = stepAdapterMap.get(stepId);
            boolean hasSelection = adapter != null && !adapter.getSelectedItemIds().isEmpty();
            if (!hasSelection && !skippedSteps.contains(stepId)) {
                return false;
            }
        }
        return true;
    }

    private void skipCurrentStep() {
        if (selectedStep == null) {
            return;
        }
        skippedSteps.add(selectedStep.getStepId());
        moveToNextStep();
        updateProgressAndCalories();
    }

    private void moveToNextStep() {
        if (selectedStep == null || steps.isEmpty()) {
            return;
        }
        int nextIndex = currentStepIndex + 1;
        if (nextIndex >= steps.size()) {
            return;
        }
        setCurrentStep(nextIndex);
    }

    private void updateProgressAndCalories() {
        int totalSteps = steps.size();
        int completedSteps = 0;
        double totalCalories = 0.0;

        for (DishStepResponse step : steps) {
            int stepId = step.getStepId();
            SelectableItemAdapter adapter = stepAdapterMap.get(stepId);
            boolean hasSelection = adapter != null && !adapter.getSelectedItemIds().isEmpty();
            if (hasSelection || skippedSteps.contains(stepId)) {
                completedSteps++;
            }
            if (adapter == null) {
                continue;
            }
            Map<Integer, Double> quantities = adapter.getSelectedQuantities();
            for (Integer itemId : adapter.getSelectedItemIds()) {
                ItemResponse item = getItemById(stepId, itemId);
                if (item == null) {
                    continue;
                }
                double defaultQty = item.getBaseQuantity() > 0 ? item.getBaseQuantity() : 50.0;
                double qty = quantities.getOrDefault(itemId, defaultQty);
                totalCalories += getCaloriesForItem(item, qty);
            }
        }

        if (progressSteps != null) {
            progressSteps.setMax(Math.max(totalSteps, 1));
            progressSteps.setProgress(completedSteps);
        }
        if (tvStepProgress != null) {
            tvStepProgress.setText("Tiến độ: " + completedSteps + "/" + totalSteps + " bước");
        }
        if (tvTotalCalories != null) {
            tvTotalCalories.setText(Math.round(totalCalories) + " kcal");
        }

        boolean allDone = totalSteps > 0 && completedSteps == totalSteps;
        if (layoutFinalize != null) {
            layoutFinalize.setVisibility(allDone ? View.VISIBLE : View.GONE);
        }
        if (btnCreateOrder != null) {
            btnCreateOrder.setEnabled(allDone);
        }
        updateStepControls();
    }

    private void setCurrentStep(int index) {
        if (index < 0 || index >= steps.size()) {
            return;
        }
        currentStepIndex = index;
        selectedStep = steps.get(index);
        displayItemsForStep(selectedStep.getStepId());
        updateCurrentStepUi();
    }

    private void updateCurrentStepUi() {
        if (tvCurrentStepName == null) {
            return;
        }
        if (steps.isEmpty() || selectedStep == null) {
            tvCurrentStepName.setText("Bước: --");
            int color = ContextCompat.getColor(this, R.color.stepDefault);
            ViewCompat.setBackgroundTintList(tvCurrentStepName, ColorStateList.valueOf(color));
            return;
        }
        String stepName = selectedStep.getStepName();
        String displayName = stepName != null && !stepName.isEmpty()
                ? "Bước " + (currentStepIndex + 1) + ": " + stepName
                : "Bước " + (currentStepIndex + 1);
        tvCurrentStepName.setText(displayName);
        int color = ContextCompat.getColor(this, getStepColorRes(stepName));
        ViewCompat.setBackgroundTintList(tvCurrentStepName, ColorStateList.valueOf(color));
    }

    private void updateStepControls() {
        if (btnNextStep == null || btnSkipStep == null) {
            return;
        }
        boolean isLastStep = currentStepIndex >= steps.size() - 1;
        if (isLastStep) {
            btnSkipStep.setVisibility(View.GONE);
            btnNextStep.setVisibility(View.GONE);
        } else {
            btnSkipStep.setVisibility(View.VISIBLE);
            btnNextStep.setVisibility(View.VISIBLE);
            btnNextStep.setText("Tiếp bước");
        }
    }

    private boolean isStepDone(int stepId) {
        SelectableItemAdapter adapter = stepAdapterMap.get(stepId);
        boolean hasSelection = adapter != null && !adapter.getSelectedItemIds().isEmpty();
        return hasSelection || skippedSteps.contains(stepId);
    }

    private int getStepColorRes(String stepName) {
        if (stepName == null) {
            return R.color.stepDefault;
        }
        String lower = stepName.toLowerCase(Locale.ROOT);
        if (lower.contains("vegetable") || lower.contains("rau")) {
            return R.color.stepVeggie;
        }
        if (lower.contains("protein") || lower.contains("thit") || lower.contains("meat")
                || lower.contains("seafood") || lower.contains("hai san") || lower.contains("hải sản")) {
            return R.color.stepProtein;
        }
        if (lower.contains("carb") || lower.contains("com") || lower.contains("cơm")
                || lower.contains("bun") || lower.contains("bún") || lower.contains("noodle")
                || lower.contains("banh") || lower.contains("bánh")) {
            return R.color.stepCarb;
        }
        if (lower.contains("sauce") || lower.contains("sot") || lower.contains("sốt")) {
            return R.color.stepSauce;
        }
        if (lower.contains("dairy") || lower.contains("milk") || lower.contains("sua")
                || lower.contains("sữa") || lower.contains("cheese")) {
            return R.color.stepDairy;
        }
        return R.color.stepDefault;
    }

    private ItemResponse getItemById(int stepId, int itemId) {
        List<ItemResponse> items = stepItemsMap.get(stepId);
        if (items == null) {
            return null;
        }
        for (ItemResponse item : items) {
            if (item.getItemId() == itemId) {
                return item;
            }
        }
        return null;
    }

    private double getCaloriesForItem(ItemResponse item, double quantity) {
        if (item.getCaloriesPerUnit() > 0) {
            return item.getCaloriesPerUnit() * quantity;
        }
        if (item.getBaseQuantity() > 0 && item.getCalories() > 0) {
            return (item.getCalories() / item.getBaseQuantity()) * quantity;
        }
        return item.getCalories() * quantity;
    }
}
