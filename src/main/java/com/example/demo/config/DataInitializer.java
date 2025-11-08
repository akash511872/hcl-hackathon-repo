package com.example.demo.config;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
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
            
            Customer customer3 = new Customer();
            customer3.setUsername("bob.wilson");
            customer3.setPassword("Secure@456");
            customer3.setFirstName("Bob");
            customer3.setLastName("Wilson");
            customer3.setEmail("bob.wilson@example.com");
            customer3.setPhoneNumber("555-9876");
            customer3.setAddress("789 Pine Rd, Suburb, State");
            
            Customer customer4 = new Customer();
            customer4.setUsername("alice.brown");
            customer4.setPassword("Strong@789");
            customer4.setFirstName("Alice");
            customer4.setLastName("Brown");
            customer4.setEmail("alice.brown@example.com");
            customer4.setPhoneNumber("555-4567");
            customer4.setAddress("321 Elm Dr, Metro, State");
            
            customer1 = customerRepository.save(customer1);
            customer2 = customerRepository.save(customer2);
            customer3 = customerRepository.save(customer3);
            customer4 = customerRepository.save(customer4);
            
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
            
            Account account4 = new Account();
            account4.setAccountNumber("ACC3001");
            account4.setBalance(new BigDecimal("12000.00"));
            account4.setCustomer(customer3);
            account4.setAccountType("CHECKING");
            
            Account account5 = new Account();
            account5.setAccountNumber("ACC3002");
            account5.setBalance(new BigDecimal("3000.00"));
            account5.setCustomer(customer3);
            account5.setAccountType("SAVINGS");
            
            Account account6 = new Account();
            account6.setAccountNumber("ACC4001");
            account6.setBalance(new BigDecimal("25000.00"));
            account6.setCustomer(customer4);
            account6.setAccountType("CHECKING");
            
            accountRepository.save(account1);
            accountRepository.save(account2);
            accountRepository.save(account3);
            accountRepository.save(account4);
            accountRepository.save(account5);
            accountRepository.save(account6);
            
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
            
            Wallet wallet3 = new Wallet();
            wallet3.setCustomer(customer3);
            wallet3.setBalance(new BigDecimal("8000.00"));
            wallet3.setCurrency("USD");
            wallet3.setIsActive(true);
            
            Wallet wallet4 = new Wallet();
            wallet4.setCustomer(customer4);
            wallet4.setBalance(new BigDecimal("30000.00"));
            wallet4.setCurrency("USD");
            wallet4.setIsActive(true);
            
            walletRepository.save(wallet1);
            walletRepository.save(wallet2);
            walletRepository.save(wallet3);
            walletRepository.save(wallet4);
            
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
            
            Merchant merchant3 = new Merchant();
            merchant3.setMerchantName("Book Haven");
            merchant3.setMerchantCode("BOOK001");
            merchant3.setEmail("merchant@bookhaven.com");
            merchant3.setPhoneNumber("555-3333");
            merchant3.setAddress("100 Library Lane, City, State");
            merchant3.setWalletBalance(new BigDecimal("15000.00"));
            merchant3.setCurrency("USD");
            merchant3.setIsActive(true);
            
            merchant1 = merchantRepository.save(merchant1);
            merchant2 = merchantRepository.save(merchant2);
            merchant3 = merchantRepository.save(merchant3);
            
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
            
            Product product4 = new Product();
            product4.setProductName("Mechanical Keyboard");
            product4.setDescription("RGB mechanical gaming keyboard");
            product4.setPrice(new BigDecimal("150.00"));
            product4.setCurrency("USD");
            product4.setAvailableQuantity(75);
            product4.setMerchant(merchant1);
            
            Product product5 = new Product();
            product5.setProductName("Running Shoes");
            product5.setDescription("Comfortable running shoes for athletes");
            product5.setPrice(new BigDecimal("120.00"));
            product5.setCurrency("USD");
            product5.setAvailableQuantity(60);
            product5.setMerchant(merchant2);
            
            Product product6 = new Product();
            product6.setProductName("Programming Book");
            product6.setDescription("Advanced Java programming guide");
            product6.setPrice(new BigDecimal("55.00"));
            product6.setCurrency("USD");
            product6.setAvailableQuantity(150);
            product6.setMerchant(merchant3);
            
            product1 = productRepository.save(product1);
            product2 = productRepository.save(product2);
            product3 = productRepository.save(product3);
            product4 = productRepository.save(product4);
            product5 = productRepository.save(product5);
            product6 = productRepository.save(product6);
            
            System.out.println("Test products initialized successfully!");
            
            // Create sample completed orders
            Order order1 = new Order();
            order1.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            order1.setCustomer(customer1);
            order1.setProduct(product2);
            order1.setQuantity(2);
            order1.setTotalAmount(new BigDecimal("90.00"));
            order1.setCurrency("USD");
            order1.setWalletFee(new BigDecimal("1.80"));
            order1.setStatus(Order.OrderStatus.COMPLETED);
            order1.setCreatedAt(LocalDateTime.now().minusDays(5));
            order1.setUpdatedAt(LocalDateTime.now().minusDays(5));
            
            Order order2 = new Order();
            order2.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            order2.setCustomer(customer2);
            order2.setProduct(product3);
            order2.setQuantity(1);
            order2.setTotalAmount(new BigDecimal("75.00"));
            order2.setCurrency("USD");
            order2.setWalletFee(new BigDecimal("1.50"));
            order2.setStatus(Order.OrderStatus.COMPLETED);
            order2.setCreatedAt(LocalDateTime.now().minusDays(3));
            order2.setUpdatedAt(LocalDateTime.now().minusDays(3));
            
            Order order3 = new Order();
            order3.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            order3.setCustomer(customer3);
            order3.setProduct(product6);
            order3.setQuantity(3);
            order3.setTotalAmount(new BigDecimal("165.00"));
            order3.setCurrency("USD");
            order3.setWalletFee(new BigDecimal("3.30"));
            order3.setStatus(Order.OrderStatus.COMPLETED);
            order3.setCreatedAt(LocalDateTime.now().minusDays(1));
            order3.setUpdatedAt(LocalDateTime.now().minusDays(1));
            
            order1 = orderRepository.save(order1);
            order2 = orderRepository.save(order2);
            order3 = orderRepository.save(order3);
            
            System.out.println("Test orders initialized successfully!");
            
            // Create sample transactions for the orders
            Transaction txn1 = new Transaction();
            txn1.setTransactionNumber("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            txn1.setOrder(order1);
            txn1.setTransactionType(Transaction.TransactionType.DEBIT);
            txn1.setAmount(new BigDecimal("90.00"));
            txn1.setCurrency("USD");
            txn1.setStatus(Transaction.TransactionStatus.COMPLETED);
            txn1.setDescription("Debit from customer wallet for order " + order1.getOrderNumber());
            txn1.setCreatedAt(LocalDateTime.now().minusDays(5));
            txn1.setUpdatedAt(LocalDateTime.now().minusDays(5));
            
            Transaction txn2 = new Transaction();
            txn2.setTransactionNumber("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            txn2.setOrder(order1);
            txn2.setTransactionType(Transaction.TransactionType.CREDIT);
            txn2.setAmount(new BigDecimal("88.20"));
            txn2.setCurrency("USD");
            txn2.setStatus(Transaction.TransactionStatus.COMPLETED);
            txn2.setDescription("Credit to merchant Tech Store Inc for order " + order1.getOrderNumber());
            txn2.setCreatedAt(LocalDateTime.now().minusDays(5));
            txn2.setUpdatedAt(LocalDateTime.now().minusDays(5));
            
            Transaction txn3 = new Transaction();
            txn3.setTransactionNumber("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            txn3.setOrder(order1);
            txn3.setTransactionType(Transaction.TransactionType.WALLET_FEE);
            txn3.setAmount(new BigDecimal("1.80"));
            txn3.setCurrency("USD");
            txn3.setStatus(Transaction.TransactionStatus.COMPLETED);
            txn3.setDescription("Wallet fee collected for order " + order1.getOrderNumber());
            txn3.setCreatedAt(LocalDateTime.now().minusDays(5));
            txn3.setUpdatedAt(LocalDateTime.now().minusDays(5));
            
            Transaction txn4 = new Transaction();
            txn4.setTransactionNumber("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            txn4.setOrder(order2);
            txn4.setTransactionType(Transaction.TransactionType.DEBIT);
            txn4.setAmount(new BigDecimal("75.00"));
            txn4.setCurrency("USD");
            txn4.setStatus(Transaction.TransactionStatus.COMPLETED);
            txn4.setDescription("Debit from customer wallet for order " + order2.getOrderNumber());
            txn4.setCreatedAt(LocalDateTime.now().minusDays(3));
            txn4.setUpdatedAt(LocalDateTime.now().minusDays(3));
            
            Transaction txn5 = new Transaction();
            txn5.setTransactionNumber("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            txn5.setOrder(order2);
            txn5.setTransactionType(Transaction.TransactionType.CREDIT);
            txn5.setAmount(new BigDecimal("73.50"));
            txn5.setCurrency("USD");
            txn5.setStatus(Transaction.TransactionStatus.COMPLETED);
            txn5.setDescription("Credit to merchant Fashion World for order " + order2.getOrderNumber());
            txn5.setCreatedAt(LocalDateTime.now().minusDays(3));
            txn5.setUpdatedAt(LocalDateTime.now().minusDays(3));
            
            transactionRepository.save(txn1);
            transactionRepository.save(txn2);
            transactionRepository.save(txn3);
            transactionRepository.save(txn4);
            transactionRepository.save(txn5);
            
            System.out.println("Test transactions initialized successfully!");
            
            // Create sample notifications
            Notification notif1 = new Notification();
            notif1.setRecipient("merchant@techstore.com");
            notif1.setNotificationType(Notification.NotificationType.EMAIL);
            notif1.setMessage("Payment of $88.20 USD received for order " + order1.getOrderNumber());
            notif1.setStatus(Notification.NotificationStatus.SENT);
            notif1.setOrder(order1);
            notif1.setRetryCount(0);
            notif1.setCreatedAt(LocalDateTime.now().minusDays(5));
            notif1.setUpdatedAt(LocalDateTime.now().minusDays(5));
            
            Notification notif2 = new Notification();
            notif2.setRecipient("john.doe@example.com");
            notif2.setNotificationType(Notification.NotificationType.EMAIL);
            notif2.setMessage("Order " + order1.getOrderNumber() + " placed successfully. Amount: $90.00 USD");
            notif2.setStatus(Notification.NotificationStatus.SENT);
            notif2.setOrder(order1);
            notif2.setRetryCount(0);
            notif2.setCreatedAt(LocalDateTime.now().minusDays(5));
            notif2.setUpdatedAt(LocalDateTime.now().minusDays(5));
            
            Notification notif3 = new Notification();
            notif3.setRecipient("merchant@fashionworld.com");
            notif3.setNotificationType(Notification.NotificationType.EMAIL);
            notif3.setMessage("Payment of $73.50 USD received for order " + order2.getOrderNumber());
            notif3.setStatus(Notification.NotificationStatus.SENT);
            notif3.setOrder(order2);
            notif3.setRetryCount(0);
            notif3.setCreatedAt(LocalDateTime.now().minusDays(3));
            notif3.setUpdatedAt(LocalDateTime.now().minusDays(3));
            
            Notification notif4 = new Notification();
            notif4.setRecipient("test@failed.com");
            notif4.setNotificationType(Notification.NotificationType.EMAIL);
            notif4.setMessage("Test failed notification");
            notif4.setStatus(Notification.NotificationStatus.FAILED);
            notif4.setOrder(order3);
            notif4.setRetryCount(1);
            notif4.setFailureReason("Email delivery failed: Connection timeout");
            notif4.setCreatedAt(LocalDateTime.now().minusDays(1));
            notif4.setUpdatedAt(LocalDateTime.now().minusDays(1));
            
            notificationRepository.save(notif1);
            notificationRepository.save(notif2);
            notificationRepository.save(notif3);
            notificationRepository.save(notif4);
            
            System.out.println("Test notifications initialized successfully!");
            
            // Create sample audit logs
            AuditLog audit1 = new AuditLog("Order", order1.getOrderId(), AuditLog.AuditAction.ORDER_CREATED,
                "Order created for customer " + customer1.getCustomerId() + " with amount $90.00", customer1.getCustomerId());
            audit1.setCreatedAt(LocalDateTime.now().minusDays(5));
            
            AuditLog audit2 = new AuditLog("Order", order1.getOrderId(), AuditLog.AuditAction.ORDER_VALIDATED,
                "Order validated, initiating payment", customer1.getCustomerId());
            audit2.setCreatedAt(LocalDateTime.now().minusDays(5));
            
            AuditLog audit3 = new AuditLog("Wallet", wallet1.getWalletId(), AuditLog.AuditAction.WALLET_DEBITED,
                "Debited $91.80 USD from customer wallet", customer1.getCustomerId());
            audit3.setCreatedAt(LocalDateTime.now().minusDays(5));
            
            AuditLog audit4 = new AuditLog("Merchant", merchant1.getMerchantId(), AuditLog.AuditAction.MERCHANT_CREDITED,
                "Credited $88.20 USD to merchant wallet after deducting wallet fee of $1.80", null);
            audit4.setCreatedAt(LocalDateTime.now().minusDays(5));
            
            AuditLog audit5 = new AuditLog("Order", order1.getOrderId(), AuditLog.AuditAction.ORDER_PAYMENT_COMPLETED,
                "Payment processed successfully for amount $90.00 USD", customer1.getCustomerId());
            audit5.setCreatedAt(LocalDateTime.now().minusDays(5));
            
            AuditLog audit6 = new AuditLog("Notification", notif1.getNotificationId(), AuditLog.AuditAction.NOTIFICATION_SENT,
                "Merchant notification sent successfully to merchant@techstore.com", null);
            audit6.setCreatedAt(LocalDateTime.now().minusDays(5));
            
            AuditLog audit7 = new AuditLog("Order", order2.getOrderId(), AuditLog.AuditAction.ORDER_CREATED,
                "Order created for customer " + customer2.getCustomerId() + " with amount $75.00", customer2.getCustomerId());
            audit7.setCreatedAt(LocalDateTime.now().minusDays(3));
            
            AuditLog audit8 = new AuditLog("Order", order2.getOrderId(), AuditLog.AuditAction.ORDER_PAYMENT_COMPLETED,
                "Payment processed successfully for amount $75.00 USD", customer2.getCustomerId());
            audit8.setCreatedAt(LocalDateTime.now().minusDays(3));
            
            auditLogRepository.save(audit1);
            auditLogRepository.save(audit2);
            auditLogRepository.save(audit3);
            auditLogRepository.save(audit4);
            auditLogRepository.save(audit5);
            auditLogRepository.save(audit6);
            auditLogRepository.save(audit7);
            auditLogRepository.save(audit8);
            
            System.out.println("Test audit logs initialized successfully!");
            System.out.println("\n========================================");
            System.out.println("All test data initialized successfully!");
            System.out.println("Customers: 4, Accounts: 6, Wallets: 4");
            System.out.println("Merchants: 3, Products: 6");
            System.out.println("Orders: 3, Transactions: 5");
            System.out.println("Notifications: 4, Audit Logs: 8");
            System.out.println("========================================\n");
        }
    }
}
