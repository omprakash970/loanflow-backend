# LoanFlow Backend - Phase 1 Authentication System

## ✅ Phase 1 Complete - Authentication Backend

This document provides a complete guide to the Phase 1 authentication system for the LoanFlow Loan Issuance System.

---

## 📁 Final Project Structure

```
loanflow-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── klef/
│   │   │           └── loanflowbackend/
│   │   │               ├── LoanflowBackendApplication.java
│   │   │               ├── config/
│   │   │               │   └── SecurityConfig.java
│   │   │               ├── controller/
│   │   │               │   └── AuthController.java
│   │   │               ├── dto/
│   │   │               │   ├── AuthRequest.java
│   │   │               │   ├── AuthResponse.java
│   │   │               │   └── RegisterRequest.java
│   │   │               ├── entity/
│   │   │               │   ├── Role.java
│   │   │               │   └── User.java
│   │   │               ├── repository/
│   │   │               │   └── UserRepository.java
│   │   │               ├── security/
│   │   │               │   ├── CustomUserDetailsService.java
│   │   │               │   ├── JwtAuthenticationFilter.java
│   │   │               │   └── JwtService.java
│   │   │               └── service/
│   │   │                   └── AuthService.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/...
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

---

## 🔧 Tech Stack & Dependencies

**Core Stack:**
- Java 17
- Spring Boot 4.0.5
- Maven
- MySQL 8.0+
- Spring Web
- Spring Data JPA
- Spring Security
- JWT (JJWT 0.12.3)
- Lombok
- Validation

**All dependencies are configured in `pom.xml`:**
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-boot-starter-validation
- mysql-connector-j
- org.projectlombok:lombok
- io.jsonwebtoken:jjwt-api (0.12.3)
- io.jsonwebtoken:jjwt-impl (0.12.3)
- io.jsonwebtoken:jjwt-jackson (0.12.3)
- spring-boot-starter-test

---

## 📝 Component Overview

### 1. **Role Enum** (`entity/Role.java`)
Defines four user roles:
- `ADMIN` - System administrator
- `LENDER` - Loan provider
- `BORROWER` - Loan applicant
- `ANALYST` - Loan analyst

### 2. **User Entity** (`entity/User.java`)
- `id` (Long) - Auto-generated primary key
- `fullName` (String) - User's full name (required)
- `email` (String) - Unique, required, validated
- `password` (String) - BCrypt encrypted (required)
- `role` (Enum) - User's role
- `createdAt` (Long) - Creation timestamp
- `updatedAt` (Long) - Last update timestamp

### 3. **DTOs** (`dto/`)
- **RegisterRequest**: fullName, email, password, role
- **AuthRequest**: email, password
- **AuthResponse**: token, role, email, fullName

### 4. **Repository** (`repository/UserRepository.java`)
JpaRepository with custom methods:
- `findByEmail(String email)` - Find user by email
- `existsByEmail(String email)` - Check email existence

### 5. **Security Components** (`security/`)

**JwtService:**
- Generates JWT tokens with expiration
- Extracts email from tokens
- Validates token integrity and expiration
- Uses HMAC-SHA256 signing algorithm

**CustomUserDetailsService:**
- Implements Spring Security's UserDetailsService
- Loads user by email
- Returns UserDetails with role-based authorities

**JwtAuthenticationFilter:**
- Extends OncePerRequestFilter
- Extracts Bearer token from Authorization header
- Validates token and sets SecurityContext
- Runs before UsernamePasswordAuthenticationFilter

### 6. **SecurityConfig** (`config/SecurityConfig.java`)
- Disables CSRF (for stateless API)
- Sets session management to STATELESS
- Permits `/api/auth/**` endpoints without authentication
- Requires authentication for all other routes
- Configures BCryptPasswordEncoder
- Configures DaoAuthenticationProvider
- Registers JWT filter
- Enables CORS for React frontend (http://localhost:5173)

### 7. **AuthService** (`service/AuthService.java`)
**register(RegisterRequest):**
- Validates email doesn't exist
- Validates role
- Encrypts password with BCrypt
- Creates and saves user
- Generates JWT token
- Returns AuthResponse

**login(AuthRequest):**
- Authenticates credentials with AuthenticationManager
- Generates JWT token
- Returns AuthResponse with user details

### 8. **AuthController** (`controller/AuthController.java`)
**Endpoints:**
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user
- CORS enabled for http://localhost:5173

---

## 🚀 Setup & Running

### Prerequisites
- Java 17+
- Maven 3.6+
- MySQL 8.0+ running
- Port 8080 available

### Database Setup
```sql
-- Create database
CREATE DATABASE loan_management;
USE loan_management;

-- Tables will be auto-created by Hibernate (ddl-auto=update)
```

### Application Properties
File: `src/main/resources/application.properties`
```properties
# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/loan_management
spring.datasource.username=root
spring.datasource.password=Nancy123abc@

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Server Configuration
server.port=8080

# JWT Configuration
jwt.secret=your_super_secret_key_which_should_be_long_enough_123456789
jwt.expiration=86400000
```

**Note:** Change credentials as needed for your environment.

### Run Application

**Using Maven:**
```bash
mvn spring-boot:run
```

**Using JAR:**
```bash
mvn clean package
java -jar target/loanflow-backend-0.0.1-SNAPSHOT.jar
```

---

## 🧪 API Testing with Postman

### Base URL
```
http://localhost:8080
```

### 1. Register New User

**Request:**
```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "fullName": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "BORROWER"
}
```

**Success Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwiaWF0IjoxNzExNzk5Nzk2LCJleHAiOjE3MTE4ODYxOTZ9.xY_h0sN...",
  "role": "BORROWER",
  "email": "john@example.com",
  "fullName": "John Doe"
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Email already in use"
}
```

### 2. Login User

**Request:**
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

**Success Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwiaWF0IjoxNzExNzk5Nzk2LCJleHAiOjE3MTE4ODYxOTZ9.xY_h0sN...",
  "role": "BORROWER",
  "email": "john@example.com",
  "fullName": "John Doe"
}
```

**Error Response (401 Unauthorized):**
```json
{
  "message": "Invalid credentials"
}
```

### 3. Sample Test Users

**Admin User:**
```json
{
  "fullName": "Admin User",
  "email": "admin@loanflow.com",
  "password": "admin123",
  "role": "ADMIN"
}
```

**Lender User:**
```json
{
  "fullName": "Lender User",
  "email": "lender@loanflow.com",
  "password": "lender123",
  "role": "LENDER"
}
```

**Borrower User:**
```json
{
  "fullName": "Borrower User",
  "email": "borrower@loanflow.com",
  "password": "borrower123",
  "role": "BORROWER"
}
```

**Analyst User:**
```json
{
  "fullName": "Analyst User",
  "email": "analyst@loanflow.com",
  "password": "analyst123",
  "role": "ANALYST"
}
```

---

## 🔐 Using JWT Token

After login/register, use the token in subsequent requests:

**Request with JWT Token:**
```
GET http://localhost:8080/api/protected-resource
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwiaWF0IjoxNzExNzk5Nzk2LCJleHAiOjE3MTE4ODYxOTZ9.xY_h0sN...
```

**In Postman:**
1. Go to the "Authorization" tab
2. Select "Bearer Token"
3. Paste the token in the "Token" field

---

## ✅ Testing Checklist

- [ ] MySQL database is running
- [ ] Database `loan_management` is created
- [ ] Application starts without errors (`mvn spring-boot:run`)
- [ ] Can register new users with different roles
- [ ] Can't register with duplicate email
- [ ] Can login with correct credentials
- [ ] Can't login with wrong password
- [ ] JWT token is returned on successful registration/login
- [ ] Token can be used in subsequent requests
- [ ] Invalid token is rejected
- [ ] Expired token is rejected (after 24 hours in this config)
- [ ] CORS is working for React frontend

---

## 📋 Validation Rules

**RegisterRequest:**
- `fullName`: Required, non-blank
- `email`: Required, valid email format, unique in database
- `password`: Required, minimum 6 characters
- `role`: Required, must be one of: ADMIN, LENDER, BORROWER, ANALYST

**AuthRequest:**
- `email`: Required, valid email format
- `password`: Required, non-blank

---

## 🔄 JWT Token Details

**Algorithm:** HMAC-SHA256 (HS256)
**Expiration:** 24 hours (86400000 ms)
**Payload:**
- `sub` (subject): User email
- `iat` (issued at): Token creation time
- `exp` (expiration): Token expiration time

---

## 🛡️ Security Features Implemented

✅ Password encryption with BCrypt
✅ JWT token-based stateless authentication
✅ CSRF protection disabled (not needed for stateless API)
✅ CORS configured for React frontend
✅ Role-based access control foundation
✅ Request validation with Jakarta validation
✅ Secure HTTP-only token handling
✅ Token expiration enforcement
✅ Unique email constraint at database level

---

## 📚 Additional Resources

- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JWT Documentation](https://tools.ietf.org/html/rfc7519)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Lombok Documentation](https://projectlombok.org/)
- [MySQL Documentation](https://dev.mysql.com/doc/)

---

## 🎯 Next Steps (Phase 2)

Potential features for Phase 2:
- [ ] Email verification
- [ ] Password reset functionality
- [ ] Refresh token mechanism
- [ ] User profile endpoints
- [ ] Role-specific endpoints
- [ ] Audit logging
- [ ] API rate limiting
- [ ] Two-factor authentication

---

**Project Status:** ✅ Phase 1 Complete
**Last Updated:** April 3, 2026
**Version:** 0.0.1-SNAPSHOT

