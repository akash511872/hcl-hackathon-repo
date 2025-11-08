package com.example.demo.config;

import com.example.demo.model.Customer;
import com.example.demo.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private CustomerRepository customerRepository;
    
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
            
            customerRepository.save(customer1);
            customerRepository.save(customer2);
            
            System.out.println("Test customers initialized successfully!");
        }
    }
}
