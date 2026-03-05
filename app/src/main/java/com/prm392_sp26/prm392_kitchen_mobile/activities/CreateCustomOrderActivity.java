package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.adapters.SelectableItemAdapter;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.CreateCustomOrderRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.DishStepResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.ItemResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.shared.PageResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity để tạo đơn hàng từ custom dish builder
 */
public class CreateCustomOrderActivity extends AppCompatActivity {

    private static final String TAG = "CreateCustomOrderActivity";

    // Views
    private ImageView btnBack;
    private Spinner spinnerSteps;
    private RecyclerView rvItems;
    private EditText etQuantity, etPickupDateTime, etNote;
    private MaterialButton btnQuantityPlus, btnQuantityMinus, btnCreateOrder;
    private ProgressBar progressCreateOrder;

    // Data
    private int quantity = 1;
    private String selectedPickupDateTime = "";
    private List<DishStepResponse> steps = new ArrayList<>();
    private DishStepResponse selectedStep = null;
    private SelectableItemAdapter itemAdapter;
    private Map<Integer, List<ItemResponse>> stepItemsMap = new HashMap<>();
    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_custom_order);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.customOrderRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefsManager = PrefsManager.getInstance(this);
        initializeViews();
        setupListeners();
        loadStepsAndItems();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        spinnerSteps = findViewById(R.id.spinnerSteps);
        rvItems = findViewById(R.id.rvItems);
        etQuantity = findViewById(R.id.etQuantity);
        etPickupDateTime = findViewById(R.id.etPickupDateTime);
        etNote = findViewById(R.id.etNote);
        btnQuantityPlus = findViewById(R.id.btnQuantityPlus);
        btnQuantityMinus = findViewById(R.id.btnQuantityMinus);
        btnCreateOrder = findViewById(R.id.btnCreateOrder);
        progressCreateOrder = findViewById(R.id.progressCreateOrder);

        rvItems.setLayoutManager(new LinearLayoutManager(this));
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

        if (spinnerSteps != null) {
            spinnerSteps.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedStep = steps.get(position);
                    displayItemsForStep(selectedStep.getStepId());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        if (btnCreateOrder != null) {
            btnCreateOrder.setOnClickListener(v -> createOrder());
            btnCreateOrder.setEnabled(true);
        }
    }

    private void loadStepsAndItems() {
        String token = "Bearer " + prefsManager.getAccessToken();

        // Lấy tất cả items (bao gồm stepName = nhóm thành phần: carb, protein, sauce...)
        ApiClient.getInstance()
                .getApiService()
                .getAllItems(token, 0, 100)
                .enqueue(new Callback<BaseResponse<PageResponse<ItemResponse>>>() {
            @Override
            public void onResponse(Call<BaseResponse<PageResponse<ItemResponse>>> call, Response<BaseResponse<PageResponse<ItemResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<ItemResponse> allItems = response.body().getData().getContent();
                    if (allItems != null && !allItems.isEmpty()) {
                        // Nhóm items theo stepName (carb, protein, sauce, etc.)
                        Map<String, List<ItemResponse>> groupedItems = new HashMap<>();
                        Map<String, Integer> stepNameToId = new HashMap<>();

                        for (ItemResponse item : allItems) {
                            String stepName = item.getStepName();
                            int stepId = item.getStepId();

                            if (stepName != null && !stepName.isEmpty()) {
                                if (!groupedItems.containsKey(stepName)) {
                                    groupedItems.put(stepName, new ArrayList<>());
                                    stepNameToId.put(stepName, stepId);
                                }
                                groupedItems.get(stepName).add(item);
                            }
                        }

                        // Tạo steps từ groupedItems
                        steps.clear();
                        for (Map.Entry<String, Integer> entry : stepNameToId.entrySet()) {
                            String stepName = entry.getKey();
                            int stepId = entry.getValue();

                            steps.add(new DishStepResponse() {
                                @Override
                                public int getStepId() { return stepId; }
                                @Override
                                public String getStepName() { return stepName; }
                            });

                            // Lưu items cho step này
                            stepItemsMap.put(stepId, groupedItems.get(stepName));
                        }

                        // Update spinner
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(CreateCustomOrderActivity.this, android.R.layout.simple_spinner_item);
                        for (DishStepResponse step : steps) {
                            adapter.add(step.getStepName());
                        }
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        if (spinnerSteps != null) {
                            spinnerSteps.setAdapter(adapter);
                        }

                        // Display items for first step
                        if (!steps.isEmpty()) {
                            selectedStep = steps.get(0);
                            displayItemsForStep(selectedStep.getStepId());
                        }
                    } else {
                        Toast.makeText(CreateCustomOrderActivity.this, "Không có nguyên liệu nào", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CreateCustomOrderActivity.this, "Lỗi tải danh sách nguyên liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<PageResponse<ItemResponse>>> call, Throwable t) {
                Toast.makeText(CreateCustomOrderActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadItemsForStep(int stepId) {
        String token = "Bearer " + prefsManager.getAccessToken();

        // API để lấy items cho step
        ApiClient.getInstance()
                .getApiService()
                .getItemsByStep(token, stepId, 0, 50)
                .enqueue(new Callback<BaseResponse<PageResponse<ItemResponse>>>() {
            @Override
            public void onResponse(Call<BaseResponse<PageResponse<ItemResponse>>> call, Response<BaseResponse<PageResponse<ItemResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<ItemResponse> items = response.body().getData().getContent();
                    if (items != null) {
                        stepItemsMap.put(stepId, items);
                        displayItemsForStep(stepId);
                    }
                } else {
                    Toast.makeText(CreateCustomOrderActivity.this, "Không có nguyên liệu cho bước này", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<PageResponse<ItemResponse>>> call, Throwable t) {
                Toast.makeText(CreateCustomOrderActivity.this, "Lỗi tải nguyên liệu: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayItemsForStep(int stepId) {
        List<ItemResponse> items = stepItemsMap.getOrDefault(stepId, new ArrayList<>());
        itemAdapter = new SelectableItemAdapter(items);
        if (rvItems != null) {
            rvItems.setAdapter(itemAdapter);
        }
    }

    private void updateQuantityFromEditText() {
        try {
            String qtyStr = etQuantity != null ? etQuantity.getText().toString() : "1";
            quantity = Integer.parseInt(qtyStr);
            if (quantity < 1) quantity = 1;
            if (quantity > 10) quantity = 10;
        } catch (NumberFormatException e) {
            quantity = 1;
            if (etQuantity != null) {
                etQuantity.setText("1");
            }
        }
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                            (view1, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                                selectedPickupDateTime = sdf.format(calendar.getTime());
                                if (etPickupDateTime != null) {
                                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
                                    etPickupDateTime.setText(displayFormat.format(calendar.getTime()));
                                }
                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

                    timePickerDialog.show();
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void createOrder() {
        if (btnCreateOrder != null) {
            btnCreateOrder.setEnabled(true);
        }

        updateQuantityFromEditText();

        // Validation
        if (quantity < 1 || quantity > 10) {
            Toast.makeText(this, "Số lượng phải từ 1 đến 10", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPickupDateTime == null || selectedPickupDateTime.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn thời gian lấy món", Toast.LENGTH_SHORT).show();
            return;
        }

        if (itemAdapter == null || itemAdapter.getSelectedItemIds().isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một nguyên liệu", Toast.LENGTH_SHORT).show();
            return;
        }

        String note = etNote != null ? etNote.getText().toString().trim() : "";

        // Build custom dish request
        List<CreateCustomOrderRequest.Step> stepsList = new ArrayList<>();

        // Collect selected items from adapter
        Map<Integer, Double> quantities = itemAdapter.getSelectedQuantities();
        Map<Integer, String> notes = itemAdapter.getSelectedNotes();
        List<Integer> selectedItemIds = itemAdapter.getSelectedItemIds();

        // Create step with selected items
        List<CreateCustomOrderRequest.Item> itemsList = new ArrayList<>();
        for (Integer itemId : selectedItemIds) {
            double qty = quantities.getOrDefault(itemId, 1.0);
            String itemNote = notes.getOrDefault(itemId, "");
            itemsList.add(new CreateCustomOrderRequest.Item(itemId, qty, itemNote));
        }

        CreateCustomOrderRequest.Step step = new CreateCustomOrderRequest.Step(
                selectedStep != null ? selectedStep.getStepId() : 1,
                selectedItemIds,
                itemsList
        );
        stepsList.add(step);

        // Create custom dish
        CreateCustomOrderRequest.CustomDish customDish = new CreateCustomOrderRequest.CustomDish(
                "Custom Dish",
                "Tùy chỉnh bởi người dùng",
                "",
                stepsList
        );

        CreateCustomOrderRequest request = new CreateCustomOrderRequest(
                quantity,
                selectedPickupDateTime,
                note,
                customDish
        );

        // Show loading
        if (progressCreateOrder != null) {
            progressCreateOrder.setVisibility(View.VISIBLE);
        }
        if (btnCreateOrder != null) {
            btnCreateOrder.setEnabled(false);
        }

        String token = "Bearer " + prefsManager.getAccessToken();

        ApiClient.getInstance()
                .getApiService()
                .createCustomOrder(token, request)
                .enqueue(new Callback<BaseResponse<OrderResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<OrderResponse>> call, Response<BaseResponse<OrderResponse>> response) {
                if (progressCreateOrder != null) {
                    progressCreateOrder.setVisibility(View.GONE);
                }
                if (btnCreateOrder != null) {
                    btnCreateOrder.setEnabled(true);
                }

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(CreateCustomOrderActivity.this, "Đặt món custom thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = "Lỗi tạo đơn hàng";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(CreateCustomOrderActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<OrderResponse>> call, Throwable t) {
                if (progressCreateOrder != null) {
                    progressCreateOrder.setVisibility(View.GONE);
                }
                if (btnCreateOrder != null) {
                    btnCreateOrder.setEnabled(true);
                }
                Toast.makeText(CreateCustomOrderActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

