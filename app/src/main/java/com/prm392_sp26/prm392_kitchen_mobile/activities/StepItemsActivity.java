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
    private TextView tvTitle;
    private int stepId = 1;
    private String stepName = "Carb";

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
        stepId = getIntent().getIntExtra(EXTRA_STEP_ID, 1);
        String extraStepName = getIntent().getStringExtra(EXTRA_STEP_NAME);
        if (extraStepName != null && !extraStepName.trim().isEmpty()) {
            stepName = extraStepName.trim();
        }

        tvTitle = findViewById(R.id.tvStepTitle);
        TextView tvSubtitle = findViewById(R.id.tvStepSubtitle);
        recyclerItems = findViewById(R.id.recyclerStepItems);
        progressItems = findViewById(R.id.progressStepItems);
        tvEmpty = findViewById(R.id.tvEmptyStepItems);
        swipeRefresh = findViewById(R.id.swipeRefreshStepItems);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvTitle.setText(stepName);
        tvSubtitle.setText("Step " + stepId);

        adapter = new StepItemCardAdapter();
        recyclerItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerItems.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadItemsByStep);
        loadItemsByStep();
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
                                "Không tải được danh sách " + stepName,
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
            tvEmpty.setText("Không có item cho " + stepName);
        }
    }
}
