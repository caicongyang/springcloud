package com.caicongyang.cloud.order.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class Order implements Serializable {


    private String userId;

    private String OrderNo;

    private BigDecimal amount;

    public String getOrderNo() {
        return OrderNo;
    }

    public void setOrderNo(String orderNo) {
        OrderNo = orderNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
