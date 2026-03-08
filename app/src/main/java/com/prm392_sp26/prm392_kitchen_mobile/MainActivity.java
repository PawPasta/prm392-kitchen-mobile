package com.prm392_sp26.prm392_kitchen_mobile;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.View;
import com.prm392_sp26.prm392_kitchen_mobile.fragments.HomeFragment;
import com.prm392_sp26.prm392_kitchen_mobile.fragments.MenuFragment;
import com.prm392_sp26.prm392_kitchen_mobile.fragments.OrdersFragment;
import com.prm392_sp26.prm392_kitchen_mobile.activities.CartActivity;
import com.prm392_sp26.prm392_kitchen_mobile.activities.CreateCustomOrderActivity;
import com.prm392_sp26.prm392_kitchen_mobile.activities.ProfileActivity;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private FloatingActionButton fabCart;
    private FloatingActionButton fabCustomOrder;
    private View fabStack;
    private int lastSelectedItemId = R.id.nav_home;

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
        fabCart = findViewById(R.id.fabCart);
        fabCustomOrder = findViewById(R.id.fabCustomOrder);
        fabStack = findViewById(R.id.fabStack);
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
            } else if (itemId == R.id.nav_orders) {
                selectedFragment = new OrdersFragment();
            } else if (itemId == R.id.nav_profile) {
                startActivity(new android.content.Intent(this, ProfileActivity.class));
                bottomNav.post(() -> bottomNav.setSelectedItemId(lastSelectedItemId));
                return false;
            } else {
                return false;
            }
            switchFragment(selectedFragment);
            lastSelectedItemId = itemId;
            setCustomOrderVisible(itemId == R.id.nav_home);
            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
        setCustomOrderVisible(bottomNav.getSelectedItemId() == R.id.nav_home);

        if (fabCart != null) {
            fabCart.setOnClickListener(v -> {
                startActivity(new android.content.Intent(this, CartActivity.class));
            });
        }

        if (fabCustomOrder != null) {
            fabCustomOrder.setOnClickListener(v -> {
                startActivity(new android.content.Intent(this, CreateCustomOrderActivity.class));
            });
        }

        if (fabStack != null) {
            bottomNav.post(() -> {
                int bottomMargin = bottomNav.getHeight() + dpToPx(10);
                android.view.ViewGroup.MarginLayoutParams params =
                        (android.view.ViewGroup.MarginLayoutParams) fabStack.getLayoutParams();
                params.bottomMargin = bottomMargin;
                fabStack.setLayoutParams(params);
            });
        }
    }

    private void setCustomOrderVisible(boolean visible) {
        if (fabCustomOrder == null) {
            return;
        }
        fabCustomOrder.setVisibility(visible ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private int dpToPx(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
