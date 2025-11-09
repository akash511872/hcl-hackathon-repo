package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.model.Customer;
import com.example.demo.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for LoginService - Positive Scenarios
 * Tests successful login flows and password validation
 */
@ExtendWith(MockitoExtension.class)
public class LoginServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private LoginService loginService;

    private Customer testCustomer;

    @BeforeEach
    public void setUp() {
        testCustomer = new Customer();
        testCustomer.setCustomerId(1L);
        testCustomer.setUsername("john.doe");
        testCustomer.setPassword("Test@123");
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Doe");
        testCustomer.setEmail("john.doe@example.com");
        testCustomer.setPhoneNumber("123-456-7890");
        testCustomer.setAddress("123 Main St, City, State");
    }

    @Test
    public void testSuccessfulLogin_WithValidCredentials() {
        // Arrange
        LoginRequest request = new LoginRequest("john.doe", "Test@123");
        when(customerRepository.findByUsername(anyString())).thenReturn(Optional.of(testCustomer));

        // Act
        LoginResponse response = loginService.login(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertNotNull(response.getCustomerDetails());
        assertEquals(1L, response.getCustomerDetails().getCustomerId());
        assertEquals("john.doe", response.getCustomerDetails().getUsername());
        assertEquals("John", response.getCustomerDetails().getFirstName());
        assertEquals("Doe", response.getCustomerDetails().getLastName());
        assertEquals("john.doe@example.com", response.getCustomerDetails().getEmail());
    }

    @Test
    public void testSuccessfulLogin_WithDifferentValidPassword() {
        // Arrange
        testCustomer.setPassword("Secure@456");
        LoginRequest request = new LoginRequest("john.doe", "Secure@456");
        when(customerRepository.findByUsername(anyString())).thenReturn(Optional.of(testCustomer));

        // Act
        LoginResponse response = loginService.login(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertNotNull(response.getCustomerDetails());
        assertEquals("john.doe", response.getCustomerDetails().getUsername());
    }

    @Test
    public void testSuccessfulLogin_WithMinimumPasswordRequirements() {
        // Arrange - Password with exactly minimum requirements
        testCustomer.setPassword("Pass@123");
        LoginRequest request = new LoginRequest("john.doe", "Pass@123");
        when(customerRepository.findByUsername(anyString())).thenReturn(Optional.of(testCustomer));

        // Act
        LoginResponse response = loginService.login(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    public void testSuccessfulLogin_WithLongPassword() {
        // Arrange - Password with more than minimum length
        testCustomer.setPassword("VerySecure@Password123");
        LoginRequest request = new LoginRequest("john.doe", "VerySecure@Password123");
        when(customerRepository.findByUsername(anyString())).thenReturn(Optional.of(testCustomer));

        // Act
        LoginResponse response = loginService.login(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    public void testSuccessfulLogin_WithMultipleSpecialCharacters() {
        // Arrange
        testCustomer.setPassword("Test@123!$%");
        LoginRequest request = new LoginRequest("john.doe", "Test@123!$%");
        when(customerRepository.findByUsername(anyString())).thenReturn(Optional.of(testCustomer));

        // Act
        LoginResponse response = loginService.login(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    public void testCustomerDetailsPopulatedCorrectly() {
        // Arrange
        LoginRequest request = new LoginRequest("john.doe", "Test@123");
        when(customerRepository.findByUsername(anyString())).thenReturn(Optional.of(testCustomer));

        // Act
        LoginResponse response = loginService.login(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        LoginResponse.CustomerDetails details = response.getCustomerDetails();
        assertNotNull(details);
        assertEquals(testCustomer.getCustomerId(), details.getCustomerId());
        assertEquals(testCustomer.getUsername(), details.getUsername());
        assertEquals(testCustomer.getFirstName(), details.getFirstName());
        assertEquals(testCustomer.getLastName(), details.getLastName());
        assertEquals(testCustomer.getEmail(), details.getEmail());
        assertEquals(testCustomer.getPhoneNumber(), details.getPhoneNumber());
        assertEquals(testCustomer.getAddress(), details.getAddress());
    }

    @Test
    public void testPasswordValidation_WithAllRequiredCharacters() {
        // Arrange - Password with uppercase, lowercase, digit, and special char
        testCustomer.setPassword("MyPass@123");
        LoginRequest request = new LoginRequest("john.doe", "MyPass@123");
        when(customerRepository.findByUsername(anyString())).thenReturn(Optional.of(testCustomer));

        // Act
        LoginResponse response = loginService.login(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
    }
}
