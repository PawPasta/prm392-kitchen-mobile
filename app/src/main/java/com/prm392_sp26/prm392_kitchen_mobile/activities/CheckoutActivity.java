package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.adapters.CheckoutDishAdapter;
import com.prm392_sp26.prm392_kitchen_mobile.model.data.UserProfile;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.CurrencyFormatter;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {

    private PrefsManager prefsManager;
    private ProgressBar progressCheckout;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private TextView tvUserId;
    private TextView tvOrderNote;
    private TextView tvEmptyDishes;
    private TextView tvSubTotal;
    private TextView tvDiscount;
    private TextView tvTotal;
    private MaterialButton btnPlaceOrder;
    private Spinner spPromotion;
    private Spinner spPaymentMethod;

    private CheckoutDishAdapter dishAdapter;
    private OrderResponse currentOrder;

    private final String[] promotionOptions = new String[]{
            "Không áp dụng",
            "SAVE5 - Giảm 5%",
            "SAVE10 - Giảm 10%"
    };
    private final double[] promotionRates = new double[]{0.0d, 0.05d, 0.10d};
    private double selectedPromotionRate = 0.0d;
    private int pendingCalls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_checkout);

        View root = findViewById(R.id.checkoutRoot);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefsManager = PrefsManager.getInstance(this);

        progressCheckout = findViewById(R.id.progressCheckout);
        tvUserName = findViewById(R.id.tvCheckoutUserName);
        tvUserEmail = findViewById(R.id.tvCheckoutUserEmail);
        tvUserId = findViewById(R.id.tvCheckoutUserId);
        tvOrderNote = findViewById(R.id.tvCheckoutOrderNote);
        tvEmptyDishes = findViewById(R.id.tvCheckoutEmptyDishes);
        tvSubTotal = findViewById(R.id.tvCheckoutSubTotal);
        tvDiscount = findViewById(R.id.tvCheckoutDiscount);
        tvTotal = findViewById(R.id.tvCheckoutTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        spPromotion = findViewById(R.id.spPromotion);
        spPaymentMethod = findViewById(R.id.spPaymentMethod);
        RecyclerView rvDishes = findViewById(R.id.rvCheckoutDishes);

        findViewById(R.id.btnBackCheckout).setOnClickListener(v -> finish());

        dishAdapter = new CheckoutDishAdapter();
        rvDishes.setLayoutManager(new LinearLayoutManager(this));
        rvDishes.setAdapter(dishAdapter);

        setupPromotionSpinner();
        setupPaymentMethodSpinner();
        setupPlaceOrderButton();
        loadCheckoutData();
    }

    private void setupPromotionSpinner() {
        ArrayAdapter<String> promotionAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, promotionOptions);
        promotionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPromotion.setAdapter(promotionAdapter);
        spPromotion.setSelection(0, false);
        spPromotion.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= promotionRates.length) {
                    selectedPromotionRate = 0.0d;
                } else {
                    selectedPromotionRate = promotionRates[position];
                }
                updatePriceSummary();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedPromotionRate = 0.0d;
                updatePriceSummary();
            }
        });
    }

    private void setupPaymentMethodSpinner() {
        List<String> paymentMethods = new ArrayList<>();
        paymentMethods.add("Tiền mặt");
        paymentMethods.add("Momo");
        paymentMethods.add("VNPay");

        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, paymentMethods);
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPaymentMethod.setAdapter(paymentAdapter);
    }

    private void setupPlaceOrderButton() {
        btnPlaceOrder.setOnClickListener(v -> {
            if (currentOrder == null || currentOrder.getDishes() == null || currentOrder.getDishes().isEmpty()) {
                Toast.makeText(this, "Không có món để đặt", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Đặt hàng thành công", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void loadCheckoutData() {
        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu token. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setLoading(true);
        pendingCalls = 2;
        loadCurrentUserProfile(token);
        loadCurrentOrder(token);
    }

    private void loadCurrentUserProfile(String token) {
        ApiClient.getInstance()
                .getApiService()
                .getCurrentUserProfile("Bearer " + token)
                .enqueue(new Callback<BaseResponse<UserProfile>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<UserProfile>> call,
                                           @NonNull Response<BaseResponse<UserProfile>> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()
                                && response.body().getData() != null) {
                            bindUser(response.body().getData());
                        } else {
                            bindUser(null);
                        }
                        finishOneCall();
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<UserProfile>> call, @NonNull Throwable t) {
                        bindUser(null);
                        finishOneCall();
                    }
                });
    }

    private void loadCurrentOrder(String token) {
        ApiClient.getInstance()
                .getApiService()
                .getCurrentOrder("Bearer " + token)
                .enqueue(new Callback<BaseResponse<OrderResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<OrderResponse>> call,
                                           @NonNull Response<BaseResponse<OrderResponse>> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()
                                && response.body().getData() != null) {
                            bindOrder(response.body().getData());
                        } else {
                            bindOrder(null);
                        }
                        finishOneCall();
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<OrderResponse>> call, @NonNull Throwable t) {
                        bindOrder(null);
                        finishOneCall();
                    }
                });
    }

    private void bindUser(UserProfile profile) {
        if (profile == null) {
            tvUserName.setText("Khách hàng");
            tvUserEmail.setText("--");
            tvUserId.setText("ID: --");
            return;
        }

        String name = safeText(profile.getDisplayName());
        if (name.isEmpty()) {
            name = "Khách hàng";
        }
        tvUserName.setText(name);

        String email = safeText(profile.getEmail());
        tvUserEmail.setText(email.isEmpty() ? "--" : email);

        String userId = safeText(profile.getUserId());
        tvUserId.setText("ID: " + (userId.isEmpty() ? "--" : userId));
    }

    private void bindOrder(OrderResponse order) {
        currentOrder = order;
        if (order == null || order.getDishes() == null || order.getDishes().isEmpty()) {
            dishAdapter.setItems(new ArrayList<>());
            tvEmptyDishes.setVisibility(View.VISIBLE);
            tvOrderNote.setText("Ghi chú: --");
            btnPlaceOrder.setEnabled(false);
            updatePriceSummary();
            return;
        }

        dishAdapter.setItems(order.getDishes());
        tvEmptyDishes.setVisibility(View.GONE);
        btnPlaceOrder.setEnabled(true);

        String note = safeText(order.getNote());
        tvOrderNote.setText("Ghi chú: " + (note.isEmpty() ? "--" : note));
        updatePriceSummary();
    }

    private void updatePriceSummary() {
        if (currentOrder == null) {
            tvSubTotal.setText(CurrencyFormatter.formatVnd(0));
            tvDiscount.setText("-" + CurrencyFormatter.formatVnd(0));
            tvTotal.setText(CurrencyFormatter.formatVnd(0));
            return;
        }

        double subtotal = Math.max(0.0d, currentOrder.getTotalPrice());
        double serverDiscount = Math.max(0.0d, currentOrder.getDiscountAmount());
        double promotionDiscount = Math.max(0.0d, subtotal * selectedPromotionRate);
        double totalDiscount = serverDiscount + promotionDiscount;
        double total = Math.max(0.0d, subtotal - totalDiscount);

        tvSubTotal.setText(CurrencyFormatter.formatVnd(subtotal));
        tvDiscount.setText("-" + CurrencyFormatter.formatVnd(totalDiscount));
        tvTotal.setText(CurrencyFormatter.formatVnd(total));
    }

    private void finishOneCall() {
        pendingCalls--;
        if (pendingCalls <= 0) {
            setLoading(false);
        }
    }

    private void setLoading(boolean loading) {
        progressCheckout.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private String safeText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
