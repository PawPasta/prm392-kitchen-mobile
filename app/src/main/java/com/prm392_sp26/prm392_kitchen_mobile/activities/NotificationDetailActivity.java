package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.NotificationItem;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationDetailActivity extends AppCompatActivity {

    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";

    private PrefsManager prefsManager;
    private ProgressBar progressBar;
    private ScrollView contentView;
    private TextView tvTitle;
    private TextView tvType;
    private TextView tvStatus;
    private TextView tvNotificationId;
    private TextView tvNotificationUserId;
    private TextView tvBody;
    private TextView tvReadAt;
    private TextView tvIsRead;
    private TextView tvCreatedAt;
    private LinearLayout dataContainer;
    private TextView tvDataEmpty;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM")
            .withLocale(Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.notificationDetailMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefsManager = PrefsManager.getInstance(this);
        progressBar = findViewById(R.id.progressNotificationDetail);
        contentView = findViewById(R.id.notificationDetailContent);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvType = findViewById(R.id.tvDetailType);
        tvStatus = findViewById(R.id.tvDetailStatus);
        tvNotificationId = findViewById(R.id.tvDetailNotificationId);
        tvNotificationUserId = findViewById(R.id.tvDetailNotificationUserId);
        tvBody = findViewById(R.id.tvDetailBody);
        tvReadAt = findViewById(R.id.tvDetailReadAt);
        tvIsRead = findViewById(R.id.tvDetailIsRead);
        tvCreatedAt = findViewById(R.id.tvDetailCreatedAt);
        dataContainer = findViewById(R.id.dataContainer);
        tvDataEmpty = findViewById(R.id.tvDetailDataEmpty);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        long notificationId = getIntent().getLongExtra(EXTRA_NOTIFICATION_ID, -1);
        if (notificationId <= 0) {
            Toast.makeText(this, "Không tìm thấy thông báo.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadNotificationDetail(notificationId);
    }

    private void loadNotificationDetail(long notificationId) {
        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu token. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);
        ApiClient.getInstance()
                .getApiService()
                .getNotificationDetail("Bearer " + token, notificationId)
                .enqueue(new Callback<BaseResponse<NotificationItem>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<NotificationItem>> call,
                                           @NonNull Response<BaseResponse<NotificationItem>> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            BaseResponse<NotificationItem> body = response.body();
                            if (body.isSuccess() && body.getData() != null) {
                                bindDetail(body.getData());
                                return;
                            }
                            Toast.makeText(NotificationDetailActivity.this,
                                    body.getMessage() != null ? body.getMessage() : "Không thể tải thông báo.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        Toast.makeText(NotificationDetailActivity.this,
                                "Server error: " + response.code(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<NotificationItem>> call,
                                          @NonNull Throwable t) {
                        setLoading(false);
                        Toast.makeText(NotificationDetailActivity.this,
                                "Lỗi mạng khi tải thông báo.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void bindDetail(NotificationItem item) {
        String title = item.getTitle();
        String type = item.getType();
        String status = item.getStatus();
        String body = item.getBody();
        String readAt = item.getReadAt();
        String createdAt = item.getCreatedAt();

        tvTitle.setText(title == null || title.trim().isEmpty() ? "Thông báo" : title);
        tvType.setText(type == null || type.trim().isEmpty() ? "Khác" : type.trim());
        tvStatus.setText(status == null || status.trim().isEmpty() ? "-" : status);
        tvNotificationId.setText("ID thông báo: " + item.getNotificationId());
        tvNotificationUserId.setText("ID user: " + item.getNotificationUserId());
        tvBody.setText(body == null || body.trim().isEmpty() ? "-" : body);
        tvReadAt.setText("Đã đọc: " + formatTime(readAt));
        tvIsRead.setText("Trạng thái đọc: " + (item.isRead() ? "Đã đọc" : "Chưa đọc"));
        tvCreatedAt.setText(formatTime(createdAt));

        Map<String, Object> data = item.getData();
        bindData(data);
    }

    private void bindData(Map<String, Object> data) {
        dataContainer.removeAllViews();
        if (data == null || data.isEmpty()) {
            tvDataEmpty.setVisibility(View.VISIBLE);
            return;
        }
        tvDataEmpty.setVisibility(View.GONE);

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(0, 0, 0, dpToPx(10));

            TextView keyView = new TextView(this);
            keyView.setText(entry.getKey());
            keyView.setTextColor(getResources().getColor(R.color.colorTextSecondary));
            keyView.setTextSize(12f);
            keyView.setTypeface(keyView.getTypeface(), android.graphics.Typeface.BOLD);

            TextView valueView = new TextView(this);
            valueView.setText(formatDataValue(entry.getValue()));
            valueView.setTextColor(getResources().getColor(R.color.colorTextPrimary));
            valueView.setTextSize(13f);

            row.addView(keyView);
            row.addView(valueView);
            dataContainer.addView(row);
        }
    }

    private String formatDataValue(Object value) {
        if (value == null) {
            return "-";
        }
        if (value instanceof Map || value instanceof java.util.List) {
            return new Gson().toJson(value);
        }
        return String.valueOf(value);
    }

    private String formatTime(String isoTime) {
        if (isoTime == null || isoTime.trim().isEmpty()) {
            return "-";
        }
        try {
            Instant instant = Instant.parse(isoTime.trim());
            return timeFormatter.format(instant.atZone(ZoneId.systemDefault()));
        } catch (Exception e) {
            return isoTime;
        }
    }

    private int dpToPx(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        contentView.setVisibility(loading ? View.GONE : View.VISIBLE);
    }
}
