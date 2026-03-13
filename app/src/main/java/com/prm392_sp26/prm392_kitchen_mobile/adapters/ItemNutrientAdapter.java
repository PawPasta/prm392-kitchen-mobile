package com.prm392_sp26.prm392_kitchen_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.ItemDetailResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItemNutrientAdapter extends RecyclerView.Adapter<ItemNutrientAdapter.NutrientViewHolder> {

    private final List<ItemDetailResponse.ItemNutrient> nutrients = new ArrayList<>();

    public void setItems(List<ItemDetailResponse.ItemNutrient> newItems) {
        nutrients.clear();
        if (newItems != null) {
            nutrients.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NutrientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nutrient, parent, false);
        return new NutrientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NutrientViewHolder holder, int position) {
        ItemDetailResponse.ItemNutrient nutrient = nutrients.get(position);
        holder.tvName.setText(nonEmpty(nutrient.getNutrientName(), "Nutrient"));
        holder.tvDescription.setText(nonEmpty(nutrient.getNutrientDescription(), "--"));
        holder.tvAmount.setText(formatNumber(nutrient.getAmount()) + " " + safeText(nutrient.getNutrientUnit()));

        String baseQty = formatNumber(nutrient.getItemBaseQuantity());
        String baseUnit = safeText(nutrient.getItemBaseUnit());
        holder.tvBase.setText("Cho " + baseQty + " " + baseUnit);
    }

    @Override
    public int getItemCount() {
        return nutrients.size();
    }

    private String nonEmpty(String value, String fallback) {
        String text = safeText(value);
        return text.isEmpty() ? fallback : text;
    }

    private String safeText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private String formatNumber(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001d) {
            return String.format(Locale.getDefault(), "%d", (long) Math.rint(value));
        }
        return String.format(Locale.getDefault(), "%.2f", value);
    }

    static class NutrientViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvDescription;
        private final TextView tvAmount;
        private final TextView tvBase;

        NutrientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvNutrientName);
            tvDescription = itemView.findViewById(R.id.tvNutrientDescription);
            tvAmount = itemView.findViewById(R.id.tvNutrientAmount);
            tvBase = itemView.findViewById(R.id.tvNutrientBase);
        }
    }
}
