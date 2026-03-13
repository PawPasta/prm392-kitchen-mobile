package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.adapters.ItemNutrientAdapter;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.ItemDetailResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.CurrencyFormatter;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "extra_item_id";

    private PrefsManager prefsManager;
    private ProgressBar progressBar;
    private ImageView ivImage;
    private TextView tvName;
    private TextView tvDescription;
    private TextView tvPrice;
    private TextView tvMeta;
    private TextView tvQuantity;
    private TextView tvCalories;
    private TextView tvNote;
    private TextView tvCreated;
    private TextView tvUpdated;
    private TextView tvNutrientsEmpty;
    private MaterialButton btnCreateCustom;
    private ItemNutrientAdapter nutrientAdapter;
    private int itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_detail);

        View root = findViewById(R.id.itemDetailRoot);
        final int basePaddingLeft = root.getPaddingLeft();
        final int basePaddingTop = root.getPaddingTop();
        final int basePaddingRight = root.getPaddingRight();
        final int basePaddingBottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    basePaddingLeft + systemBars.left,
                    basePaddingTop + systemBars.top,
                    basePaddingRight + systemBars.right,
                    basePaddingBottom + systemBars.bottom);
            return insets;
        });

        prefsManager = PrefsManager.getInstance(this);
        itemId = getIntent().getIntExtra(EXTRA_ITEM_ID, 0);
        if (itemId <= 0) {
            Toast.makeText(this, "Thiếu itemId", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        progressBar = findViewById(R.id.progressItemDetail);
        ivImage = findViewById(R.id.ivItemDetailImage);
        tvName = findViewById(R.id.tvItemDetailName);
        tvDescription = findViewById(R.id.tvItemDetailDescription);
        tvPrice = findViewById(R.id.tvItemDetailPrice);
        tvMeta = findViewById(R.id.tvItemDetailMeta);
        tvQuantity = findViewById(R.id.tvItemDetailQuantity);
        tvCalories = findViewById(R.id.tvItemDetailCalories);
        tvNote = findViewById(R.id.tvItemDetailNote);
        tvCreated = findViewById(R.id.tvItemDetailCreated);
        tvUpdated = findViewById(R.id.tvItemDetailUpdated);
        tvNutrientsEmpty = findViewById(R.id.tvItemNutrientsEmpty);
        btnCreateCustom = findViewById(R.id.btnCreateCustomFromItem);

        RecyclerView rvNutrients = findViewById(R.id.rvItemNutrients);
        nutrientAdapter = new ItemNutrientAdapter();
        rvNutrients.setLayoutManager(new LinearLayoutManager(this));
        rvNutrients.setAdapter(nutrientAdapter);

        findViewById(R.id.btnBackItemDetail).setOnClickListener(v -> finish());
        btnCreateCustom.setOnClickListener(v ->
                startActivity(new Intent(ItemDetailActivity.this, CreateCustomOrderActivity.class)));

        loadItemDetail();
    }

    private void loadItemDetail() {
        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu token. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setLoading(true);
        ApiClient.getInstance()
                .getApiService()
                .getItemDetailWithNutrients("Bearer " + token, itemId)
                .enqueue(new Callback<BaseResponse<ItemDetailResponse>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<BaseResponse<ItemDetailResponse>> call,
                            @NonNull Response<BaseResponse<ItemDetailResponse>> response) {
                        setLoading(false);
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()
                                && response.body().getData() != null) {
                            bindItem(response.body().getData());
                            return;
                        }
                        Toast.makeText(ItemDetailActivity.this,
                                "Không tải được chi tiết thành phần",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<BaseResponse<ItemDetailResponse>> call,
                            @NonNull Throwable t) {
                        setLoading(false);
                        Toast.makeText(ItemDetailActivity.this,
                                "Lỗi kết nối: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void bindItem(ItemDetailResponse item) {
        tvName.setText(nonEmpty(item.getName(), "Item"));
        tvDescription.setText(nonEmpty(item.getDescription(), "--"));
        tvPrice.setText(CurrencyFormatter.formatVnd(item.getPrice()));
        tvMeta.setText(nonEmpty(item.getStepName(), "--") + " • " + nonEmpty(item.getStatus(), "--"));
        tvQuantity.setText("Base quantity: " + formatNumber(item.getBaseQuantity()) + " " + nonEmpty(item.getUnit(), ""));
        tvCalories.setText("Calories: " + formatNumber(item.getCalories()) + " kcal • "
                + formatNumber(item.getCaloriesPerUnit()) + " / unit");
        tvNote.setText("Note: " + nonEmpty(item.getNote(), "--"));
        tvCreated.setText("Created: " + nonEmpty(item.getCreatedAt(), "--"));
        tvUpdated.setText("Updated: " + nonEmpty(item.getUpdatedAt(), "--"));

        if (item.getImageUrl() == null || item.getImageUrl().trim().isEmpty()) {
            ivImage.setImageResource(R.drawable.ic_dish);
        } else {
            Glide.with(this)
                    .load(item.getImageUrl().trim())
                    .placeholder(R.drawable.ic_dish)
                    .error(R.drawable.ic_dish)
                    .centerCrop()
                    .into(ivImage);
        }

        if (item.getNutrients() == null || item.getNutrients().isEmpty()) {
            nutrientAdapter.setItems(new ArrayList<>());
            tvNutrientsEmpty.setVisibility(View.VISIBLE);
        } else {
            nutrientAdapter.setItems(item.getNutrients());
            tvNutrientsEmpty.setVisibility(View.GONE);
        }
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private String nonEmpty(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private String formatNumber(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001d) {
            return String.format(Locale.getDefault(), "%d", (long) Math.rint(value));
        }
        return String.format(Locale.getDefault(), "%.2f", value);
    }
}
