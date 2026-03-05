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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.model.data.UserProfile;
import com.prm392_sp26.prm392_kitchen_mobile.model.data.UserWallet;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.UpdateProfileRequest;
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
    private ImageView ivProfileAvatar;
    private TextView btnEditProfile;
    private TextView etProfileName;
    private ProgressBar progressProfile;
    private View avatarContainer;
    private View itemCart;
    private View itemOrderHistory;
    private View itemWallet;
    private View walletBalanceContainer;
    private TextView tvWalletBalance;
    private UserProfile currentProfile;
    private boolean isEditingProfile;

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
        ivProfileAvatar = findViewById(R.id.ivProfileAvatar);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        etProfileName = findViewById(R.id.etProfileName);
        progressProfile = findViewById(R.id.progressProfile);
        avatarContainer = findViewById(R.id.avatarContainer);
        itemCart = findViewById(R.id.itemCart);
        itemOrderHistory = findViewById(R.id.itemOrderHistory);
        itemWallet = findViewById(R.id.itemWallet);
        walletBalanceContainer = findViewById(R.id.walletBalanceContainer);
        tvWalletBalance = findViewById(R.id.tvWalletBalance);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnLogout).setOnClickListener(v -> performLogout());
        itemCart.setOnClickListener(v -> navigateToCart());
        itemOrderHistory.setOnClickListener(v -> navigateToOrderHistory());
        itemWallet.setOnClickListener(v -> toggleWalletBalance());
        btnEditProfile.setOnClickListener(v -> toggleEditProfile());

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
        currentProfile = profile;
        String displayName = profile.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = "User";
        }

        String email = profile.getEmail() == null ? "" : profile.getEmail();
        String imageUrl = profile.getImageUrl();

        tvProfileName.setText(displayName);
        tvProfileEmail.setText(email);
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            ivProfileAvatar.setImageDrawable(null);
            ivProfileAvatar.setVisibility(View.GONE);
            tvProfileInitials.setVisibility(View.VISIBLE);
            tvProfileInitials.setText(getInitials(displayName));
        } else {
            tvProfileInitials.setVisibility(View.GONE);
            ivProfileAvatar.setVisibility(View.VISIBLE);
            Glide.with(this)
                .load(imageUrl.trim())
                .centerCrop()
                .into(ivProfileAvatar);
        }
        if (!isEditingProfile) {
            etProfileName.setText(displayName);
            tvProfileName.setVisibility(View.VISIBLE);
        }

    }

    private void toggleEditProfile() {
        if (!isEditingProfile) {
            isEditingProfile = true;
            btnEditProfile.setText("Lưu");
            etProfileName.setVisibility(View.VISIBLE);
            tvProfileName.setVisibility(View.GONE);
            String displayName = currentProfile != null ? currentProfile.getDisplayName() : "";
            etProfileName.setText(displayName == null ? "" : displayName);
            avatarContainer.setBackgroundResource(R.drawable.bg_avatar_ring_orange);
            return;
        }

        String newDisplayName = etProfileName.getText().toString().trim();
        String imageUrl = currentProfile != null ? currentProfile.getImageUrl() : null;
        updateProfile(newDisplayName, imageUrl);
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
                            String amount = java.text.NumberFormat.getCurrencyInstance(Locale.US)
                                    .format(balance);
                            tvWalletBalance.setText("Số dư: " + amount);
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

    private void updateProfile(String displayName, String imageUrl) {
        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu token. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }

        String safeDisplayName = displayName == null ? "" : displayName.trim();
        String safeImageUrl = imageUrl == null || imageUrl.trim().isEmpty() ? null : imageUrl.trim();

        setLoading(true);
        ApiClient.getInstance()
                .getApiService()
                .updateCurrentUserProfile("Bearer " + token,
                        new UpdateProfileRequest(safeDisplayName, safeImageUrl))
                .enqueue(new Callback<BaseResponse<UserProfile>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<UserProfile>> call,
                            @NonNull Response<BaseResponse<UserProfile>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            BaseResponse<UserProfile> baseResponse = response.body();
                            if (baseResponse.isSuccess() && baseResponse.getData() != null) {
                                bindProfile(baseResponse.getData());
                                isEditingProfile = false;
                                btnEditProfile.setText("Edit");
                                etProfileName.setVisibility(View.GONE);
                                tvProfileName.setVisibility(View.VISIBLE);
                                avatarContainer.setBackground(null);
                                Toast.makeText(ProfileActivity.this,
                                        "Cập nhật hồ sơ thành công.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Toast.makeText(ProfileActivity.this,
                                    baseResponse.getMessage() != null ? baseResponse.getMessage()
                                            : "Cập nhật hồ sơ thất bại.",
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
                                "Lỗi mạng khi cập nhật hồ sơ.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToOrderHistory() {
        Intent intent = new Intent(this, OrderHistoryActivity.class);
        startActivity(intent);
    }

    private void navigateToCart() {
        Intent intent = new Intent(this, CartActivity.class);
        startActivity(intent);
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
