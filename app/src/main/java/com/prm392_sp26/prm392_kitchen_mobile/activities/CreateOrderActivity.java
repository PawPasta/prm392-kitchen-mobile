package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.CreateOrderFromDishRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.DishDetailResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity để tạo đơn hàng từ một dish có sẵn
 */
public class CreateOrderActivity extends AppCompatActivity {

    private int dishId;
    private DishDetailResponse dishDetail;
    private int quantity = 1;
    private String selectedPickupDateTime;

    private TextView tvDishEmoji;
    private TextView tvDishName;
    private TextView tvDishPrice;
    private TextView tvDishCalories;
    private EditText etQuantity;
    private EditText etPickupDateTime;
    private EditText etNote;
    private Button btnCreateOrder;
    private Button btnQuantityMinus;
    private Button btnQuantityPlus;
    private ProgressBar progressCreateOrder;
    private ImageView btnBack;

    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_order);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.createOrderRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        getDishIdFromIntent();
        fetchDishDetail();
        setupListeners();
    }

    private void initializeViews() {
        tvDishEmoji = findViewById(R.id.tvDishEmoji);
        tvDishName = findViewById(R.id.tvDishName);
        tvDishPrice = findViewById(R.id.tvDishPrice);
        tvDishCalories = findViewById(R.id.tvDishCalories);
        etQuantity = findViewById(R.id.etQuantity);
        etPickupDateTime = findViewById(R.id.etPickupDateTime);
        etNote = findViewById(R.id.etNote);
        btnCreateOrder = findViewById(R.id.btnCreateOrder);
        btnQuantityMinus = findViewById(R.id.btnQuantityMinus);
        btnQuantityPlus = findViewById(R.id.btnQuantityPlus);
        progressCreateOrder = findViewById(R.id.progressCreateOrder);
        btnBack = findViewById(R.id.btnBack);
    }

    private void getDishIdFromIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            dishId = extras.getInt("dishId", 0);
        }
    }

    private void fetchDishDetail() {
        String token = "Bearer " + PrefsManager.getInstance(this).getAccessToken();

        ApiClient.getInstance()
                .getApiService()
                .getDishDetail(token, dishId)
                .enqueue(new Callback<BaseResponse<DishDetailResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<DishDetailResponse>> call, Response<BaseResponse<DishDetailResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dishDetail = response.body().getData();
                    displayDishDetail();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<DishDetailResponse>> call, Throwable t) {
                Toast.makeText(CreateOrderActivity.this, "Lỗi tải thông tin món ăn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayDishDetail() {
        if (dishDetail == null) return;

        tvDishEmoji.setText("🍽️");
        tvDishName.setText(dishDetail.getName());
        tvDishPrice.setText(String.format(Locale.US, "$%.2f", dishDetail.getPrice()));
        tvDishCalories.setText(String.format(Locale.US, "🔥 %.0f kcal", dishDetail.getCalories()));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnQuantityPlus.setOnClickListener(v -> {
            if (quantity < 10) {
                quantity++;
                etQuantity.setText(String.valueOf(quantity));
            }
        });

        btnQuantityMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                etQuantity.setText(String.valueOf(quantity));
            }
        });

        etQuantity.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                updateQuantityFromEditText();
            }
        });

        etPickupDateTime.setOnClickListener(v -> showDateTimePicker());

        btnCreateOrder.setOnClickListener(v -> createOrder());
    }

    private void updateQuantityFromEditText() {
        try {
            String text = etQuantity.getText().toString().trim();
            if (text.isEmpty()) {
                quantity = 1;
            } else {
                quantity = Integer.parseInt(text);
                if (quantity < 1) quantity = 1;
                if (quantity > 10) quantity = 10;
            }
            etQuantity.setText(String.valueOf(quantity));
        } catch (NumberFormatException e) {
            quantity = 1;
            etQuantity.setText("1");
        }
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            this,
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                calendar.set(Calendar.SECOND, 0);

                                selectedPickupDateTime = dateTimeFormat.format(calendar.getTime());
                                etPickupDateTime.setText(displayFormat.format(calendar.getTime()));
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    );
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void createOrder() {
        updateQuantityFromEditText();

        // Validation
        if (quantity < 1 || quantity > 10) {
            Toast.makeText(this, R.string.error_quantity_invalid, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPickupDateTime == null || selectedPickupDateTime.isEmpty()) {
            Toast.makeText(this, R.string.error_pickup_time_required, Toast.LENGTH_SHORT).show();
            return;
        }

        String note = etNote.getText().toString().trim();

        CreateOrderFromDishRequest request = new CreateOrderFromDishRequest(
                dishId,
                quantity,
                selectedPickupDateTime,
                note
        );

        progressCreateOrder.setVisibility(android.view.View.VISIBLE);
        btnCreateOrder.setEnabled(false);

        String token = "Bearer " + PrefsManager.getInstance(this).getAccessToken();

        ApiClient.getInstance()
                .getApiService()
                .createOrderFromDish(token, request)
                .enqueue(new Callback<BaseResponse<OrderResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<OrderResponse>> call, Response<BaseResponse<OrderResponse>> response) {
                progressCreateOrder.setVisibility(android.view.View.GONE);
                btnCreateOrder.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(CreateOrderActivity.this, R.string.order_created_success, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CreateOrderActivity.this, "Lỗi tạo đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<OrderResponse>> call, Throwable t) {
                progressCreateOrder.setVisibility(android.view.View.GONE);
                btnCreateOrder.setEnabled(true);
                Toast.makeText(CreateOrderActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}






