package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.prm392_sp26.prm392_kitchen_mobile.MainActivity;
import com.prm392_sp26.prm392_kitchen_mobile.R;

public class PaymentSuccessActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "extra_message";
    public static final String EXTRA_ORDER_ID = "extra_order_id";
    public static final String EXTRA_PAYMENT_ID = "extra_payment_id";
    public static final String EXTRA_PAYMENT_STATUS = "extra_payment_status";
    public static final String EXTRA_MOMO_TRANS_ID = "extra_momo_trans_id";
    public static final String EXTRA_RESULT_CODE = "extra_result_code";
    public static final String EXTRA_RESULT_MESSAGE = "extra_result_message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_success);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.paymentSuccessRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String message = safeText(intent.getStringExtra(EXTRA_MESSAGE));
        String orderId = safeText(intent.getStringExtra(EXTRA_ORDER_ID));
        String paymentId = safeText(intent.getStringExtra(EXTRA_PAYMENT_ID));
        String paymentStatus = safeText(intent.getStringExtra(EXTRA_PAYMENT_STATUS));
        String momoTransId = safeText(intent.getStringExtra(EXTRA_MOMO_TRANS_ID));
        int resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0);
        String resultMessage = safeText(intent.getStringExtra(EXTRA_RESULT_MESSAGE));

        ((TextView) findViewById(R.id.tvPaymentSuccessMessage))
                .setText(message.isEmpty() ? "Payment Successful" : message);
        ((TextView) findViewById(R.id.tvPaymentSuccessOrderId))
                .setText(orderId.isEmpty() ? "--" : orderId);
        ((TextView) findViewById(R.id.tvPaymentSuccessPaymentId))
                .setText(paymentId.isEmpty() ? "--" : paymentId);
        ((TextView) findViewById(R.id.tvPaymentSuccessPaymentStatus))
                .setText(paymentStatus.isEmpty() ? "--" : paymentStatus);
        ((TextView) findViewById(R.id.tvPaymentSuccessMomoTransId))
                .setText(momoTransId.isEmpty() ? "--" : momoTransId);
        ((TextView) findViewById(R.id.tvPaymentSuccessResultCode))
                .setText(String.valueOf(resultCode));
        ((TextView) findViewById(R.id.tvPaymentSuccessResultMessage))
                .setText(resultMessage.isEmpty() ? "--" : resultMessage);

        findViewById(R.id.btnBackHomeAfterPayment).setOnClickListener(v -> {
            Intent homeIntent = new Intent(this, MainActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(homeIntent);
            finish();
        });
    }

    private String safeText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
