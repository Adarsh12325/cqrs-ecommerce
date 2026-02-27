package com.example.command;
import jakarta.persistence.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long customerId;
    private String status = "CREATED";
    @Column(name = "items_json")
    private String itemsJson;
    private transient List<OrderItem> items;
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public static class OrderItem {
        private Long productId;
        private Integer quantity;
        private Double price;
        // constructors, getters, setters
        public OrderItem() {}
        public OrderItem(Long productId, Integer quantity, Double price) {
            this.productId = productId;
            this.quantity = quantity;
            this.price = price;
        }
        // getters and setters...
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
    }
    
    // getters and setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<OrderItem> getItems() {
    if (items == null && itemsJson != null) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            items = mapper.readValue(itemsJson,
                    mapper.getTypeFactory().constructCollectionType(List.class, OrderItem.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    return items;
}

    public void setItems(List<OrderItem> items) { 
        this.items = items; 
        try {
            this.itemsJson = new ObjectMapper().writeValueAsString(items);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public String getItemsJson() { return itemsJson; }
    public void setItemsJson(String itemsJson) { this.itemsJson = itemsJson; }
}
