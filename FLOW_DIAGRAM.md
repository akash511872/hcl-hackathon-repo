# E-Commerce Backend Platform - API Flow Diagrams

This document contains comprehensive flow diagrams for all APIs in the application.

## Overview Flow Diagram

```mermaid
graph TB
    Client[Client Application]
    
    Client -->|1. Authenticate| LoginAPI[POST /api/login]
    Client -->|2. Get Account Info| AccountAPI[GET /api/accounts/customer/:id]
    Client -->|3. Place Order| OrderAPI[POST /api/orders]
    
    LoginAPI --> LoginService[Login Service]
    LoginService --> CustomerRepo[(Customer Repository)]
    LoginService -->|Validate Password| PasswordValidator[Password Validator]
    LoginService -->|Success| ClientResponse1[Return Customer Details]
    
    AccountAPI --> AccountService[Account Service]
    AccountService --> AccountRepo[(Account Repository)]
    AccountService -->|Success| ClientResponse2[Return Account List]
    
    OrderAPI --> OrderService[Order Service]
    OrderService --> ValidationLayer[Validation Layer]
    OrderService --> PaymentService[Payment Service]
    PaymentService --> NotificationService[Notification Service]
    PaymentService --> AuditService[Audit Service]
    OrderService -->|Success| ClientResponse3[Return Order Details]
    
    style Client fill:#e1f5ff
    style LoginAPI fill:#ffe1e1
    style AccountAPI fill:#ffe1e1
    style OrderAPI fill:#ffe1e1
    style PaymentService fill:#fff4e1
    style NotificationService fill:#e1ffe1
    style AuditService fill:#f0e1ff
```

## 1. Login Service Flow

```mermaid
sequenceDiagram
    participant Client
    participant LoginController
    participant LoginService
    participant CustomerRepository
    participant Database
    
    Client->>LoginController: POST /api/login<br/>{username, password}
    LoginController->>LoginService: authenticate(request)
    
    LoginService->>LoginService: Validate password format<br/>(min 8 chars, uppercase,<br/>lowercase, digit, special char)
    
    alt Password format invalid
        LoginService-->>LoginController: 401 Unauthorized<br/>"Invalid password format"
        LoginController-->>Client: Error Response
    else Password format valid
        LoginService->>CustomerRepository: findByUsername(username)
        CustomerRepository->>Database: Query customer
        Database-->>CustomerRepository: Customer data
        CustomerRepository-->>LoginService: Customer object
        
        alt Customer not found
            LoginService-->>LoginController: 401 Unauthorized<br/>"Invalid username"
            LoginController-->>Client: Error Response
        else Customer found
            LoginService->>LoginService: Compare passwords
            
            alt Password mismatch
                LoginService-->>LoginController: 401 Unauthorized<br/>"Invalid password"
                LoginController-->>Client: Error Response
            else Password match
                LoginService-->>LoginController: 200 OK<br/>Customer details
                LoginController-->>Client: Success Response<br/>{customerId, name, email}
            end
        end
    end
```

## 2. Account Service Flow

```mermaid
sequenceDiagram
    participant Client
    participant AccountController
    participant AccountService
    participant CustomerRepository
    participant AccountRepository
    participant Database
    
    Client->>AccountController: GET /api/accounts/customer/{customerId}
    AccountController->>AccountService: getAccountsByCustomerId(customerId)
    
    AccountService->>CustomerRepository: findById(customerId)
    CustomerRepository->>Database: Query customer
    Database-->>CustomerRepository: Customer data
    CustomerRepository-->>AccountService: Customer object
    
    alt Customer not found
        AccountService-->>AccountController: 404 Not Found<br/>"Customer not found"
        AccountController-->>Client: Error Response
    else Customer found
        AccountService->>AccountRepository: findByCustomerId(customerId)
        AccountRepository->>Database: Query accounts
        Database-->>AccountRepository: Account list
        AccountRepository-->>AccountService: List of accounts
        
        AccountService->>AccountService: Build response with<br/>account details
        AccountService-->>AccountController: 200 OK<br/>Account details list
        AccountController-->>Client: Success Response<br/>[{accountNumber, balance,<br/>accountType, customerName}]
    end
```

## 3. Order & Payment Service Flow

