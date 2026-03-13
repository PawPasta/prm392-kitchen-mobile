package com.prm392_sp26.prm392_kitchen_mobile.model.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DishDetailResponse {
    @SerializedName("dishId") private int dishId;
    @SerializedName("name") private String name;
    @SerializedName("description") private String description;
    @SerializedName("price") private double price;
    @SerializedName("status") private String status;
    @SerializedName("imageUrl") private String imageUrl;
    @SerializedName("calories") private double calories;
    @SerializedName("steps") private List<DishStepResponse> steps;
    @SerializedName("totalNutrients") private List<DishNutrient> totalNutrients;

    public int getDishId() { return dishId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getStatus() { return status; }
    public String getImageUrl() { return imageUrl; }
    public double getCalories() { return calories; }
    public List<DishStepResponse> getSteps() { return steps; }
    public List<DishNutrient> getTotalNutrients() { return totalNutrients; }

    public static class DishNutrient {
        @SerializedName("nutrientId") private int nutrientId;
        @SerializedName("nutrientName") private String nutrientName;
        @SerializedName("nutrientDescription") private String nutrientDescription;
        @SerializedName("nutrientUnit") private String nutrientUnit;
        @SerializedName("totalAmount") private double totalAmount;

        public int getNutrientId() { return nutrientId; }
        public String getNutrientName() { return nutrientName; }
        public String getNutrientDescription() { return nutrientDescription; }
        public String getNutrientUnit() { return nutrientUnit; }
        public double getTotalAmount() { return totalAmount; }
    }
}
