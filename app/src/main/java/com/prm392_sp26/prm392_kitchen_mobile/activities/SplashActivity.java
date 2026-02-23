package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.prm392_sp26.prm392_kitchen_mobile.MainActivity;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

/**
 * SplashActivity - Màn hình khởi động
 * Kiểm tra trạng thái đăng nhập và chuyển hướng đến màn hình phù hợp
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 2000;

    private FirebaseAuth firebaseAuth;
    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        firebaseAuth = FirebaseAuth.getInstance();
        prefsManager = PrefsManager.getInstance(this);

        // Delay để hiển thị splash screen
        new Handler(Looper.getMainLooper()).postDelayed(this::checkLoginStatus, SPLASH_DELAY);
    }

    /**
     * Kiểm tra trạng thái đăng nhập
     * - Nếu đã đăng nhập (có Firebase user và có token trong prefs) -> MainActivity
     * - Nếu chưa đăng nhập -> AuthActivity
     */
    private void checkLoginStatus() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        boolean isLoggedIn = prefsManager.isLoggedIn();

        Intent intent;
        if (currentUser != null && isLoggedIn) {
            // Đã đăng nhập -> chuyển đến MainActivity (routes chính)
            intent = new Intent(this, MainActivity.class);
        } else {
            // Chưa đăng nhập -> chuyển đến AuthActivity
            intent = new Intent(this, AuthActivity.class);
        }

        startActivity(intent);
        finish(); // Đóng SplashActivity để không quay lại được
    }
}