```mermaid
sequenceDiagram
    participant Client
    participant OrderController
    participant OrderService
    participant PaymentService
    participant NotificationService
    participant AuditService
    participant Repositories
    participant Database
    
    Client->>OrderController: POST /api/orders<br/>{customerId, productId,<br/>quantity, currency}
    OrderController->>OrderService: placeOrder(request)
    
    Note over OrderService: VALIDATION PHASE
    OrderService->>OrderService: 1. Validate request input
    OrderService->>Repositories: 2. Validate customer exists
    OrderService->>Repositories: 3. Validate product exists
    OrderService->>OrderService: 4. Validate currency<br/>(USD, EUR, GBP, INR)
    OrderService->>Repositories: 5. Validate product availability
    OrderService->>OrderService: 6. Calculate total amount
    OrderService->>Repositories: 7. Validate wallet balance<br/>(including 2% fee)
    
    alt Validation fails
        OrderService-->>OrderController: 400 Bad Request<br/>Error message
        OrderController-->>Client: Error Response
    else All validations pass
        Note over OrderService: ORDER CREATION
        OrderService->>Repositories: 8. Create order record
        Repositories->>Database: Insert order
        OrderService->>AuditService: Log: Order Created
        
        Note over PaymentService: PAYMENT PROCESSING
        OrderService->>PaymentService: processPayment(order, wallet, product)
        
        PaymentService->>PaymentService: Calculate amounts<br/>(total + 2% fee)
        PaymentService->>Repositories: Lock wallet (pessimistic)
        PaymentService->>Repositories: Debit customer wallet
        PaymentService->>AuditService: Log: Wallet Debited
        
        Note over PaymentService: ✓ PAYMENT SUCCESSFUL
        PaymentService->>AuditService: Log: Payment Confirmed
        
        Note over PaymentService: POST-PAYMENT OPERATIONS
        
        rect rgb(255, 250, 205)
            Note over PaymentService: 1. WALLET FEE COLLECTION
            PaymentService->>Repositories: Record wallet fee transaction
            PaymentService->>AuditService: Log: Wallet Fee Collected
        end
        
        rect rgb(230, 255, 230)
            Note over PaymentService: 2. MERCHANT PAYMENT GATEWAY
            PaymentService->>PaymentService: initiatePaymentGateway()<br/>Generate gateway txn ID
            PaymentService->>Repositories: Credit merchant wallet<br/>(amount - fee)
            PaymentService->>AuditService: Log: Gateway Initiated
            PaymentService->>Repositories: Record credit transaction
        end
        
        PaymentService->>Repositories: Mark all transactions<br/>as COMPLETED
        PaymentService-->>OrderService: Payment successful
        
        Note over NotificationService: NOTIFICATIONS
        OrderService->>NotificationService: sendMerchantNotification()
        NotificationService->>NotificationService: Attempt send
        
        alt Notification fails
            NotificationService->>NotificationService: Retry once
            alt Retry fails
                NotificationService->>Repositories: Log to transaction ledger
            end
        end
        
        NotificationService->>Repositories: Save notification record
        
        OrderService->>Repositories: Update product quantity
        OrderService->>AuditService: Log: Order Completed
        
        OrderService-->>OrderController: 201 Created<br/>Order details + txn info
        OrderController-->>Client: Success Response<br/>{orderNumber, status,<br/>totalAmount, walletFee,<br/>remainingBalance}
    end
    
    Note over PaymentService: Transaction Rollback on Any Failure
```

## 4. Payment Service Transaction Flow (Detailed)

