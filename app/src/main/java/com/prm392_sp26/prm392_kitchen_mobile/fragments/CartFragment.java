package com.prm392_sp26.prm392_kitchen_mobile.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.activities.OrderDetailActivity;
import com.prm392_sp26.prm392_kitchen_mobile.adapters.OrderHistoryAdapter;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderHistoryResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshCart;
    private ProgressBar progressCart;
    private LinearLayout layoutCartEmpty;
    private RecyclerView recyclerCart;

    private OrderHistoryAdapter adapter;
    private PrefsManager prefsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        prefsManager = PrefsManager.getInstance(requireContext());

        swipeRefreshCart = view.findViewById(R.id.swipeRefreshCart);
        progressCart = view.findViewById(R.id.progressCart);
        layoutCartEmpty = view.findViewById(R.id.layoutCartEmpty);
        recyclerCart = view.findViewById(R.id.recyclerCart);
        Button btnCreateCustomOrder = view.findViewById(R.id.btnCreateCustomOrder);

        adapter = new OrderHistoryAdapter();
        recyclerCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerCart.setAdapter(adapter);
        adapter.setOnItemClickListener(this::openOrderDetail);

        swipeRefreshCart.setOnRefreshListener(this::loadCartOrders);

        btnCreateCustomOrder.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(),
                com.prm392_sp26.prm392_kitchen_mobile.activities.CreateCustomOrderActivity.class);
            startActivity(intent);
        });

        loadCartOrders();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCartOrders();
    }

    private void loadCartOrders() {
        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        progressCart.setVisibility(View.VISIBLE);
        layoutCartEmpty.setVisibility(View.GONE);
        recyclerCart.setVisibility(View.GONE);

        ApiClient.getInstance().getApiService()
                .getOrderHistory("Bearer " + token, 0, 100, "CREATED")
                .enqueue(new Callback<BaseResponse<OrderHistoryResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<OrderHistoryResponse>> call,
                                           @NonNull Response<BaseResponse<OrderHistoryResponse>> response) {
                        progressCart.setVisibility(View.GONE);
                        swipeRefreshCart.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()
                                && response.body().getData() != null
                                && response.body().getData().getOrders() != null) {

                            List<OrderHistoryResponse.OrderItem> orders =
                                    response.body().getData().getOrders().getContent();

                            if (orders == null || orders.isEmpty()) {
                                showEmpty();
                            } else {
                                adapter.setItems(orders);
                                recyclerCart.setVisibility(View.VISIBLE);
                                layoutCartEmpty.setVisibility(View.GONE);
                            }
                        } else {
                            showEmpty();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<OrderHistoryResponse>> call,
                                          @NonNull Throwable t) {
                        progressCart.setVisibility(View.GONE);
                        swipeRefreshCart.setRefreshing(false);
                        showEmpty();
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showEmpty() {
        recyclerCart.setVisibility(View.GONE);
        layoutCartEmpty.setVisibility(View.VISIBLE);
    }

    private void openOrderDetail(OrderHistoryResponse.OrderItem item) {
        String json = new Gson().toJson(item);
        Intent intent = new Intent(requireActivity(), OrderDetailActivity.class);
        intent.putExtra("order_json", json);
        startActivity(intent);
    }
}
