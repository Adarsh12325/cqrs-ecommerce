package com.example.command.dto;

import java.util.List;

public class OrderRequest {
    private Long customerId;
    private List<OrderItemRequest> items;

    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
        private Double price;

        public OrderItemRequest() {}

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
    }

    public OrderRequest() {}

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
}
