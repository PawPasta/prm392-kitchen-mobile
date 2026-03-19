package com.prm392_sp26.prm392_kitchen_mobile;

import android.Manifest;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.prm392_sp26.prm392_kitchen_mobile.fragments.HomeFragment;
import com.prm392_sp26.prm392_kitchen_mobile.fragments.GuideFragment;
import com.prm392_sp26.prm392_kitchen_mobile.fragments.MenuFragment;
import com.prm392_sp26.prm392_kitchen_mobile.fragments.OrdersFragment;
import com.prm392_sp26.prm392_kitchen_mobile.activities.CartActivity;
import com.prm392_sp26.prm392_kitchen_mobile.activities.CreateCustomOrderActivity;
import com.prm392_sp26.prm392_kitchen_mobile.activities.ProfileActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATIONS = 1001;

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

        requestNotificationPermissionIfNeeded();
        // Giữ khoảng cách trên/dưới cân bằng cho icon + label; đưa system inset ra ngoài bằng margin.
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            int topPad = dpToPx(3);
            int bottomPad = dpToPx(0);
            v.setPadding(0, topPad, 0, bottomPad);

            ViewGroup.LayoutParams rawParams = v.getLayoutParams();
            if (rawParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) rawParams;
                if (params.bottomMargin != systemBars.bottom) {
                    params.bottomMargin = systemBars.bottom;
                    v.setLayoutParams(params);
                }
            }

            updateFabStackBottomMargin();
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
            } else if (itemId == R.id.nav_guide) {
                selectedFragment = new GuideFragment();
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
            bottomNav.post(this::updateFabStackBottomMargin);
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

    private void updateFabStackBottomMargin() {
        if (fabStack == null || bottomNav == null) {
            return;
        }

        int navInsetBottom = 0;
        ViewGroup.LayoutParams navRawParams = bottomNav.getLayoutParams();
        if (navRawParams instanceof ViewGroup.MarginLayoutParams) {
            navInsetBottom = ((ViewGroup.MarginLayoutParams) navRawParams).bottomMargin;
        }

        int bottomMargin = bottomNav.getHeight() + navInsetBottom + dpToPx(10);
        ViewGroup.LayoutParams fabRawParams = fabStack.getLayoutParams();
        if (!(fabRawParams instanceof ViewGroup.MarginLayoutParams)) {
            return;
        }
        ViewGroup.MarginLayoutParams fabParams = (ViewGroup.MarginLayoutParams) fabRawParams;
        if (fabParams.bottomMargin == bottomMargin) {
            return;
        }
        fabParams.bottomMargin = bottomMargin;
        fabStack.setLayoutParams(fabParams);
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ActivityCompat.requestPermissions(
                this,
                new String[] { Manifest.permission.POST_NOTIFICATIONS },
                REQUEST_NOTIFICATIONS
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_NOTIFICATIONS) {
            return;
        }
        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (!granted) {
            Toast.makeText(this, "Thông báo bị tắt. Bạn có thể bật lại trong Settings.", Toast.LENGTH_LONG).show();
        }
    }
}
