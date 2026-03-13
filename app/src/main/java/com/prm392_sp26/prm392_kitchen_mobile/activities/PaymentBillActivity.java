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

public class PaymentBillActivity extends AppCompatActivity {

    public static final String EXTRA_API_MESSAGE = "extra_api_message";
    public static final String EXTRA_ORDER_ID = "extra_order_id";
    public static final String EXTRA_PAYMENT_METHOD_NAME = "extra_payment_method_name";
    public static final String EXTRA_PAYMENT_ID = "extra_payment_id";
    public static final String EXTRA_PAYMENT_STATUS = "extra_payment_status";
    public static final String EXTRA_TOTAL_PRICE = "extra_total_price";
    public static final String EXTRA_DISCOUNT_AMOUNT = "extra_discount_amount";
    public static final String EXTRA_FINAL_AMOUNT = "extra_final_amount";
    public static final String EXTRA_PAYMENT_URL = "extra_payment_url";

    private String paymentUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_bill);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.paymentBillRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.btnBackPaymentBill).setOnClickListener(v -> finish());

        TextView tvMessage = findViewById(R.id.tvPaymentBillMessage);
        TextView tvOrderId = findViewById(R.id.tvPaymentBillOrderId);
        TextView tvPaymentMethod = findViewById(R.id.tvPaymentBillPaymentMethod);
        TextView tvPaymentId = findViewById(R.id.tvPaymentBillPaymentId);
        TextView tvPaymentStatus = findViewById(R.id.tvPaymentBillPaymentStatus);
        TextView tvTotalPrice = findViewById(R.id.tvPaymentBillTotalPrice);
        TextView tvDiscount = findViewById(R.id.tvPaymentBillDiscount);
        TextView tvFinalAmount = findViewById(R.id.tvPaymentBillFinalAmount);
        MaterialButton btnConfirm = findViewById(R.id.btnConfirmPayment);

        Intent intent = getIntent();
        String apiMessage = safeText(intent.getStringExtra(EXTRA_API_MESSAGE));
        String orderId = safeText(intent.getStringExtra(EXTRA_ORDER_ID));
        String paymentMethodName = safeText(intent.getStringExtra(EXTRA_PAYMENT_METHOD_NAME));
        String paymentId = safeText(intent.getStringExtra(EXTRA_PAYMENT_ID));
        String paymentStatus = safeText(intent.getStringExtra(EXTRA_PAYMENT_STATUS));
        double totalPrice = intent.getDoubleExtra(EXTRA_TOTAL_PRICE, 0.0d);
        double discountAmount = intent.getDoubleExtra(EXTRA_DISCOUNT_AMOUNT, 0.0d);
        double finalAmount = intent.getDoubleExtra(EXTRA_FINAL_AMOUNT, 0.0d);
        paymentUrl = safeText(intent.getStringExtra(EXTRA_PAYMENT_URL));

        tvMessage.setText(apiMessage.isEmpty() ? "Thông tin thanh toán" : apiMessage);
        tvOrderId.setText("Order ID: " + (orderId.isEmpty() ? "--" : orderId));
        tvPaymentMethod.setText("Phương thức: " + (paymentMethodName.isEmpty() ? "--" : paymentMethodName));
        tvPaymentId.setText("Payment ID: " + (paymentId.isEmpty() ? "--" : paymentId));
        tvPaymentStatus.setText("Trạng thái thanh toán: " + (paymentStatus.isEmpty() ? "--" : paymentStatus));
        tvTotalPrice.setText(CurrencyFormatter.formatVnd(totalPrice));
        tvDiscount.setText("-" + CurrencyFormatter.formatVnd(discountAmount));
        tvFinalAmount.setText(CurrencyFormatter.formatVnd(finalAmount));

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
