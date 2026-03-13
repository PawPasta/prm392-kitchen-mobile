package com.prm392_sp26.prm392_kitchen_mobile.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchResponse {

    @SerializedName("dishes")
    private List<DishResponse> dishes;

    @SerializedName("items")
    private List<ItemResponse> items;

    public List<DishResponse> getDishes() {
        return dishes;
    }

    public List<ItemResponse> getItems() {
        return items;
    }
}
