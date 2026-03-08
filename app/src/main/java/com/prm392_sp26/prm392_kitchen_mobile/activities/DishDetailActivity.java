package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
import com.prm392_sp26.prm392_kitchen_mobile.model.response.DishDetailResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.DishStepResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.ItemResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.shared.PageResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.CurrencyFormatter;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DishDetailActivity extends AppCompatActivity {

    private static final String TAG = "DishDetailActivity";

    private ImageView ivDishImage;
    private TextView tvEmoji, tvName, tvDescription, tvPrice, tvCalories, tvStatus;
    private TextView btnBack;
    private MaterialButton btnOrder;
    private RecyclerView rvSteps;
    private StepAdapter stepAdapter;
    private PrefsManager prefsManager;
    private int dishId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_detail);

        prefsManager = PrefsManager.getInstance(this);
        dishId = getIntent().getIntExtra("dishId", -1);

        // Bind views
        tvEmoji = findViewById(R.id.tvDishEmoji);
        ivDishImage = findViewById(R.id.ivDishImage);
        tvName = findViewById(R.id.tvDishName);
        tvDescription = findViewById(R.id.tvDishDescription);
        tvPrice = findViewById(R.id.tvDishPrice);
        tvCalories = findViewById(R.id.tvDishCalories);
        tvStatus = findViewById(R.id.tvDishStatus);
        btnBack = findViewById(R.id.btnBack);
        btnOrder = findViewById(R.id.btnOrder);
        rvSteps = findViewById(R.id.rvSteps);

        rvSteps.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());
        btnOrder.setOnClickListener(v -> {
            if (dishId != -1) {
                Intent intent = new Intent(DishDetailActivity.this, CreateOrderActivity.class);
                intent.putExtra("dishId", dishId);
                startActivity(intent);
            } else {
                Toast.makeText(DishDetailActivity.this, "Không tìm thấy ID món ăn", Toast.LENGTH_SHORT).show();
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
        tvName.setText(dish.getName());
        tvDescription.setText(dish.getDescription() != null ? dish.getDescription() : "");
        String imageUrl = dish.getImageUrl();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            ivDishImage.setVisibility(View.GONE);
            tvEmoji.setVisibility(View.VISIBLE);
        } else {
            tvEmoji.setVisibility(View.GONE);
            ivDishImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                .load(imageUrl.trim())
                .centerCrop()
                .into(ivDishImage);
        }

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
        }
    }

    private void loadStepItems(int dishId, int stepId) {
        String token = "Bearer " + prefsManager.getAccessToken();

        ApiClient.getInstance().getApiService()
            .getItemsByDishStep(token, dishId, stepId, 0, 20)
            .enqueue(new Callback<BaseResponse<PageResponse<ItemResponse>>>() {
                @Override
                public void onResponse(@NonNull Call<BaseResponse<PageResponse<ItemResponse>>> call,
                                       @NonNull Response<BaseResponse<PageResponse<ItemResponse>>> response) {
                    if (response.isSuccessful() && response.body() != null
                            && response.body().isSuccess() && response.body().getData() != null) {
                        List<ItemResponse> items = response.body().getData().getContent();
                        if (items != null && stepAdapter != null) {
                            stepAdapter.setItemsForStep(stepId, items);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BaseResponse<PageResponse<ItemResponse>>> call,
                                      @NonNull Throwable t) {
                    Log.e(TAG, "Load items for step " + stepId + " failed", t);
                }
            });
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
