package com.prm392_sp26.prm392_kitchen_mobile.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.activities.OrderDetailActivity;
import com.prm392_sp26.prm392_kitchen_mobile.adapters.OrderHistoryAdapter;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.CancelOrderRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderHistoryResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment hiển thị lịch sử đơn hàng của user
 * Bao gồm filter theo status, pull-to-refresh, và lazy loading
 */
public class OrdersFragment extends Fragment {

    private RecyclerView recyclerOrders;
    private ProgressBar progressOrders, progressLoadMore;
    private LinearLayout layoutEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private ChipGroup chipGroupStatus;
    private OrderHistoryAdapter adapter;
    private PrefsManager prefsManager;

    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = 0;
    private final int pageSize = 10;
    private String selectedStatus = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        prefsManager = PrefsManager.getInstance(requireContext());

        // Initialize views
        recyclerOrders = view.findViewById(R.id.recyclerOrders);
        progressOrders = view.findViewById(R.id.progressOrders);
        progressLoadMore = view.findViewById(R.id.progressLoadMore);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        chipGroupStatus = view.findViewById(R.id.chipGroupStatus);

        // Setup RecyclerView
        adapter = new OrderHistoryAdapter();
        recyclerOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerOrders.setAdapter(adapter);
        adapter.setOnItemClickListener(this::openOrderDetail);
        adapter.setOnCancelClickListener(this::showCancelReasonDialog);
        adapter.setOnFeedbackClickListener(this::openOrderDetailForFeedback);

        // Setup listeners
        setupChipFilters();
        setupRefresh();
        setupPagination();

        // Load initial data
        loadOrders(0, pageSize, null, false);

