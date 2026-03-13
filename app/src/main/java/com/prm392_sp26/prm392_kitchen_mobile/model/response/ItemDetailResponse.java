package com.prm392_sp26.prm392_kitchen_mobile.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ItemDetailResponse {

    @SerializedName("itemId")
    private int itemId;

    @SerializedName("stepId")
    private int stepId;

    @SerializedName("stepName")
    private String stepName;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("baseQuantity")
    private double baseQuantity;

    @SerializedName("unit")
    private String unit;

    @SerializedName("caloriesPerUnit")
    private double caloriesPerUnit;

    @SerializedName("calories")
    private double calories;

    @SerializedName("price")
    private double price;

    @SerializedName("status")
    private String status;

    @SerializedName("note")
    private String note;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("nutrients")
    private List<ItemNutrient> nutrients;

    public int getItemId() {
        return itemId;
    }

    public int getStepId() {
        return stepId;
    }

    public String getStepName() {
        return stepName;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getBaseQuantity() {
        return baseQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public double getCaloriesPerUnit() {
        return caloriesPerUnit;
    }

    public double getCalories() {
        return calories;
    }

    public double getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public List<ItemNutrient> getNutrients() {
        return nutrients;
    }

    public static class ItemNutrient {
        @SerializedName("nutrientId")
        private int nutrientId;

        @SerializedName("nutrientName")
        private String nutrientName;

        @SerializedName("nutrientDescription")
        private String nutrientDescription;

        @SerializedName("nutrientUnit")
        private String nutrientUnit;

        @SerializedName("amount")
        private double amount;

        @SerializedName("itemBaseQuantity")
        private double itemBaseQuantity;

        @SerializedName("itemBaseUnit")
        private String itemBaseUnit;

        public int getNutrientId() {
            return nutrientId;
        }

        public String getNutrientName() {
            return nutrientName;
        }

        public String getNutrientDescription() {
            return nutrientDescription;
        }

        public String getNutrientUnit() {
            return nutrientUnit;
        }

        public double getAmount() {
            return amount;
        }

        public double getItemBaseQuantity() {
            return itemBaseQuantity;
        }

        public String getItemBaseUnit() {
            return itemBaseUnit;
        }
    }
}
