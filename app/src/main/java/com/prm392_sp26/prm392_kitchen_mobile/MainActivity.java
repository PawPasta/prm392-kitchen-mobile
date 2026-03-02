package com.prm392_sp26.prm392_kitchen_mobile;

import android.os.Bundle;

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

    private BottomNavigationView bottomNav;

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

        bottomNav = findViewById(R.id.bottomNav);
        // Padding bottom = system nav bar để không bị che, top thêm chút cho cân đối
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int topPad = (int) (8 * getResources().getDisplayMetrics().density);
            v.setPadding(0, topPad, 0, systemBars.bottom + topPad);
            return insets;
        });

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_menu) {
                selectedFragment = new MenuFragment();
            } else if (itemId == R.id.nav_cart) {
                selectedFragment = new CartFragment();
            } else if (itemId == R.id.nav_orders) {
                selectedFragment = new OrdersFragment();
            } else {
                return false;
            }
            switchFragment(selectedFragment);
            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
