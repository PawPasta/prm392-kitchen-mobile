package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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
import com.prm392_sp26.prm392_kitchen_mobile.model.response.PaymentMethodResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.PromotionResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.shared.PageResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.CurrencyFormatter;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private ArrayAdapter<String> promotionAdapter;
    private ArrayAdapter<String> paymentMethodAdapter;
    private final List<PromotionResponse> promotions = new ArrayList<>();
    private final List<String> promotionLabels = new ArrayList<>();
    private final List<PaymentMethodResponse> paymentMethods = new ArrayList<>();
    private final List<String> paymentMethodLabels = new ArrayList<>();
    private PromotionResponse selectedPromotion;
    private PaymentMethodResponse selectedPaymentMethod;
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
        promotionLabels.clear();
        promotionLabels.add("Không áp dụng");
        promotionAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, promotionLabels);
        promotionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPromotion.setAdapter(promotionAdapter);
        spPromotion.setSelection(0, false);
        spPromotion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= 0 || position - 1 >= promotions.size()) {
                    selectedPromotion = null;
                } else {
                    selectedPromotion = promotions.get(position - 1);
                }
                updatePriceSummary();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedPromotion = null;
                updatePriceSummary();
            }
        });
    }

    private void setupPaymentMethodSpinner() {
        paymentMethodLabels.clear();
        paymentMethodLabels.add("Đang tải...");
        paymentMethodAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, paymentMethodLabels);
        paymentMethodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPaymentMethod.setAdapter(paymentMethodAdapter);
        spPaymentMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= paymentMethods.size()) {
                    selectedPaymentMethod = null;
                } else {
                    selectedPaymentMethod = paymentMethods.get(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedPaymentMethod = null;
            }
        });
    }

    private void setupPlaceOrderButton() {
        btnPlaceOrder.setOnClickListener(v -> {
            if (currentOrder == null || currentOrder.getDishes() == null || currentOrder.getDishes().isEmpty()) {
                Toast.makeText(this, "Không có món để đặt", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedPaymentMethod == null) {
                Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
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
        pendingCalls = 4;
        loadCurrentUserProfile(token);
        loadCurrentOrder(token);
        loadPromotions(token);
        loadPaymentMethods(token);
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

    private void loadPromotions(String token) {
        ApiClient.getInstance()
                .getApiService()
                .getPromotions("Bearer " + token, 0, 10)
                .enqueue(new Callback<BaseResponse<PageResponse<PromotionResponse>>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<BaseResponse<PageResponse<PromotionResponse>>> call,
                            @NonNull Response<BaseResponse<PageResponse<PromotionResponse>>> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()
                                && response.body().getData() != null
                                && response.body().getData().getContent() != null) {
                            bindPromotions(response.body().getData().getContent());
                        } else {
                            bindPromotions(new ArrayList<>());
                        }
                        finishOneCall();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<BaseResponse<PageResponse<PromotionResponse>>> call,
                            @NonNull Throwable t) {
                        bindPromotions(new ArrayList<>());
                        finishOneCall();
                    }
                });
    }

    private void loadPaymentMethods(String token) {
        ApiClient.getInstance()
                .getApiService()
                .getPaymentMethods("Bearer " + token)
                .enqueue(new Callback<BaseResponse<List<PaymentMethodResponse>>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<BaseResponse<List<PaymentMethodResponse>>> call,
                            @NonNull Response<BaseResponse<List<PaymentMethodResponse>>> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()
                                && response.body().getData() != null) {
                            bindPaymentMethods(response.body().getData());
                        } else {
                            bindPaymentMethods(new ArrayList<>());
                        }
                        finishOneCall();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<BaseResponse<List<PaymentMethodResponse>>> call,
                            @NonNull Throwable t) {
                        bindPaymentMethods(new ArrayList<>());
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

    private void bindPromotions(List<PromotionResponse> rawPromotions) {
        promotions.clear();
        if (rawPromotions != null) {
            for (PromotionResponse promotion : rawPromotions) {
                if (promotion == null) {
                    continue;
                }
                String status = safeText(promotion.getStatus());
                if (!"ACTIVE".equalsIgnoreCase(status)) {
                    continue;
                }
                if (promotion.getQuantity() <= 0) {
                    continue;
                }
                promotions.add(promotion);
            }
        }

        promotionLabels.clear();
        promotionLabels.add("Không áp dụng");
        for (PromotionResponse promotion : promotions) {
            promotionLabels.add(buildPromotionLabel(promotion));
        }
        promotionAdapter.notifyDataSetChanged();
        spPromotion.setSelection(0, false);
        selectedPromotion = null;
        updatePriceSummary();
    }

    private String buildPromotionLabel(PromotionResponse promotion) {
        String code = safeText(promotion.getCode());
        String name = safeText(promotion.getName());
        String discountType = safeText(promotion.getDiscountType());
        double value = Math.max(0.0d, promotion.getDiscountValue());

        String discountText;
        if ("PERCENT".equalsIgnoreCase(discountType)) {
            discountText = formatNumber(value) + "%";
        } else {
            discountText = CurrencyFormatter.formatVnd(value);
        }

        if (!code.isEmpty() && !name.isEmpty()) {
            return code + " - " + name + " (" + discountText + ")";
        }
        if (!name.isEmpty()) {
            return name + " (" + discountText + ")";
        }
        return discountText;
    }

    private void bindPaymentMethods(List<PaymentMethodResponse> rawPaymentMethods) {
        paymentMethods.clear();
        if (rawPaymentMethods != null) {
            for (PaymentMethodResponse method : rawPaymentMethods) {
                if (method != null && method.isActive()) {
                    paymentMethods.add(method);
                }
            }
        }

        paymentMethodLabels.clear();
        if (paymentMethods.isEmpty()) {
            paymentMethodLabels.add("Không có phương thức thanh toán");
            selectedPaymentMethod = null;
            btnPlaceOrder.setEnabled(false);
        } else {
            for (PaymentMethodResponse method : paymentMethods) {
                paymentMethodLabels.add(buildPaymentMethodLabel(method));
            }
            selectedPaymentMethod = paymentMethods.get(0);
            btnPlaceOrder.setEnabled(currentOrder != null
                    && currentOrder.getDishes() != null
                    && !currentOrder.getDishes().isEmpty());
        }
        paymentMethodAdapter.notifyDataSetChanged();
        spPaymentMethod.setSelection(0, false);
    }

    private String buildPaymentMethodLabel(PaymentMethodResponse method) {
        String name = safeText(method.getName());
        if (name.isEmpty()) {
            name = "Payment";
        }

        if ("WALLET".equalsIgnoreCase(name) && method.getWalletBalance() != null) {
            return name + " (" + CurrencyFormatter.formatVnd(method.getWalletBalance()) + ")";
        }
        return name;
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
        btnPlaceOrder.setEnabled(hasAvailablePaymentMethod());

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
        double promotionDiscount = calculatePromotionDiscount(subtotal);
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

    private double calculatePromotionDiscount(double subtotal) {
        if (selectedPromotion == null || subtotal <= 0) {
            return 0.0d;
        }

        String discountType = safeText(selectedPromotion.getDiscountType());
        double discountValue = Math.max(0.0d, selectedPromotion.getDiscountValue());
        double discount;
        if ("PERCENT".equalsIgnoreCase(discountType)) {
            discount = subtotal * (discountValue / 100.0d);
        } else {
            discount = discountValue;
        }
        return Math.min(discount, subtotal);
    }

    private String formatNumber(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001d) {
            return String.format(Locale.getDefault(), "%d", (long) Math.rint(value));
        }
        return String.format(Locale.getDefault(), "%.1f", value);
    }

    private boolean hasAvailablePaymentMethod() {
        return selectedPaymentMethod != null || !paymentMethods.isEmpty();
    }
}
