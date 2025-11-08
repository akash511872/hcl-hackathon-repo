# HCL Hackathon - E-commerce Backend API

This is a Spring Boot application that provides REST APIs for an e-commerce platform.

## 📚 API Documentation

**[VIEW OPENAPI SPECIFICATION](openapi.yaml)** - Complete OpenAPI 3.0 specification with all endpoints, schemas, and examples.

**[VIEW COMPREHENSIVE FLOW DIAGRAMS](FLOW_DIAGRAM.md)** - Complete visual documentation of all API flows, system architecture, and transaction processing.

The flow diagrams include:
- Overview of all APIs and their interactions
- Detailed sequence diagrams for Login, Account, and Order services
- Payment processing flow with post-payment operations
- System architecture with layered design
- Data model relationships (ER diagram)
- Error handling flows
- Transaction management visualization

## OpenAPI Specification

The `openapi.yaml` file provides comprehensive API documentation including:
- All REST endpoint definitions
- Request/response schemas with validation rules
- Detailed examples for success and error scenarios
- Supported HTTP status codes
- Authentication requirements (future implementation)
- Currency and validation constraints

You can:
- View the spec directly in GitHub
- Import into Swagger UI for interactive testing
- Generate client SDKs using OpenAPI Generator
- Use for API testing tools like Postman or Insomnia

## Features Implemented

### 1. Login Service API

**Endpoint:** `POST /api/login`

**Description:** Authenticates a user with username and password validation.

**Request Body:**
```json
{
  "username": "Akash",
  "password": "Test@123"
}
```

**Response (Success):**
```json
{
  "success": true,
  "message": "Login successful",
  "customerDetails": {
    "customerId": 1,
    "username": "john.doe",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "123-456-7890",
    "address": "123 Main St, City, State"
  }
}
```

**Response (Failure):**
```json
{
  "success": false,
  "message": "Invalid username or password",
  "customerDetails": null
}
```

**Password Validation Requirements:**
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character (@$!%*?&)

### 2. Account Service API

**Endpoint:** `GET /api/accounts/customer/{customerId}`

**Description:** Retrieves all account details (account number and balance) for a specific customer.

**Path Parameter:**
- `customerId` - The unique identifier of the customer

**Response (Success):**
```json
[
  {
    "success": true,
    "message": "Account details retrieved successfully",
    "accountDetails": {
      "accountId": 1,
      "accountNumber": "ACC1001",
      "balance": 5000.00,
      "accountType": "SAVINGS",
      "customerId": 1,
      "customerName": "John Doe"
    }
  },
  {
    "success": true,
    "message": "Account details retrieved successfully",
    "accountDetails": {
      "accountId": 2,
      "accountNumber": "ACC1002",
      "balance": 10000.00,
      "accountType": "CHECKING",
      "customerId": 1,
      "customerName": "John Doe"
    }
  }
]
```

**Response (Customer Not Found):**
```json
[
  {
    "success": false,
    "message": "Customer not found",
    "accountDetails": null
  }
]
```

### 3. Order and Payment Service API

**Endpoint:** `POST /api/orders`

**Description:** Places an order with complete payment processing, transaction management, and notifications. The service validates wallet balance, processes payments, credits merchants, collects wallet fees, and maintains comprehensive audit trails.

**Request Body:**
```json
{
  "customerId": 1,
  "productId": 2,
  "quantity": 2,
  "productCost": 45.00,
  "currency": "USD"
}
```

**Request Parameters:**
- `customerId` - The unique identifier of the customer placing the order
- `productId` - The unique identifier of the product to purchase
- `quantity` - Number of items to order (must be positive)
- `productCost` - Cost per unit of the product
- `currency` - Currency code (supported: USD, EUR, GBP, INR)

**Response (Success):**
```json
{
  "success": true,
  "message": "Order placed successfully",
  "orderDetails": {
    "orderId": 1,
    "orderNumber": "ORD-A1B2C3D4",
    "productName": "Wireless Mouse",
    "merchantName": "Tech Store Inc",
    "quantity": 2,
    "totalAmount": 90.00,
    "walletFee": 1.80,
    "currency": "USD",
    "orderStatus": "COMPLETED",
    "remainingWalletBalance": 19908.20
  }
}
```

