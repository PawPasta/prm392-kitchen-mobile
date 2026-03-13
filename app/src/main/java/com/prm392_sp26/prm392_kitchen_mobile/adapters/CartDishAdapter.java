package com.prm392_sp26.prm392_kitchen_mobile.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.CurrencyFormatter;
import com.prm392_sp26.prm392_kitchen_mobile.util.PlaceholderImageResolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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

        String imageUrl = PlaceholderImageResolver.resolveDishImageUrl(item.getDishImageUrl());
        Glide.with(holder.itemView)
                .load(imageUrl)
                .placeholder(R.drawable.ic_dish)
                .error(R.drawable.ic_dish)
                .into(holder.ivDish);

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

        boolean isCustomDish = isCustomDish(item);
        if (!isCustomDish) {
            expandedDishKeys.remove(dishKey);
            holder.tvExpandToggle.setVisibility(View.GONE);
            holder.layoutDishComponents.setVisibility(View.GONE);
            holder.layoutDishComponents.removeAllViews();
        } else {
            holder.tvExpandToggle.setVisibility(View.VISIBLE);
            boolean expanded = expandedDishKeys.contains(dishKey);
            holder.tvExpandToggle.setText(expanded ? "Ẩn thành phần ▲" : "Xem thành phần ▼");
            holder.layoutDishComponents.setVisibility(expanded ? View.VISIBLE : View.GONE);

            if (expanded) {
                renderComponentSteps(holder.layoutDishComponents, item.getCustomItems());
            } else {
                holder.layoutDishComponents.removeAllViews();
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
        }

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

    private boolean isCustomDish(OrderResponse.OrderDishDetail dish) {
        if (dish == null || dish.getDishStatus() == null) {
            return false;
        }
        return "CUSTOM".equalsIgnoreCase(dish.getDishStatus().trim());
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

    private void renderComponentSteps(
            LinearLayout container,
            List<OrderResponse.OrderDishItemDetail> customItems) {
        container.removeAllViews();
        Context context = container.getContext();

        if (customItems == null || customItems.isEmpty()) {
            TextView emptyView = new TextView(context);
            emptyView.setText("Không có thành phần tùy chỉnh.");
            emptyView.setTextColor(ContextCompat.getColor(context, R.color.colorTextSecondary));
            emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            container.addView(emptyView);
            return;
        }

        Map<Integer, List<OrderResponse.OrderDishItemDetail>> groupedSteps = new TreeMap<>();
        for (OrderResponse.OrderDishItemDetail component : customItems) {
            if (component == null) {
                continue;
            }
            int stepId = component.getStepId();
            if (stepId <= 0) {
                stepId = Integer.MAX_VALUE;
            }
            groupedSteps.computeIfAbsent(stepId, key -> new ArrayList<>()).add(component);
        }

        if (groupedSteps.isEmpty()) {
            TextView emptyView = new TextView(context);
            emptyView.setText("Không có thành phần tùy chỉnh.");
            emptyView.setTextColor(ContextCompat.getColor(context, R.color.colorTextSecondary));
            emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            container.addView(emptyView);
            return;
        }

        int stepNumber = 1;
        for (Map.Entry<Integer, List<OrderResponse.OrderDishItemDetail>> entry : groupedSteps.entrySet()) {
            addStepHeader(container, "Bước " + stepNumber);
            for (OrderResponse.OrderDishItemDetail component : entry.getValue()) {
                addComponentRow(container, component);
            }
            stepNumber++;
        }
    }

    private void addStepHeader(LinearLayout container, String title) {
        Context context = container.getContext();
        TextView header = new TextView(context);
        header.setText(title);
        header.setTextColor(ContextCompat.getColor(context, R.color.colorTextPrimary));
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        header.setTypeface(Typeface.DEFAULT_BOLD);
        header.setBackgroundResource(R.drawable.bg_chip);
        int horizontalPadding = dp(context, 10);
        int verticalPadding = dp(context, 4);
        header.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = container.getChildCount() == 0 ? 0 : dp(context, 10);
        header.setLayoutParams(params);
        container.addView(header);
    }

    private void addComponentRow(
            LinearLayout container,
            OrderResponse.OrderDishItemDetail component) {
        Context context = container.getContext();
        View rowView = LayoutInflater.from(context)
                .inflate(R.layout.item_cart_component_row, container, false);

        ImageView ivComponentImage = rowView.findViewById(R.id.ivComponentImage);
        TextView tvComponentName = rowView.findViewById(R.id.tvComponentName);
        TextView tvComponentQty = rowView.findViewById(R.id.tvComponentQty);
        TextView tvComponentPrice = rowView.findViewById(R.id.tvComponentPrice);

        tvComponentName.setText(nonEmpty(component.getItemName(), "Thành phần"));
        tvComponentQty.setText(buildQuantityText(component.getQuantity(), component.getUnit()));
        tvComponentPrice.setText(CurrencyFormatter.formatVnd(component.getPrice()));

        String imageUrl = PlaceholderImageResolver.resolveItemImageUrl(
                component.getItemImageUrl(),
                component.getStepId(),
                null);
        Glide.with(rowView)
                .load(imageUrl)
                .placeholder(R.drawable.ic_dish)
                .error(R.drawable.ic_dish)
                .into(ivComponentImage);

        container.addView(rowView);
    }

    private String buildQuantityText(double quantity, String unit) {
        String text = formatNumber(quantity);
        if (!TextUtils.isEmpty(unit)) {
            text += " " + unit.trim();
        }
        return text;
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

    private int dp(Context context, int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                context.getResources().getDisplayMetrics());
    }

    static class CartDishViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox cbSelectDish;
        private final ImageView ivDish;
        private final TextView tvDishName;
        private final TextView tvDishMeta;
        private final TextView tvDishPrice;
        private final TextView tvExpandToggle;
        private final LinearLayout layoutDishComponents;

        CartDishViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelectDish = itemView.findViewById(R.id.cbSelectDish);
            ivDish = itemView.findViewById(R.id.ivDishImage);
            tvDishName = itemView.findViewById(R.id.tvDishName);
            tvDishMeta = itemView.findViewById(R.id.tvDishMeta);
            tvDishPrice = itemView.findViewById(R.id.tvDishPrice);
            tvExpandToggle = itemView.findViewById(R.id.tvExpandToggle);
            layoutDishComponents = itemView.findViewById(R.id.layoutDishComponents);
        }
    }
}
