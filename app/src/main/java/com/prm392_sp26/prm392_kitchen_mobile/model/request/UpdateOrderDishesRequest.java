package com.prm392_sp26.prm392_kitchen_mobile.model.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UpdateOrderDishesRequest {

    @SerializedName("dishes")
    private List<DishInput> dishes;

    public UpdateOrderDishesRequest(List<DishInput> dishes) {
        this.dishes = dishes;
    }

    public List<DishInput> getDishes() {
        return dishes;
    }

    public void setDishes(List<DishInput> dishes) {
        this.dishes = dishes;
    }

    public static class DishInput {
        @SerializedName("dishId")
        private Integer dishId;

        @SerializedName("customDish")
        private CustomDish customDish;

        @SerializedName("quantity")
        private int quantity;

        public DishInput(Integer dishId, CustomDish customDish, int quantity) {
            this.dishId = dishId;
            this.customDish = customDish;
            this.quantity = quantity;
        }

        public static DishInput fromDish(int dishId, int quantity) {
            return new DishInput(dishId, null, quantity);
        }

        public static DishInput fromCustom(CustomDish customDish, int quantity) {
            return new DishInput(null, customDish, quantity);
        }

        public Integer getDishId() {
            return dishId;
        }

        public void setDishId(Integer dishId) {
            this.dishId = dishId;
        }

        public CustomDish getCustomDish() {
            return customDish;
        }

        public void setCustomDish(CustomDish customDish) {
            this.customDish = customDish;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    public static class CustomDish {
        @SerializedName("name")
        private String name;

        @SerializedName("description")
        private String description;

        @SerializedName("steps")
        private List<Step> steps;

        public CustomDish(String name, String description, List<Step> steps) {
            this.name = name;
            this.description = description;
            this.steps = steps;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<Step> getSteps() {
            return steps;
        }

        public void setSteps(List<Step> steps) {
            this.steps = steps;
        }
    }

    public static class Step {
        @SerializedName("stepId")
        private int stepId;

        @SerializedName("itemIds")
        private List<Integer> itemIds;

        @SerializedName("items")
        private List<StepItem> items;

        public Step(int stepId, List<Integer> itemIds, List<StepItem> items) {
            this.stepId = stepId;
            this.itemIds = itemIds;
            this.items = items;
        }

        public int getStepId() {
            return stepId;
        }

        public void setStepId(int stepId) {
            this.stepId = stepId;
        }

        public List<Integer> getItemIds() {
            return itemIds;
        }

        public void setItemIds(List<Integer> itemIds) {
            this.itemIds = itemIds;
        }

        public List<StepItem> getItems() {
            return items;
        }

        public void setItems(List<StepItem> items) {
            this.items = items;
        }
    }

    public static class StepItem {
        @SerializedName("itemId")
        private int itemId;

        @SerializedName("quantity")
        private double quantity;

        @SerializedName("note")
        private String note;

        public StepItem(int itemId, double quantity, String note) {
            this.itemId = itemId;
            this.quantity = quantity;
            this.note = note;
        }

        public int getItemId() {
            return itemId;
        }

        public void setItemId(int itemId) {
            this.itemId = itemId;
        }

        public double getQuantity() {
            return quantity;
        }

        public void setQuantity(double quantity) {
            this.quantity = quantity;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }
}