        return view;
    }

    private void setupChipFilters() {
        if (chipGroupStatus == null) return;

        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedStatus = null;
            } else {
                int checkedId = checkedIds.get(0);
                selectedStatus = getStatusFromChipId(checkedId);
            }

            // Reset and reload
            currentPage = 0;
            isLastPage = false;
            loadOrders(0, pageSize, selectedStatus, false);
        });
    }

    private String getStatusFromChipId(int chipId) {
        if (chipId == R.id.chipAll) return null;
        if (chipId == R.id.chipConfirmed) return "CONFIRMED";
        if (chipId == R.id.chipProcessing) return "PROCESSING";
        if (chipId == R.id.chipReady) return "READY";
        if (chipId == R.id.chipCompleted) return "COMPLETED";
        if (chipId == R.id.chipCancelled) return "CANCELLED";
        return null;
    }

    private void setupRefresh() {
        if (swipeRefresh == null) return;

        swipeRefresh.setOnRefreshListener(() -> {
            currentPage = 0;
            isLastPage = false;
            loadOrders(0, pageSize, selectedStatus, false);
        });
    }

    private void setupPagination() {
        if (recyclerOrders == null) return;

        recyclerOrders.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy <= 0) return;

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) return;

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadOrders(currentPage + 1, pageSize, selectedStatus, true);
                    }
                }
            }
        });
    }

    private void loadOrders(int page, int size, String status, boolean isLoadMore) {
        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isLoading) return;

        setLoading(true, isLoadMore);

        ApiClient.getInstance()
                .getApiService()
                .getOrderHistory("Bearer " + token, page, size, status)
                .enqueue(new Callback<BaseResponse<OrderHistoryResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<OrderHistoryResponse>> call,
                                           @NonNull Response<BaseResponse<OrderHistoryResponse>> response) {
                        setLoading(false, isLoadMore);

                        if (response.isSuccessful() && response.body() != null) {
                            BaseResponse<OrderHistoryResponse> baseResponse = response.body();

                            android.util.Log.d("OrdersFragment", "Response success: " + baseResponse.isSuccess());
                            android.util.Log.d("OrdersFragment", "Response message: " + baseResponse.getMessage());

                            if (baseResponse.isSuccess() && baseResponse.getData() != null
                                    && baseResponse.getData().getOrders() != null) {

                                OrderHistoryResponse.OrderPage orderPage = baseResponse.getData().getOrders();
                                List<OrderHistoryResponse.OrderItem> orders = orderPage.getContent();

                                android.util.Log.d("OrdersFragment", "Orders count: " + (orders != null ? orders.size() : 0));

                                List<OrderHistoryResponse.OrderItem> displayOrders = filterOutCreated(orders);

                                android.util.Log.d("OrdersFragment", "Display orders count: " + displayOrders.size());

                                if (page == 0) {
                                    adapter.setItems(displayOrders);
                                } else {
                                    adapter.addItems(displayOrders);
                                }

                                currentPage = page;
                                isLastPage = orderPage.isLast();

                                // Show/hide empty state
                                if (adapter.getItemCount() == 0) {
                                    if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
                                    if (recyclerOrders != null) recyclerOrders.setVisibility(View.GONE);
                                } else {
                                    if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
                                    if (recyclerOrders != null) recyclerOrders.setVisibility(View.VISIBLE);
                                }
                            } else {
                                android.util.Log.e("OrdersFragment", "Data is null or invalid");
                                Toast.makeText(requireContext(), "Không có dữ liệu đơn hàng", Toast.LENGTH_SHORT).show();
                                if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
                                if (recyclerOrders != null) recyclerOrders.setVisibility(View.GONE);
                            }
                        } else {
                            android.util.Log.e("OrdersFragment", "Response not successful. Code: " + response.code());
                            Toast.makeText(requireContext(), "Lỗi tải đơn hàng: " + response.code(), Toast.LENGTH_SHORT).show();
                            if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
                            if (recyclerOrders != null) recyclerOrders.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<OrderHistoryResponse>> call,
                                          @NonNull Throwable t) {
                        setLoading(false, isLoadMore);
                        android.util.Log.e("OrdersFragment", "Load orders failed", t);
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
                        if (recyclerOrders != null) recyclerOrders.setVisibility(View.GONE);
                    }
                });
    }

    private void setLoading(boolean loading, boolean isLoadMore) {
        isLoading = loading;

        if (swipeRefresh != null && !isLoadMore) {
            swipeRefresh.setRefreshing(loading);
        }

        if (loading) {
            if (isLoadMore) {
                if (progressLoadMore != null) progressLoadMore.setVisibility(View.VISIBLE);
                if (progressOrders != null) progressOrders.setVisibility(View.GONE);
            } else {
                if (progressOrders != null) progressOrders.setVisibility(View.VISIBLE);
                if (progressLoadMore != null) progressLoadMore.setVisibility(View.GONE);
            }
        } else {
            if (progressOrders != null) progressOrders.setVisibility(View.GONE);
            if (progressLoadMore != null) progressLoadMore.setVisibility(View.GONE);
        }
    }


    private void openOrderDetail(OrderHistoryResponse.OrderItem item) {
        String json = new Gson().toJson(item);
        Intent intent = new Intent(requireActivity(), OrderDetailActivity.class);
        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, item.getOrderId());
        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_JSON, json);
        startActivity(intent);
    }

    private void openOrderDetailForFeedback(OrderHistoryResponse.OrderItem item) {
        String json = new Gson().toJson(item);
        Intent intent = new Intent(requireActivity(), OrderDetailActivity.class);
        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, item.getOrderId());
        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_JSON, json);
        intent.putExtra(OrderDetailActivity.EXTRA_SCROLL_TO_FEEDBACK, true);
        startActivity(intent);
    }

    private void showCancelReasonDialog(OrderHistoryResponse.OrderItem item) {
        if (item == null || item.getOrderId() == null || item.getOrderId().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Không tìm thấy ID đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        EditText input = new EditText(requireContext());
        input.setHint("Nhập lý do hủy");
        input.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout container = new LinearLayout(requireContext());
        container.setPadding(padding, padding / 2, padding, 0);
        container.addView(input);

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Lý do hủy đơn")
                .setView(container)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String reason = input.getText().toString().trim();
                    if (reason.isEmpty()) {
                        Toast.makeText(requireContext(), "Vui lòng nhập lý do hủy", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    cancelOrder(item.getOrderId(), reason);
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void cancelOrder(String orderId, String reason) {
        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClient.getInstance()
                .getApiService()
                .cancelOrder("Bearer " + token, orderId, new CancelOrderRequest(reason))
                .enqueue(new Callback<BaseResponse<OrderResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<OrderResponse>> call,
                                           @NonNull Response<BaseResponse<OrderResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(requireContext(), "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
                            currentPage = 0;
                            isLastPage = false;
                            loadOrders(0, pageSize, selectedStatus, false);
                            return;
                        }
                        String msg = "Hủy đơn thất bại";
                        if (response.body() != null && response.body().getMessage() != null) {
                            msg = response.body().getMessage();
                        }
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<OrderResponse>> call,
                                          @NonNull Throwable t) {
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private List<OrderHistoryResponse.OrderItem> filterOutCreated(List<OrderHistoryResponse.OrderItem> orders) {
        List<OrderHistoryResponse.OrderItem> filtered = new ArrayList<>();
        if (orders == null || orders.isEmpty()) {
            return filtered;
        }
        for (OrderHistoryResponse.OrderItem item : orders) {
            if (item == null) {
                continue;
            }
            String status = item.getStatus();
            if (status == null || status.trim().isEmpty()) {
                filtered.add(item);
                continue;
            }
            if (!"CREATED".equalsIgnoreCase(status.trim())) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to this fragment
        currentPage = 0;
        isLastPage = false;
        loadOrders(0, pageSize, selectedStatus, false);
    }
}
