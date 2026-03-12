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

import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.adapters.StepItemCardAdapter;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.ItemResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.shared.PageResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StepItemsActivity extends AppCompatActivity {

    public static final String EXTRA_STEP_ID = "extra_step_id";
    public static final String EXTRA_STEP_NAME = "extra_step_name";
    private static final int PAGE_SIZE = 10;

    private PrefsManager prefsManager;
    private StepItemCardAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerItems;
    private ProgressBar progressItems;
    private TextView tvEmpty;

    private View categoryCarb;
    private View categoryProtein;
    private View categoryVegetables;
    private View categorySauce;
    private View categoryExtra;

    private int stepId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_step_items);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.stepItemsRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefsManager = PrefsManager.getInstance(this);
        stepId = sanitizeStepId(getIntent().getIntExtra(EXTRA_STEP_ID, 1));

        recyclerItems = findViewById(R.id.recyclerStepItems);
        progressItems = findViewById(R.id.progressStepItems);
        tvEmpty = findViewById(R.id.tvEmptyStepItems);
        swipeRefresh = findViewById(R.id.swipeRefreshStepItems);

        categoryCarb = findViewById(R.id.categoryCarb);
        categoryProtein = findViewById(R.id.categoryProtein);
        categoryVegetables = findViewById(R.id.categoryVegetables);
        categorySauce = findViewById(R.id.categorySauce);
        categoryExtra = findViewById(R.id.categoryExtra);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        adapter = new StepItemCardAdapter();
        recyclerItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerItems.setAdapter(adapter);

        setupCategoryNavigation();
        updateCategorySelection();

        swipeRefresh.setOnRefreshListener(this::loadItemsByStep);
        loadItemsByStep();
    }

    private void setupCategoryNavigation() {
        bindCategoryClick(categoryCarb, 1);
        bindCategoryClick(categoryProtein, 2);
        bindCategoryClick(categoryVegetables, 3);
        bindCategoryClick(categorySauce, 4);
        bindCategoryClick(categoryExtra, 5);
    }

    private void bindCategoryClick(View categoryView, int targetStepId) {
        if (categoryView == null) {
            return;
        }
        categoryView.setOnClickListener(v -> {
            if (stepId == targetStepId) {
                return;
            }
            stepId = targetStepId;
            updateCategorySelection();
            loadItemsByStep();
        });
    }

    private void updateCategorySelection() {
        setCategorySelected(categoryCarb, stepId == 1);
        setCategorySelected(categoryProtein, stepId == 2);
        setCategorySelected(categoryVegetables, stepId == 3);
        setCategorySelected(categorySauce, stepId == 4);
        setCategorySelected(categoryExtra, stepId == 5);
    }

    private void setCategorySelected(View categoryView, boolean selected) {
        if (categoryView == null) {
            return;
        }
        categoryView.setBackgroundResource(
                selected ? R.drawable.bg_category_tile_selected : R.drawable.bg_category_tile);
    }

    private int sanitizeStepId(int rawStepId) {
        if (rawStepId < 1 || rawStepId > 5) {
            return 1;
        }
        return rawStepId;
    }

    private void loadItemsByStep() {
        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu token. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);
        ApiClient.getInstance()
                .getApiService()
                .getItemsByStep("Bearer " + token, stepId, 0, PAGE_SIZE)
                .enqueue(new Callback<BaseResponse<PageResponse<ItemResponse>>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<BaseResponse<PageResponse<ItemResponse>>> call,
                            @NonNull Response<BaseResponse<PageResponse<ItemResponse>>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()
                                && response.body().getData() != null) {
                            List<ItemResponse> data = response.body().getData().getContent();
                            showItems(data);
                            return;
                        }

                        showItems(new ArrayList<>());
                        Toast.makeText(StepItemsActivity.this,
                                "Không tải được danh sách item",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<BaseResponse<PageResponse<ItemResponse>>> call,
                            @NonNull Throwable t) {
                        setLoading(false);
                        showItems(new ArrayList<>());
                        Toast.makeText(StepItemsActivity.this,
                                "Lỗi kết nối: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        if (!swipeRefresh.isRefreshing()) {
            progressItems.setVisibility(loading ? View.VISIBLE : View.GONE);
            if (loading) {
                recyclerItems.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.GONE);
            }
        }
        swipeRefresh.setRefreshing(false);
    }

    private void showItems(List<ItemResponse> items) {
        adapter.setItems(items);
        boolean isEmpty = items == null || items.isEmpty();
        recyclerItems.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (isEmpty) {
            tvEmpty.setText("Không có item cho danh mục này");
        }
    }
}
