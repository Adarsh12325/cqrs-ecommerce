package com.example.command.events;

import com.example.command.Order;
import java.util.List;

public class OrderCreatedEvent {
    private String eventType = "OrderCreated";
    private Payload payload;

    public static class Payload {
        private Long id;
        private Long customerId;
        private String status;
        private List<Order.OrderItem> items;

        public Payload() {}

        public Payload(Long id, Long customerId, String status, List<Order.OrderItem> items) {
            this.id = id;
            this.customerId = customerId;
            this.status = status;
            this.items = items;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public List<Order.OrderItem> getItems() { return items; }
        public void setItems(List<Order.OrderItem> items) { this.items = items; }
    }

    public OrderCreatedEvent() {}
    public OrderCreatedEvent(Payload payload) {
        this.payload = payload;
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Payload getPayload() { return payload; }
    public void setPayload(Payload payload) { this.payload = payload; }
}
