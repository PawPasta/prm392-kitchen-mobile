package com.prm392_sp26.prm392_kitchen_mobile.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.CurrencyFormatter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CartDishAdapter extends RecyclerView.Adapter<CartDishAdapter.CartDishViewHolder> {

    public interface OnSelectionChangeListener {
        void onSelectionChanged(int selectedCount, int totalCount, boolean allSelected);
    }

    private final List<OrderResponse.OrderDishDetail> items = new ArrayList<>();
    private final Set<Integer> expandedDishKeys = new HashSet<>();
    private final Set<Integer> selectedDishKeys = new HashSet<>();
    private OnSelectionChangeListener onSelectionChangeListener;
    private boolean selectionMode;

    public void setItems(List<OrderResponse.OrderDishDetail> newItems) {
        items.clear();
        expandedDishKeys.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        syncSelectionWithCurrentItems();
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public void setOnSelectionChangeListener(OnSelectionChangeListener listener) {
        this.onSelectionChangeListener = listener;
    }

    public void setSelectionMode(boolean enabled) {
        selectionMode = enabled;
        if (!enabled) {
            selectedDishKeys.clear();
        }
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    public void setSelectAll(boolean selected) {
        selectedDishKeys.clear();
        if (selected) {
            for (int i = 0; i < items.size(); i++) {
                selectedDishKeys.add(getDishKey(items.get(i), i));
            }
        }
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public Set<Integer> getSelectedDishKeys() {
        return new HashSet<>(selectedDishKeys);
    }

    public int getSelectedCount() {
        return selectedDishKeys.size();
    }

    public boolean isAllSelected() {
        return !items.isEmpty() && selectedDishKeys.size() == items.size();
    }

    @NonNull
    @Override
    public CartDishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_dish, parent, false);
        return new CartDishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartDishViewHolder holder, int position) {
        OrderResponse.OrderDishDetail item = items.get(position);

        holder.tvDishName.setText(nonEmpty(item.getDishName(), "Món ăn"));
        holder.tvDishMeta.setText("Số lượng: " + item.getQuantity() + " • " + formatCalories(item.getDishCalories()));
        holder.tvDishPrice.setText(CurrencyFormatter.formatVnd(item.getLineTotal()));

        String imageUrl = item.getDishImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(holder.itemView)
                    .load(imageUrl.trim())
                    .placeholder(R.drawable.ic_dish)
                    .error(R.drawable.ic_dish)
                    .into(holder.ivDish);
        } else {
            holder.ivDish.setImageResource(R.drawable.ic_dish);
        }

        int dishKey = getDishKey(item, position);

        holder.cbSelectDish.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
        holder.cbSelectDish.setOnCheckedChangeListener(null);
        holder.cbSelectDish.setChecked(selectionMode && selectedDishKeys.contains(dishKey));
        holder.cbSelectDish.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedDishKeys.add(dishKey);
            } else {
                selectedDishKeys.remove(dishKey);
            }
            notifySelectionChanged();
        });

        boolean expanded = expandedDishKeys.contains(dishKey);
        holder.tvExpandToggle.setText(expanded ? "Ẩn thành phần ▲" : "Xem thành phần ▼");
        holder.tvComponents.setVisibility(expanded ? View.VISIBLE : View.GONE);
        if (expanded) {
            holder.tvComponents.setText(buildComponentsText(item.getCustomItems()));
        }

        holder.tvExpandToggle.setOnClickListener(v -> {
            if (expandedDishKeys.contains(dishKey)) {
                expandedDishKeys.remove(dishKey);
            } else {
                expandedDishKeys.add(dishKey);
            }
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(adapterPosition);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (!selectionMode) {
                return;
            }
            holder.cbSelectDish.toggle();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private int getDishKey(OrderResponse.OrderDishDetail item, int position) {
        if (item == null) {
            return -1 - position;
        }
        if (item.getOrderDishId() > 0) {
            return item.getOrderDishId();
        }
        return -1 - position;
    }

    private void syncSelectionWithCurrentItems() {
        if (selectedDishKeys.isEmpty()) {
            return;
        }
        Set<Integer> validKeys = new HashSet<>();
        for (int i = 0; i < items.size(); i++) {
            validKeys.add(getDishKey(items.get(i), i));
        }
        selectedDishKeys.retainAll(validKeys);
    }

    private void notifySelectionChanged() {
        if (onSelectionChangeListener == null) {
            return;
        }
        onSelectionChangeListener.onSelectionChanged(
                selectedDishKeys.size(),
                items.size(),
                isAllSelected());
    }

    private String buildComponentsText(List<OrderResponse.OrderDishItemDetail> customItems) {
        if (customItems == null || customItems.isEmpty()) {
            return "Không có thành phần tùy chỉnh.";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < customItems.size(); i++) {
            OrderResponse.OrderDishItemDetail component = customItems.get(i);
            if (component == null) {
                continue;
            }

            if (builder.length() > 0) {
                builder.append('\n');
            }

            builder.append(i + 1)
                    .append(". ")
                    .append(nonEmpty(component.getItemName(), "Thành phần"));

            builder.append(" • ")
                    .append(formatNumber(component.getQuantity()));

            if (!TextUtils.isEmpty(component.getUnit())) {
                builder.append(" ").append(component.getUnit());
            }

            builder.append(" • ")
                    .append(CurrencyFormatter.formatVnd(component.getPrice()));

            if (!TextUtils.isEmpty(component.getNote())) {
                builder.append(" • ").append(component.getNote().trim());
            }
        }
        if (builder.length() == 0) {
            return "Không có thành phần tùy chỉnh.";
        }
        return builder.toString();
    }

    private String formatCalories(double calories) {
        if (calories <= 0) {
            return "-- kcal";
        }
        return formatNumber(calories) + " kcal";
    }

    private String formatNumber(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001d) {
            return String.valueOf((long) Math.rint(value));
        }
        return String.format(Locale.getDefault(), "%.1f", value);
    }

    private String nonEmpty(String value, String fallback) {
        if (TextUtils.isEmpty(value)) {
            return fallback;
        }
        return value.trim();
    }

    static class CartDishViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox cbSelectDish;
        private final ImageView ivDish;
        private final TextView tvDishName;
        private final TextView tvDishMeta;
        private final TextView tvDishPrice;
        private final TextView tvExpandToggle;
        private final TextView tvComponents;

        CartDishViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelectDish = itemView.findViewById(R.id.cbSelectDish);
            ivDish = itemView.findViewById(R.id.ivDishImage);
            tvDishName = itemView.findViewById(R.id.tvDishName);
            tvDishMeta = itemView.findViewById(R.id.tvDishMeta);
            tvDishPrice = itemView.findViewById(R.id.tvDishPrice);
            tvExpandToggle = itemView.findViewById(R.id.tvExpandToggle);
            tvComponents = itemView.findViewById(R.id.tvDishComponents);
        }
    }
}
