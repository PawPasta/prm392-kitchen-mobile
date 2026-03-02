package com.prm392_sp26.prm392_kitchen_mobile.model.response;

import com.google.gson.annotations.SerializedName;

public class DishStepResponse {
    @SerializedName("dishStepId") private int dishStepId;
    @SerializedName("stepId") private int stepId;
    @SerializedName("stepName") private String stepName;
    @SerializedName("stepDescription") private String stepDescription;
    @SerializedName("stepNumber") private int stepNumber;
    @SerializedName("stepStatus") private String stepStatus;
    @SerializedName("stepOrder") private int stepOrder;
    @SerializedName("minSelect") private int minSelect;
    @SerializedName("maxSelect") private int maxSelect;
    @SerializedName("isRequired") private boolean isRequired;

    public int getDishStepId() { return dishStepId; }
    public int getStepId() { return stepId; }
    public String getStepName() { return stepName; }
    public String getStepDescription() { return stepDescription; }
    public int getStepNumber() { return stepNumber; }
    public String getStepStatus() { return stepStatus; }
    public int getStepOrder() { return stepOrder; }
    public int getMinSelect() { return minSelect; }
    public int getMaxSelect() { return maxSelect; }
    public boolean isRequired() { return isRequired; }
}
