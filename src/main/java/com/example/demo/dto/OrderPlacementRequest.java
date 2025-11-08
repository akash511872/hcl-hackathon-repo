package com.example.demo.dto;

import java.math.BigDecimal;

public class OrderPlacementRequest {
    
    private Long customerId;
    private Long productId;
    private Integer quantity;
    private BigDecimal productCost;
    private String currency;
    
    // Constructors
    public OrderPlacementRequest() {
    }
    
    public OrderPlacementRequest(Long customerId, Long productId, Integer quantity, BigDecimal productCost, String currency) {
        this.customerId = customerId;
        this.productId = productId;
        this.quantity = quantity;
        this.productCost = productCost;
        this.currency = currency;
    }
    
    // Getters and Setters
    public Long getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getProductCost() {
        return productCost;
    }
    
    public void setProductCost(BigDecimal productCost) {
        this.productCost = productCost;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
