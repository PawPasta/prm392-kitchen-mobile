package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.prm392_sp26.prm392_kitchen_mobile.MainActivity;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.MomoCallbackRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.MomoCallbackResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentCallbackActivity extends AppCompatActivity {

    private boolean callbackSubmitted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_callback);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.paymentCallbackRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (!callbackSubmitted) {
            handleDeepLink(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    private void handleDeepLink(Intent intent) {
        if (callbackSubmitted) {
            return;
        }
        Uri data = intent != null ? intent.getData() : null;
        if (data == null) {
            Toast.makeText(this, "Thiếu dữ liệu callback thanh toán", Toast.LENGTH_SHORT).show();
            navigateMain();
            return;
        }

        callbackSubmitted = true;
        MomoCallbackRequest request = new MomoCallbackRequest(
                safeParam(data, "partnerCode"),
                safeParam(data, "orderId"),
                safeParam(data, "requestId"),
                parseLong(data.getQueryParameter("amount"), 0L),
                safeParam(data, "orderInfo"),
                safeParam(data, "orderType"),
                safeParam(data, "transId"),
                parseInt(data.getQueryParameter("resultCode"), 0),
                safeParam(data, "message"),
                safeParam(data, "payType"),
                parseLong(data.getQueryParameter("responseTime"), 0L),
                safeParam(data, "extraData"),
                safeParam(data, "signature"));

        ApiClient.getInstance()
                .getApiService()
                .momoPaymentCallback(request)
                .enqueue(new Callback<BaseResponse<MomoCallbackResponse>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<BaseResponse<MomoCallbackResponse>> call,
                            @NonNull Response<BaseResponse<MomoCallbackResponse>> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()
                                && response.body().getData() != null) {
                            navigateSuccess(response.body().getMessage(), response.body().getData());
                            return;
                        }

                        String message = "Xác nhận thanh toán thất bại";
                        if (response.body() != null && response.body().getMessage() != null) {
                            message = response.body().getMessage();
                        }
                        Toast.makeText(PaymentCallbackActivity.this, message, Toast.LENGTH_LONG).show();
                        navigateMain();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<BaseResponse<MomoCallbackResponse>> call,
                            @NonNull Throwable t) {
                        Toast.makeText(PaymentCallbackActivity.this,
                                "Lỗi callback: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                        navigateMain();
                    }
                });
    }

    private void navigateSuccess(String apiMessage, MomoCallbackResponse data) {
        Intent intent = new Intent(this, PaymentSuccessActivity.class);
        intent.putExtra(PaymentSuccessActivity.EXTRA_MESSAGE, safeText(apiMessage));
        intent.putExtra(PaymentSuccessActivity.EXTRA_ORDER_ID, safeText(data.getOrderId()));
        intent.putExtra(PaymentSuccessActivity.EXTRA_PAYMENT_ID, safeText(data.getPaymentId()));
        intent.putExtra(PaymentSuccessActivity.EXTRA_PAYMENT_STATUS, safeText(data.getPaymentStatus()));
        intent.putExtra(PaymentSuccessActivity.EXTRA_MOMO_TRANS_ID, safeText(data.getMomoTransId()));
        intent.putExtra(PaymentSuccessActivity.EXTRA_RESULT_CODE, data.getResultCode());
        intent.putExtra(PaymentSuccessActivity.EXTRA_RESULT_MESSAGE, safeText(data.getResultMessage()));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void navigateMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private String safeParam(Uri uri, String key) {
        String value = uri.getQueryParameter(key);
        return value == null ? "" : value;
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private long parseLong(String value, long fallback) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return fallback;
            }
            String normalized = value.trim();
            if (normalized.contains(".")) {
                return Math.round(Double.parseDouble(normalized));
            }
            return Long.parseLong(normalized);
        } catch (Exception e) {
            return fallback;
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return fallback;
            }
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }
}
