package com.prm392_sp26.prm392_kitchen_mobile.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class CheckoutDishAdapter extends RecyclerView.Adapter<CheckoutDishAdapter.CheckoutDishViewHolder> {

    private final List<OrderResponse.OrderDishDetail> dishes = new ArrayList<>();
    private final Set<Integer> expandedDishKeys = new HashSet<>();

    public void setItems(List<OrderResponse.OrderDishDetail> newItems) {
        dishes.clear();
        expandedDishKeys.clear();
        if (newItems != null) {
            dishes.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CheckoutDishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checkout_dish, parent, false);
        return new CheckoutDishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutDishViewHolder holder, int position) {
        OrderResponse.OrderDishDetail dish = dishes.get(position);
        int dishKey = getDishKey(dish, position);

        holder.tvDishName.setText(nonEmpty(dish.getDishName(), "Món ăn"));
        holder.tvDishMeta.setText("SL: " + Math.max(1, dish.getQuantity())
                + " • " + formatNumber(dish.getDishCalories()) + " kcal"
                + " • Đơn giá: " + CurrencyFormatter.formatVnd(dish.getDishPrice()));
        holder.tvLineTotal.setText(CurrencyFormatter.formatVnd(dish.getLineTotal()));

        String imageUrl = PlaceholderImageResolver.resolveDishImageUrl(dish.getDishImageUrl());
        Glide.with(holder.itemView)
                .load(imageUrl)
                .placeholder(R.drawable.ic_dish)
                .error(R.drawable.ic_dish)
                .centerCrop()
                .into(holder.ivDishImage);

        boolean hasExpandableItems = hasExpandableItems(dish);
        if (hasExpandableItems) {
            holder.tvExpandToggle.setVisibility(View.VISIBLE);
            boolean expanded = expandedDishKeys.contains(dishKey);
            holder.tvExpandToggle.setText(expanded ? "Ẩn thành phần ▲" : "Xem thành phần ▼");
            holder.layoutDishComponents.setVisibility(expanded ? View.VISIBLE : View.GONE);

            if (expanded) {
                renderComponentSteps(holder.layoutDishComponents, dish.getCustomItems());
            } else {
                holder.layoutDishComponents.removeAllViews();
            }

            holder.tvExpandToggle.setOnClickListener(v -> {
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION) {
                    return;
                }
                int key = getDishKey(dishes.get(adapterPosition), adapterPosition);
                if (expandedDishKeys.contains(key)) {
                    expandedDishKeys.remove(key);
                } else {
                    expandedDishKeys.add(key);
                }
                notifyItemChanged(adapterPosition);
            });
        } else {
            expandedDishKeys.remove(dishKey);
            holder.tvExpandToggle.setVisibility(View.GONE);
            holder.layoutDishComponents.setVisibility(View.GONE);
            holder.layoutDishComponents.removeAllViews();
            holder.tvExpandToggle.setOnClickListener(null);
        }

        holder.viewDivider.setVisibility(position == dishes.size() - 1 ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return dishes.size();
    }

    private String nonEmpty(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private String formatNumber(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001d) {
            return String.valueOf((long) Math.rint(value));
        }
        return String.format(Locale.getDefault(), "%.1f", value);
    }

    private boolean hasExpandableItems(OrderResponse.OrderDishDetail dish) {
        return dish != null && dish.getCustomItems() != null && !dish.getCustomItems().isEmpty();
    }

    private int getDishKey(OrderResponse.OrderDishDetail dish, int position) {
        if (dish == null) {
            return -1 - position;
        }
        if (dish.getOrderDishId() > 0) {
            return dish.getOrderDishId();
        }
        return -1 - position;
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
        tvComponentQty.setText(buildComponentMeta(component));
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

    private String buildComponentMeta(OrderResponse.OrderDishItemDetail component) {
        if (component == null) {
            return "--";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(buildQuantityText(component.getQuantity(), component.getUnit()));
        if (component.getCalories() > 0) {
            builder.append(" • ").append(formatNumber(component.getCalories())).append(" kcal");
        }
        String note = component.getNote();
        if (!TextUtils.isEmpty(note)) {
            builder.append(" • ").append(note.trim());
        }
        return builder.toString();
    }

    private int dp(Context context, int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                context.getResources().getDisplayMetrics());
    }

    static class CheckoutDishViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivDishImage;
        private final TextView tvDishName;
        private final TextView tvDishMeta;
        private final TextView tvLineTotal;
        private final TextView tvExpandToggle;
        private final LinearLayout layoutDishComponents;
        private final View viewDivider;

        CheckoutDishViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDishImage = itemView.findViewById(R.id.ivCheckoutDishImage);
            tvDishName = itemView.findViewById(R.id.tvCheckoutDishName);
            tvDishMeta = itemView.findViewById(R.id.tvCheckoutDishMeta);
            tvLineTotal = itemView.findViewById(R.id.tvCheckoutDishPrice);
            tvExpandToggle = itemView.findViewById(R.id.tvCheckoutExpandToggle);
            layoutDishComponents = itemView.findViewById(R.id.layoutCheckoutDishComponents);
            viewDivider = itemView.findViewById(R.id.viewCheckoutDishDivider);
        }
    }
}
