package com.example.demo.service;

import com.example.demo.dto.OrderPlacementRequest;
import com.example.demo.dto.OrderPlacementResponse;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private static final List<String> SUPPORTED_CURRENCIES = Arrays.asList("USD", "EUR", "GBP", "INR");
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Place order with complete validation and transaction management
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderPlacementResponse placeOrder(OrderPlacementRequest request) {
        logger.info("Received order placement request for customer: {}, product: {}", 
            request.getCustomerId(), request.getProductId());
        
        try {
            // Step 1: Validate input
            validateOrderRequest(request);
            
            // Step 2: Fetch and validate customer
            Customer customer = validateCustomer(request.getCustomerId());
            
            // Step 3: Fetch and validate product
            Product product = validateProduct(request.getProductId());
            
            // Step 4: Validate currency
            validateCurrency(request.getCurrency(), product.getCurrency());
            
            // Step 5: Validate product availability
            validateProductAvailability(product, request.getQuantity());
            
            // Step 6: Calculate total amount
            BigDecimal totalAmount = product.getPrice().multiply(new BigDecimal(request.getQuantity()));
            
            // Step 7: Get customer wallet and validate balance
            Wallet wallet = validateWalletBalance(customer, totalAmount, request.getCurrency());
            
            // Step 8: Create order
            Order order = createOrder(customer, product, request.getQuantity(), totalAmount, request.getCurrency());
            
            auditService.logAudit("Order", order.getOrderId(), AuditLog.AuditAction.ORDER_CREATED, 
                "Order created for customer " + customer.getCustomerId() + " with amount " + totalAmount, 
                customer.getCustomerId());
            
            // Step 9: Validate order
            order.setStatus(Order.OrderStatus.PAYMENT_IN_PROGRESS);
            orderRepository.save(order);
            
            auditService.logAudit("Order", order.getOrderId(), AuditLog.AuditAction.ORDER_VALIDATED, 
                "Order validated, initiating payment", customer.getCustomerId());
            
            // Step 10: Process payment
            auditService.logAudit("Order", order.getOrderId(), AuditLog.AuditAction.ORDER_PAYMENT_INITIATED, 
                "Payment initiated for order", customer.getCustomerId());
            
            PaymentService.PaymentResult paymentResult = paymentService.processPayment(order, wallet, product);
            
            if (!paymentResult.isSuccess()) {
                order.setStatus(Order.OrderStatus.FAILED);
                order.setFailureReason(paymentResult.getMessage());
                orderRepository.save(order);
                
                // Notify customer of failure
                notificationService.sendCustomerNotification(order, customer.getEmail(), 
                    "Order " + order.getOrderNumber() + " failed: " + paymentResult.getMessage());
                
                return new OrderPlacementResponse(false, "Order placement failed: " + paymentResult.getMessage());
            }
            
            // Step 11: Update product quantity
            product.setAvailableQuantity(product.getAvailableQuantity() - request.getQuantity());
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);
            
            // Step 12: Mark order as completed
            order.setStatus(Order.OrderStatus.COMPLETED);
            order.setWalletFee(paymentResult.getWalletFee());
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            
            logger.info("Order placed successfully: {}", order.getOrderNumber());
            
            // Step 13: Notify customer of success
            notificationService.sendCustomerNotification(order, customer.getEmail(), 
                "Order " + order.getOrderNumber() + " placed successfully. Amount: " + 
                totalAmount + " " + request.getCurrency());
            
            // Step 14: Build response
            OrderPlacementResponse.OrderDetails orderDetails = buildOrderDetails(order, product, paymentResult.getRemainingBalance());
            
            return new OrderPlacementResponse(true, "Order placed successfully", orderDetails);
            
        } catch (PaymentService.InsufficientBalanceException e) {
            logger.error("Insufficient balance for order: {}", e.getMessage());
            return new OrderPlacementResponse(false, e.getMessage());
        } catch (PaymentService.PaymentProcessingException e) {
            logger.error("Payment processing failed: {}", e.getMessage(), e);
            return new OrderPlacementResponse(false, "Payment processing failed. Transaction has been rolled back.");
        } catch (ValidationException e) {
            logger.error("Validation error: {}", e.getMessage());
            return new OrderPlacementResponse(false, e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during order placement: {}", e.getMessage(), e);
            return new OrderPlacementResponse(false, "Order placement failed due to unexpected error. Please try again.");
        }
    }
    
    /**
     * Validate order request
     */
    private void validateOrderRequest(OrderPlacementRequest request) {
        if (request.getCustomerId() == null || request.getCustomerId() <= 0) {
            throw new ValidationException("Invalid customer ID");
        }
        if (request.getProductId() == null || request.getProductId() <= 0) {
            throw new ValidationException("Invalid product ID");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new ValidationException("Invalid quantity. Must be greater than 0");
        }
        if (request.getCurrency() == null || request.getCurrency().trim().isEmpty()) {
            throw new ValidationException("Currency is required");
        }
    }
    
    /**
     * Validate customer exists
     */
    private Customer validateCustomer(Long customerId) {
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (!customerOpt.isPresent()) {
            throw new ValidationException("Customer not found with ID: " + customerId);
        }
        return customerOpt.get();
    }
    
    /**
     * Validate product exists
     */
    private Product validateProduct(Long productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (!productOpt.isPresent()) {
            throw new ValidationException("Product not found with ID: " + productId);
        }
        return productOpt.get();
    }
    
    /**
     * Validate currency is supported
     */
    private void validateCurrency(String requestCurrency, String productCurrency) {
        if (!SUPPORTED_CURRENCIES.contains(requestCurrency.toUpperCase())) {
            throw new ValidationException("Currency " + requestCurrency + " is not supported. Supported currencies: " + 
                String.join(", ", SUPPORTED_CURRENCIES));
        }
        if (!requestCurrency.equalsIgnoreCase(productCurrency)) {
            throw new ValidationException("Currency mismatch. Product currency is " + productCurrency + 
                " but requested currency is " + requestCurrency);
        }
    }
    
    /**
     * Validate product availability
     */
    private void validateProductAvailability(Product product, Integer quantity) {
        if (product.getAvailableQuantity() < quantity) {
            throw new ValidationException("Insufficient product quantity. Available: " + 
                product.getAvailableQuantity() + ", Requested: " + quantity);
        }
    }
    
    /**
     * Validate wallet balance
     */
    private Wallet validateWalletBalance(Customer customer, BigDecimal totalAmount, String currency) {
        Optional<Wallet> walletOpt = walletRepository.findByCustomerCustomerId(customer.getCustomerId());
        if (!walletOpt.isPresent()) {
            throw new ValidationException("Wallet not found for customer: " + customer.getCustomerId());
        }
        
        Wallet wallet = walletOpt.get();
        
        if (!wallet.getIsActive()) {
            throw new ValidationException("Wallet is not active");
        }
        
        if (!wallet.getCurrency().equalsIgnoreCase(currency)) {
            throw new ValidationException("Wallet currency mismatch. Wallet currency: " + 
                wallet.getCurrency() + ", Transaction currency: " + currency);
        }
        
        // Calculate total with wallet fee (2%)
        BigDecimal walletFee = totalAmount.multiply(new BigDecimal("0.02"));
        BigDecimal totalRequired = totalAmount.add(walletFee);
        
        if (wallet.getBalance().compareTo(totalRequired) < 0) {
            throw new ValidationException("Insufficient wallet balance. Required: " + 
                totalRequired + " " + currency + ", Available: " + wallet.getBalance() + " " + currency);
        }
        
        return wallet;
    }
    
    /**
     * Create order entity
     */
    private Order createOrder(Customer customer, Product product, Integer quantity, 
                             BigDecimal totalAmount, String currency) {
        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString());
        order.setCustomer(customer);
        order.setProduct(product);
        order.setQuantity(quantity);
        order.setTotalAmount(totalAmount);
        order.setCurrency(currency);
        order.setWalletFee(BigDecimal.ZERO); // Will be set after payment
        order.setStatus(Order.OrderStatus.PENDING);
        
        return orderRepository.save(order);
    }
    
    /**
     * Build order details for response
     */
    private OrderPlacementResponse.OrderDetails buildOrderDetails(Order order, Product product, BigDecimal remainingBalance) {
        OrderPlacementResponse.OrderDetails details = new OrderPlacementResponse.OrderDetails();
        details.setOrderId(order.getOrderId());
        details.setOrderNumber(order.getOrderNumber());
        details.setProductName(product.getProductName());
        details.setMerchantName(product.getMerchant().getMerchantName());
        details.setQuantity(order.getQuantity());
        details.setTotalAmount(order.getTotalAmount());
        details.setWalletFee(order.getWalletFee());
        details.setCurrency(order.getCurrency());
        details.setOrderStatus(order.getStatus().name());
        details.setRemainingWalletBalance(remainingBalance);
        
        return details;
    }
    
    /**
     * Custom validation exception
     */
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }
}
