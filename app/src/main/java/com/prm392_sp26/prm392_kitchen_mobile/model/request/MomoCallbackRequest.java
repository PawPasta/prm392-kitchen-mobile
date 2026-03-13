package com.prm392_sp26.prm392_kitchen_mobile.model.request;

import com.google.gson.annotations.SerializedName;

public class MomoCallbackRequest {

    @SerializedName("partnerCode")
    private final String partnerCode;

    @SerializedName("orderId")
    private final String orderId;

    @SerializedName("requestId")
    private final String requestId;

    @SerializedName("amount")
    private final Long amount;

    @SerializedName("orderInfo")
    private final String orderInfo;

    @SerializedName("orderType")
    private final String orderType;

    @SerializedName("transId")
    private final String transId;

    @SerializedName("resultCode")
    private final Integer resultCode;

    @SerializedName("message")
    private final String message;

    @SerializedName("payType")
    private final String payType;

    @SerializedName("responseTime")
    private final Long responseTime;

    @SerializedName("extraData")
    private final String extraData;

    @SerializedName("signature")
    private final String signature;

    public MomoCallbackRequest(
            String partnerCode,
            String orderId,
            String requestId,
            Long amount,
            String orderInfo,
            String orderType,
            String transId,
            Integer resultCode,
            String message,
            String payType,
            Long responseTime,
            String extraData,
            String signature) {
        this.partnerCode = partnerCode;
        this.orderId = orderId;
        this.requestId = requestId;
        this.amount = amount;
        this.orderInfo = orderInfo;
        this.orderType = orderType;
        this.transId = transId;
        this.resultCode = resultCode;
        this.message = message;
        this.payType = payType;
        this.responseTime = responseTime;
        this.extraData = extraData;
        this.signature = signature;
    }
}
