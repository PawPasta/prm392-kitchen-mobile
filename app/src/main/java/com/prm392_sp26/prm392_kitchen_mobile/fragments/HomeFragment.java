package com.prm392_sp26.prm392_kitchen_mobile.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.activities.DishDetailActivity;
import com.prm392_sp26.prm392_kitchen_mobile.activities.ProfileActivity;
import com.prm392_sp26.prm392_kitchen_mobile.adapters.BannerAdapter;
import com.prm392_sp26.prm392_kitchen_mobile.adapters.DishAdapter;
import com.prm392_sp26.prm392_kitchen_mobile.model.data.BannerItem;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.RefreshTokenRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.DishResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.LoginResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiService;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.shared.PageResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final int PAGE_SIZE = 10;
    private static final int BANNER_INTERVAL_MS = 4000;
    private TextView tvWelcome, tvUserInfo;
    private View avatarPlaceholder;
    private NestedScrollView homeScroll;
    private ViewPager2 bannerPager;
    private LinearLayout bannerIndicators;
    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private RecyclerView rvDishes;
    private DishAdapter dishAdapter;
    private List<DishResponse> dishList = new ArrayList<>();
    private PrefsManager prefsManager;
    private FirebaseAuth firebaseAuth;
    private int currentPage;
    private boolean isLoading;
    private boolean isLastPage;
    private boolean isRetrying = false;
    private Runnable bannerRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        prefsManager = PrefsManager.getInstance(requireContext());
        firebaseAuth = FirebaseAuth.getInstance();

        // Bind views
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvUserInfo = view.findViewById(R.id.tvUserInfo);
        rvDishes = view.findViewById(R.id.rvDishes);
        avatarPlaceholder = view.findViewById(R.id.avatarPlaceholder);
        homeScroll = view.findViewById(R.id.homeScroll);
        bannerPager = view.findViewById(R.id.bannerPager);
        bannerIndicators = view.findViewById(R.id.bannerIndicators);

        // Setup Layout
        rvDishes.setLayoutManager(new LinearLayoutManager(requireContext()));
        dishAdapter = new DishAdapter(dishList, dish -> {
            Intent intent = new Intent(requireActivity(), DishDetailActivity.class);
            intent.putExtra("dishId", dish.getDishId());
            startActivity(intent);
        });
        rvDishes.setAdapter(dishAdapter);

        // Logic
        displayUserInfo();
        avatarPlaceholder.setOnClickListener(v -> navigateToProfile());
        setupBanner();
        setupLazyLoading();
        currentPage = 0;
        isLastPage = false;
        loadDishes(0);

        return view;
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

    private void navigateToProfile() {
        Intent intent = new Intent(requireActivity(), ProfileActivity.class);
        startActivity(intent);
    }

    private void setupLazyLoading() {
        homeScroll.setOnScrollChangeListener((NestedScrollView v, int scrollX, int scrollY,
                                              int oldScrollX, int oldScrollY) -> {
            if (scrollY <= oldScrollY) {
                return;
            }
            View content = v.getChildAt(0);
            if (content == null) {
                return;
            }
            int distanceToBottom = content.getMeasuredHeight() - v.getMeasuredHeight() - scrollY;
            if (distanceToBottom <= dpToPx(120)) {
                loadNextPage();
            }
        });
    }

    private void loadNextPage() {
        if (isLoading || isLastPage || isRetrying) {
            return;
        }
        loadDishes(currentPage + 1);
    }

    private void loadDishes(int page) {
        if (isLoading || isLastPage || isRetrying) {
            return;
        }
        isLoading = true;
        String token = "Bearer " + prefsManager.getAccessToken();
        ApiService apiService = ApiClient.getInstance().getApiService();
        
        apiService.getDishes(token, page, PAGE_SIZE).enqueue(new Callback<BaseResponse<PageResponse<DishResponse>>>() {
            @Override
            public void onResponse(Call<BaseResponse<PageResponse<DishResponse>>> call, Response<BaseResponse<PageResponse<DishResponse>>> response) {
                isLoading = false;
                if (!isAdded()) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<PageResponse<DishResponse>> body = response.body();
                    if (body.isSuccess() && body.getData() != null) {
                        List<DishResponse> dishes = body.getData().getContent();
                        if (page == 0) {
                            dishList.clear();
                        }
                        if (dishes != null && !dishes.isEmpty()) {
                            dishList.addAll(dishes);
                            dishAdapter.notifyDataSetChanged();
                        }
                        isLastPage = body.getData().isLast();
                        currentPage = page;
                    }
                } else if (response.code() == 401 && !isRetrying) {
                    refreshTokenAndRetry(page);
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<PageResponse<DishResponse>>> call, Throwable t) {
                isLoading = false;
                if (isAdded()) {
                    Log.e(TAG, "API Failure", t);
                }
            }
        });
    }

    private void refreshTokenAndRetry(int page) {
        String refreshToken = prefsManager.getRefreshToken();
        if (refreshToken == null || refreshToken.isEmpty()) {
            return;
        }

        isRetrying = true;
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

        ApiClient.getInstance().getApiService().refreshToken(request)
            .enqueue(new Callback<BaseResponse<LoginResponse>>() {
                @Override
                public void onResponse(@NonNull Call<BaseResponse<LoginResponse>> call,
                                       @NonNull Response<BaseResponse<LoginResponse>> response) {
                    if (response.isSuccessful() && response.body() != null
                            && response.body().isSuccess() && response.body().getData() != null) {
                        prefsManager.saveLoginResponse(response.body().getData());
                        isLastPage = false;
                        loadDishes(page);
                    }
                    isRetrying = false;
                }

                @Override
                public void onFailure(@NonNull Call<BaseResponse<LoginResponse>> call, @NonNull Throwable t) {
                    isRetrying = false;
                }
            });
    }

    private void setupBanner() {
        List<BannerItem> items = new ArrayList<>();
        items.add(new BannerItem("Deal of the day", "Save more on fresh meals today", "Order now"));
        items.add(new BannerItem("Chef picks", "Popular dishes recommended this week", "View menu"));
        items.add(new BannerItem("Fast pickup", "Quick pickup options for busy days", "Explore"));

        BannerAdapter bannerAdapter = new BannerAdapter(items);
        bannerPager.setAdapter(bannerAdapter);
        setupBannerIndicators(items.size());
        updateBannerIndicators(0);

        bannerPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateBannerIndicators(position);
                resetBannerAutoScroll();
            }
        });

        bannerRunnable = () -> {
            if (bannerAdapter.getItemCount() == 0) {
                return;
            }
            int next = (bannerPager.getCurrentItem() + 1) % bannerAdapter.getItemCount();
            bannerPager.setCurrentItem(next, true);
            bannerHandler.postDelayed(bannerRunnable, BANNER_INTERVAL_MS);
        };
    }

    private void setupBannerIndicators(int count) {
        bannerIndicators.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = new View(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(8), dpToPx(8));
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.indicator_dot_inactive);
            bannerIndicators.addView(dot);
        }
    }

    private void updateBannerIndicators(int activePosition) {
        for (int i = 0; i < bannerIndicators.getChildCount(); i++) {
            View dot = bannerIndicators.getChildAt(i);
            LinearLayout.LayoutParams params;
            if (i == activePosition) {
                params = new LinearLayout.LayoutParams(dpToPx(24), dpToPx(8));
                dot.setBackgroundResource(R.drawable.indicator_dot_active);
            } else {
                params = new LinearLayout.LayoutParams(dpToPx(8), dpToPx(8));
                dot.setBackgroundResource(R.drawable.indicator_dot_inactive);
            }
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            dot.setLayoutParams(params);
        }
    }

    private void resetBannerAutoScroll() {
        bannerHandler.removeCallbacks(bannerRunnable);
        bannerHandler.postDelayed(bannerRunnable, BANNER_INTERVAL_MS);
    }

    private int dpToPx(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bannerRunnable != null) {
            bannerHandler.postDelayed(bannerRunnable, BANNER_INTERVAL_MS);
        }
    }

    @Override
    public void onPause() {
        bannerHandler.removeCallbacksAndMessages(null);
        super.onPause();
    }
}
