package com.example.demo.config;

import com.example.demo.model.*;
import com.example.demo.repository.*;
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
    
    @Autowired
    private MerchantRepository merchantRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
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
            
            // Create test wallets for customers
            Wallet wallet1 = new Wallet();
            wallet1.setCustomer(customer1);
            wallet1.setBalance(new BigDecimal("20000.00"));
            wallet1.setCurrency("USD");
            wallet1.setIsActive(true);
            
            Wallet wallet2 = new Wallet();
            wallet2.setCustomer(customer2);
            wallet2.setBalance(new BigDecimal("15000.00"));
            wallet2.setCurrency("USD");
            wallet2.setIsActive(true);
            
            walletRepository.save(wallet1);
            walletRepository.save(wallet2);
            
            System.out.println("Test wallets initialized successfully!");
            
            // Create test merchants
            Merchant merchant1 = new Merchant();
            merchant1.setMerchantName("Tech Store Inc");
            merchant1.setMerchantCode("TECH001");
            merchant1.setEmail("merchant@techstore.com");
            merchant1.setPhoneNumber("555-1234");
            merchant1.setAddress("789 Business Blvd, City, State");
            merchant1.setWalletBalance(new BigDecimal("50000.00"));
            merchant1.setCurrency("USD");
            merchant1.setIsActive(true);
            
            Merchant merchant2 = new Merchant();
            merchant2.setMerchantName("Fashion World");
            merchant2.setMerchantCode("FASH001");
            merchant2.setEmail("merchant@fashionworld.com");
            merchant2.setPhoneNumber("555-5678");
            merchant2.setAddress("321 Fashion Ave, City, State");
            merchant2.setWalletBalance(new BigDecimal("30000.00"));
            merchant2.setCurrency("USD");
            merchant2.setIsActive(true);
            
            merchant1 = merchantRepository.save(merchant1);
            merchant2 = merchantRepository.save(merchant2);
            
            System.out.println("Test merchants initialized successfully!");
            
            // Create test products
            Product product1 = new Product();
            product1.setProductName("Laptop Pro 15");
            product1.setDescription("High-performance laptop with 16GB RAM");
            product1.setPrice(new BigDecimal("1200.00"));
            product1.setCurrency("USD");
            product1.setAvailableQuantity(50);
            product1.setMerchant(merchant1);
            
            Product product2 = new Product();
            product2.setProductName("Wireless Mouse");
            product2.setDescription("Ergonomic wireless mouse");
            product2.setPrice(new BigDecimal("45.00"));
            product2.setCurrency("USD");
            product2.setAvailableQuantity(200);
            product2.setMerchant(merchant1);
            
            Product product3 = new Product();
            product3.setProductName("Designer T-Shirt");
            product3.setDescription("Premium cotton designer t-shirt");
            product3.setPrice(new BigDecimal("75.00"));
            product3.setCurrency("USD");
            product3.setAvailableQuantity(100);
            product3.setMerchant(merchant2);
            
            productRepository.save(product1);
            productRepository.save(product2);
            productRepository.save(product3);
            
            System.out.println("Test products initialized successfully!");
        }
    }
}
