package com.example.demo.dto;

import java.math.BigDecimal;

public class OrderPlacementResponse {
    
    private boolean success;
    private String message;
    private OrderDetails orderDetails;
    
    // Constructors
    public OrderPlacementResponse() {
    }
    
    public OrderPlacementResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public OrderPlacementResponse(boolean success, String message, OrderDetails orderDetails) {
        this.success = success;
        this.message = message;
        this.orderDetails = orderDetails;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public OrderDetails getOrderDetails() {
        return orderDetails;
    }
    
    public void setOrderDetails(OrderDetails orderDetails) {
        this.orderDetails = orderDetails;
    }
    
    // Inner class for order details
    public static class OrderDetails {
        private Long orderId;
        private String orderNumber;
        private String productName;
        private String merchantName;
        private Integer quantity;
        private BigDecimal totalAmount;
        private BigDecimal walletFee;
        private String currency;
        private String orderStatus;
        private BigDecimal remainingWalletBalance;
        
        public OrderDetails() {
        }
        
        // Getters and Setters
        public Long getOrderId() {
            return orderId;
        }
        
        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }
        
        public String getOrderNumber() {
            return orderNumber;
        }
        
        public void setOrderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
        }
        
        public String getProductName() {
            return productName;
        }
        
        public void setProductName(String productName) {
            this.productName = productName;
        }
        
        public String getMerchantName() {
            return merchantName;
        }
        
        public void setMerchantName(String merchantName) {
            this.merchantName = merchantName;
        }
        
        public Integer getQuantity() {
            return quantity;
        }
        
        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
        
        public BigDecimal getTotalAmount() {
            return totalAmount;
        }
        
        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }
        
        public BigDecimal getWalletFee() {
            return walletFee;
        }
        
        public void setWalletFee(BigDecimal walletFee) {
            this.walletFee = walletFee;
        }
        
        public String getCurrency() {
            return currency;
        }
        
        public void setCurrency(String currency) {
            this.currency = currency;
        }
        
        public String getOrderStatus() {
            return orderStatus;
        }
        
        public void setOrderStatus(String orderStatus) {
            this.orderStatus = orderStatus;
        }
        
        public BigDecimal getRemainingWalletBalance() {
            return remainingWalletBalance;
        }
        
        public void setRemainingWalletBalance(BigDecimal remainingWalletBalance) {
            this.remainingWalletBalance = remainingWalletBalance;
        }
    }
}