**Response (Insufficient Balance):**
```json
{
  "success": false,
  "message": "Insufficient wallet balance. Required: 91.80 USD, Available: 100.00 USD",
  "orderDetails": null
}
```

**Response (Product Not Available):**
```json
{
  "success": false,
  "message": "Insufficient product quantity. Available: 5, Requested: 10",
  "orderDetails": null
}
```

**Response (Invalid Currency):**
```json
{
  "success": false,
  "message": "Currency JPY is not supported. Supported currencies: USD, EUR, GBP, INR",
  "orderDetails": null
}
```

**Payment Processing Features:**
- **Wallet Balance Validation:** Checks if customer has sufficient balance including 2% wallet fee
- **Currency Validation:** Ensures the requested currency is supported and matches product currency
- **Product Availability Check:** Verifies sufficient inventory before processing
- **Atomic Transactions:** All operations (debit, credit, fee collection) succeed or rollback together
- **Payment Gateway Integration:** Simulates real payment gateway for merchant bank transfers
- **Post-Payment Operations:** Wallet fee collection and merchant payment initiation happen after successful payment
- **Transaction Ledger:** All operations recorded in transaction history
- **Notifications:** Merchant and customer notified of transaction status with settlement timeline
- **Audit Trail:** Complete audit log maintained for compliance and tracking

**Transaction Flow:**
1. Validate customer, product, currency, and availability
2. Calculate total amount including 2% wallet fee
3. Lock customer wallet (pessimistic locking to prevent concurrent modifications)
4. Debit total amount from customer wallet
5. Mark payment as successful (payment gateway confirmation)
6. **WALLET FEE COLLECTION** - Collect 2% wallet fee after successful payment
7. **MERCHANT PAYMENT INITIATION** - Initiate payment to merchant bank via payment gateway
8. Credit merchant account (amount - wallet fee) - Local record for settlement
9. Record all transactions in ledger
10. Send notification to merchant about payment initiation
11. Send notification to customer about order status
12. Capture comprehensive audit logs

**Post-Payment Operations:**
After payment is confirmed successful, the following operations are automatically performed:
1. **Wallet Fee Collection:** A 2% wallet fee is collected and recorded as a separate transaction
2. **Merchant Payment Gateway Initiation:** Payment is initiated to the merchant's bank account via payment gateway
   - Gateway simulates real-world payment processing
   - Generates gateway transaction ID for tracking
   - Updates merchant wallet balance (local record)
   - Actual settlement to merchant bank occurs within 1-3 business days

**Supported Currencies:**
- USD - US Dollar
- EUR - Euro
- GBP - British Pound
- INR - Indian Rupee

**Wallet Fee:**
- A 2% fee is applied to all transactions
- Fee is deducted from the amount credited to the merchant
- Customer pays: Product Cost + Wallet Fee
- Merchant receives: Product Cost - Wallet Fee

## Running the Application

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher

### Build
```bash
mvn clean package
```

