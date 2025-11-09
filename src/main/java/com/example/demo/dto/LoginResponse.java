package com.example.demo.dto;

public class LoginResponse {
    
    private boolean success;
    private String message;
    private CustomerDetails customerDetails;
    
    // Constructors
    public LoginResponse() {
    }
    
    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public LoginResponse(boolean success, String message, CustomerDetails customerDetails) {
        this.success = success;
        this.message = message;
        this.customerDetails = customerDetails;
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
    
    public CustomerDetails getCustomerDetails() {
        return customerDetails;
    }
    
    public void setCustomerDetails(CustomerDetails customerDetails) {
        this.customerDetails = customerDetails;
    }
    
    // Inner class for customer details
    public static class CustomerDetails {
        private Long customerId;
        private String username;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String address;
        
        public CustomerDetails() {
        }
        
        public CustomerDetails(Long customerId, String username, String firstName, String lastName, String email, String phoneNumber, String address) {
            this.customerId = customerId;
            this.username = username;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.address = address;
        }
        
        // Getters and Setters
        public Long getCustomerId() {
            return customerId;
        }
        
        public void setCustomerId(Long customerId) {
            this.customerId = customerId;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getFirstName() {
            return firstName;
        }
        
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getPhoneNumber() {
            return phoneNumber;
        }
        
        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
        
        public String getAddress() {
            return address;
        }
        
        public void setAddress(String address) {
            this.address = address;
        }
    }
}
