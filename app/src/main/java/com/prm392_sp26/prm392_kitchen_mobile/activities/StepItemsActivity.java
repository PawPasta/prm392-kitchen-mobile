package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.adapters.DishAdapter;
import com.prm392_sp26.prm392_kitchen_mobile.adapters.StepItemCardAdapter;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.DishResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.ItemResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.SearchResponse;
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
    private static final int DISH_PAGE_SIZE = 10;
    private static final int SEARCH_LIMIT = 10;
    private static final long SEARCH_DEBOUNCE_MS = 1000L;

    private PrefsManager prefsManager;
    private DishAdapter dishAdapter;
    private StepItemCardAdapter itemAdapter;
    private ConcatAdapter concatAdapter;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerItems;
    private ProgressBar progressItems;
    private TextView tvEmpty;
    private EditText etSearchKeyword;

    private View categoryCarb;
    private View categoryProtein;
    private View categoryVegetables;
    private View categorySauce;
    private View categoryExtra;

    private final Set<Integer> selectedStepIds = new LinkedHashSet<>();
    private final Map<Integer, Integer> nextPageByStepId = new LinkedHashMap<>();
    private final Map<Integer, Boolean> hasMoreByStepId = new LinkedHashMap<>();
    private final List<ItemResponse> loadedCategoryItems = new ArrayList<>();
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearchRunnable;
    private Call<BaseResponse<SearchResponse>> searchCall;
    private int loadSessionId = 0;
    private boolean isLoadingMoreCategoryItems = false;

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
        int initialStepId = getIntent().getIntExtra(EXTRA_STEP_ID, 0);
        if (isValidStepId(initialStepId)) {
            selectedStepIds.add(initialStepId);
        }

        recyclerItems = findViewById(R.id.recyclerStepItems);
        progressItems = findViewById(R.id.progressStepItems);
        tvEmpty = findViewById(R.id.tvEmptyStepItems);
        swipeRefresh = findViewById(R.id.swipeRefreshStepItems);
        etSearchKeyword = findViewById(R.id.etSearchKeyword);

        categoryCarb = findViewById(R.id.categoryCarb);
        categoryProtein = findViewById(R.id.categoryProtein);
        categoryVegetables = findViewById(R.id.categoryVegetables);
        categorySauce = findViewById(R.id.categorySauce);
        categoryExtra = findViewById(R.id.categoryExtra);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        dishAdapter = new DishAdapter(new ArrayList<>(), this::openDishDetail);
        itemAdapter = new StepItemCardAdapter(this::openItemDetail);
        concatAdapter = new ConcatAdapter(dishAdapter, itemAdapter);
        recyclerItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerItems.setAdapter(concatAdapter);
        setupPaginationScroll();

        setupCategoryNavigation();
        setupSearchInput();
        updateCategorySelection();

        swipeRefresh.setOnRefreshListener(this::reloadContentForCurrentState);
        reloadContentForCurrentState();
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
            reloadContentForCurrentState();
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

    private boolean isValidStepId(int stepId) {
        return stepId >= 1 && stepId <= 5;
    }

    private void setupSearchInput() {
        if (etSearchKeyword == null) {
            return;
        }
        etSearchKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (pendingSearchRunnable != null) {
                    searchHandler.removeCallbacks(pendingSearchRunnable);
                }
                final String keyword = getSearchKeyword();
                pendingSearchRunnable = () -> {
                    if (keyword.isEmpty()) {
                        loadItemsForSelectedSteps();
                    } else {
                        searchByKeyword(keyword);
                    }
                };
                searchHandler.postDelayed(pendingSearchRunnable, SEARCH_DEBOUNCE_MS);
            }
        });
    }

    private void setupPaginationScroll() {
        recyclerItems.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0 || !shouldLoadNextCategoryPages()) {
                    return;
                }

                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (!(layoutManager instanceof LinearLayoutManager)) {
                    return;
                }
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                int lastVisible = linearLayoutManager.findLastVisibleItemPosition();
                int totalItems = concatAdapter.getItemCount();
                if (totalItems == 0) {
                    return;
                }
                if (lastVisible >= totalItems - 2) {
                    loadNextCategoryPages();
                }
            }
        });
    }

    private void reloadContentForCurrentState() {
        String keyword = getSearchKeyword();
        if (keyword.isEmpty()) {
            if (selectedStepIds.isEmpty()) {
                clearCategoryPaginationState();
                loadAvailableDishes();
            } else {
                loadItemsForSelectedSteps();
            }
        } else {
            clearCategoryPaginationState();
            searchByKeyword(keyword);
        }
    }

    private String getSearchKeyword() {
        if (etSearchKeyword == null || etSearchKeyword.getText() == null) {
            return "";
        }
        return etSearchKeyword.getText().toString().trim();
    }

    private void searchByKeyword(String keyword) {
        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu token. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }

        cancelSearchCall();
        setLoading(true);
        final int sessionId = ++loadSessionId;

        searchCall = ApiClient.getInstance()
                .getApiService()
                .search("Bearer " + token, keyword, SEARCH_LIMIT);

        searchCall.enqueue(new Callback<BaseResponse<SearchResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<SearchResponse>> call,
                    @NonNull Response<BaseResponse<SearchResponse>> response) {
                if (sessionId != loadSessionId) {
                    return;
                }

                setLoading(false);
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    showSearchResults(new ArrayList<>(), new ArrayList<>(), keyword);
                    Toast.makeText(StepItemsActivity.this,
                            "Không tải được kết quả tìm kiếm",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                SearchResponse data = response.body().getData();
                List<DishResponse> dishes = data != null && data.getDishes() != null
                        ? data.getDishes()
                        : new ArrayList<>();
                List<ItemResponse> items = data != null && data.getItems() != null
                        ? data.getItems()
                        : new ArrayList<>();
                if (selectedStepIds.isEmpty()) {
                    showSearchResults(dishes, new ArrayList<>(), keyword);
                } else {
                    showSearchResults(new ArrayList<>(), filterItemsBySelectedSteps(items), keyword);
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<SearchResponse>> call,
                    @NonNull Throwable t) {
                if (sessionId != loadSessionId || call.isCanceled()) {
                    return;
                }
                setLoading(false);
                showSearchResults(new ArrayList<>(), new ArrayList<>(), keyword);
                Toast.makeText(StepItemsActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<ItemResponse> filterItemsBySelectedSteps(List<ItemResponse> rawItems) {
        if (rawItems == null) {
            return new ArrayList<>();
        }
        List<ItemResponse> filteredItems = new ArrayList<>();
        for (ItemResponse item : rawItems) {
            if (item != null && selectedStepIds.contains(item.getStepId())) {
                filteredItems.add(item);
            }
        }
        return filteredItems;
    }

    private void loadAvailableDishes() {
        cancelSearchCall();
        final int sessionId = ++loadSessionId;

        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu token. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);
        ApiClient.getInstance()
                .getApiService()
                .getDishes("Bearer " + token, 0, DISH_PAGE_SIZE)
                .enqueue(new Callback<BaseResponse<PageResponse<DishResponse>>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<BaseResponse<PageResponse<DishResponse>>> call,
                            @NonNull Response<BaseResponse<PageResponse<DishResponse>>> response) {
                        if (sessionId != loadSessionId) {
                            return;
                        }
                        setLoading(false);
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()
                                && response.body().getData() != null) {
                            List<DishResponse> dishes = response.body().getData().getContent();
                            showAvailableDishes(dishes == null ? new ArrayList<>() : dishes);
                            return;
                        }
                        showAvailableDishes(new ArrayList<>());
                        Toast.makeText(StepItemsActivity.this,
                                "Không tải được danh sách món ăn",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<BaseResponse<PageResponse<DishResponse>>> call,
                            @NonNull Throwable t) {
                        if (sessionId != loadSessionId) {
                            return;
                        }
                        setLoading(false);
                        showAvailableDishes(new ArrayList<>());
                        Toast.makeText(StepItemsActivity.this,
                                "Lỗi kết nối: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadItemsForSelectedSteps() {
        cancelSearchCall();
        final int sessionId = ++loadSessionId;

        if (selectedStepIds.isEmpty()) {
            setLoading(false);
            showCategoryItems(new ArrayList<>());
            tvEmpty.setText("Chọn ít nhất một danh mục");
            return;
        }

        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu token. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }

        initializeCategoryPaginationState();
        loadCategoryItemsPageBatch(sessionId, "Bearer " + token, false);
    }

    private void loadNextCategoryPages() {
        if (isLoadingMoreCategoryItems || !hasAnyRemainingCategoryPages()) {
            return;
        }

        String keyword = getSearchKeyword();
        if (!keyword.isEmpty()) {
            return;
        }

        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu token. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }

        loadCategoryItemsPageBatch(loadSessionId, "Bearer " + token, true);
    }

    private void initializeCategoryPaginationState() {
        nextPageByStepId.clear();
        hasMoreByStepId.clear();
        loadedCategoryItems.clear();
        isLoadingMoreCategoryItems = false;
        for (Integer stepId : selectedStepIds) {
            nextPageByStepId.put(stepId, 0);
            hasMoreByStepId.put(stepId, true);
        }
    }

    private void clearCategoryPaginationState() {
        nextPageByStepId.clear();
        hasMoreByStepId.clear();
        loadedCategoryItems.clear();
        isLoadingMoreCategoryItems = false;
    }

    private boolean shouldLoadNextCategoryPages() {
        if (isLoadingMoreCategoryItems) {
            return false;
        }
        if (recyclerItems.getVisibility() != View.VISIBLE) {
            return false;
        }
        if (!getSearchKeyword().isEmpty()) {
            return false;
        }
        if (selectedStepIds.isEmpty()) {
            return false;
        }
        return hasAnyRemainingCategoryPages();
    }

    private boolean hasAnyRemainingCategoryPages() {
        for (Integer stepId : selectedStepIds) {
            if (Boolean.TRUE.equals(hasMoreByStepId.get(stepId))) {
                return true;
            }
        }
        return false;
    }

    private void loadCategoryItemsPageBatch(int sessionId, String bearerToken, boolean appendMode) {
        if (sessionId != loadSessionId) {
            return;
        }

        List<Integer> stepsToLoad = new ArrayList<>();
        Map<Integer, Integer> pageByStep = new LinkedHashMap<>();
        for (Integer stepId : selectedStepIds) {
            if (!Boolean.TRUE.equals(hasMoreByStepId.get(stepId))) {
                continue;
            }
            Integer page = nextPageByStepId.get(stepId);
            if (page == null) {
                page = 0;
                nextPageByStepId.put(stepId, page);
            }
            stepsToLoad.add(stepId);
            pageByStep.put(stepId, page);
        }

        if (stepsToLoad.isEmpty()) {
            isLoadingMoreCategoryItems = false;
            if (!appendMode) {
                setLoading(false);
                showCategoryItems(new ArrayList<>(loadedCategoryItems));
            }
            return;
        }

        isLoadingMoreCategoryItems = true;
        if (!appendMode) {
            setLoading(true);
        }

        final List<ItemResponse> batchItems = new ArrayList<>();
        final int totalRequests = stepsToLoad.size();
        final int[] finishedRequests = {0};
        final int[] failedRequests = {0};

        for (Integer stepId : stepsToLoad) {
            int page = pageByStep.get(stepId);
            ApiClient.getInstance()
                    .getApiService()
                    .getItemsByStep(bearerToken, stepId, page, PAGE_SIZE)
                    .enqueue(new Callback<BaseResponse<PageResponse<ItemResponse>>>() {
                        @Override
                        public void onResponse(
                                @NonNull Call<BaseResponse<PageResponse<ItemResponse>>> call,
                                @NonNull Response<BaseResponse<PageResponse<ItemResponse>>> response) {
                            if (sessionId != loadSessionId) {
                                return;
                            }
                            if (response.isSuccessful() && response.body() != null
                                    && response.body().isSuccess()
                                    && response.body().getData() != null) {
                                PageResponse<ItemResponse> pageData = response.body().getData();
                                List<ItemResponse> content = pageData.getContent();
                                if (content != null && !content.isEmpty()) {
                                    batchItems.addAll(content);
                                }
                                hasMoreByStepId.put(stepId, !pageData.isLast());
                                nextPageByStepId.put(stepId, page + 1);
                            } else {
                                failedRequests[0]++;
                            }
                            onCategoryItemsBatchRequestFinished(
                                    sessionId,
                                    appendMode,
                                    finishedRequests,
                                    totalRequests,
                                    failedRequests[0],
                                    batchItems);
                        }

                        @Override
                        public void onFailure(
                                @NonNull Call<BaseResponse<PageResponse<ItemResponse>>> call,
                                @NonNull Throwable t) {
                            if (sessionId != loadSessionId) {
                                return;
                            }
                            failedRequests[0]++;
                            onCategoryItemsBatchRequestFinished(
                                    sessionId,
                                    appendMode,
                                    finishedRequests,
                                    totalRequests,
                                    failedRequests[0],
                                    batchItems);
                        }
                    });
        }
    }

    private void onCategoryItemsBatchRequestFinished(
            int sessionId,
            boolean appendMode,
            int[] finishedRequests,
            int totalRequests,
            int failedRequests,
            List<ItemResponse> batchItems) {
        if (sessionId != loadSessionId) {
            return;
        }
        finishedRequests[0]++;
        if (finishedRequests[0] < totalRequests) {
            return;
        }

        isLoadingMoreCategoryItems = false;
        if (!appendMode) {
            setLoading(false);
            loadedCategoryItems.clear();
        }

        if (batchItems != null && !batchItems.isEmpty()) {
            loadedCategoryItems.addAll(batchItems);
            List<ItemResponse> deduplicatedItems = deduplicateByItemId(loadedCategoryItems);
            loadedCategoryItems.clear();
            loadedCategoryItems.addAll(deduplicatedItems);
        }

        showCategoryItems(new ArrayList<>(loadedCategoryItems));
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

    private void showCategoryItems(List<ItemResponse> items) {
        dishAdapter.updateDishes(new ArrayList<>());
        itemAdapter.setItems(items);
        boolean isEmpty = items == null || items.isEmpty();
        recyclerItems.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (isEmpty && !selectedStepIds.isEmpty()) {
            tvEmpty.setText("Không có item cho danh mục đã chọn");
        }
    }

    private void showAvailableDishes(List<DishResponse> dishes) {
        dishAdapter.updateDishes(dishes == null ? new ArrayList<>() : dishes);
        itemAdapter.setItems(new ArrayList<>());
        boolean isEmpty = dishes == null || dishes.isEmpty();
        recyclerItems.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (isEmpty) {
            tvEmpty.setText("Không có món ăn có sẵn");
        }
    }

    private void showSearchResults(List<DishResponse> dishes, List<ItemResponse> items, String keyword) {
        dishAdapter.updateDishes(dishes == null ? new ArrayList<>() : dishes);
        itemAdapter.setItems(items == null ? new ArrayList<>() : items);

        boolean hasDishes = dishes != null && !dishes.isEmpty();
        boolean hasItems = items != null && !items.isEmpty();
        boolean isEmpty = !hasDishes && !hasItems;

        recyclerItems.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (isEmpty) {
            tvEmpty.setText("Không tìm thấy kết quả cho \"" + keyword + "\"");
        }
    }

    private void openDishDetail(DishResponse dish) {
        if (dish == null) {
            return;
        }
        Intent intent = new Intent(this, DishDetailActivity.class);
        intent.putExtra("dishId", dish.getDishId());
        startActivity(intent);
    }

    private void openItemDetail(ItemResponse item) {
        if (item == null || item.getItemId() <= 0) {
            return;
        }
        Intent intent = new Intent(this, ItemDetailActivity.class);
        intent.putExtra(ItemDetailActivity.EXTRA_ITEM_ID, item.getItemId());
        startActivity(intent);
    }

    private void cancelSearchCall() {
        if (searchCall != null) {
            searchCall.cancel();
            searchCall = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pendingSearchRunnable != null) {
            searchHandler.removeCallbacks(pendingSearchRunnable);
        }
        cancelSearchCall();
    }
}