### Run
```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

### Test Data
The application initializes with comprehensive test data for all entities:

**Customers:**
1. **Username:** john.doe, **Password:** Test@123, **Customer ID:** 1
   - Accounts: ACC1001 (SAVINGS) - $5,000.00, ACC1002 (CHECKING) - $10,000.00
   - Wallet: $20,000.00 USD
2. **Username:** jane.smith, **Password:** Pass@word1, **Customer ID:** 2
   - Account: ACC2001 (SAVINGS) - $7,500.50
   - Wallet: $15,000.00 USD
3. **Username:** bob.wilson, **Password:** Secure@456, **Customer ID:** 3
   - Accounts: ACC3001 (CHECKING) - $12,000.00, ACC3002 (SAVINGS) - $3,000.00
   - Wallet: $8,000.00 USD
4. **Username:** alice.brown, **Password:** Strong@789, **Customer ID:** 4
   - Account: ACC4001 (CHECKING) - $25,000.00
   - Wallet: $30,000.00 USD

**Merchants:**
1. **Tech Store Inc** (TECH001) - Wallet: $50,000.00
2. **Fashion World** (FASH001) - Wallet: $30,000.00
3. **Book Haven** (BOOK001) - Wallet: $15,000.00

**Products:**
1. **Laptop Pro 15** - $1,200.00 (50 available) - Tech Store Inc
2. **Wireless Mouse** - $45.00 (200 available) - Tech Store Inc
3. **Designer T-Shirt** - $75.00 (100 available) - Fashion World
4. **Mechanical Keyboard** - $150.00 (75 available) - Tech Store Inc
5. **Running Shoes** - $120.00 (60 available) - Fashion World
6. **Programming Book** - $55.00 (150 available) - Book Haven

**Sample Orders:**
- 3 completed historical orders with full transaction records
- Transaction ledger entries for debit, credit, and wallet fee operations
- Notification records (both successful and failed scenarios)
- Comprehensive audit logs tracking the complete order lifecycle

## API Testing Examples

### Login Service

#### Successful Login
```bash
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john.doe","password":"Test@123"}'
```

#### Invalid Password
```bash
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john.doe","password":"wrongpass"}'
```

#### Invalid Password Format
```bash
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john.doe","password":"short"}'
```

### Account Service

#### Get Accounts for Customer ID 1
```bash
curl -X GET http://localhost:8080/api/accounts/customer/1 \
  -H "Content-Type: application/json"
```

#### Get Accounts for Customer ID 2
```bash
curl -X GET http://localhost:8080/api/accounts/customer/2 \
  -H "Content-Type: application/json"
```

#### Get Accounts for Non-existent Customer
```bash
curl -X GET http://localhost:8080/api/accounts/customer/999 \
  -H "Content-Type: application/json"
```

### Order Service

#### Place Order for Wireless Mouse
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "productId": 2,
    "quantity": 2,
    "productCost": 45.00,
    "currency": "USD"
  }'
```

#### Place Order for Designer T-Shirt
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 3,
    "productId": 3,
    "quantity": 1,
    "productCost": 75.00,
    "currency": "USD"
  }'
```

#### Place Order for Programming Book
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 4,
    "productId": 6,
    "quantity": 3,
    "productCost": 55.00,
    "currency": "USD"
  }'
```

#### Order with Insufficient Balance (Will Fail)
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 3,
    "productId": 1,
    "quantity": 10,
    "productCost": 1200.00,
    "currency": "USD"
  }'
```

#### Order with Unsupported Currency (Will Fail)
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "productId": 2,
    "quantity": 1,
    "productCost": 45.00,
    "currency": "JPY"
  }'
```

## Database

The application uses H2 in-memory database for development/testing. The database is reset on each application restart.

### H2 Console
Access the H2 console at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (leave empty)

## Technology Stack
- Spring Boot 2.7.12
- Spring Data JPA
- H2 Database
- Maven
- Java 11

## Architecture

### Layered Design
- **Controller Layer:** Handles HTTP requests and responses (LoginController, AccountController, OrderController)
- **Service Layer:** Contains business logic and transaction management (LoginService, AccountService, OrderService, PaymentService, NotificationService, AuditService)
- **Repository Layer:** Manages database operations with JPA repositories
- **Model Layer:** Domain entities with proper JPA annotations and relationships

### Key Features
- **Transaction Management:** ACID-compliant transactions with automatic rollback on failures
- **Pessimistic Locking:** Prevents concurrent wallet modifications
- **Audit Trail:** Comprehensive logging of all operations
- **Error Handling:** Robust validation and error responses
- **Notification System:** Merchant and customer notifications with retry logic
- **Security:** Password validation and secure data handling

## Error Handling

The application provides comprehensive error handling with appropriate HTTP status codes:

- **200 OK:** Successful GET requests
- **201 Created:** Successful order placement
- **400 Bad Request:** Invalid input data, validation failures
- **401 Unauthorized:** Invalid login credentials
- **404 Not Found:** Customer or resource not found
- **500 Internal Server Error:** Unexpected server errors

All error responses include descriptive messages to help diagnose issues.
