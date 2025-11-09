package com.example.demo.service;

import com.example.demo.dto.AccountDetailsResponse;
import com.example.demo.model.Account;
import com.example.demo.model.Customer;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AccountService - Positive Scenarios
 * Tests successful account retrieval for customers
 */
@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountService accountService;

    private Customer testCustomer;
    private Account savingsAccount;
    private Account checkingAccount;

    @BeforeEach
    public void setUp() {
        testCustomer = new Customer();
        testCustomer.setCustomerId(1L);
        testCustomer.setUsername("john.doe");
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Doe");
        testCustomer.setEmail("john.doe@example.com");

        savingsAccount = new Account();
        savingsAccount.setAccountId(1L);
        savingsAccount.setAccountNumber("ACC1001");
        savingsAccount.setBalance(new BigDecimal("5000.00"));
        savingsAccount.setAccountType("SAVINGS");
        savingsAccount.setCustomer(testCustomer);

        checkingAccount = new Account();
        checkingAccount.setAccountId(2L);
        checkingAccount.setAccountNumber("ACC1002");
        checkingAccount.setBalance(new BigDecimal("10000.00"));
        checkingAccount.setAccountType("CHECKING");
        checkingAccount.setCustomer(testCustomer);
    }

    @Test
    public void testGetAccountDetails_WithSingleAccount() {
        // Arrange
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(testCustomer));
        when(accountRepository.findByCustomerCustomerId(anyLong()))
                .thenReturn(Arrays.asList(savingsAccount));

        // Act
        List<AccountDetailsResponse> responses = accountService.getAccountsByCustomerId(1L);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        
        AccountDetailsResponse response = responses.get(0);
        assertTrue(response.isSuccess());
        assertEquals("Account details retrieved successfully", response.getMessage());
        
        AccountDetailsResponse.AccountDetails details = response.getAccountDetails();
        assertNotNull(details);
        assertEquals(1L, details.getAccountId());
        assertEquals("ACC1001", details.getAccountNumber());
        assertEquals(new BigDecimal("5000.00"), details.getBalance());
        assertEquals("SAVINGS", details.getAccountType());
        assertEquals(1L, details.getCustomerId());
        assertEquals("John Doe", details.getCustomerName());
    }

    @Test
    public void testGetAccountDetails_WithMultipleAccounts() {
        // Arrange
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(testCustomer));
        when(accountRepository.findByCustomerCustomerId(anyLong()))
                .thenReturn(Arrays.asList(savingsAccount, checkingAccount));

        // Act
        List<AccountDetailsResponse> responses = accountService.getAccountsByCustomerId(1L);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        
        // Verify first account
        AccountDetailsResponse response1 = responses.get(0);
        assertTrue(response1.isSuccess());
        assertEquals("ACC1001", response1.getAccountDetails().getAccountNumber());
        assertEquals(new BigDecimal("5000.00"), response1.getAccountDetails().getBalance());
        assertEquals("SAVINGS", response1.getAccountDetails().getAccountType());
        
        // Verify second account
        AccountDetailsResponse response2 = responses.get(1);
        assertTrue(response2.isSuccess());
        assertEquals("ACC1002", response2.getAccountDetails().getAccountNumber());
        assertEquals(new BigDecimal("10000.00"), response2.getAccountDetails().getBalance());
        assertEquals("CHECKING", response2.getAccountDetails().getAccountType());
    }

    @Test
    public void testGetAccountDetails_WithLargeBalance() {
        // Arrange
        savingsAccount.setBalance(new BigDecimal("1000000.00"));
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(testCustomer));
        when(accountRepository.findByCustomerCustomerId(anyLong()))
                .thenReturn(Arrays.asList(savingsAccount));

        // Act
        List<AccountDetailsResponse> responses = accountService.getAccountsByCustomerId(1L);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        AccountDetailsResponse.AccountDetails details = responses.get(0).getAccountDetails();
        assertEquals(new BigDecimal("1000000.00"), details.getBalance());
    }

    @Test
    public void testGetAccountDetails_WithZeroBalance() {
        // Arrange
        savingsAccount.setBalance(BigDecimal.ZERO);
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(testCustomer));
        when(accountRepository.findByCustomerCustomerId(anyLong()))
                .thenReturn(Arrays.asList(savingsAccount));

        // Act
        List<AccountDetailsResponse> responses = accountService.getAccountsByCustomerId(1L);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        AccountDetailsResponse.AccountDetails details = responses.get(0).getAccountDetails();
        assertEquals(BigDecimal.ZERO, details.getBalance());
    }

    @Test
    public void testGetAccountDetails_WithDecimalBalance() {
        // Arrange
        savingsAccount.setBalance(new BigDecimal("7500.50"));
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(testCustomer));
        when(accountRepository.findByCustomerCustomerId(anyLong()))
                .thenReturn(Arrays.asList(savingsAccount));

        // Act
        List<AccountDetailsResponse> responses = accountService.getAccountsByCustomerId(1L);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        AccountDetailsResponse.AccountDetails details = responses.get(0).getAccountDetails();
        assertEquals(new BigDecimal("7500.50"), details.getBalance());
    }

    @Test
    public void testGetAccountDetails_CustomerNameFormatted() {
        // Arrange
        testCustomer.setFirstName("Alice");
        testCustomer.setLastName("Brown");
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(testCustomer));
        when(accountRepository.findByCustomerCustomerId(anyLong()))
                .thenReturn(Arrays.asList(savingsAccount));

        // Act
        List<AccountDetailsResponse> responses = accountService.getAccountsByCustomerId(1L);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Alice Brown", responses.get(0).getAccountDetails().getCustomerName());
    }

    @Test
    public void testGetAccountDetails_AllFieldsPopulated() {
        // Arrange
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(testCustomer));
        when(accountRepository.findByCustomerCustomerId(anyLong()))
                .thenReturn(Arrays.asList(savingsAccount));

        // Act
        List<AccountDetailsResponse> responses = accountService.getAccountsByCustomerId(1L);

        // Assert
        assertNotNull(responses);
        AccountDetailsResponse.AccountDetails details = responses.get(0).getAccountDetails();
        
        assertNotNull(details.getAccountId());
        assertNotNull(details.getAccountNumber());
        assertNotNull(details.getBalance());
        assertNotNull(details.getAccountType());
        assertNotNull(details.getCustomerId());
        assertNotNull(details.getCustomerName());
    }
}
