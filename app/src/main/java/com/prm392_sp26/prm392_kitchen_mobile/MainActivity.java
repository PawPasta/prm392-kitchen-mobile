package com.prm392_sp26.prm392_kitchen_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.prm392_sp26.prm392_kitchen_mobile.fragments.CartFragment;
import com.prm392_sp26.prm392_kitchen_mobile.fragments.HomeFragment;
import com.prm392_sp26.prm392_kitchen_mobile.fragments.MenuFragment;
import com.prm392_sp26.prm392_kitchen_mobile.fragments.OrdersFragment;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private PrefsManager prefsManager;
    private TextView tvWelcome;
    private TextView tvUserInfo;
    private TextView btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Xử lý Insets: top padding cho status bar, bottom = 0 vì navbar tự nằm sát đáy
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        // Padding bottom = system nav bar để không bị che, top thêm chút cho cân đối
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int topPad = (int) (8 * getResources().getDisplayMetrics().density);
            v.setPadding(0, topPad, 0, systemBars.bottom + topPad);
            return insets;
        });

        // Mặc định load HomeFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
        tvWelcome = findViewById(R.id.tvWelcome);
        tvUserInfo = findViewById(R.id.tvUserInfo);
        btnLogout = findViewById(R.id.btnLogout);
        avatarPlaceholder = findViewById(R.id.avatarPlaceholder);

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_menu) {
                selectedFragment = new MenuFragment();
            } else if (itemId == R.id.nav_cart) {
                selectedFragment = new CartFragment();
            } else if (itemId == R.id.nav_orders) {
                selectedFragment = new OrdersFragment();
            }
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