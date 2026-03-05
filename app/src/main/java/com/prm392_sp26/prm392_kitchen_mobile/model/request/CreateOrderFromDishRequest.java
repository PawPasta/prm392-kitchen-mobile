package com.prm392_sp26.prm392_kitchen_mobile.model.request;

import com.google.gson.annotations.SerializedName;

/**
 * Request body cho API POST /api/orders/from-dish
 * Tạo đơn hàng từ một dish có sẵn
 */
public class CreateOrderFromDishRequest {

    @SerializedName("dishId")
    private int dishId;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("pickupAt")
    private String pickupAt;

    @SerializedName("note")
    private String note;

    public CreateOrderFromDishRequest() {
    }

    public CreateOrderFromDishRequest(int dishId, int quantity, String pickupAt, String note) {
        this.dishId = dishId;
        this.quantity = quantity;
        this.pickupAt = pickupAt;
        this.note = note;
    }

    public int getDishId() {
        return dishId;
    }

    public void setDishId(int dishId) {
        this.dishId = dishId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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
}
