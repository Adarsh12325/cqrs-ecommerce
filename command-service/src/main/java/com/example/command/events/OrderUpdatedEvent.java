package com.example.command.events;

public class OrderUpdatedEvent {
    private String eventType = "OrderUpdated";
    private Payload payload;

    public static class Payload {
        private Long orderId;
        private String newStatus;

        public Payload() {}

        public Payload(Long orderId, String newStatus) {
            this.orderId = orderId;
            this.newStatus = newStatus;
        }

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public String getNewStatus() { return newStatus; }
        public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    }

    public OrderUpdatedEvent() {}
    public OrderUpdatedEvent(Payload payload) {
        this.payload = payload;
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Payload getPayload() { return payload; }
    public void setPayload(Payload payload) { this.payload = payload; }
}
