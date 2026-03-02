package com.prm392_sp26.prm392_kitchen_mobile.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.activities.DishDetailActivity;
import com.prm392_sp26.prm392_kitchen_mobile.adapters.DishAdapter;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.DishResponse;
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

public class MenuFragment extends Fragment {

    private static final String TAG = "MenuFragment";
    private static final int PAGE_SIZE = 20;
    private RecyclerView rvMenuDishes;
    private DishAdapter dishAdapter;
    private List<DishResponse> dishList = new ArrayList<>();
    private PrefsManager prefsManager;
    private int currentPage;
    private boolean isLoading;
    private boolean isLastPage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        prefsManager = PrefsManager.getInstance(requireContext());
        rvMenuDishes = view.findViewById(R.id.rvMenuDishes);

        rvMenuDishes.setLayoutManager(new LinearLayoutManager(requireContext()));
        dishAdapter = new DishAdapter(dishList, dish -> {
            Intent intent = new Intent(requireActivity(), DishDetailActivity.class);
            intent.putExtra("dishId", dish.getDishId());
            startActivity(intent);
        });
        rvMenuDishes.setAdapter(dishAdapter);

        setupPagination();
        currentPage = 0;
        isLastPage = false;
        loadAllDishes(0);

        return view;
    }

    private void setupPagination() {
        rvMenuDishes.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy <= 0) {
                    return;
                }
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) {
                    return;
                }
                int totalItemCount = layoutManager.getItemCount();
                int lastVisible = layoutManager.findLastVisibleItemPosition();
                if (!isLoading && !isLastPage && lastVisible >= totalItemCount - 2) {
                    loadAllDishes(currentPage + 1);
                }
            }
        });
    }

    private void loadAllDishes(int page) {
        if (isLoading || isLastPage) {
            return;
        }
        isLoading = true;
        String token = "Bearer " + prefsManager.getAccessToken();
        ApiService apiService = ApiClient.getInstance().getApiService();

        apiService.getDishes(token, page, PAGE_SIZE).enqueue(new Callback<BaseResponse<PageResponse<DishResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse<PageResponse<DishResponse>>> call, 
                                   @NonNull Response<BaseResponse<PageResponse<DishResponse>>> response) {
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
                } else if (response.code() == 401) {
                    // Xử lý refresh token nếu cần, ở đây tạm thời bỏ qua cho đơn giản 
                    // hoặc gọi logout nếu muốn nghiêm ngặt.
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse<PageResponse<DishResponse>>> call, @NonNull Throwable t) {
                isLoading = false;
                if (isAdded()) {
                    Log.e(TAG, "Fail to load menu", t);
                    Toast.makeText(requireContext(), "Không thể tải thực đơn", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
