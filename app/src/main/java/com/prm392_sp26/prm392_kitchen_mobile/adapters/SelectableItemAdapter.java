package com.prm392_sp26.prm392_kitchen_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.ItemResponse;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adapter để hiển thị items với checkbox cho custom order
 */
public class SelectableItemAdapter extends RecyclerView.Adapter<SelectableItemAdapter.SelectableItemViewHolder> {

    private List<ItemResponse> items;
    private Map<Integer, Double> selectedQuantities = new HashMap<>();
    private Map<Integer, String> selectedNotes = new HashMap<>();

    public SelectableItemAdapter(List<ItemResponse> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public SelectableItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selectable_item, parent, false);
        return new SelectableItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectableItemViewHolder holder, int position) {
        ItemResponse item = items.get(position);

        holder.tvName.setText(item.getName());
        holder.tvDescription.setText(item.getDescription() != null ? item.getDescription() : "");
        holder.tvCalories.setText((int) item.getCalories() + " kcal");

        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
        holder.tvPrice.setText(nf.format(item.getPrice()));
        holder.tvUnit.setText("/ " + (item.getUnit() != null ? item.getUnit() : "G"));

        String imageUrl = item.getImageUrl();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            holder.ivItemImage.setVisibility(View.GONE);
        } else {
            holder.ivItemImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView)
                .load(imageUrl.trim())
                .centerCrop()
                .into(holder.ivItemImage);
        }

        // Set checked state
        boolean isSelected = selectedQuantities.containsKey(item.getItemId());
        holder.cbSelect.setChecked(isSelected);

        // Show/hide quantity and note fields based on selection
        if (isSelected) {
            holder.etQuantity.setVisibility(View.VISIBLE);
            holder.etNote.setVisibility(View.VISIBLE);
            holder.etQuantity.setText(String.valueOf(selectedQuantities.get(item.getItemId())));
            holder.etNote.setText(selectedNotes.getOrDefault(item.getItemId(), ""));
        } else {
            holder.etQuantity.setVisibility(View.GONE);
            holder.etNote.setVisibility(View.GONE);
        }

        // Handle checkbox change
        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedQuantities.put(item.getItemId(), item.getBaseQuantity());
                selectedNotes.put(item.getItemId(), "");
                holder.etQuantity.setVisibility(View.VISIBLE);
                holder.etNote.setVisibility(View.VISIBLE);
                holder.etQuantity.setText(String.valueOf(item.getBaseQuantity()));
                holder.etNote.setText("");
            } else {
                selectedQuantities.remove(item.getItemId());
                selectedNotes.remove(item.getItemId());
                holder.etQuantity.setVisibility(View.GONE);
                holder.etNote.setVisibility(View.GONE);
            }
        });

        // Handle quantity change
        holder.etQuantity.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                try {
                    double quantity = Double.parseDouble(holder.etQuantity.getText().toString());
                    if (quantity > 0) {
                        selectedQuantities.put(item.getItemId(), quantity);
                    }
                } catch (NumberFormatException e) {
                    holder.etQuantity.setText(String.valueOf(item.getBaseQuantity()));
                    selectedQuantities.put(item.getItemId(), item.getBaseQuantity());
                }
            }
        });

        // Handle note change
        holder.etNote.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String note = holder.etNote.getText().toString().trim();
                selectedNotes.put(item.getItemId(), note);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public Map<Integer, Double> getSelectedQuantities() {
        return selectedQuantities;
    }

    public Map<Integer, String> getSelectedNotes() {
        return selectedNotes;
    }

    public List<Integer> getSelectedItemIds() {
        return List.copyOf(selectedQuantities.keySet());
    }

    static class SelectableItemViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbSelect;
        ImageView ivItemImage;
        TextView tvName, tvDescription, tvCalories, tvPrice, tvUnit;
        EditText etQuantity, etNote;

        SelectableItemViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelect = itemView.findViewById(R.id.cbSelectItem);
            ivItemImage = itemView.findViewById(R.id.ivItemImage);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvDescription = itemView.findViewById(R.id.tvItemDescription);
            tvCalories = itemView.findViewById(R.id.tvItemCalories);
            tvPrice = itemView.findViewById(R.id.tvItemPrice);
            tvUnit = itemView.findViewById(R.id.tvItemUnit);
            etQuantity = itemView.findViewById(R.id.etItemQuantity);
            etNote = itemView.findViewById(R.id.etItemNote);
        }
    }
}

