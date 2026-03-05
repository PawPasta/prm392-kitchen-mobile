package com.prm392_sp26.prm392_kitchen_mobile.model.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Request body cho API POST /api/orders/custom
 * Tạo đơn hàng từ custom dish builder
 */
public class CreateCustomOrderRequest {

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("pickupAt")
    private String pickupAt;

    @SerializedName("note")
    private String note;

    @SerializedName("customDish")
    private CustomDish customDish;

    public CreateCustomOrderRequest() {
    }

    public CreateCustomOrderRequest(int quantity, String pickupAt, String note, CustomDish customDish) {
        this.quantity = quantity;
        this.pickupAt = pickupAt;
        this.note = note;
        this.customDish = customDish;
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

    public CustomDish getCustomDish() {
        return customDish;
    }

    public void setCustomDish(CustomDish customDish) {
        this.customDish = customDish;
    }

    public static class CustomDish {
        @SerializedName("name")
        private String name;

        @SerializedName("description")
        private String description;

        @SerializedName("imageUrl")
        private String imageUrl;

        @SerializedName("steps")
        private List<Step> steps;

        public CustomDish() {
        }

        public CustomDish(String name, String description, String imageUrl, List<Step> steps) {
            this.name = name;
            this.description = description;
            this.imageUrl = imageUrl;
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

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
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
        private List<Item> items;

        public Step() {
        }

        public Step(int stepId, List<Integer> itemIds, List<Item> items) {
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

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }
    }

    public static class Item {
        @SerializedName("itemId")
        private int itemId;

        @SerializedName("quantity")
        private double quantity;

        @SerializedName("note")
        private String note;

        public Item() {
        }

        public Item(int itemId, double quantity, String note) {
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

