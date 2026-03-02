package com.prm392_sp26.prm392_kitchen_mobile.shared;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PageResponse<T> {

    @SerializedName("content")
    private List<T> content;

    @SerializedName("totalElements")
    private long totalElements;

    @SerializedName("totalPages")
    private int totalPages;

    @SerializedName("last")
    private boolean last;

    // Getters
    public List<T> getContent() { return content; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public boolean isLast() { return last; }
}
