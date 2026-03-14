package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.adapters.NotificationAdapter;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.NotificationItem;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.NotificationListResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity {

    private PrefsManager prefsManager;
    private NotificationAdapter adapter;
    private final List<NotificationItem> notifications = new ArrayList<>();
    private final List<NotificationItem> allNotifications = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ProgressBar progressBar;
    private LinearLayout filterContainer;
    private String selectedFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.notificationsMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefsManager = PrefsManager.getInstance(this);
        recyclerView = findViewById(R.id.rvNotifications);
        tvEmpty = findViewById(R.id.tvEmptyNotifications);
        progressBar = findViewById(R.id.progressNotifications);
        filterContainer = findViewById(R.id.notificationFilterContainer);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        adapter = new NotificationAdapter(notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this::openNotificationDetail);

        setupFilters();
        loadNotifications();
    }

    private void loadNotifications() {
        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu token. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        setLoading(true);
        ApiClient.getInstance()
                .getApiService()
                .getNotificationsMe("Bearer " + token)
                .enqueue(new Callback<BaseResponse<NotificationListResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<NotificationListResponse>> call,
                                           @NonNull Response<BaseResponse<NotificationListResponse>> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            BaseResponse<NotificationListResponse> body = response.body();
                            if (body.isSuccess() && body.getData() != null) {
                                List<NotificationItem> items = body.getData().getNotifications();
                                allNotifications.clear();
                                if (items != null) {
                                    allNotifications.addAll(items);
                                }
                                applyFilter();
                                return;
                            }
                            Toast.makeText(NotificationActivity.this,
                                    body.getMessage() != null ? body.getMessage() : "Không thể tải thông báo.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        Toast.makeText(NotificationActivity.this,
                                "Server error: " + response.code(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<NotificationListResponse>> call,
                                          @NonNull Throwable t) {
                        setLoading(false);
                        Toast.makeText(NotificationActivity.this,
                                "Lỗi mạng khi tải thông báo.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    private void openNotificationDetail(NotificationItem item) {
        if (item == null) {
            return;
        }
        android.content.Intent intent = new android.content.Intent(this, NotificationDetailActivity.class);
        intent.putExtra(NotificationDetailActivity.EXTRA_NOTIFICATION_ID, item.getNotificationId());
        startActivity(intent);
    }

    private void setupFilters() {
        String[] filters = new String[] { "ALL", "UNREAD", "READ" };
        for (String filter : filters) {
            TextView chip = new TextView(this);
            chip.setText("ALL".equals(filter) ? "Tất cả" : ("UNREAD".equals(filter) ? "Chưa đọc" : "Đã đọc"));
            chip.setTag(filter);
            chip.setTextSize(12f);
            chip.setTextColor(getResources().getColor(R.color.colorTextPrimary));
            int paddingH = (int) (12 * getResources().getDisplayMetrics().density);
            int paddingV = (int) (6 * getResources().getDisplayMetrics().density);
            chip.setPadding(paddingH, paddingV, paddingH, paddingV);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd((int) (8 * getResources().getDisplayMetrics().density));
            chip.setLayoutParams(params);

            chip.setBackgroundResource(R.drawable.bg_chip);
            chip.setOnClickListener(v -> {
                selectedFilter = (String) v.getTag();
                updateFilterStyles();
                applyFilter();
            });

            filterContainer.addView(chip);
        }
        updateFilterStyles();
    }

    private void updateFilterStyles() {
        for (int i = 0; i < filterContainer.getChildCount(); i++) {
            View view = filterContainer.getChildAt(i);
            if (!(view instanceof TextView)) {
                continue;
            }
            TextView chip = (TextView) view;
            String tag = (String) chip.getTag();
            boolean selected = selectedFilter.equals(tag);
            chip.setBackgroundResource(selected ? R.drawable.bg_chip_selected : R.drawable.bg_chip);
        }
    }

    private void applyFilter() {
        notifications.clear();
        for (NotificationItem item : allNotifications) {
            if (item == null) {
                continue;
            }
            if ("UNREAD".equals(selectedFilter) && item.isRead()) {
                continue;
            }
            if ("READ".equals(selectedFilter) && !item.isRead()) {
                continue;
            }
            notifications.add(item);
        }
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
