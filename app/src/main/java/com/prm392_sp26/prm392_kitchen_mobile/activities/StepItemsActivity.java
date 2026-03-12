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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private final Set<Integer> selectedStepIds = new LinkedHashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_step_items);

        final View root = findViewById(R.id.stepItemsRoot);
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
        int initialStepId = sanitizeStepId(getIntent().getIntExtra(EXTRA_STEP_ID, 1));
        selectedStepIds.add(initialStepId);

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

        swipeRefresh.setOnRefreshListener(this::loadItemsForSelectedSteps);
        loadItemsForSelectedSteps();
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
            if (selectedStepIds.contains(targetStepId)) {
                selectedStepIds.remove(targetStepId);
            } else {
                selectedStepIds.add(targetStepId);
            }
            updateCategorySelection();
            loadItemsForSelectedSteps();
        });
    }

    private void updateCategorySelection() {
        setCategorySelected(categoryCarb, selectedStepIds.contains(1));
        setCategorySelected(categoryProtein, selectedStepIds.contains(2));
        setCategorySelected(categoryVegetables, selectedStepIds.contains(3));
        setCategorySelected(categorySauce, selectedStepIds.contains(4));
        setCategorySelected(categoryExtra, selectedStepIds.contains(5));
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

    private void loadItemsForSelectedSteps() {
        if (selectedStepIds.isEmpty()) {
            setLoading(false);
            showItems(new ArrayList<>());
            tvEmpty.setText("Chọn ít nhất một danh mục");
            return;
        }

        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu token. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }

        List<Integer> steps = new ArrayList<>(selectedStepIds);
        setLoading(true);

        List<ItemResponse> mergedItems = new ArrayList<>();
        final int totalRequests = steps.size();
        final int[] finishedRequests = {0};
        final int[] failedRequests = {0};

        for (Integer stepId : steps) {
            ApiClient.getInstance()
                    .getApiService()
                    .getItemsByStep("Bearer " + token, stepId, 0, PAGE_SIZE)
                    .enqueue(new Callback<BaseResponse<PageResponse<ItemResponse>>>() {
                        @Override
                        public void onResponse(
                                @NonNull Call<BaseResponse<PageResponse<ItemResponse>>> call,
                                @NonNull Response<BaseResponse<PageResponse<ItemResponse>>> response) {
                            if (response.isSuccessful() && response.body() != null
                                    && response.body().isSuccess()
                                    && response.body().getData() != null
                                    && response.body().getData().getContent() != null) {
                                mergedItems.addAll(response.body().getData().getContent());
                            } else {
                                failedRequests[0]++;
                            }
                            onStepItemsRequestFinished(finishedRequests, totalRequests, failedRequests[0], mergedItems);
                        }

                        @Override
                        public void onFailure(
                                @NonNull Call<BaseResponse<PageResponse<ItemResponse>>> call,
                                @NonNull Throwable t) {
                            failedRequests[0]++;
                            onStepItemsRequestFinished(finishedRequests, totalRequests, failedRequests[0], mergedItems);
                        }
                    });
        }
    }

    private void onStepItemsRequestFinished(
            int[] finishedRequests,
            int totalRequests,
            int failedRequests,
            List<ItemResponse> mergedItems) {
        finishedRequests[0]++;
        if (finishedRequests[0] < totalRequests) {
            return;
        }

        setLoading(false);
        showItems(deduplicateByItemId(mergedItems));
        if (failedRequests > 0) {
            Toast.makeText(this,
                    "Một số danh mục tải thất bại",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private List<ItemResponse> deduplicateByItemId(List<ItemResponse> rawItems) {
        Map<Integer, ItemResponse> uniqueItems = new LinkedHashMap<>();
        if (rawItems == null) {
            return new ArrayList<>();
        }
        for (ItemResponse item : rawItems) {
            if (item == null) {
                continue;
            }
            uniqueItems.put(item.getItemId(), item);
        }
        return new ArrayList<>(uniqueItems.values());
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
        if (isEmpty && !selectedStepIds.isEmpty()) {
            tvEmpty.setText("Không có item cho danh mục đã chọn");
        }
    }
}
