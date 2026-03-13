package com.prm392_sp26.prm392_kitchen_mobile.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import com.prm392_sp26.prm392_kitchen_mobile.model.request.CancelOrderRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderHistoryResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartFragment extends Fragment {

    private static final int SORT_NEWEST = 0;
    private static final int SORT_OLDEST = 1;

    private SwipeRefreshLayout swipeRefreshCart;
    private ProgressBar progressCart;
    private LinearLayout layoutCartEmpty;
    private RecyclerView recyclerCart;
    private Spinner spinnerSortTime;

    private OrderHistoryAdapter adapter;
    private PrefsManager prefsManager;
    private final List<OrderHistoryResponse.OrderItem> cachedOrders = new ArrayList<>();
    private int currentSort = SORT_NEWEST;

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
        spinnerSortTime = view.findViewById(R.id.spinnerSortTime);

        adapter = new OrderHistoryAdapter();
        recyclerCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerCart.setAdapter(adapter);
        adapter.setOnItemClickListener(this::openOrderDetail);
        adapter.setOnCancelClickListener(this::showCancelReasonDialog);

        swipeRefreshCart.setOnRefreshListener(this::loadCartOrders);
        setupSortSpinner();

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
        loadCartOrdersPage("Bearer " + token, 0, new ArrayList<>());
    }

    private void loadCartOrdersPage(String bearerToken, int page, List<OrderHistoryResponse.OrderItem> accumulator) {
        ApiClient.getInstance().getApiService()
                .getOrderHistory(bearerToken, page, 50, "CREATED")
                .enqueue(new Callback<BaseResponse<OrderHistoryResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<OrderHistoryResponse>> call,
                                           @NonNull Response<BaseResponse<OrderHistoryResponse>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()
                                && response.body().getData() != null
                                && response.body().getData().getOrders() != null) {
                            OrderHistoryResponse.OrderPage orderPage = response.body().getData().getOrders();
                            List<OrderHistoryResponse.OrderItem> orders = orderPage.getContent();
                            if (orders != null && !orders.isEmpty()) {
                                accumulator.addAll(orders);
                            }
                            if (orderPage.isLast()) {
                                finalizeCartOrders(accumulator);
                                return;
                            }
                            loadCartOrdersPage(bearerToken, page + 1, accumulator);
                            return;
                        }
                        progressCart.setVisibility(View.GONE);
                        swipeRefreshCart.setRefreshing(false);
                        showEmpty();
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

    private void finalizeCartOrders(List<OrderHistoryResponse.OrderItem> orders) {
        progressCart.setVisibility(View.GONE);
        swipeRefreshCart.setRefreshing(false);
        if (orders == null || orders.isEmpty()) {
            cachedOrders.clear();
            showEmpty();
            return;
        }
        cachedOrders.clear();
        cachedOrders.addAll(orders);
        applySortAndRender();
    }

    private void showEmpty() {
        cachedOrders.clear();
        adapter.setItems(new ArrayList<>());
        recyclerCart.setVisibility(View.GONE);
        layoutCartEmpty.setVisibility(View.VISIBLE);
    }

    private void setupSortSpinner() {
        if (spinnerSortTime == null) {
            return;
        }
        List<String> options = new ArrayList<>();
        options.add("Mới nhất");
        options.add("Cũ nhất");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortTime.setAdapter(adapter);
        spinnerSortTime.setSelection(currentSort, false);
        spinnerSortTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (currentSort != position) {
                    currentSort = position;
                    applySortAndRender();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void applySortAndRender() {
        List<OrderHistoryResponse.OrderItem> sorted = new ArrayList<>(cachedOrders);
        Collections.sort(sorted, (left, right) -> {
            long leftTime = getCreatedAtMillis(left);
            long rightTime = getCreatedAtMillis(right);
            if (currentSort == SORT_NEWEST) {
                return Long.compare(rightTime, leftTime);
            }
            return Long.compare(leftTime, rightTime);
        });
        if (sorted.isEmpty()) {
            showEmpty();
            return;
        }
        adapter.setItems(sorted);
        recyclerCart.setVisibility(View.VISIBLE);
        layoutCartEmpty.setVisibility(View.GONE);
    }

    private long getCreatedAtMillis(OrderHistoryResponse.OrderItem item) {
        if (item == null) {
            return 0L;
        }
        String createdAt = item.getCreatedAt();
        if (createdAt == null || createdAt.trim().isEmpty()) {
            return 0L;
        }
        Date parsed = parseIsoDate(createdAt);
        return parsed == null ? 0L : parsed.getTime();
    }

    private Date parseIsoDate(String input) {
        String value = input.trim();
        String[] patterns = new String[] {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                return sdf.parse(value);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    private void openOrderDetail(OrderHistoryResponse.OrderItem item) {
        String json = new Gson().toJson(item);
        Intent intent = new Intent(requireActivity(), OrderDetailActivity.class);
        intent.putExtra("order_json", json);
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
                            loadCartOrders();
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
}
