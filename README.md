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
The application initializes with two test customers:

1. **Username:** john.doe, **Password:** Test@123
2. **Username:** jane.smith, **Password:** Pass@word1

## API Testing Examples

### Successful Login
```bash
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john.doe","password":"Test@123"}'
```

### Invalid Password
```bash
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john.doe","password":"wrongpass"}'
```

### Invalid Password Format
```bash
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john.doe","password":"short"}'
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
