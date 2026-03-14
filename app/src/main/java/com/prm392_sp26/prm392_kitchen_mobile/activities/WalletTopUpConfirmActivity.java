package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.util.CurrencyFormatter;

public class WalletTopUpConfirmActivity extends AppCompatActivity {

    public static final String EXTRA_API_MESSAGE = "extra_api_message";
    public static final String EXTRA_PAYMENT_METHOD_NAME = "extra_payment_method_name";
    public static final String EXTRA_PAYMENT_ID = "extra_payment_id";
    public static final String EXTRA_PAYMENT_STATUS = "extra_payment_status";
    public static final String EXTRA_AMOUNT = "extra_amount";
    public static final String EXTRA_PAYMENT_URL = "extra_payment_url";

    private String paymentUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wallet_top_up_confirm);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.walletTopUpRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.btnBackWalletTopUp).setOnClickListener(v -> finish());

        TextView tvMessage = findViewById(R.id.tvTopUpMessage);
        TextView tvPaymentMethod = findViewById(R.id.tvTopUpPaymentMethod);
        TextView tvPaymentId = findViewById(R.id.tvTopUpPaymentId);
        TextView tvPaymentStatus = findViewById(R.id.tvTopUpPaymentStatus);
        TextView tvAmount = findViewById(R.id.tvTopUpAmount);
        MaterialButton btnConfirm = findViewById(R.id.btnConfirmTopUp);

        Intent intent = getIntent();
        String apiMessage = safeText(intent.getStringExtra(EXTRA_API_MESSAGE));
        String paymentMethodName = safeText(intent.getStringExtra(EXTRA_PAYMENT_METHOD_NAME));
        String paymentId = safeText(intent.getStringExtra(EXTRA_PAYMENT_ID));
        String paymentStatus = safeText(intent.getStringExtra(EXTRA_PAYMENT_STATUS));
        double amount = intent.getDoubleExtra(EXTRA_AMOUNT, 0.0d);
        paymentUrl = safeText(intent.getStringExtra(EXTRA_PAYMENT_URL));

        tvMessage.setText(apiMessage.isEmpty() ? "Thông tin nạp ví" : apiMessage);
        tvPaymentMethod.setText("Phương thức: " + (paymentMethodName.isEmpty() ? "--" : paymentMethodName));
        tvPaymentId.setText("Payment ID: " + (paymentId.isEmpty() ? "--" : paymentId));
        tvPaymentStatus.setText("Trạng thái: " + (paymentStatus.isEmpty() ? "--" : paymentStatus));
        tvAmount.setText(CurrencyFormatter.formatVnd(amount));

        btnConfirm.setOnClickListener(v -> openPaymentUrl());
    }

    private void openPaymentUrl() {
        if (paymentUrl.isEmpty()) {
            Toast.makeText(this, "Không có đường dẫn thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở ứng dụng thanh toán", Toast.LENGTH_SHORT).show();
        }
    }

    private String safeText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
