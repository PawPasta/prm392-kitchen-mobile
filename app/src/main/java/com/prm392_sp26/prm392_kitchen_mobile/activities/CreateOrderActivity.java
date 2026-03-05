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
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnQuantityPlus != null) {
            btnQuantityPlus.setOnClickListener(v -> {
                if (quantity < 10) {
                    quantity++;
                    if (etQuantity != null) {
                        etQuantity.setText(String.valueOf(quantity));
                    }
                }
            });
        }

        if (btnQuantityMinus != null) {
            btnQuantityMinus.setOnClickListener(v -> {
                if (quantity > 1) {
                    quantity--;
                    if (etQuantity != null) {
                        etQuantity.setText(String.valueOf(quantity));
                    }
                }
            });
        }

        if (etQuantity != null) {
            etQuantity.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    updateQuantityFromEditText();
                }
            });
        }

        if (etPickupDateTime != null) {
            etPickupDateTime.setOnClickListener(v -> showDateTimePicker());
        }

        if (btnCreateOrder != null) {
            btnCreateOrder.setOnClickListener(v -> createOrder());
            btnCreateOrder.setEnabled(true);
        }
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
        // Đảm bảo button được enable
        if (btnCreateOrder != null) {
            btnCreateOrder.setEnabled(true);
        }

        updateQuantityFromEditText();

        // Validation: Kiểm tra số lượng
        if (quantity < 1 || quantity > 10) {
            Toast.makeText(this, "Số lượng phải từ 1 đến 10", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validation: Kiểm tra thời gian lấy
        if (selectedPickupDateTime == null || selectedPickupDateTime.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn thời gian lấy món", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validation: Kiểm tra dishId
        if (dishId <= 0) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID món ăn", Toast.LENGTH_SHORT).show();
            return;
        }

        String note = etNote != null ? etNote.getText().toString().trim() : "";

        CreateOrderFromDishRequest request = new CreateOrderFromDishRequest(
                dishId,
                quantity,
                selectedPickupDateTime,
                note
        );

        // Show loading state
        if (progressCreateOrder != null) {
            progressCreateOrder.setVisibility(android.view.View.VISIBLE);
        }
        if (btnCreateOrder != null) {
            btnCreateOrder.setEnabled(false);
        }

        String token = "Bearer " + PrefsManager.getInstance(this).getAccessToken();

        ApiClient.getInstance()
                .getApiService()
                .createOrderFromDish(token, request)
                .enqueue(new Callback<BaseResponse<OrderResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<OrderResponse>> call, Response<BaseResponse<OrderResponse>> response) {
                // Hide loading state
                if (progressCreateOrder != null) {
                    progressCreateOrder.setVisibility(android.view.View.GONE);
                }
                if (btnCreateOrder != null) {
                    btnCreateOrder.setEnabled(true);
                }

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(CreateOrderActivity.this, "Đặt món thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = "Lỗi tạo đơn hàng";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(CreateOrderActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<OrderResponse>> call, Throwable t) {
                // Hide loading state
                if (progressCreateOrder != null) {
                    progressCreateOrder.setVisibility(android.view.View.GONE);
                }
                if (btnCreateOrder != null) {
                    btnCreateOrder.setEnabled(true);
                }
                Toast.makeText(CreateOrderActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}






