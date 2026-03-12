package com.prm392_sp26.prm392_kitchen_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderHistoryResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.CurrencyFormatter;
import com.prm392_sp26.prm392_kitchen_mobile.util.StatusColorUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private final List<OrderHistoryResponse.OrderItem> items = new ArrayList<>();
    private final Set<String> selectedOrderIds = new HashSet<>();
    private OnItemClickListener onItemClickListener;
    private OnCancelClickListener onCancelClickListener;
    private OnSelectionChangeListener onSelectionChangeListener;
    private boolean selectionMode;
    private boolean cancelEnabled = true;

    public void setItems(List<OrderHistoryResponse.OrderItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        syncSelectionWithCurrentItems();
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public void addItems(List<OrderHistoryResponse.OrderItem> newItems) {
        if (newItems == null || newItems.isEmpty()) {
            return;
        }
        int start = items.size();
        items.addAll(newItems);
        notifyItemRangeInserted(start, newItems.size());
        syncSelectionWithCurrentItems();
        notifySelectionChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnCancelClickListener(OnCancelClickListener listener) {
        this.onCancelClickListener = listener;
    }

    public void setOnSelectionChangeListener(OnSelectionChangeListener listener) {
        this.onSelectionChangeListener = listener;
    }

    public void setSelectionMode(boolean enabled) {
        selectionMode = enabled;
        if (!enabled) {
            selectedOrderIds.clear();
        }
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    public void setCancelEnabled(boolean enabled) {
        cancelEnabled = enabled;
        notifyDataSetChanged();
    }

    public void setSelectAll(boolean selected) {
        selectedOrderIds.clear();
        if (selected) {
            for (OrderHistoryResponse.OrderItem item : items) {
                if (item == null || item.getOrderId() == null) {
                    continue;
                }
                String orderId = item.getOrderId().trim();
                if (!orderId.isEmpty()) {
                    selectedOrderIds.add(orderId);
                }
            }
        }
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public int getSelectedCount() {
        return selectedOrderIds.size();
    }

    public List<String> getSelectedOrderIds() {
        return new ArrayList<>(selectedOrderIds);
    }

    public boolean isAllSelected() {
        int selectableCount = 0;
        for (OrderHistoryResponse.OrderItem item : items) {
            if (item == null || item.getOrderId() == null || item.getOrderId().trim().isEmpty()) {
                continue;
            }
            selectableCount++;
        }
        return selectableCount > 0 && selectedOrderIds.size() == selectableCount;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderHistoryResponse.OrderItem item = items.get(position);
        String orderId = item.getOrderId() == null ? "" : item.getOrderId();
        holder.tvOrderId.setText("Order #" + shortId(orderId));
        String status = item.getStatus() == null ? "" : item.getStatus();
        holder.tvStatus.setText(status);
        int statusColor = holder.itemView.getContext()
                .getColor(StatusColorUtil.getStatusColorRes(status));
        holder.tvStatus.setTextColor(statusColor);

        holder.tvCreatedAt.setText("Ngày: " + formatDateTime(item.getCreatedAt()));
        String pickup = item.getPickupAt();
        if (pickup == null || pickup.trim().isEmpty()) {
            holder.tvPickupAt.setVisibility(View.GONE);
        } else {
            holder.tvPickupAt.setVisibility(View.VISIBLE);
            holder.tvPickupAt.setText("Nhận: " + formatDateTime(pickup));
        }

        int totalQty = sumQuantity(item.getDishes());
        holder.tvItemsCount.setText("Món: " + totalQty);

        double amount = item.getFinalAmount() > 0 ? item.getFinalAmount() : item.getTotalPrice();
        holder.tvTotal.setText("Tổng: " + formatCurrency(amount));

        boolean showCancel = !selectionMode
                && cancelEnabled
                && onCancelClickListener != null
                && shouldShowCancel(item.getStatus());
        holder.btnCancelOrder.setVisibility(showCancel ? View.VISIBLE : View.GONE);
        holder.btnCancelOrder.setOnClickListener(showCancel ? v -> onCancelClickListener.onCancelClick(item) : null);

        String normalizedOrderId = item.getOrderId() == null ? "" : item.getOrderId().trim();
        holder.cbSelectOrder.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
        holder.cbSelectOrder.setOnCheckedChangeListener(null);
        holder.cbSelectOrder.setChecked(selectionMode && selectedOrderIds.contains(normalizedOrderId));
        holder.cbSelectOrder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (normalizedOrderId.isEmpty()) {
                return;
            }
            if (isChecked) {
                selectedOrderIds.add(normalizedOrderId);
            } else {
                selectedOrderIds.remove(normalizedOrderId);
            }
            notifySelectionChanged();
        });

        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                holder.cbSelectOrder.toggle();
                return;
            }
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void syncSelectionWithCurrentItems() {
        if (selectedOrderIds.isEmpty()) {
            return;
        }
        Set<String> validIds = new HashSet<>();
        for (OrderHistoryResponse.OrderItem item : items) {
            if (item == null || item.getOrderId() == null) {
                continue;
            }
            String id = item.getOrderId().trim();
            if (!id.isEmpty()) {
                validIds.add(id);
            }
        }
        selectedOrderIds.retainAll(validIds);
    }

    private void notifySelectionChanged() {
        if (onSelectionChangeListener == null) {
            return;
        }
        onSelectionChangeListener.onSelectionChanged(
                selectedOrderIds.size(),
                items.size(),
                isAllSelected());
    }

    private int sumQuantity(List<OrderHistoryResponse.OrderDish> dishes) {
        int total = 0;
        if (dishes == null) {
            return total;
        }
        for (OrderHistoryResponse.OrderDish dish : dishes) {
            total += dish == null ? 0 : dish.getQuantity();
        }
        return total;
    }

    private String shortId(String id) {
        if (id == null) {
            return "";
        }
        String trimmed = id.trim();
        if (trimmed.length() <= 8) {
            return trimmed;
        }
        return trimmed.substring(trimmed.length() - 8);
    }

    private String formatDateTime(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "--";
        }
        Date date = parseIsoDate(input);
        if (date == null) {
            return input.replace("T", " ").replace("Z", "");
        }
        SimpleDateFormat out = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return out.format(date);
    }

    private Date parseIsoDate(String input) {
        String value = input.trim();
        String[] patterns = new String[] {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                return sdf.parse(value);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    private String formatCurrency(double amount) {
        return CurrencyFormatter.formatVnd(amount);
    }

    private boolean shouldShowCancel(String status) {
        if (status == null) {
            return true;
        }
        return !"COMPLETED".equalsIgnoreCase(status.trim());
    }

    public interface OnItemClickListener {
        void onItemClick(OrderHistoryResponse.OrderItem item);
    }

    public interface OnCancelClickListener {
        void onCancelClick(OrderHistoryResponse.OrderItem item);
    }

    public interface OnSelectionChangeListener {
        void onSelectionChanged(int selectedCount, int totalCount, boolean allSelected);
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvOrderId;
        private final TextView tvStatus;
        private final TextView tvCreatedAt;
        private final TextView tvPickupAt;
        private final TextView tvItemsCount;
        private final TextView tvTotal;
        private final Button btnCancelOrder;
        private final CheckBox cbSelectOrder;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            tvPickupAt = itemView.findViewById(R.id.tvPickupAt);
            tvItemsCount = itemView.findViewById(R.id.tvItemsCount);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            btnCancelOrder = itemView.findViewById(R.id.btnCancelOrder);
            cbSelectOrder = itemView.findViewById(R.id.cbSelectOrder);
        }
    }
}
