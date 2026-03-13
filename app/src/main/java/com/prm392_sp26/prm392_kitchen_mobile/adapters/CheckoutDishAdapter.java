package com.prm392_sp26.prm392_kitchen_mobile.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.CurrencyFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheckoutDishAdapter extends RecyclerView.Adapter<CheckoutDishAdapter.CheckoutDishViewHolder> {

    private final List<OrderResponse.OrderDishDetail> dishes = new ArrayList<>();

    public void setItems(List<OrderResponse.OrderDishDetail> newItems) {
        dishes.clear();
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

        holder.tvDishName.setText(nonEmpty(dish.getDishName(), "Món ăn"));
        holder.tvDishMeta.setText("SL: " + Math.max(1, dish.getQuantity())
                + " • " + formatNumber(dish.getDishCalories()) + " kcal");
        holder.tvLineTotal.setText(CurrencyFormatter.formatVnd(dish.getLineTotal()));

        String imageUrl = dish.getDishImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(holder.itemView)
                    .load(imageUrl.trim())
                    .placeholder(R.drawable.ic_dish)
                    .error(R.drawable.ic_dish)
                    .centerCrop()
                    .into(holder.ivDishImage);
        } else {
            holder.ivDishImage.setImageResource(R.drawable.ic_dish);
        }
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

    static class CheckoutDishViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivDishImage;
        private final TextView tvDishName;
        private final TextView tvDishMeta;
        private final TextView tvLineTotal;

        CheckoutDishViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDishImage = itemView.findViewById(R.id.ivCheckoutDishImage);
            tvDishName = itemView.findViewById(R.id.tvCheckoutDishName);
            tvDishMeta = itemView.findViewById(R.id.tvCheckoutDishMeta);
            tvLineTotal = itemView.findViewById(R.id.tvCheckoutDishPrice);
        }
    }
}
