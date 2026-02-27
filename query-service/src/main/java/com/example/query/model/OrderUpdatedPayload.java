package com.example.query.model;

public class OrderUpdatedPayload {
    private Long orderId;
    private String newStatus;

    public OrderUpdatedPayload() {}

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
}
