package com.prm392_sp26.prm392_kitchen_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.ItemResponse;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<ItemResponse> items;
    private int maxSelect;

    public ItemAdapter(List<ItemResponse> items, int maxSelect) {
        this.items = items;
        this.maxSelect = maxSelect;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_step_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ItemResponse item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.tvCalories.setText((int) item.getCalories() + " kcal");

        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
        holder.tvPrice.setText(nf.format(item.getPrice()));

        holder.cbSelected.setChecked(item.isSelected());

        holder.itemView.setOnClickListener(v -> {
            boolean newState = !item.isSelected();
            
            if (newState && maxSelect == 1) {
                // Single select logic: unselect others
                for (ItemResponse i : items) {
                    i.setSelected(false);
                }
                item.setSelected(true);
                notifyDataSetChanged();
            } else if (newState) {
                // Multi select logic: check if we reached limit
                int count = 0;
                for (ItemResponse i : items) if (i.isSelected()) count++;
                
                if (count < maxSelect) {
                    item.setSelected(true);
                    notifyItemChanged(position);
                }
            } else {
                // Unselect always allowed
                item.setSelected(false);
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCalories, tvPrice;
        CheckBox cbSelected;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvCalories = itemView.findViewById(R.id.tvItemCalories);
            tvPrice = itemView.findViewById(R.id.tvItemPrice);
            cbSelected = itemView.findViewById(R.id.cbSelected);
        }
    }
}
