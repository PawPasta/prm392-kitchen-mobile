package com.prm392_sp26.prm392_kitchen_mobile.model.response;

import com.google.gson.annotations.SerializedName;

public class ItemResponse {
    @SerializedName("itemId") private int itemId;
    @SerializedName("stepId") private int stepId;
    @SerializedName("stepName") private String stepName;
    @SerializedName("name") private String name;
    @SerializedName("description") private String description;
    @SerializedName("imageUrl") private String imageUrl;
    @SerializedName("baseQuantity") private double baseQuantity;
    @SerializedName("unit") private String unit;        // G, ML, PCS
    @SerializedName("caloriesPerUnit") private double caloriesPerUnit;
    @SerializedName("calories") private double calories;
    @SerializedName("price") private double price;
    @SerializedName("status") private String status;    // ENABLE, DISABLE, EXPIRED
    @SerializedName("note") private String note;

    private boolean isSelected;

    public int getItemId() { return itemId; }
    public int getStepId() { return stepId; }
    public String getStepName() { return stepName; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public double getBaseQuantity() { return baseQuantity; }
    public String getUnit() { return unit; }
    public double getCaloriesPerUnit() { return caloriesPerUnit; }
    public double getCalories() { return calories; }
    public double getPrice() { return price; }
    public String getStatus() { return status; }
    public String getNote() { return note; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}
