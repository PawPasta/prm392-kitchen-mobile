package com.prm392_sp26.prm392_kitchen_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.prm392_sp26.prm392_kitchen_mobile.activities.AuthActivity;
import com.prm392_sp26.prm392_kitchen_mobile.activities.ProfileActivity;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private PrefsManager prefsManager;
    private TextView tvWelcome;
    private TextView tvUserInfo;
    private TextView btnLogout;
    private View avatarPlaceholder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseAuth = FirebaseAuth.getInstance();
        prefsManager = PrefsManager.getInstance(this);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvUserInfo = findViewById(R.id.tvUserInfo);
        btnLogout = findViewById(R.id.btnLogout);
        avatarPlaceholder = findViewById(R.id.avatarPlaceholder);

        displayUserInfo();
        btnLogout.setOnClickListener(v -> logout());
        avatarPlaceholder.setOnClickListener(v -> navigateToProfile());
    }

    private void displayUserInfo() {
        if (firebaseAuth.getCurrentUser() != null) {
            String displayName = firebaseAuth.getCurrentUser().getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                tvWelcome.setText("Xin chào, " + displayName + "! 👋");
            } else {
                tvWelcome.setText("Xin chào! 👋");
            }
        } else {
            tvWelcome.setText("Xin chào! 👋");
        }
        tvUserInfo.setText("Bạn muốn ăn gì hôm nay?");
    }

    private void logout() {
        firebaseAuth.signOut();
        prefsManager.clearSession();
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // =====================================================
    // NAVIGATION METHODS - Thêm các phương thức điều hướng ở đây
    // =====================================================

    /**
     * Điều hướng đến màn hình Menu
     */
    // private void navigateToMenu() {
    //     Intent intent = new Intent(this, MenuActivity.class);
    //     startActivity(intent);
    // }

    /**
     * Điều hướng đến màn hình Orders
     */
    // private void navigateToOrders() {
    //     Intent intent = new Intent(this, OrdersActivity.class);
    //     startActivity(intent);
    // }

    /**
     * Điều hướng đến màn hình Profile
     */
    // private void navigateToProfile() {
    //     Intent intent = new Intent(this, ProfileActivity.class);
    //     startActivity(intent);
    // }

    private void navigateToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }
}