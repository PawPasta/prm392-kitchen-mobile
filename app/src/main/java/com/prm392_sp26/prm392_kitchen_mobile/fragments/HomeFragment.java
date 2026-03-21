package com.prm392_sp26.prm392_kitchen_mobile.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.activities.DishDetailActivity;
import com.prm392_sp26.prm392_kitchen_mobile.activities.LocationDetailActivity;
import com.prm392_sp26.prm392_kitchen_mobile.activities.NotificationActivity;
import com.prm392_sp26.prm392_kitchen_mobile.activities.StepItemsActivity;
import com.prm392_sp26.prm392_kitchen_mobile.adapters.DishAdapter;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.RefreshTokenRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.DishResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.LoginResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiService;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.shared.PageResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.Constants;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final int PAGE_SIZE = 10;
    private TextView tvWelcome, tvUserInfo;
    private NestedScrollView homeScroll;
    private MapView homeMapView;
    private View homeMapTouchOverlay;
    private RecyclerView rvDishes;
    private DishAdapter dishAdapter;
    private List<DishResponse> dishList = new ArrayList<>();
    private PrefsManager prefsManager;
    private FirebaseAuth firebaseAuth;
    private int currentPage;
    private boolean isLoading;
    private boolean isLastPage;
    private boolean isRetrying = false;

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
        homeScroll = view.findViewById(R.id.homeScroll);
        homeMapView = view.findViewById(R.id.homeMapView);
        homeMapTouchOverlay = view.findViewById(R.id.homeMapTouchOverlay);
        View searchBar = view.findViewById(R.id.layoutHomeSearchBar);
        View notificationButton = view.findViewById(R.id.btnNotification);

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
        setupSearchNavigation(searchBar);
        setupCategoryNavigation(view);
        setupNotificationNavigation(notificationButton);

        setupHomeMap();
        setupHomeMapNavigation();
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
                tvWelcome.setText("Xin chào, " + displayName + "!");
            } else {
                tvWelcome.setText("Xin chào!");
            }
        } else {
            tvWelcome.setText("Xin chào!");
        }
        tvUserInfo.setText("Bạn muốn ăn gì hôm nay?");
    }

    private void setupCategoryNavigation(@NonNull View root) {
        bindCategoryClick(root, R.id.categoryCarb, 1, "Carb");
        bindCategoryClick(root, R.id.categoryProtein, 2, "Protein");
        bindCategoryClick(root, R.id.categoryVegetables, 3, "Vegetables");
        bindCategoryClick(root, R.id.categorySauce, 4, "Sauce");
        bindCategoryClick(root, R.id.categoryExtra, 5, "Extra");
    }

    private void setupSearchNavigation(@Nullable View searchBar) {
        if (searchBar == null) {
            return;
        }
        searchBar.setOnClickListener(v -> openStepItems(0, "Tìm kiếm"));
    }

    private void setupNotificationNavigation(@Nullable View notificationButton) {
        if (notificationButton == null) {
            return;
        }
        notificationButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), NotificationActivity.class);
            startActivity(intent);
        });
    }

    private void setupHomeMapNavigation() {
        if (homeMapTouchOverlay == null) {
            return;
        }
        homeMapTouchOverlay.setOnClickListener(v -> openLocationDetail());
    }

    private void openLocationDetail() {
        Intent intent = new Intent(requireActivity(), LocationDetailActivity.class);
        startActivity(intent);
    }

    private void bindCategoryClick(@NonNull View root, int viewId, int stepId, @NonNull String stepName) {
        View categoryView = root.findViewById(viewId);
        if (categoryView == null) {
            return;
        }
        categoryView.setOnClickListener(v -> openStepItems(stepId, stepName));
    }

    private void openStepItems(int stepId, @NonNull String stepName) {
        Intent intent = new Intent(requireActivity(), StepItemsActivity.class);
        intent.putExtra(StepItemsActivity.EXTRA_STEP_ID, stepId);
        intent.putExtra(StepItemsActivity.EXTRA_STEP_NAME, stepName);
        startActivity(intent);
    }

    private void setupHomeMap() {
        if (homeMapView == null) {
            return;
        }

        GeoPoint storePoint = new GeoPoint(Constants.STORE_LATITUDE, Constants.STORE_LONGITUDE);
        homeMapView.setTileSource(TileSourceFactory.MAPNIK);
        homeMapView.getController().setZoom(16.0);
        homeMapView.getController().setCenter(storePoint);
        homeMapView.setMultiTouchControls(false);
        homeMapView.setClickable(false);
        homeMapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        Marker marker = new Marker(homeMapView);
        marker.setPosition(storePoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(Constants.STORE_NAME);
        homeMapView.getOverlays().add(marker);
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

    private int dpToPx(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (homeMapView != null) {
            homeMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        if (homeMapView != null) {
            homeMapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (homeMapView != null) {
            homeMapView.onDetach();
        }
        super.onDestroyView();
    }
}
