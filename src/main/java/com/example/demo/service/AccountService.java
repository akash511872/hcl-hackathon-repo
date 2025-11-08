package com.example.demo.service;

import com.example.demo.dto.AccountDetailsResponse;
import com.example.demo.model.Account;
import com.example.demo.model.Customer;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    /**
     * Get account details for a specific customer
     * @param customerId - The customer ID
     * @return AccountDetailsResponse with account information
     */
    public List<AccountDetailsResponse> getAccountsByCustomerId(Long customerId) {
        // Validate customer ID
        if (customerId == null || customerId <= 0) {
            AccountDetailsResponse response = new AccountDetailsResponse(false, "Invalid customer ID");
            List<AccountDetailsResponse> responses = new ArrayList<>();
            responses.add(response);
            return responses;
        }
        
        // Check if customer exists
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (!customerOpt.isPresent()) {
            AccountDetailsResponse response = new AccountDetailsResponse(false, "Customer not found");
            List<AccountDetailsResponse> responses = new ArrayList<>();
            responses.add(response);
            return responses;
        }
        
        Customer customer = customerOpt.get();
        
        // Get all accounts for the customer
        List<Account> accounts = accountRepository.findByCustomerCustomerId(customerId);
        
        if (accounts.isEmpty()) {
            AccountDetailsResponse response = new AccountDetailsResponse(false, "No accounts found for this customer");
            List<AccountDetailsResponse> responses = new ArrayList<>();
            responses.add(response);
            return responses;
        }
        
        // Create response with account details
        List<AccountDetailsResponse> responses = new ArrayList<>();
        for (Account account : accounts) {
            AccountDetailsResponse response = getAccountDetailsResponse(account, customer);
            responses.add(response);
        }
        
        return responses;
    }

    private static AccountDetailsResponse getAccountDetailsResponse(Account account, Customer customer) {
        AccountDetailsResponse.AccountDetails details = new AccountDetailsResponse.AccountDetails(
            account.getAccountId(),
            account.getAccountNumber(),
            account.getBalance(),
            account.getAccountType(),
            customer.getCustomerId(),
            customer.getFirstName() + " " + customer.getLastName()
        );

        AccountDetailsResponse response = new AccountDetailsResponse(true, "Account details retrieved successfully", details);
        return response;
    }
}
