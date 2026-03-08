package com.prm392_sp26.prm392_kitchen_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.ItemResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.CurrencyFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter để hiển thị items được nhóm theo step
 * Mỗi step là một nhóm (ví dụ: Carb, Protein, Sauce)
 */
public class ItemGroupAdapter extends RecyclerView.Adapter<ItemGroupAdapter.ItemViewHolder> {

    public interface OnItemSelectListener {
        void onItemSelected(ItemResponse item, boolean isSelected);
    }

    public static class ItemGroup {
        public int stepId;
        public String stepName;
        public List<ItemResponse> items;

        public ItemGroup(int stepId, String stepName, List<ItemResponse> items) {
            this.stepId = stepId;
            this.stepName = stepName;
            this.items = items != null ? items : new ArrayList<>();
        }
    }

    private List<ItemGroup> itemGroups = new ArrayList<>();
    private Map<Integer, ItemResponse> selectedItemsMap = new HashMap<>(); // itemId -> ItemResponse
    private OnItemSelectListener onItemSelectListener;

    public void setItemGroups(List<ItemGroup> groups) {
        itemGroups.clear();
        if (groups != null) {
            itemGroups.addAll(groups);
        }
        notifyDataSetChanged();
    }

    public Map<Integer, ItemResponse> getSelectedItems() {
        return selectedItemsMap;
    }

    public void setSelectedItems(Map<Integer, ItemResponse> selected) {
        selectedItemsMap.clear();
        if (selected != null) {
            selectedItemsMap.putAll(selected);
        }
        notifyDataSetChanged();
    }

    public void setOnItemSelectListener(OnItemSelectListener listener) {
        this.onItemSelectListener = listener;
    }

    @Override
    public int getItemCount() {
        int total = 0;
        for (ItemGroup group : itemGroups) {
            total += 1 + group.items.size(); // 1 header + items
        }
        return total;
    }

    @Override
    public int getItemViewType(int position) {
        int count = 0;
        for (ItemGroup group : itemGroups) {
            if (count == position) {
                return 0; // Header
            }
            count++;
            if (position < count + group.items.size()) {
                return 1; // Item
            }
            count += group.items.size();
        }
        return 1;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == 0) {
            View view = inflater.inflate(R.layout.item_step_header, parent, false);
            return new ItemViewHolder(view, true);
        } else {
            View view = inflater.inflate(R.layout.item_selectable_item, parent, false);
            return new ItemViewHolder(view, false);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        int count = 0;
        for (ItemGroup group : itemGroups) {
            if (count == position) {
                holder.bindHeader(group.stepName);
                return;
            }
            count++;
            if (position < count + group.items.size()) {
                int itemIndex = position - count;
                ItemResponse item = group.items.get(itemIndex);
                boolean isSelected = selectedItemsMap.containsKey(item.getItemId());
                holder.bindItem(item, isSelected, (view, isChecked) -> {
                    if (isChecked) {
                        selectedItemsMap.put(item.getItemId(), item);
                    } else {
                        selectedItemsMap.remove(item.getItemId());
                    }
                    if (onItemSelectListener != null) {
                        onItemSelectListener.onItemSelected(item, isChecked);
                    }
                });
                return;
            }
            count += group.items.size();
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final boolean isHeader;
        private TextView tvStepName;
        private TextView tvItemName;
        private TextView tvPrice;
        private MaterialCheckBox cbSelect;

        public ItemViewHolder(@NonNull View itemView, boolean isHeader) {
            super(itemView);
            this.isHeader = isHeader;
            if (isHeader) {
                tvStepName = itemView.findViewById(R.id.tvStepName);
            } else {
                tvItemName = itemView.findViewById(R.id.tvItemName);
                tvPrice = itemView.findViewById(R.id.tvItemPrice);
                cbSelect = itemView.findViewById(R.id.cbSelectItem);
            }
        }

        public void bindHeader(String stepName) {
            if (tvStepName != null) {
                tvStepName.setText(stepName);
            }
        }

        public void bindItem(ItemResponse item, boolean isSelected, android.widget.CompoundButton.OnCheckedChangeListener listener) {
            if (tvItemName != null) {
                tvItemName.setText(item.getName());
            }
            if (tvPrice != null) {
                tvPrice.setText(CurrencyFormatter.formatVnd(item.getPrice()));
            }
            if (cbSelect != null) {
                cbSelect.setOnCheckedChangeListener(null); // Remove previous listener
                cbSelect.setChecked(isSelected);
                cbSelect.setOnCheckedChangeListener(listener);
            }
        }
    }
}






