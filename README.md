# HCL Hackathon - E-commerce Backend API

This is a Spring Boot application that provides REST APIs for an e-commerce platform.

## Features Implemented

### 1. Login Service API

**Endpoint:** `POST /api/login`

**Description:** Authenticates a user with username and password validation.

**Request Body:**
```json
{
  "username": "john.doe",
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
The application initializes with two test customers and their accounts:

**Customers:**
1. **Username:** john.doe, **Password:** Test@123, **Customer ID:** 1
   - Account 1: ACC1001 (SAVINGS) - Balance: $5,000.00
   - Account 2: ACC1002 (CHECKING) - Balance: $10,000.00
2. **Username:** jane.smith, **Password:** Pass@word1, **Customer ID:** 2
   - Account 1: ACC2001 (SAVINGS) - Balance: $7,500.50

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
