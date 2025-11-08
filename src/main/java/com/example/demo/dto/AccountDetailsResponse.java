package com.example.demo.dto;

import java.math.BigDecimal;

public class AccountDetailsResponse {
    
    private boolean success;
    private String message;
    private AccountDetails accountDetails;
    
    // Constructors
    public AccountDetailsResponse() {
    }
    
    public AccountDetailsResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public AccountDetailsResponse(boolean success, String message, AccountDetails accountDetails) {
        this.success = success;
        this.message = message;
        this.accountDetails = accountDetails;
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
    
    public AccountDetails getAccountDetails() {
        return accountDetails;
    }
    
    public void setAccountDetails(AccountDetails accountDetails) {
        this.accountDetails = accountDetails;
    }
    
    // Inner class for account details
    public static class AccountDetails {
        private Long accountId;
        private String accountNumber;
        private BigDecimal balance;
        private String accountType;
        private Long customerId;
        private String customerName;
        
        public AccountDetails() {
        }
        
        public AccountDetails(Long accountId, String accountNumber, BigDecimal balance, String accountType, Long customerId, String customerName) {
            this.accountId = accountId;
            this.accountNumber = accountNumber;
            this.balance = balance;
            this.accountType = accountType;
            this.customerId = customerId;
            this.customerName = customerName;
        }
        
        // Getters and Setters
        public Long getAccountId() {
            return accountId;
        }
        
        public void setAccountId(Long accountId) {
            this.accountId = accountId;
        }
        
        public String getAccountNumber() {
            return accountNumber;
        }
        
        public void setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
        }
        
        public BigDecimal getBalance() {
            return balance;
        }
        
        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }
        
        public String getAccountType() {
            return accountType;
        }
        
        public void setAccountType(String accountType) {
            this.accountType = accountType;
        }
        
        public Long getCustomerId() {
            return customerId;
        }
        
        public void setCustomerId(Long customerId) {
            this.customerId = customerId;
        }
        
        public String getCustomerName() {
            return customerName;
        }
        
        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }
    }
}
