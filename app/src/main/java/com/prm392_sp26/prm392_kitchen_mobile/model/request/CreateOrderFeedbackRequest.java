package com.prm392_sp26.prm392_kitchen_mobile.model.request;

import com.google.gson.annotations.SerializedName;

public class CreateOrderFeedbackRequest {

    @SerializedName("rating")
    private final int rating;

    @SerializedName("title")
    private final String title;

    @SerializedName("content")
    private final String content;

    public CreateOrderFeedbackRequest(int rating, String title, String content) {
        this.rating = rating;
        this.title = title;
        this.content = content;
    }

    public int getRating() {
        return rating;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
