package com.prm392_sp26.prm392_kitchen_mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.adapters.ItemGroupAdapter;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.CreateCustomOrderRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.ItemResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.shared.PageResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment để tạo đơn hàng custom
 * Người dùng có thể chọn các items theo steps (carb, protein, sauce, v.v.)
 */
public class CreateCustomOrderFragment extends Fragment {

    private RecyclerView recyclerItems;
    private ProgressBar progressLoading;
    private EditText etNote;
    private EditText etPickupDate;
    private EditText etPickupTime;
    private Button btnCreateOrder;
    private Button btnCancel;

    private ItemGroupAdapter adapter;
    private PrefsManager prefsManager;

    private Calendar selectedDateTime = Calendar.getInstance();
    private long selectedDateMillis = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_custom_order, container, false);

        prefsManager = PrefsManager.getInstance(requireContext());

        // Initialize views
        recyclerItems = view.findViewById(R.id.recyclerItems);
        progressLoading = view.findViewById(R.id.progressLoading);
        etNote = view.findViewById(R.id.etNote);
        etPickupDate = view.findViewById(R.id.etPickupDate);
        etPickupTime = view.findViewById(R.id.etPickupTime);
        btnCreateOrder = view.findViewById(R.id.btnCreateOrder);
        btnCancel = view.findViewById(R.id.btnCancel);

        // Setup RecyclerView
        adapter = new ItemGroupAdapter();
        recyclerItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerItems.setAdapter(adapter);

        // Setup listeners
        setupDatePickerListener();
        setupTimePickerListener();
        setupCreateOrderListener();
        setupCancelListener();

        // Load items
        loadAllItems();

        return view;
    }

    private void setupDatePickerListener() {
        etPickupDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.select_date)
                    .setSelection(selectedDateMillis > 0 ? selectedDateMillis : System.currentTimeMillis())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                selectedDateMillis = selection;
                updateDateDisplay();
            });

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });
    }

    private void setupTimePickerListener() {
        etPickupTime.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(selectedDateTime.get(Calendar.HOUR_OF_DAY))
                    .setMinute(selectedDateTime.get(Calendar.MINUTE))
                    .setTitleText(R.string.select_time)
                    .build();

            timePicker.addOnPositiveButtonClickListener(dialog -> {
                selectedDateTime.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                selectedDateTime.set(Calendar.MINUTE, timePicker.getMinute());
                updateDateDisplay();
            });

            timePicker.show(getParentFragmentManager(), "TIME_PICKER");
        });
    }

    private void updateDateDisplay() {
        if (selectedDateMillis > 0) {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selectedDateMillis);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etPickupDate.setText(dateFormat.format(calendar.getTime()));
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        etPickupTime.setText(timeFormat.format(selectedDateTime.getTime()));
    }

    private void setupCreateOrderListener() {
        btnCreateOrder.setOnClickListener(v -> createOrder());
    }

    private void setupCancelListener() {
        btnCancel.setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    private void loadAllItems() {
        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        // Load first page with high page size to get all items
        ApiClient.getInstance()
                .getApiService()
                .getAllItems("Bearer " + token, 0, 1000)
                .enqueue(new Callback<BaseResponse<PageResponse<ItemResponse>>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<PageResponse<ItemResponse>>> call,
                                           @NonNull Response<BaseResponse<PageResponse<ItemResponse>>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            BaseResponse<PageResponse<ItemResponse>> baseResponse = response.body();
                            if (baseResponse.isSuccess() && baseResponse.getData() != null) {
                                List<ItemResponse> allItems = baseResponse.getData().getContent();
                                groupAndDisplayItems(allItems);
                            }
                        } else {
                            Toast.makeText(requireContext(), "Lỗi tải nguyên liệu", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<PageResponse<ItemResponse>>> call,
                                          @NonNull Throwable t) {
                        setLoading(false);
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Nhóm items theo step
     */
    private void groupAndDisplayItems(List<ItemResponse> items) {
        Map<String, ItemGroupAdapter.ItemGroup> groupMap = new HashMap<>();

        if (items != null) {
            for (ItemResponse item : items) {
                if (item == null) continue;

                String stepName = item.getStepName();
                int stepId = item.getStepId();

                if (stepName == null || stepName.trim().isEmpty()) {
                    stepName = "Khác";
                }

                ItemGroupAdapter.ItemGroup group = groupMap.get(stepName);
                if (group == null) {
                    group = new ItemGroupAdapter.ItemGroup(stepId, stepName, new ArrayList<>());
                    groupMap.put(stepName, group);
                }

                group.items.add(item);
            }
        }

        // Convert to list and display
        List<ItemGroupAdapter.ItemGroup> itemGroups = new ArrayList<>(groupMap.values());
        adapter.setItemGroups(itemGroups);
    }

    private void createOrder() {
        Map<Integer, ItemResponse> selectedItems = adapter.getSelectedItems();

        if (selectedItems == null || selectedItems.isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_no_ingredients, Toast.LENGTH_SHORT).show();
            return;
        }

        String pickupAtStr = getPickupAtString();
        if (pickupAtStr == null) {
            Toast.makeText(requireContext(), R.string.error_no_pickup_time, Toast.LENGTH_SHORT).show();
            return;
        }

        String token = prefsManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build custom order request
        CreateCustomOrderRequest.CustomDish customDish = buildCustomDish(selectedItems);
        String note = etNote.getText().toString().trim();

        CreateCustomOrderRequest request = new CreateCustomOrderRequest(
                1,  // quantity = 1 for custom order
                pickupAtStr,
                note.isEmpty() ? null : note,
                customDish
        );

        setLoading(true);

        ApiClient.getInstance()
                .getApiService()
                .createCustomOrder("Bearer " + token, request)
                .enqueue(new Callback<BaseResponse<OrderResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse<OrderResponse>> call,
                                           @NonNull Response<BaseResponse<OrderResponse>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            BaseResponse<OrderResponse> baseResponse = response.body();
                            if (baseResponse.isSuccess()) {
                                Toast.makeText(requireContext(), R.string.order_created_success, Toast.LENGTH_SHORT).show();
                                getParentFragmentManager().popBackStack();
                            } else {
                                Toast.makeText(requireContext(), baseResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), "Lỗi tạo đơn hàng", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse<OrderResponse>> call,
                                          @NonNull Throwable t) {
                        setLoading(false);
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Build CustomDish từ selected items
     */
    private CreateCustomOrderRequest.CustomDish buildCustomDish(Map<Integer, ItemResponse> selectedItems) {
        Map<Integer, CreateCustomOrderRequest.Step> stepsMap = new HashMap<>();

        // Group items by stepId
        for (ItemResponse item : selectedItems.values()) {
            int stepId = item.getStepId();
            CreateCustomOrderRequest.Step step = stepsMap.get(stepId);

            if (step == null) {
                step = new CreateCustomOrderRequest.Step();
                step.setStepId(stepId);
                step.setItemIds(new ArrayList<>());
                step.setItems(new ArrayList<>());
                stepsMap.put(stepId, step);
            }

            step.getItemIds().add(item.getItemId());

            // Create item entry with base quantity
            CreateCustomOrderRequest.Item itemSelection = new CreateCustomOrderRequest.Item();
            itemSelection.setItemId(item.getItemId());
            itemSelection.setQuantity(item.getBaseQuantity());
            itemSelection.setNote(null);
            step.getItems().add(itemSelection);
        }

        // Create CustomDish
        CreateCustomOrderRequest.CustomDish customDish = new CreateCustomOrderRequest.CustomDish();
        customDish.setName("Món ăn tùy chỉnh");
        customDish.setDescription("Món ăn được tạo từ các nguyên liệu được chọn");
        customDish.setImageUrl(null);
        customDish.setSteps(new ArrayList<>(stepsMap.values()));

        return customDish;
    }

    private String getPickupAtString() {
        if (selectedDateMillis <= 0) {
            return null;
        }

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(selectedDateMillis);
        calendar.set(Calendar.HOUR_OF_DAY, selectedDateTime.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, selectedDateTime.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, 0);

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return isoFormat.format(calendar.getTime());
    }

    private void setLoading(boolean loading) {
        if (progressLoading != null) {
            progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (btnCreateOrder != null) {
            btnCreateOrder.setEnabled(!loading);
        }
    }
}





