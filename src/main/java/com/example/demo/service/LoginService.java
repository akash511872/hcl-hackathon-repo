package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.model.Customer;
import com.example.demo.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class LoginService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    // Password validation pattern: minimum 8 characters, at least one uppercase, one lowercase, one digit, one special character
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );
    
    public LoginResponse login(LoginRequest loginRequest) {
        // Validate input
        if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
            return new LoginResponse(false, "Username is required");
        }
        
        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            return new LoginResponse(false, "Password is required");
        }
        
        // Validate password format
        if (!isValidPassword(loginRequest.getPassword())) {
            return new LoginResponse(false, "Invalid password format. Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character");
        }
        
        // Find customer by username
        Optional<Customer> customerOpt = customerRepository.findByUsername(loginRequest.getUsername());
        
        if (!customerOpt.isPresent()) {
            return new LoginResponse(false, "Invalid username or password");
        }
        
        Customer customer = customerOpt.get();
        
        // Verify password (in production, use proper password hashing like BCrypt)
        if (!customer.getPassword().equals(loginRequest.getPassword())) {
            return new LoginResponse(false, "Invalid username or password");
        }
        
        // Create customer details for response (excluding password)
        LoginResponse.CustomerDetails customerDetails = new LoginResponse.CustomerDetails(
            customer.getCustomerId(),
            customer.getUsername(),
            customer.getFirstName(),
            customer.getLastName(),
            customer.getEmail(),
            customer.getPhoneNumber(),
            customer.getAddress()
        );
        
        return new LoginResponse(true, "Login successful", customerDetails);
    }
    
    /**
     * Validates password against security requirements
     * - Minimum 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     * - At least one special character (@$!%*?&)
     */
    public boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}
