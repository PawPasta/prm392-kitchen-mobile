package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.bumptech.glide.Glide;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.adapters.StepAdapter;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.CreateOrderRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.RefreshTokenRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.DishDetailResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.DishStepResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.ItemResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.LoginResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderResponse;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.shared.PageResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.Constants;
import com.prm392_sp26.prm392_kitchen_mobile.util.CurrencyFormatter;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;
import com.prm392_sp26.prm392_kitchen_mobile.util.PlaceholderImageResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DishDetailActivity extends AppCompatActivity {

    private static final String TAG = "DishDetailActivity";

    private ImageView ivDishImage;
    private FrameLayout dishHeroContainer;
    private TextView tvEmoji, tvName, tvDescription, tvPrice, tvCalories, tvStatus;
    private TextView tvQuantity;
    private EditText etOrderNote;
    private TextView btnBack;
    private MaterialButton btnOrder;
    private MaterialButton btnQuantityMinus;
    private MaterialButton btnQuantityPlus;
    private RecyclerView rvSteps;
    private LinearLayout layoutNutrientsContainer;
    private TextView tvNutrientsEmpty;
    private StepAdapter stepAdapter;
    private PrefsManager prefsManager;
    private DishDetailResponse currentDish;
    private int quantity = 1;
    private int dishId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_detail);

        prefsManager = PrefsManager.getInstance(this);
        dishId = getIntent().getIntExtra("dishId", -1);

        // Bind views
        dishHeroContainer = findViewById(R.id.dishHeroContainer);
        tvEmoji = findViewById(R.id.tvDishEmoji);
        ivDishImage = findViewById(R.id.ivDishImage);
        tvName = findViewById(R.id.tvDishName);
        tvDescription = findViewById(R.id.tvDishDescription);
        tvPrice = findViewById(R.id.tvDishPrice);
        tvCalories = findViewById(R.id.tvDishCalories);
        tvStatus = findViewById(R.id.tvDishStatus);
        tvQuantity = findViewById(R.id.tvQuantity);
        etOrderNote = findViewById(R.id.etOrderNote);
        btnBack = findViewById(R.id.btnBack);
        btnOrder = findViewById(R.id.btnOrder);
        btnQuantityMinus = findViewById(R.id.btnQuantityMinus);
        btnQuantityPlus = findViewById(R.id.btnQuantityPlus);
        rvSteps = findViewById(R.id.rvSteps);
        layoutNutrientsContainer = findViewById(R.id.layoutDishNutrientsContainer);
        tvNutrientsEmpty = findViewById(R.id.tvDishNutrientsEmpty);

        rvSteps.setLayoutManager(new LinearLayoutManager(this));
        adjustHeroImageRatio();
        updateQuantityUi();

        btnBack.setOnClickListener(v -> finish());
        btnOrder.setOnClickListener(v -> addToCart());
        btnQuantityMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantityUi();
            }
        });
        btnQuantityPlus.setOnClickListener(v -> {
            if (quantity < 10) {
                quantity++;
                updateQuantityUi();
            }
        });

        if (dishId != -1) {
            loadDishDetail();
        } else {
            Toast.makeText(this, "Không tìm thấy món", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadDishDetail() {
        String token = "Bearer " + prefsManager.getAccessToken();

        ApiClient.getInstance().getApiService()
            .getDishDetail(token, dishId)
            .enqueue(new Callback<BaseResponse<DishDetailResponse>>() {
                @Override
                public void onResponse(@NonNull Call<BaseResponse<DishDetailResponse>> call,
                                       @NonNull Response<BaseResponse<DishDetailResponse>> response) {
                    if (response.isSuccessful() && response.body() != null
                            && response.body().isSuccess() && response.body().getData() != null) {
                        displayDish(response.body().getData());
                    } else {
                        Toast.makeText(DishDetailActivity.this,
                            "Không thể tải chi tiết món", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Load dish failed: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BaseResponse<DishDetailResponse>> call,
                                      @NonNull Throwable t) {
                    Toast.makeText(DishDetailActivity.this,
                        "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Load dish error", t);
                }
            });
    }

    private void displayDish(DishDetailResponse dish) {
        currentDish = dish;
        tvName.setText(dish.getName());
        tvDescription.setText(dish.getDescription() != null ? dish.getDescription() : "");
        String imageUrl = PlaceholderImageResolver.resolveDishImageUrl(dish.getImageUrl());
        tvEmoji.setVisibility(View.GONE);
        ivDishImage.setVisibility(View.VISIBLE);
        Glide.with(this)
            .load(imageUrl)
            .centerCrop()
            .into(ivDishImage);

        tvPrice.setText(CurrencyFormatter.formatVnd(dish.getPrice()));
        tvCalories.setText("🔥 " + (int) dish.getCalories() + " kcal");

        // Status badge
        switch (dish.getStatus() != null ? dish.getStatus() : "") {
            case "RECOMMENDED": tvStatus.setText("⭐ Đề xuất"); break;
            case "SEASONAL": tvStatus.setText("🌸 Theo mùa"); break;
            case "SPECIAL": tvStatus.setText("🔥 Đặc biệt"); break;
            default: tvStatus.setText(dish.getStatus()); break;
        }

        // Emoji dựa theo tên
        tvEmoji.setText(getDishEmoji(dish.getName()));

        // Setup steps
        List<DishStepResponse> steps = dish.getSteps();
        if (steps != null && !steps.isEmpty()) {
            stepAdapter = new StepAdapter(steps);
            rvSteps.setAdapter(stepAdapter);

            // Load items cho từng step
            for (DishStepResponse step : steps) {
                loadStepItems(dish.getDishId(), step.getStepId());
            }
        } else {
            rvSteps.setAdapter(null);
        }

        renderNutrients(dish.getTotalNutrients());
    }

    private void adjustHeroImageRatio() {
        if (dishHeroContainer == null) {
            return;
        }
        dishHeroContainer.post(() -> {
            int width = dishHeroContainer.getWidth();
            if (width <= 0) {
                return;
            }
            ViewGroup.LayoutParams params = dishHeroContainer.getLayoutParams();
            int expectedHeight = Math.round(width * 2f / 3f);
            if (params.height == expectedHeight) {
                return;
            }
            params.height = expectedHeight;
            dishHeroContainer.setLayoutParams(params);
        });
    }

    private void updateQuantityUi() {
        if (tvQuantity != null) {
            tvQuantity.setText(String.valueOf(quantity));
        }
    }

    private void addToCart() {
        if (currentDish == null || dishId <= 0) {
            Toast.makeText(this, "Không tìm thấy món ăn", Toast.LENGTH_SHORT).show();
            return;
        }
        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        String note = etOrderNote != null ? etOrderNote.getText().toString().trim() : "";
        List<CreateOrderRequest.DishInput> dishes = new ArrayList<>();
        dishes.add(CreateOrderRequest.DishInput.fromDish(dishId, quantity));
        CreateOrderRequest request = new CreateOrderRequest(null, note, dishes);

        btnOrder.setEnabled(false);
        ApiClient.getInstance().getApiService()
                .createOrder(Constants.ORDER_CREATE_URL, "Bearer " + token, request)
                .enqueue(new Callback<BaseResponse<OrderResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<OrderResponse>> call,
                                           @NonNull Response<BaseResponse<OrderResponse>> response) {
                        btnOrder.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(DishDetailActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                            finish();
                        } else if (response.code() == 401) {
                            refreshTokenAndRetry();
                        } else {
                            String msg = "Không thể thêm vào giỏ hàng";
                            if (response.body() != null && response.body().getMessage() != null) {
                                msg = response.body().getMessage();
                            }
                            Toast.makeText(DishDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<OrderResponse>> call,
                                          @NonNull Throwable t) {
                        btnOrder.setEnabled(true);
                        Toast.makeText(DishDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void refreshTokenAndRetry() {
        String refreshToken = prefsManager.getRefreshToken();
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }
        ApiClient.getInstance().getApiService()
                .refreshToken(new RefreshTokenRequest(refreshToken))
                .enqueue(new Callback<BaseResponse<LoginResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<LoginResponse>> call,
                                           @NonNull Response<BaseResponse<LoginResponse>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess() && response.body().getData() != null) {
                            prefsManager.saveLoginResponse(response.body().getData());
                            addToCart();
                            return;
                        }
                        Toast.makeText(DishDetailActivity.this,
                                "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<LoginResponse>> call,
                                          @NonNull Throwable t) {
                        Toast.makeText(DishDetailActivity.this,
                                "Lỗi xác thực: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadStepItems(int dishId, int stepId) {
        String token = "Bearer " + prefsManager.getAccessToken();
        loadStepItemsPage(token, dishId, stepId, 0, new ArrayList<>());
    }

    private void loadStepItemsPage(
            String token,
            int dishId,
            int stepId,
            int page,
            @NonNull List<ItemResponse> accumulator) {

        ApiClient.getInstance().getApiService()
            .getItemsByDishStep(token, dishId, stepId, page, 20)
            .enqueue(new Callback<BaseResponse<PageResponse<ItemResponse>>>() {
                @Override
                public void onResponse(@NonNull Call<BaseResponse<PageResponse<ItemResponse>>> call,
                                       @NonNull Response<BaseResponse<PageResponse<ItemResponse>>> response) {
                    if (response.isSuccessful() && response.body() != null
                            && response.body().isSuccess() && response.body().getData() != null) {
                        PageResponse<ItemResponse> pageData = response.body().getData();
                        List<ItemResponse> items = pageData.getContent();
                        if (items != null && !items.isEmpty()) {
                            accumulator.addAll(items);
                        }
                        if (pageData.isLast()) {
                            if (stepAdapter != null) {
                                stepAdapter.setItemsForStep(stepId, new ArrayList<>(accumulator));
                            }
                        } else {
                            loadStepItemsPage(token, dishId, stepId, page + 1, accumulator);
                        }
                    } else if (stepAdapter != null && !accumulator.isEmpty()) {
                        stepAdapter.setItemsForStep(stepId, new ArrayList<>(accumulator));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BaseResponse<PageResponse<ItemResponse>>> call,
                                      @NonNull Throwable t) {
                    if (stepAdapter != null && !accumulator.isEmpty()) {
                        stepAdapter.setItemsForStep(stepId, new ArrayList<>(accumulator));
                    }
                    Log.e(TAG, "Load items for step " + stepId + " failed", t);
                }
            });
    }

    private void renderNutrients(List<DishDetailResponse.DishNutrient> nutrients) {
        if (layoutNutrientsContainer == null || tvNutrientsEmpty == null) {
            return;
        }
        layoutNutrientsContainer.removeAllViews();

        if (nutrients == null || nutrients.isEmpty()) {
            tvNutrientsEmpty.setVisibility(View.VISIBLE);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (DishDetailResponse.DishNutrient nutrient : nutrients) {
            if (nutrient == null) {
                continue;
            }
            View rowView = inflater.inflate(R.layout.item_nutrient, layoutNutrientsContainer, false);
            TextView tvNutrientName = rowView.findViewById(R.id.tvNutrientName);
            TextView tvNutrientDescription = rowView.findViewById(R.id.tvNutrientDescription);
            TextView tvNutrientAmount = rowView.findViewById(R.id.tvNutrientAmount);
            TextView tvNutrientBase = rowView.findViewById(R.id.tvNutrientBase);

            tvNutrientName.setText(nonEmpty(nutrient.getNutrientName(), "Nutrient"));
            tvNutrientDescription.setText(nonEmpty(nutrient.getNutrientDescription(), "--"));
            tvNutrientAmount.setText(formatNumber(nutrient.getTotalAmount()) + " "
                    + nonEmpty(nutrient.getNutrientUnit(), ""));
            tvNutrientBase.setText("Tổng trong 1 phần");

            layoutNutrientsContainer.addView(rowView);
        }

        tvNutrientsEmpty.setVisibility(
                layoutNutrientsContainer.getChildCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private String nonEmpty(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private String formatNumber(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001d) {
            return String.valueOf((long) Math.rint(value));
        }
        return String.format(Locale.getDefault(), "%.2f", value);
    }

    private String getDishEmoji(String name) {
        if (name == null) return "🍽️";
        String lower = name.toLowerCase();
        if (lower.contains("cơm") || lower.contains("com")) return "🍛";
        if (lower.contains("phở") || lower.contains("pho")) return "🍜";
        if (lower.contains("bánh mì") || lower.contains("banh mi")) return "🥖";
        if (lower.contains("trà") || lower.contains("tra")) return "🧋";
        if (lower.contains("nước") || lower.contains("nuoc")) return "🥤";
        if (lower.contains("gà") || lower.contains("ga")) return "🍗";
        return "🍽️";
    }
}
