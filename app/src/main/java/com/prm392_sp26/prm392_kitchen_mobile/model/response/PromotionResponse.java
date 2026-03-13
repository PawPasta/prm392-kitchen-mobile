package com.prm392_sp26.prm392_kitchen_mobile.model.response;

import com.google.gson.annotations.SerializedName;

public class PromotionResponse {

    @SerializedName("promotionId")
    private int promotionId;

    @SerializedName("name")
    private String name;

    @SerializedName("code")
    private String code;

    @SerializedName("description")
    private String description;

    @SerializedName("discountType")
    private String discountType;

    @SerializedName("discountValue")
    private double discountValue;

    @SerializedName("status")
    private String status;

    @SerializedName("quantity")
    private int quantity;

    public int getPromotionId() {
        return promotionId;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getDiscountType() {
        return discountType;
    }

    public double getDiscountValue() {
        return discountValue;
    }

    public String getStatus() {
        return status;
    }

    public int getQuantity() {
        return quantity;
    }
}
