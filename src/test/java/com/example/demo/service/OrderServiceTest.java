package com.example.demo.service;

import com.example.demo.dto.OrderPlacementRequest;
import com.example.demo.dto.OrderPlacementResponse;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderService - Positive Scenarios
 * Tests successful order placement with various valid inputs
 */
@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private AuditService auditService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderService orderService;

    private Customer testCustomer;
    private Product testProduct;
    private Merchant testMerchant;
    private Wallet testWallet;
    private Order testOrder;

    @BeforeEach
    public void setUp() {
        // Setup Customer
        testCustomer = new Customer();
        testCustomer.setCustomerId(1L);
        testCustomer.setUsername("john.doe");
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Doe");
        testCustomer.setEmail("john.doe@example.com");

        // Setup Merchant
        testMerchant = new Merchant();
        testMerchant.setMerchantId(1L);
        testMerchant.setMerchantName("Tech Store Inc");
        testMerchant.setMerchantCode("TECH001");
        testMerchant.setEmail("merchant@techstore.com");
        testMerchant.setWalletBalance(new BigDecimal("50000.00"));
        testMerchant.setCurrency("USD");

        // Setup Product
        testProduct = new Product();
        testProduct.setProductId(1L);
        testProduct.setProductName("Wireless Mouse");
        testProduct.setPrice(new BigDecimal("45.00"));
        testProduct.setCurrency("USD");
        testProduct.setAvailableQuantity(200);
        testProduct.setMerchant(testMerchant);

        // Setup Wallet
        testWallet = new Wallet();
        testWallet.setWalletId(1L);
        testWallet.setCustomer(testCustomer);
        testWallet.setBalance(new BigDecimal("20000.00"));
        testWallet.setCurrency("USD");
        testWallet.setIsActive(true);

        // Setup Order
        testOrder = new Order();
        testOrder.setOrderId(1L);
        testOrder.setOrderNumber("ORD-TEST123");
        testOrder.setCustomer(testCustomer);
        testOrder.setProduct(testProduct);
        testOrder.setQuantity(2);
        testOrder.setTotalAmount(new BigDecimal("90.00"));
        testOrder.setCurrency("USD");
        testOrder.setWalletFee(new BigDecimal("1.80"));
        testOrder.setStatus(Order.OrderStatus.COMPLETED);
    }

    @Test
    public void testPlaceOrder_WithValidSingleItem() {
        // Arrange
        OrderPlacementRequest request = new OrderPlacementRequest();
        request.setCustomerId(1L);
        request.setProductId(1L);
        request.setQuantity(1);
        request.setProductCost(new BigDecimal("45.00"));
        request.setCurrency("USD");

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
        when(walletRepository.findByCustomerCustomerId(anyLong())).thenReturn(Optional.of(testWallet));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        PaymentService.PaymentResult paymentResult = new PaymentService.PaymentResult();
        paymentResult.setSuccess(true);
        paymentResult.setMessage("Payment processed successfully");
        paymentResult.setWalletFee(new BigDecimal("0.90"));
        paymentResult.setRemainingBalance(new BigDecimal("19954.10"));

        when(paymentService.processPayment(any(), any(), any())).thenReturn(paymentResult);

        // Act
        OrderPlacementResponse response = orderService.placeOrder(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Order placed successfully", response.getMessage());
        verify(orderRepository, atLeastOnce()).save(any(Order.class));
        verify(paymentService, times(1)).processPayment(any(), any(), any());
    }

    @Test
    public void testPlaceOrder_WithMultipleQuantity() {
        // Arrange
        OrderPlacementRequest request = new OrderPlacementRequest();
        request.setCustomerId(1L);
        request.setProductId(1L);
        request.setQuantity(5);
        request.setProductCost(new BigDecimal("45.00"));
        request.setCurrency("USD");

        testOrder.setQuantity(5);
        testOrder.setTotalAmount(new BigDecimal("225.00"));
        testOrder.setWalletFee(new BigDecimal("4.50"));

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
        when(walletRepository.findByCustomerCustomerId(anyLong())).thenReturn(Optional.of(testWallet));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        PaymentService.PaymentResult paymentResult = new PaymentService.PaymentResult();
        paymentResult.setSuccess(true);
        paymentResult.setWalletFee(new BigDecimal("4.50"));
        paymentResult.setRemainingBalance(new BigDecimal("19770.50"));

        when(paymentService.processPayment(any(), any(), any())).thenReturn(paymentResult);

        // Act
        OrderPlacementResponse response = orderService.placeOrder(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void testPlaceOrder_WithUSDCurrency() {
        // Arrange
        OrderPlacementRequest request = new OrderPlacementRequest();
        request.setCustomerId(1L);
        request.setProductId(1L);
        request.setQuantity(1);
        request.setProductCost(new BigDecimal("45.00"));
        request.setCurrency("USD");

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
        when(walletRepository.findByCustomerCustomerId(anyLong())).thenReturn(Optional.of(testWallet));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        PaymentService.PaymentResult paymentResult = new PaymentService.PaymentResult();
        paymentResult.setSuccess(true);
        paymentResult.setWalletFee(new BigDecimal("0.90"));
        paymentResult.setRemainingBalance(new BigDecimal("19954.10"));

        when(paymentService.processPayment(any(), any(), any())).thenReturn(paymentResult);

        // Act
        OrderPlacementResponse response = orderService.placeOrder(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
    }

    @Test
    public void testPlaceOrder_WithEURCurrency() {
        // Arrange
        testProduct.setCurrency("EUR");
        testWallet.setCurrency("EUR");
        
        OrderPlacementRequest request = new OrderPlacementRequest();
        request.setCustomerId(1L);
        request.setProductId(1L);
        request.setQuantity(1);
        request.setProductCost(new BigDecimal("45.00"));
        request.setCurrency("EUR");

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
        when(walletRepository.findByCustomerCustomerId(anyLong())).thenReturn(Optional.of(testWallet));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        PaymentService.PaymentResult paymentResult = new PaymentService.PaymentResult();
        paymentResult.setSuccess(true);
        paymentResult.setWalletFee(new BigDecimal("0.90"));
        paymentResult.setRemainingBalance(new BigDecimal("19954.10"));

        when(paymentService.processPayment(any(), any(), any())).thenReturn(paymentResult);

        // Act
        OrderPlacementResponse response = orderService.placeOrder(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
    }

    @Test
    public void testPlaceOrder_NotificationsSent() {
        // Arrange
        OrderPlacementRequest request = new OrderPlacementRequest();
        request.setCustomerId(1L);
        request.setProductId(1L);
        request.setQuantity(1);
        request.setProductCost(new BigDecimal("45.00"));
        request.setCurrency("USD");

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
        when(walletRepository.findByCustomerCustomerId(anyLong())).thenReturn(Optional.of(testWallet));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        PaymentService.PaymentResult paymentResult = new PaymentService.PaymentResult();
        paymentResult.setSuccess(true);
        paymentResult.setWalletFee(new BigDecimal("0.90"));
        paymentResult.setRemainingBalance(new BigDecimal("19954.10"));

        when(paymentService.processPayment(any(), any(), any())).thenReturn(paymentResult);

        // Act
        OrderPlacementResponse response = orderService.placeOrder(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(notificationService, times(1)).sendCustomerNotification(any(), anyString(), anyString());
    }

    @Test
    public void testPlaceOrder_ProductQuantityUpdated() {
        // Arrange
        OrderPlacementRequest request = new OrderPlacementRequest();
        request.setCustomerId(1L);
        request.setProductId(1L);
        request.setQuantity(10);
        request.setProductCost(new BigDecimal("45.00"));
        request.setCurrency("USD");

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
        when(walletRepository.findByCustomerCustomerId(anyLong())).thenReturn(Optional.of(testWallet));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        PaymentService.PaymentResult paymentResult = new PaymentService.PaymentResult();
        paymentResult.setSuccess(true);
        paymentResult.setWalletFee(new BigDecimal("9.00"));
        paymentResult.setRemainingBalance(new BigDecimal("19541.00"));

        when(paymentService.processPayment(any(), any(), any())).thenReturn(paymentResult);

        // Act
        OrderPlacementResponse response = orderService.placeOrder(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void testPlaceOrder_AuditLogsCreated() {
        // Arrange
        OrderPlacementRequest request = new OrderPlacementRequest();
        request.setCustomerId(1L);
        request.setProductId(1L);
        request.setQuantity(1);
        request.setProductCost(new BigDecimal("45.00"));
        request.setCurrency("USD");

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
        when(walletRepository.findByCustomerCustomerId(anyLong())).thenReturn(Optional.of(testWallet));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        PaymentService.PaymentResult paymentResult = new PaymentService.PaymentResult();
        paymentResult.setSuccess(true);
        paymentResult.setWalletFee(new BigDecimal("0.90"));
        paymentResult.setRemainingBalance(new BigDecimal("19954.10"));

        when(paymentService.processPayment(any(), any(), any())).thenReturn(paymentResult);

        // Act
        OrderPlacementResponse response = orderService.placeOrder(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(auditService, atLeastOnce()).logAudit(anyString(), anyLong(), any(), anyString(), anyLong());
    }
}
