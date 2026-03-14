package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.WalletTopUpRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.PaymentMethodResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.WalletTopUpResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletTopUpActivity extends AppCompatActivity {

    private PrefsManager prefsManager;
    private EditText etAmount;
    private TextView tvMethodDescription;
    private LinearLayout methodContainer;
    private ProgressBar progressTopUp;
    private MaterialButton btnSubmit;
    private final List<PaymentMethodResponse> paymentMethods = new ArrayList<>();
    private PaymentMethodResponse selectedMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wallet_top_up);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.walletTopUpRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefsManager = PrefsManager.getInstance(this);
        etAmount = findViewById(R.id.etTopUpAmount);
        tvMethodDescription = findViewById(R.id.tvTopUpMethodDescription);
        methodContainer = findViewById(R.id.topUpMethodContainer);
        progressTopUp = findViewById(R.id.progressTopUp);
        btnSubmit = findViewById(R.id.btnSubmitTopUp);

        findViewById(R.id.btnBackWalletTopUp).setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> submitTopUp());

        loadPaymentMethods();
    }

    private void loadPaymentMethods() {
        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu token. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);
        ApiClient.getInstance()
                .getApiService()
                .getPaymentMethods("Bearer " + token)
                .enqueue(new Callback<BaseResponse<List<PaymentMethodResponse>>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<List<PaymentMethodResponse>>> call,
                                           @NonNull Response<BaseResponse<List<PaymentMethodResponse>>> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            bindPaymentMethods(response.body().getData());
                            return;
                        }
                        Toast.makeText(WalletTopUpActivity.this,
                                "Không thể tải phương thức thanh toán.",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<List<PaymentMethodResponse>>> call,
                                          @NonNull Throwable t) {
                        setLoading(false);
                        Toast.makeText(WalletTopUpActivity.this,
                                "Lỗi mạng khi tải phương thức.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void bindPaymentMethods(List<PaymentMethodResponse> methods) {
        paymentMethods.clear();
        methodContainer.removeAllViews();
        selectedMethod = null;

        if (methods != null) {
            for (PaymentMethodResponse method : methods) {
                if (method == null || !method.isActive()) {
                    continue;
                }
                if (isWalletMethod(method)) {
                    continue;
                }
                paymentMethods.add(method);
            }
        }

        if (paymentMethods.isEmpty()) {
            tvMethodDescription.setText("Chưa có phương thức thanh toán khả dụng.");
            return;
        }

        for (PaymentMethodResponse method : paymentMethods) {
            TextView chip = new TextView(this);
            chip.setText(buildPaymentMethodLabel(method));
            chip.setTag(method);
            chip.setTextSize(12f);
            chip.setTextColor(getResources().getColor(R.color.colorTextPrimary));
            int paddingH = (int) (12 * getResources().getDisplayMetrics().density);
            int paddingV = (int) (6 * getResources().getDisplayMetrics().density);
            chip.setPadding(paddingH, paddingV, paddingH, paddingV);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd((int) (8 * getResources().getDisplayMetrics().density));
            chip.setLayoutParams(params);

            chip.setBackgroundResource(R.drawable.bg_chip);
            chip.setOnClickListener(v -> {
                selectedMethod = (PaymentMethodResponse) v.getTag();
                updateChipStyles();
                updateMethodDescription(selectedMethod);
            });

            methodContainer.addView(chip);
        }

        if (!paymentMethods.isEmpty()) {
            selectedMethod = paymentMethods.get(0);
        }
        updateMethodDescription(selectedMethod);
        updateChipStyles();
    }

    private void updateChipStyles() {
        for (int i = 0; i < methodContainer.getChildCount(); i++) {
            View view = methodContainer.getChildAt(i);
            if (!(view instanceof TextView)) {
                continue;
            }
            TextView chip = (TextView) view;
            PaymentMethodResponse method = (PaymentMethodResponse) chip.getTag();
            boolean selected = selectedMethod != null
                    && method.getPaymentMethodId() == selectedMethod.getPaymentMethodId();
            chip.setBackgroundResource(selected ? R.drawable.bg_chip_selected : R.drawable.bg_chip);
        }
    }

    private void updateMethodDescription(PaymentMethodResponse method) {
        if (method == null) {
            tvMethodDescription.setText("Vui lòng chọn phương thức.");
            return;
        }
        String description = method.getDescription();
        if (description == null || description.trim().isEmpty()) {
            tvMethodDescription.setText("Thanh toán qua " + buildPaymentMethodLabel(method) + ".");
            return;
        }
        tvMethodDescription.setText(description.trim());
    }

    private void submitTopUp() {
        String amountText = etAmount.getText().toString().trim();
        if (amountText.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền.", Toast.LENGTH_SHORT).show();
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (amount <= 0) {
            Toast.makeText(this, "Số tiền phải lớn hơn 0.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedMethod == null) {
            Toast.makeText(this, "Vui lòng chọn phương thức thanh toán.", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu token. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);
        ApiClient.getInstance()
                .getApiService()
                .topUpWallet("Bearer " + token,
                        new WalletTopUpRequest(amount, selectedMethod.getPaymentMethodId()))
                .enqueue(new Callback<BaseResponse<WalletTopUpResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<WalletTopUpResponse>> call,
                                           @NonNull Response<BaseResponse<WalletTopUpResponse>> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            BaseResponse<WalletTopUpResponse> baseResponse = response.body();
                            if (baseResponse.isSuccess() && baseResponse.getData() != null) {
                                openTopUpConfirm(baseResponse.getMessage(), baseResponse.getData());
                                return;
                            }
                            Toast.makeText(WalletTopUpActivity.this,
                                    baseResponse.getMessage() != null ? baseResponse.getMessage()
                                            : "Không thể tạo thanh toán.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        Toast.makeText(WalletTopUpActivity.this,
                                "Server error: " + response.code(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<WalletTopUpResponse>> call,
                                          @NonNull Throwable t) {
                        setLoading(false);
                        Toast.makeText(WalletTopUpActivity.this,
                                "Lỗi mạng khi tạo thanh toán.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void openTopUpConfirm(String message, WalletTopUpResponse data) {
        android.content.Intent intent = new android.content.Intent(this, WalletTopUpConfirmActivity.class);
        intent.putExtra(WalletTopUpConfirmActivity.EXTRA_API_MESSAGE, message == null ? "" : message);
        intent.putExtra(WalletTopUpConfirmActivity.EXTRA_PAYMENT_ID, data.getPaymentId());
        intent.putExtra(WalletTopUpConfirmActivity.EXTRA_PAYMENT_METHOD_NAME, data.getPaymentMethodName());
        intent.putExtra(WalletTopUpConfirmActivity.EXTRA_PAYMENT_STATUS, data.getPaymentStatus());
        intent.putExtra(WalletTopUpConfirmActivity.EXTRA_AMOUNT, data.getAmount());
        intent.putExtra(WalletTopUpConfirmActivity.EXTRA_PAYMENT_URL, data.getPaymentUrl());
        startActivity(intent);
    }

    private void setLoading(boolean loading) {
        progressTopUp.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!loading);
    }

    private boolean isWalletMethod(PaymentMethodResponse method) {
        String name = method.getName();
        return name != null && name.trim().equalsIgnoreCase("WALLET");
    }

    private String buildPaymentMethodLabel(PaymentMethodResponse method) {
        String name = method.getName();
        if (name == null || name.trim().isEmpty()) {
            return "Payment";
        }
        return name.trim();
    }
}
