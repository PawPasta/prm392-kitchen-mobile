package com.prm392_sp26.prm392_kitchen_mobile.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrderHistoryResponse {

    @SerializedName("selectedStatus")
    private String selectedStatus;

    @SerializedName("orders")
    private OrderPage orders;

    @SerializedName("statusSummary")
    private List<StatusSummary> statusSummary;

    public String getSelectedStatus() {
        return selectedStatus;
    }

    public void setSelectedStatus(String selectedStatus) {
        this.selectedStatus = selectedStatus;
    }

    public OrderPage getOrders() {
        return orders;
    }

    public void setOrders(OrderPage orders) {
        this.orders = orders;
    }

    public List<StatusSummary> getStatusSummary() {
        return statusSummary;
    }

    public void setStatusSummary(List<StatusSummary> statusSummary) {
        this.statusSummary = statusSummary;
    }

    public static class OrderPage {
        @SerializedName("content")
        private List<OrderItem> content;

        @SerializedName("pageNumber")
        private int pageNumber;

        @SerializedName("pageSize")
        private int pageSize;

        @SerializedName("totalElements")
        private long totalElements;

        @SerializedName("totalPages")
        private int totalPages;

        @SerializedName("first")
        private boolean first;

        @SerializedName("last")
        private boolean last;

        public List<OrderItem> getContent() {
            return content;
        }

        public void setContent(List<OrderItem> content) {
            this.content = content;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public void setPageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public void setTotalElements(long totalElements) {
            this.totalElements = totalElements;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public boolean isFirst() {
            return first;
        }

        public void setFirst(boolean first) {
            this.first = first;
        }

        public boolean isLast() {
            return last;
        }

        public void setLast(boolean last) {
            this.last = last;
        }
    }

    public static class OrderItem {
        @SerializedName("orderId")
        private String orderId;

        @SerializedName("totalPrice")
        private double totalPrice;

        @SerializedName("discountAmount")
        private double discountAmount;

        @SerializedName("finalAmount")
        private double finalAmount;

        @SerializedName("status")
        private String status;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("pickupAt")
        private String pickupAt;

        @SerializedName("note")
        private String note;

        @SerializedName("dishes")
        private List<OrderDish> dishes;

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public double getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(double totalPrice) {
            this.totalPrice = totalPrice;
        }

        public double getDiscountAmount() {
            return discountAmount;
        }

        public void setDiscountAmount(double discountAmount) {
            this.discountAmount = discountAmount;
        }

        public double getFinalAmount() {
            return finalAmount;
        }

        public void setFinalAmount(double finalAmount) {
            this.finalAmount = finalAmount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getPickupAt() {
            return pickupAt;
        }

        public void setPickupAt(String pickupAt) {
            this.pickupAt = pickupAt;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public List<OrderDish> getDishes() {
            return dishes;
        }

        public void setDishes(List<OrderDish> dishes) {
            this.dishes = dishes;
        }
    }

    public static class OrderDish {
        @SerializedName("dishName")
        private String dishName;

        @SerializedName("quantity")
        private int quantity;

        @SerializedName("lineTotal")
        private double lineTotal;

        @SerializedName("customItems")
        private List<OrderDishItem> customItems;

        public String getDishName() {
            return dishName;
        }

        public void setDishName(String dishName) {
            this.dishName = dishName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getLineTotal() {
            return lineTotal;
        }

        public void setLineTotal(double lineTotal) {
            this.lineTotal = lineTotal;
        }

        public List<OrderDishItem> getCustomItems() {
            return customItems;
        }

        public void setCustomItems(List<OrderDishItem> customItems) {
            this.customItems = customItems;
        }
    }

    public static class OrderDishItem {
        @SerializedName("itemName")
        private String itemName;

        @SerializedName("unit")
        private String unit;

        @SerializedName("quantity")
        private double quantity;

        @SerializedName("price")
        private double price;

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public double getQuantity() {
            return quantity;
        }

        public void setQuantity(double quantity) {
            this.quantity = quantity;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }

    public static class StatusSummary {
        @SerializedName("status")
        private String status;

        @SerializedName("count")
        private long count;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }
}
