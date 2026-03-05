package com.prm392_sp26.prm392_kitchen_mobile.model.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response body cho API POST /api/orders/from-dish và POST /api/orders/custom
 * Chứa thông tin chi tiết của đơn hàng vừa tạo
 */
public class OrderResponse {

    @SerializedName("orderId")
    private String orderId;

    @SerializedName("userId")
    private String userId;

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
    private List<OrderDishDetail> dishes;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public List<OrderDishDetail> getDishes() {
        return dishes;
    }

    public void setDishes(List<OrderDishDetail> dishes) {
        this.dishes = dishes;
    }

    public static class OrderDishDetail {
        @SerializedName("orderDishId")
        private int orderDishId;

        @SerializedName("dishId")
        private int dishId;

        @SerializedName("dishName")
        private String dishName;

        @SerializedName("dishStatus")
        private String dishStatus;

        @SerializedName("dishPrice")
        private double dishPrice;

        @SerializedName("dishCalories")
        private double dishCalories;

        @SerializedName("quantity")
        private int quantity;

        @SerializedName("lineTotal")
        private double lineTotal;

        @SerializedName("customItems")
        private List<OrderDishItemDetail> customItems;

        public int getOrderDishId() {
            return orderDishId;
        }

        public void setOrderDishId(int orderDishId) {
            this.orderDishId = orderDishId;
        }

        public int getDishId() {
            return dishId;
        }

        public void setDishId(int dishId) {
            this.dishId = dishId;
        }

        public String getDishName() {
            return dishName;
        }

        public void setDishName(String dishName) {
            this.dishName = dishName;
        }

        public String getDishStatus() {
            return dishStatus;
        }

        public void setDishStatus(String dishStatus) {
            this.dishStatus = dishStatus;
        }

        public double getDishPrice() {
            return dishPrice;
        }

        public void setDishPrice(double dishPrice) {
            this.dishPrice = dishPrice;
        }

        public double getDishCalories() {
            return dishCalories;
        }

        public void setDishCalories(double dishCalories) {
            this.dishCalories = dishCalories;
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

        public List<OrderDishItemDetail> getCustomItems() {
            return customItems;
        }

        public void setCustomItems(List<OrderDishItemDetail> customItems) {
            this.customItems = customItems;
        }
    }

    public static class OrderDishItemDetail {
        @SerializedName("orderDishItemId")
        private int orderDishItemId;

        @SerializedName("stepId")
        private int stepId;

        @SerializedName("itemId")
        private int itemId;

        @SerializedName("itemName")
        private String itemName;

        @SerializedName("unit")
        private String unit;

        @SerializedName("quantity")
        private double quantity;

        @SerializedName("price")
        private double price;

        @SerializedName("calories")
        private double calories;

        @SerializedName("note")
        private String note;

        public int getOrderDishItemId() {
            return orderDishItemId;
        }

        public void setOrderDishItemId(int orderDishItemId) {
            this.orderDishItemId = orderDishItemId;
        }

        public int getStepId() {
            return stepId;
        }

        public void setStepId(int stepId) {
            this.stepId = stepId;
        }

        public int getItemId() {
            return itemId;
        }

        public void setItemId(int itemId) {
            this.itemId = itemId;
        }

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

        public double getCalories() {
            return calories;
        }

        public void setCalories(double calories) {
            this.calories = calories;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }
}

