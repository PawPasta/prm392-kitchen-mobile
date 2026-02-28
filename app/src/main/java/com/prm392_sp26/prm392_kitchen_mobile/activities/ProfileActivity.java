package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.app.AlertDialog;
import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.model.data.UserProfile;
import com.prm392_sp26.prm392_kitchen_mobile.model.data.UserWallet;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private PrefsManager prefsManager;

    private TextView tvProfileInitials;
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private ProgressBar progressProfile;
    private View itemWallet;
    private View walletBalanceContainer;
    private TextView tvWalletBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefsManager = PrefsManager.getInstance(this);

        tvProfileInitials = findViewById(R.id.tvProfileInitials);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        progressProfile = findViewById(R.id.progressProfile);
        itemWallet = findViewById(R.id.itemWallet);
        walletBalanceContainer = findViewById(R.id.walletBalanceContainer);
        tvWalletBalance = findViewById(R.id.tvWalletBalance);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnLogout).setOnClickListener(v -> performLogout());
        itemWallet.setOnClickListener(v -> toggleWalletBalance());

        loadProfile();
    }

    private void loadProfile() {
        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Missing access token. Please login again.", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);
        ApiClient.getInstance()
                .getApiService()
                .getCurrentUserProfile("Bearer " + token)
                .enqueue(new Callback<BaseResponse<UserProfile>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<UserProfile>> call,
                            @NonNull Response<BaseResponse<UserProfile>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            BaseResponse<UserProfile> baseResponse = response.body();
                            if (baseResponse.isSuccess() && baseResponse.getData() != null) {
                                bindProfile(baseResponse.getData());
                                return;
                            }
                            Toast.makeText(ProfileActivity.this,
                                    baseResponse.getMessage() != null ? baseResponse.getMessage()
                                            : "Failed to load profile.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        Toast.makeText(ProfileActivity.this,
                                "Server error: " + response.code(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<UserProfile>> call, @NonNull Throwable t) {
                        setLoading(false);
                        Toast.makeText(ProfileActivity.this,
                                "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void bindProfile(UserProfile profile) {
        String displayName = profile.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = "User";
        }

        String email = profile.getEmail() == null ? "" : profile.getEmail();

        tvProfileName.setText(displayName);
        tvProfileEmail.setText(email);
        tvProfileInitials.setText(getInitials(displayName));

    }

    private void setLoading(boolean loading) {
        progressProfile.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void toggleWalletBalance() {
        if (walletBalanceContainer.getVisibility() == View.VISIBLE) {
            walletBalanceContainer.setVisibility(View.GONE);
            return;
        }

        walletBalanceContainer.setVisibility(View.VISIBLE);
        tvWalletBalance.setText("Đang tải số dư...");
        loadWalletBalance();
    }

    private void loadWalletBalance() {
        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            tvWalletBalance.setText("Thiếu token. Vui lòng đăng nhập lại.");
            return;
        }

        ApiClient.getInstance()
                .getApiService()
                .getCurrentUserProfile("Bearer " + token)
                .enqueue(new Callback<BaseResponse<UserProfile>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<UserProfile>> call,
                            @NonNull Response<BaseResponse<UserProfile>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess() && response.body().getData() != null) {
                            UserWallet wallet = response.body().getData().getWallet();
                            double balance = wallet == null ? 0 : wallet.getBalance();
                            tvWalletBalance.setText(String.format(Locale.getDefault(),
                                    "Số dư: %.2f", balance));
                            return;
                        }

                        tvWalletBalance.setText("Không thể tải số dư.");
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<UserProfile>> call, @NonNull Throwable t) {
                        tvWalletBalance.setText("Lỗi mạng khi tải số dư.");
                    }
                });
    }

    private String getInitials(String name) {
        if (name == null) {
            return "U";
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return "U";
        }
        String[] parts = trimmed.split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase(Locale.US);
        }
        String first = parts[0].substring(0, 1).toUpperCase(Locale.US);
        String second = parts[1].substring(0, 1).toUpperCase(Locale.US);
        return first + second;
    }

    private void performLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    prefsManager.clearSession();
                    FirebaseAuth.getInstance().signOut();

                    Intent intent = new Intent(this, AuthActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