```mermaid
graph TB
    Start([Start Payment Processing])
    Start --> Validate[Validate Wallet Balance<br/>Total + 2% Fee]
    
    Validate -->|Insufficient| Fail1[Throw InsufficientBalanceException]
    Validate -->|Sufficient| Lock[Lock Customer Wallet<br/>Pessimistic Lock]
    
    Lock --> Debit[Debit Total Amount<br/>from Customer Wallet]
    Debit --> RecordDebit[Record Debit Transaction]
    RecordDebit --> AuditDebit[Audit: Wallet Debited]
    
    AuditDebit --> Confirm[Mark Payment as Successful<br/>Payment Gateway Confirmation]
    Confirm --> AuditConfirm[Audit: Payment Confirmed]
    
    Note1[POST-PAYMENT OPERATIONS]
    AuditConfirm --> Note1
    
    Note1 --> CollectFee[COLLECT WALLET FEE<br/>2% of Transaction]
    CollectFee --> RecordFee[Record Fee Transaction]
    RecordFee --> AuditFee[Audit: Wallet Fee Collected]
    
    AuditFee --> InitGateway[INITIATE PAYMENT GATEWAY<br/>Call Gateway API]
    InitGateway --> GenTxnID[Generate Gateway<br/>Transaction ID]
    GenTxnID --> CheckGateway{Gateway<br/>Success?}
    
    CheckGateway -->|No| Fail2[Throw PaymentGatewayException]
    CheckGateway -->|Yes| CreditMerchant[Credit Merchant Wallet<br/>Amount - Fee]
    
    CreditMerchant --> RecordCredit[Record Credit Transaction]
    RecordCredit --> AuditGateway[Audit: Gateway Payment Initiated]
    
    AuditGateway --> MarkComplete[Mark All Transactions<br/>as COMPLETED]
    MarkComplete --> SendNotif[Send Merchant Notification<br/>with Settlement Timeline]
    
    SendNotif --> Success([Payment Processing Complete])
    
    Fail1 --> Rollback[Transaction Rollback]
    Fail2 --> Rollback
    Rollback --> MarkFailed[Mark Transactions as FAILED]
    MarkFailed --> AuditFail[Audit: Payment Failed]
    AuditFail --> Error([Return Error])
    
    style Start fill:#e1f5ff
    style Success fill:#e1ffe1
    style Error fill:#ffe1e1
    style Rollback fill:#ffe1e1
    style Note1 fill:#fff4e1
    style CollectFee fill:#fffacd
    style InitGateway fill:#e6ffe6
```

## 5. System Architecture Flow

```mermaid
graph TB
    subgraph "Client Layer"
        Client[Client Application/Postman/curl]
    end
    
    subgraph "Controller Layer (REST Endpoints)"
        LC[LoginController<br/>POST /api/login]
        AC[AccountController<br/>GET /api/accounts/customer/:id]
        OC[OrderController<br/>POST /api/orders]
    end
    
    subgraph "Service Layer (Business Logic)"
        LS[LoginService<br/>- Password Validation<br/>- Authentication]
        AS[AccountService<br/>- Account Retrieval<br/>- Balance Info]
        OS[OrderService<br/>- Order Validation<br/>- Order Creation]
        PS[PaymentService<br/>- Payment Processing<br/>- Transaction Management<br/>- Gateway Integration]
        NS[NotificationService<br/>- Merchant Notifications<br/>- Customer Notifications<br/>- Retry Logic]
        AUS[AuditService<br/>- Audit Logging<br/>- Compliance Tracking]
    end
    
    subgraph "Repository Layer (Data Access)"
        CR[CustomerRepository]
        AR[AccountRepository]
        WR[WalletRepository]
        PR[ProductRepository]
        MR[MerchantRepository]
        OR[OrderRepository]
        TR[TransactionRepository]
        NR[NotificationRepository]
        ALR[AuditLogRepository]
    end
    
    subgraph "Data Layer"
        DB[(H2 In-Memory Database<br/>9 Tables)]
    end
    
    Client --> LC
    Client --> AC
    Client --> OC
    
    LC --> LS
    AC --> AS
    OC --> OS
    
    LS --> CR
    AS --> CR
    AS --> AR
    
    OS --> CR
    OS --> PR
    OS --> WR
    OS --> PS
    OS --> NS
    OS --> AUS
    
    PS --> WR
    PS --> MR
    PS --> TR
    PS --> AUS
    PS --> NS
    
    NS --> NR
    AUS --> ALR
    
    CR --> DB
    AR --> DB
    WR --> DB
    PR --> DB
    MR --> DB
    OR --> DB
    TR --> DB
    NR --> DB
    ALR --> DB
    
    style Client fill:#e1f5ff
    style LC fill:#ffe1e1
    style AC fill:#ffe1e1
    style OC fill:#ffe1e1
    style PS fill:#fff4e1
    style NS fill:#e1ffe1
    style AUS fill:#f0e1ff
    style DB fill:#e1e1ff
```

## 6. Data Model Relationships

