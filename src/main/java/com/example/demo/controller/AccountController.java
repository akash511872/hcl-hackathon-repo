package com.example.demo.controller;

import com.example.demo.dto.AccountDetailsResponse;
import com.example.demo.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AccountController {
    
    @Autowired
    private AccountService accountService;
    
    /**
     * Get account details by customer ID
     * 
     * @param customerId - The customer ID
     * @return List of AccountDetailsResponse with account information
     */
    @GetMapping("/accounts/customer/{customerId}")
    public ResponseEntity<List<AccountDetailsResponse>> getAccountsByCustomerId(@PathVariable Long customerId) {
        List<AccountDetailsResponse> responses = accountService.getAccountsByCustomerId(customerId);
        
        if (responses.isEmpty() || !responses.get(0).isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responses);
        }
        
        return ResponseEntity.ok(responses);
    }
}
