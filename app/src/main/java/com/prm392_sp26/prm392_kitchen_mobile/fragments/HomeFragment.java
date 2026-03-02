package com.prm392_sp26.prm392_kitchen_mobile.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.activities.AuthActivity;
import com.prm392_sp26.prm392_kitchen_mobile.activities.DishDetailActivity;
import com.prm392_sp26.prm392_kitchen_mobile.adapters.DishAdapter;
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
    private TextView tvWelcome, tvUserInfo;
    private RecyclerView rvDishes;
    private DishAdapter dishAdapter;
    private List<DishResponse> dishList = new ArrayList<>();
    private PrefsManager prefsManager;
    private boolean isRetrying = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        prefsManager = PrefsManager.getInstance(requireContext());

        // Bind views
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvUserInfo = view.findViewById(R.id.tvUserInfo);
        rvDishes = view.findViewById(R.id.rvDishes);

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
        loadDishes();

        return view;
    }

    private void displayUserInfo() {
        tvWelcome.setText("Xin chào khách hàng! 👋");
        tvUserInfo.setText("Bạn muốn ăn gì hôm nay?");
    }

    private void loadDishes() {
        String token = "Bearer " + prefsManager.getAccessToken();
        ApiService apiService = ApiClient.getInstance().getApiService();
        
        apiService.getDishes(token, 0, 10).enqueue(new Callback<BaseResponse<PageResponse<DishResponse>>>() {
            @Override
            public void onResponse(Call<BaseResponse<PageResponse<DishResponse>>> call, Response<BaseResponse<PageResponse<DishResponse>>> response) {
                if (!isAdded()) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<PageResponse<DishResponse>> body = response.body();
                    if (body.isSuccess() && body.getData() != null) {
                        List<DishResponse> dishes = body.getData().getContent();
                        if (dishes != null && !dishes.isEmpty()) {
                            dishList.clear();
                            dishList.addAll(dishes);
                            dishAdapter.notifyDataSetChanged();
                        }
                    }
                } else if (response.code() == 401 && !isRetrying) {
                    refreshTokenAndRetry();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<PageResponse<DishResponse>>> call, Throwable t) {
                if (isAdded()) {
                    Log.e(TAG, "API Failure", t);
                }
            }
        });
    }

    private void refreshTokenAndRetry() {
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
                        loadDishes();
                    }
                    isRetrying = false;
                }

                @Override
                public void onFailure(@NonNull Call<BaseResponse<LoginResponse>> call, @NonNull Throwable t) {
                    isRetrying = false;
                }
            });
    }
}
