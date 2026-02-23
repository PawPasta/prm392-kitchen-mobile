package com.prm392_sp26.prm392_kitchen_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.prm392_sp26.prm392_kitchen_mobile.activities.AuthActivity;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

/**
 * MainActivity - Đóng vai trò như routes/navigation chính của app
 * Sau khi đăng nhập thành công, đây là màn hình home
 * Từ đây có thể điều hướng đến các màn hình khác trong app
 */
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private PrefsManager prefsManager;

    private TextView tvWelcome;
    private TextView tvUserInfo;
    private Button btnLogout;

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

        // Khởi tạo
        firebaseAuth = FirebaseAuth.getInstance();
        prefsManager = PrefsManager.getInstance(this);

        // Bind views
        tvWelcome = findViewById(R.id.tvWelcome);
        tvUserInfo = findViewById(R.id.tvUserInfo);
        btnLogout = findViewById(R.id.btnLogout);

        // Hiển thị thông tin user
        displayUserInfo();

        // Set click listeners cho navigation
        btnLogout.setOnClickListener(v -> logout());

        // TODO: Thêm các button/navigation khác tại đây
        // Ví dụ: navigateToMenu(), navigateToOrders(), navigateToProfile(), etc.
    }

    /**
     * Hiển thị thông tin user đã đăng nhập
     * Vì LoginResponse chỉ trả về accessToken và refreshToken,
     * thông tin user có thể lấy từ Firebase Auth nếu cần
     */
    private void displayUserInfo() {
        // Lấy thông tin từ Firebase Auth nếu user đang đăng nhập
        if (firebaseAuth.getCurrentUser() != null) {
            String displayName = firebaseAuth.getCurrentUser().getDisplayName();
            String email = firebaseAuth.getCurrentUser().getEmail();

            if (displayName != null && !displayName.isEmpty()) {
                tvWelcome.setText("Xin chào, " + displayName + "!");
            } else {
                tvWelcome.setText("Xin chào!");
            }

            StringBuilder userInfo = new StringBuilder();
            userInfo.append("Email: ").append(email != null ? email : "N/A");
            userInfo.append("\nTrạng thái: Đã đăng nhập");
            tvUserInfo.setText(userInfo.toString());
        } else {
            tvWelcome.setText("Xin chào!");
            tvUserInfo.setText("Trạng thái: Đã đăng nhập");
        }
    }

    /**
     * Đăng xuất và quay lại màn hình đăng nhập
     */
    private void logout() {
        // Đăng xuất Firebase
        firebaseAuth.signOut();

        // Xóa session trong SharedPreferences
        prefsManager.clearSession();

        // Chuyển về AuthActivity
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
}