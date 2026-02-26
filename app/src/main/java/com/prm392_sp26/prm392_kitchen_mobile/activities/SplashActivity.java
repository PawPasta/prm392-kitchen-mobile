package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.prm392_sp26.prm392_kitchen_mobile.MainActivity;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.adapters.OnboardingAdapter;
import com.prm392_sp26.prm392_kitchen_mobile.model.data.OnboardingItem;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout layoutIndicators;
    private TextView btnNext;
    private TextView btnSkip;
    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefsManager = PrefsManager.getInstance(this);

        // // ====== KIỂM TRA: đã xem onboarding chưa? ======
        // if (prefsManager.isOnboardingDone()) {
        //     navigateBasedOnLoginStatus();
        //     return; 
        // }

        // ====== CHƯA XEM → Hiển thị onboarding ======
        setContentView(R.layout.activity_splash);

        viewPager = findViewById(R.id.viewPagerOnboarding);
        layoutIndicators = findViewById(R.id.layoutIndicators);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);

        List<OnboardingItem> items = new ArrayList<>();
        items.add(new OnboardingItem(
                "Khám phá món ngon",
                "Hàng trăm món ăn đa dạng từ các đầu bếp tài năng đang chờ bạn"
        ));
        items.add(new OnboardingItem(
                "Đặt hàng dễ dàng",
                "Chỉ vài thao tác đơn giản để đặt món ăn yêu thích của bạn"
        ));
        items.add(new OnboardingItem(
                "Giao hàng nhanh chóng",
                "Món ăn nóng hổi sẽ đến tay bạn trong thời gian ngắn nhất"
        ));

        OnboardingAdapter adapter = new OnboardingAdapter(items);
        viewPager.setAdapter(adapter);

        setupIndicators(items.size());  
        updateIndicators(0);            

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);

                if (position == items.size() - 1) {
                    btnNext.setText("Bắt đầu");
                    btnSkip.setVisibility(View.INVISIBLE); 
                } else {
                    btnNext.setText("Tiếp theo");
                    btnSkip.setVisibility(View.VISIBLE);  
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem(); 
            if (current < items.size() - 1) {
                viewPager.setCurrentItem(current + 1);
            } else {
                finishOnboarding();
            }
        });

        btnSkip.setOnClickListener(v -> finishOnboarding());
    }

    
    private void setupIndicators(int count) {
        layoutIndicators.removeAllViews();

        for (int i = 0; i < count; i++) {
            View dot = new View(this);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(8), dpToPx(8));
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);

            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.indicator_dot_inactive);
            layoutIndicators.addView(dot);
        }
    }

    
    private void updateIndicators(int activePosition) {
        for (int i = 0; i < layoutIndicators.getChildCount(); i++) {
            View dot = layoutIndicators.getChildAt(i);

            if (i == activePosition) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        dpToPx(24), dpToPx(8));
                params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
                dot.setLayoutParams(params);
                dot.setBackgroundResource(R.drawable.indicator_dot_active);
            } else {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        dpToPx(8), dpToPx(8));
                params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
                dot.setLayoutParams(params);
                dot.setBackgroundResource(R.drawable.indicator_dot_inactive);
            }
        }
    }

    private void finishOnboarding() {
        prefsManager.setOnboardingDone(true); 
        navigateBasedOnLoginStatus();
    }

    private void navigateBasedOnLoginStatus() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        boolean isLoggedIn = prefsManager.isLoggedIn();

        Intent intent;
        if (currentUser != null && isLoggedIn) {
            // Đã đăng nhập → đi thẳng đến MainActivity
            intent = new Intent(this, MainActivity.class);
        } else {
            // Chưa đăng nhập → đến AuthActivity
            intent = new Intent(this, AuthActivity.class);
        }

        startActivity(intent);
        finish();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
