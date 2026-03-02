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

    public int getDishId() { return dishId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getStatus() { return status; }
    public String getImageUrl() { return imageUrl; }
    public double getCalories() { return calories; }
    public List<DishStepResponse> getSteps() { return steps; }
}
