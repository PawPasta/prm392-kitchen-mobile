package com.prm392_sp26.prm392_kitchen_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.ItemResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.CurrencyFormatter;
import com.prm392_sp26.prm392_kitchen_mobile.util.PlaceholderImageResolver;

import android.content.res.ColorStateList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

/**
 * Adapter để hiển thị items với checkbox cho custom order
 */
public class SelectableItemAdapter extends RecyclerView.Adapter<SelectableItemAdapter.SelectableItemViewHolder> {

    public interface OnSelectionChangedListener {
        void onSelectionChanged();
    }

    private List<ItemResponse> items;
    private Map<Integer, Double> selectedQuantities = new HashMap<>();
    private Map<Integer, String> selectedNotes = new HashMap<>();
    private OnSelectionChangedListener selectionChangedListener;

    public SelectableItemAdapter(List<ItemResponse> items) {
        this.items = items;
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
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
        String stepName = item.getStepName();

        holder.tvName.setText(item.getName());
        String description = item.getDescription() != null ? item.getDescription().trim() : "";
        if (description.isEmpty()) {
            holder.tvDescription.setVisibility(View.GONE);
        } else {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(description);
        }
        holder.tvCalories.setText((int) item.getCalories() + " kcal");

        holder.tvPrice.setText(CurrencyFormatter.formatVnd(item.getPrice()));
        holder.tvUnit.setText("/ " + (item.getUnit() != null ? item.getUnit() : "G"));

        if (stepName == null || stepName.trim().isEmpty()) {
            stepName = "Khác";
        }
        holder.tvTag.setText(stepName);
        int stepColor = ContextCompat.getColor(holder.itemView.getContext(), getStepColorRes(stepName));
        ViewCompat.setBackgroundTintList(holder.tvTag, ColorStateList.valueOf(stepColor));
        ViewCompat.setBackgroundTintList(holder.viewAccent, ColorStateList.valueOf(stepColor));

        String imageUrl = PlaceholderImageResolver.resolveItemImageUrl(item);
        holder.ivItemImage.setVisibility(View.VISIBLE);
        Glide.with(holder.itemView)
            .load(imageUrl)
            .placeholder(R.drawable.ic_dish)
            .error(R.drawable.ic_dish)
            .centerCrop()
            .into(holder.ivItemImage);

        // Disable item nếu không phải ENABLE
        boolean isEnabled = "ENABLE".equals(item.getStatus());
        holder.itemView.setAlpha(isEnabled ? 1.0f : 0.4f);
        holder.cbSelect.setEnabled(isEnabled);

        // Set checked state
        boolean isSelected = isEnabled && selectedQuantities.containsKey(item.getItemId());
        // Tạm bỏ listener để tránh trigger khi set checked bằng code
        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(isSelected);
        updateCardSelectionState(holder, isSelected, stepColor);

        // Show/hide quantity and note fields based on selection
        holder.layoutItemOptions.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        if (isSelected) {
            double qty = selectedQuantities.getOrDefault(item.getItemId(), getDefaultQuantity(item));
            holder.tvQuantity.setText(formatQuantity(qty));
            holder.etNote.setText(selectedNotes.getOrDefault(item.getItemId(), ""));
        }

        // Handle checkbox change — chỉ cho phép nếu item ENABLE
        android.widget.CompoundButton.OnCheckedChangeListener selectionListener =
                (buttonView, isChecked) -> {
                    if (!isEnabled) return;
                    setItemSelected(holder, item, isChecked);
                };
        holder.cbSelect.setOnCheckedChangeListener(selectionListener);

        holder.itemView.setOnClickListener(v -> {
            if (!isEnabled) {
                return;
            }
            boolean nextSelected = !selectedQuantities.containsKey(item.getItemId());
            holder.cbSelect.setOnCheckedChangeListener(null);
            holder.cbSelect.setChecked(nextSelected);
            holder.cbSelect.setOnCheckedChangeListener(selectionListener);
            setItemSelected(holder, item, nextSelected);
        });

        // Handle quantity change (step = 50)
        holder.btnQuantityMinus.setOnClickListener(v -> {
            double current = selectedQuantities.getOrDefault(item.getItemId(), getDefaultQuantity(item));
            double minQuantity = getDefaultQuantity(item);
            double updated = Math.max(minQuantity, current - 50.0);
            selectedQuantities.put(item.getItemId(), updated);
            holder.tvQuantity.setText(formatQuantity(updated));
            notifySelectionChanged();
        });
        holder.btnQuantityPlus.setOnClickListener(v -> {
            double current = selectedQuantities.getOrDefault(item.getItemId(), getDefaultQuantity(item));
            double updated = current + 50.0;
            selectedQuantities.put(item.getItemId(), updated);
            holder.tvQuantity.setText(formatQuantity(updated));
            notifySelectionChanged();
        });

        // Handle note change
        holder.etNote.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String note = holder.etNote.getText().toString().trim();
                selectedNotes.put(item.getItemId(), note);
                notifySelectionChanged();
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

    private void notifySelectionChanged() {
        if (selectionChangedListener != null) {
            selectionChangedListener.onSelectionChanged();
        }
    }

    private int getStepColorRes(String stepName) {
        String lower = stepName.toLowerCase(Locale.ROOT);
        if (lower.contains("vegetable") || lower.contains("rau")) {
            return R.color.stepVeggie;
        }
        if (lower.contains("protein") || lower.contains("thit") || lower.contains("meat")
                || lower.contains("seafood") || lower.contains("hai san") || lower.contains("hải sản")) {
            return R.color.stepProtein;
        }
        if (lower.contains("carb") || lower.contains("com") || lower.contains("cơm")
                || lower.contains("bun") || lower.contains("bún") || lower.contains("noodle")
                || lower.contains("banh") || lower.contains("bánh")) {
            return R.color.stepCarb;
        }
        if (lower.contains("sauce") || lower.contains("sot") || lower.contains("sốt")) {
            return R.color.stepSauce;
        }
        if (lower.contains("dairy") || lower.contains("milk") || lower.contains("sua")
                || lower.contains("sữa") || lower.contains("cheese")) {
            return R.color.stepDairy;
        }
        return R.color.stepDefault;
    }

    private double getDefaultQuantity(ItemResponse item) {
        double base = item.getBaseQuantity();
        if (base <= 0) {
            return 50.0;
        }
        return base;
    }

    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.format(Locale.US, "%.0f", quantity);
        }
        return String.format(Locale.US, "%.1f", quantity);
    }

    private void updateCardSelectionState(SelectableItemViewHolder holder, boolean selected, int stepColor) {
        if (holder.cardItem == null) {
            return;
        }
        int strokeColor = selected
                ? stepColor
                : ContextCompat.getColor(holder.itemView.getContext(), R.color.cardStroke);
        int strokeWidth = dpToPx(holder.itemView, selected ? 2 : 1);
        holder.cardItem.setStrokeColor(strokeColor);
        holder.cardItem.setStrokeWidth(strokeWidth);
    }

    private int dpToPx(View view, int dp) {
        float density = view.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void setItemSelected(SelectableItemViewHolder holder, ItemResponse item, boolean isSelected) {
        int stepColor = resolveStepColor(holder, item);
        if (isSelected) {
            double defaultQty = getDefaultQuantity(item);
            selectedQuantities.put(item.getItemId(), defaultQty);
            selectedNotes.put(item.getItemId(), "");
            holder.layoutItemOptions.setVisibility(View.VISIBLE);
            holder.tvQuantity.setText(formatQuantity(defaultQty));
            holder.etNote.setText("");
            updateCardSelectionState(holder, true, stepColor);
        } else {
            selectedQuantities.remove(item.getItemId());
            selectedNotes.remove(item.getItemId());
            holder.layoutItemOptions.setVisibility(View.GONE);
            updateCardSelectionState(holder, false, stepColor);
        }
        notifySelectionChanged();
    }

    private int resolveStepColor(SelectableItemViewHolder holder, ItemResponse item) {
        String stepName = item != null ? item.getStepName() : null;
        if (stepName == null || stepName.trim().isEmpty()) {
            stepName = "Khac";
        }
        return ContextCompat.getColor(holder.itemView.getContext(), getStepColorRes(stepName));
    }

    static class SelectableItemViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardItem;
        CheckBox cbSelect;
        ImageView ivItemImage;
        View viewAccent;
        TextView tvName, tvDescription, tvCalories, tvPrice, tvUnit, tvTag;
        View layoutItemOptions;
        TextView tvQuantity;
        MaterialButton btnQuantityMinus, btnQuantityPlus;
        EditText etNote;

        SelectableItemViewHolder(@NonNull View itemView) {
            super(itemView);
            cardItem = itemView.findViewById(R.id.cardSelectableItem);
            cbSelect = itemView.findViewById(R.id.cbSelectItem);
            ivItemImage = itemView.findViewById(R.id.ivItemImage);
            viewAccent = itemView.findViewById(R.id.viewItemAccent);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvDescription = itemView.findViewById(R.id.tvItemDescription);
            tvCalories = itemView.findViewById(R.id.tvItemCalories);
            tvPrice = itemView.findViewById(R.id.tvItemPrice);
            tvUnit = itemView.findViewById(R.id.tvItemUnit);
            tvTag = itemView.findViewById(R.id.tvItemTag);
            layoutItemOptions = itemView.findViewById(R.id.layoutItemOptions);
            btnQuantityMinus = itemView.findViewById(R.id.btnItemQuantityMinus);
            btnQuantityPlus = itemView.findViewById(R.id.btnItemQuantityPlus);
            tvQuantity = itemView.findViewById(R.id.tvItemQuantity);
            etNote = itemView.findViewById(R.id.etItemNote);
        }
    }
}
