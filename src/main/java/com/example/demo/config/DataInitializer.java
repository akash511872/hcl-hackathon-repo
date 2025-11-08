package com.example.demo.config;

import com.example.demo.model.Account;
import com.example.demo.model.Customer;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Create test customers with valid passwords
        if (customerRepository.count() == 0) {
            Customer customer1 = new Customer();
            customer1.setUsername("john.doe");
            customer1.setPassword("Test@123"); // Valid password format
            customer1.setFirstName("John");
            customer1.setLastName("Doe");
            customer1.setEmail("john.doe@example.com");
            customer1.setPhoneNumber("123-456-7890");
            customer1.setAddress("123 Main St, City, State");
            
            Customer customer2 = new Customer();
            customer2.setUsername("jane.smith");
            customer2.setPassword("Pass@word1"); // Valid password format
            customer2.setFirstName("Jane");
            customer2.setLastName("Smith");
            customer2.setEmail("jane.smith@example.com");
            customer2.setPhoneNumber("098-765-4321");
            customer2.setAddress("456 Oak Ave, Town, State");
            
            customer1 = customerRepository.save(customer1);
            customer2 = customerRepository.save(customer2);
            
            System.out.println("Test customers initialized successfully!");
            
            // Create test accounts for customers
            Account account1 = new Account();
            account1.setAccountNumber("ACC1001");
            account1.setBalance(new BigDecimal("5000.00"));
            account1.setCustomer(customer1);
            account1.setAccountType("SAVINGS");
            
            Account account2 = new Account();
            account2.setAccountNumber("ACC1002");
            account2.setBalance(new BigDecimal("10000.00"));
            account2.setCustomer(customer1);
            account2.setAccountType("CHECKING");
            
            Account account3 = new Account();
            account3.setAccountNumber("ACC2001");
            account3.setBalance(new BigDecimal("7500.50"));
            account3.setCustomer(customer2);
            account3.setAccountType("SAVINGS");
            
            accountRepository.save(account1);
            accountRepository.save(account2);
            accountRepository.save(account3);
            
            System.out.println("Test accounts initialized successfully!");
        }
    }
}