```mermaid
erDiagram
    CUSTOMER ||--o{ ACCOUNT : has
    CUSTOMER ||--o| WALLET : has
    CUSTOMER ||--o{ ORDER : places
    
    MERCHANT ||--o{ PRODUCT : sells
    MERCHANT ||--o{ ORDER : receives
    
    PRODUCT ||--o{ ORDER : "ordered in"
    
    ORDER ||--o{ TRANSACTION : generates
    ORDER ||--o{ NOTIFICATION : triggers
    ORDER ||--o{ AUDIT_LOG : tracked_by
    
    WALLET ||--o{ TRANSACTION : "participates in"
    
    CUSTOMER {
        Long customerId PK
        String username
        String password
        String firstName
        String lastName
        String email
        String phone
    }
    
    ACCOUNT {
        Long accountId PK
        Long customerId FK
        String accountNumber
        BigDecimal balance
        String accountType
    }
    
    WALLET {
        Long walletId PK
        Long customerId FK
        BigDecimal balance
        String currency
    }
    
    MERCHANT {
        Long merchantId PK
        String merchantName
        String email
        BigDecimal walletBalance
    }
    
    PRODUCT {
        Long productId PK
        Long merchantId FK
        String productName
        BigDecimal price
        String currency
        Integer availableQuantity
    }
    
    ORDER {
        Long orderId PK
        Long customerId FK
        Long productId FK
        String orderNumber
        Integer quantity
        BigDecimal totalAmount
        String currency
        String status
    }
    
    TRANSACTION {
        Long transactionId PK
        Long orderId FK
        String transactionType
        BigDecimal amount
        String currency
        String status
    }
    
    NOTIFICATION {
        Long notificationId PK
        Long orderId FK
        String recipient
        String message
        String status
    }
    
    AUDIT_LOG {
        Long auditId PK
        String entityType
        Long entityId
        String action
        String details
    }
```

## 7. Error Handling Flow

```mermaid
graph TB
    Request[API Request]
    Request --> Validation{Validation}
    
    Validation -->|Invalid Input| E1[400 Bad Request<br/>Invalid request parameters]
    Validation -->|Invalid Credentials| E2[401 Unauthorized<br/>Authentication failed]
    Validation -->|Customer Not Found| E3[404 Not Found<br/>Resource not found]
    Validation -->|Insufficient Balance| E4[400 Bad Request<br/>Insufficient wallet balance]
    Validation -->|Invalid Currency| E5[400 Bad Request<br/>Currency not supported]
    Validation -->|Product Unavailable| E6[400 Bad Request<br/>Insufficient product quantity]
    Validation -->|Payment Gateway Failure| E7[500 Internal Server Error<br/>Payment gateway failed]
    
    Validation -->|All Pass| Process[Process Request]
    
    Process --> Transaction{Transaction<br/>Processing}
    Transaction -->|Exception| Rollback[Automatic Rollback]
    Rollback --> E8[500 Internal Server Error<br/>Transaction failed]
    
    Transaction -->|Success| Response[Success Response]
    
    E7 --> AuditFail[Audit: Payment Failed]
    E8 --> AuditFail
    Rollback --> MarkFailed[Mark Transactions Failed]
    
    style E1 fill:#ffe1e1
    style E2 fill:#ffe1e1
    style E3 fill:#ffe1e1
    style E4 fill:#ffe1e1
    style E5 fill:#ffe1e1
    style E6 fill:#ffe1e1
    style E7 fill:#ffe1e1
    style E8 fill:#ffe1e1
    style Response fill:#e1ffe1
```

## API Endpoints Summary

| API | Method | Endpoint                              | Description |
|-----|--------|---------------------------------------|-------------|
| Login Service | POST | `/api/login`                          | Authenticate user with username/password |
| Account Service | GET | `/api/accounts/customer/{customerId}` | Get account details for a customer |
| Order Service | POST | `/api/placeOrder`                    | Place an order with payment processing |

## Key Features Illustrated in Diagrams

1. **Authentication Flow**: Password validation with format requirements
2. **Account Retrieval**: Multi-account support per customer
3. **Order Processing**: Complete validation chain before payment
4. **Payment Flow**: Atomic transaction with rollback capability
5. **Post-Payment Operations**: 
   - Wallet fee collection (2%)
   - Payment gateway integration for merchant transfer
6. **Notification System**: Retry mechanism with ledger logging
7. **Audit Trail**: Complete tracking of all operations
8. **Error Handling**: Comprehensive error responses with proper HTTP codes
9. **Transaction Management**: Pessimistic locking and automatic rollback
10. **Data Integrity**: Proper entity relationships and foreign keys

## Technologies Used

- **Spring Boot** - Application framework
- **Spring Data JPA** - Data persistence
- **H2 Database** - In-memory database
- **Spring Transactions** - Transaction management
- **Mockito & JUnit 5** - Testing framework
