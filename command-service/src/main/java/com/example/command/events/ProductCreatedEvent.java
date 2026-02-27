package com.example.command.events;

import java.math.BigDecimal;

public class ProductCreatedEvent {
    private String eventType = "ProductCreated";
    private Payload payload;

    public static class Payload {
        private Long id;
        private String name;
        private String category;
        private BigDecimal price;

        public Payload() {}

        public Payload(Long id, String name, String category, BigDecimal price) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }

    public ProductCreatedEvent() {}

    public ProductCreatedEvent(Payload payload) {
        this.payload = payload;
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Payload getPayload() { return payload; }
    public void setPayload(Payload payload) { this.payload = payload; }
}
