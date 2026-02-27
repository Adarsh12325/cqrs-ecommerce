package com.example.command.dto;

import java.math.BigDecimal;

public class ProductRequest {
    private String name;
    private String category;
    private BigDecimal price;

    public ProductRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
