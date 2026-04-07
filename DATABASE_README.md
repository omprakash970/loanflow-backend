# LoanFlow - Database Documentation

## Table of Contents
1. [Database Overview](#database-overview)
2. [Database Connection Configuration](#database-connection-configuration)
3. [Entity-Relationship Model (ER Model)](#entity-relationship-model)
4. [Database Schema](#database-schema)
5. [Tables and Fields](#tables-and-fields)
6. [Relationships](#relationships)
7. [Database Diagrams](#database-diagrams)
8. [Connection Details](#connection-details)

---

## Database Overview

**LoanFlow** is a comprehensive peer-to-peer lending platform designed to facilitate connections between borrowers and lenders. The system manages the complete lifecycle of loans, from request and offer stages through approval, disbursement, and EMI payments.

### Key Characteristics:
- **Type**: Relational Database (MySQL)
- **Database Name**: `loan_management`
- **Engine**: MySQL 5.7+
- **ORM Framework**: Spring Data JPA with Hibernate
- **SQL Dialect**: MySQL

---

## Database Connection Configuration

### Connection Details

**File**: `src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/loan_management
spring.datasource.username=root
spring.datasource.password=Nancy123abc@

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

server.port=8082
jwt.secret=your_super_secret_key_which_should_be_long_enough_123456789
jwt.expiration=86400000
```

### Connection Parameters

| Parameter | Value | Description |
|-----------|-------|-------------|
| **Protocol** | jdbc:mysql | MySQL JDBC driver protocol |
| **Host** | localhost | Database server address |
| **Port** | 3306 | Default MySQL port |
| **Database Name** | loan_management | Database name |
| **Username** | root | MySQL user account |
| **Password** | Nancy123abc@ | MySQL password |
| **DDL Auto** | update | Hibernate auto-updates schema on startup |
| **Show SQL** | true | Logs all SQL queries to console |
| **Format SQL** | true | Formats SQL queries for readability |

### Setting Up the Database

#### Prerequisites:
- MySQL Server (5.7 or higher)
- MySQL Client or MySQL Workbench
- Java 17 or higher

#### Steps:

1. **Create the Database**:
   ```sql
   CREATE DATABASE loan_management;
   ```

2. **Create MySQL User** (Optional):
   ```sql
   CREATE USER 'root'@'localhost' IDENTIFIED BY 'Nancy123abc@';
   GRANT ALL PRIVILEGES ON loan_management.* TO 'root'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **Start the Application**:
   ```bash
   mvn spring-boot:run
   ```
   
   Hibernate will automatically create all tables with the `spring.jpa.hibernate.ddl-auto=update` setting.

---

## Entity-Relationship Model

### ER Diagram (ASCII Art)

```
┌─────────────────────────────────────────────────────────────────┐
│                      LOANFLOW DATABASE                          │
└─────────────────────────────────────────────────────────────────┘

                            ┌──────────────┐
                            │    USERS     │
                            ├──────────────┤
                            │ id (PK)      │
                            │ fullName     │
                            │ email (UQ)   │
                            │ password     │
                            │ role (ENUM)  │
                            │ createdAt    │
                            │ updatedAt    │
                            └──────────────┘
                             ▲             ▲
                    ┌────────┘             └────────┐
                    │                                │
                    │ OneToOne                       │ OneToOne
                    │                                │
            ┌───────────────┐                ┌──────────────┐
            │  BORROWERS    │                │   LENDERS    │
            ├───────────────┤                ├──────────────┤
            │ id (PK)       │                │ id (PK)      │
            │ user_id (FK)  │                │ user_id (FK) │
            │ activeLoans   │                │ companyName  │
            │ riskLevel     │                │ activeLoans  │
            │ creditScore   │                │ totalDisbursed│
            │ kycVerified   │                │ createdAt    │
            │ createdAt     │                │ updatedAt    │
            │ updatedAt     │                └──────────────┘
            └───────────────┘                       ▲
                    ▲                               │
                    │ OneToMany                     │ ManyToOne
                    │                               │
            ┌───────────────────────────────────────┴────────────┐
            │                                                    │
    ┌──────────────┐                                    ┌──────────────┐
    │ LOAN_REQUESTS│                                    │    LOANS     │
    ├──────────────┤                                    ├──────────────┤
    │ id (PK)      │                                    │ id (PK)      │
    │ request_code │                                    │ loanId       │
    │ borrower_id  │◄────── ManyToOne                  │ borrower_id  │
    │ amount       │                                    │ lender_id    │
    │ tenure       │                                    │ amount       │
    │ purpose      │                                    │ interestRate │
    │ interestRate │                                    │ tenure       │
    │ status       │                                    │ purpose      │
    │ approved...  │◄─── ManyToOne (Lender)           │ status       │
    │ sanctioned..│◄─── OneToOne (Loan)                │ startDate    │
    │ createdAt    │                                    │ nextPaymentDate│
    │ updatedAt    │                                    │ createdAt    │
    └──────────────┘                                    │ updatedAt    │
                                                        └──────────────┘
    ┌──────────────┐                                           ▲ ▲
    │ LOAN_OFFERS  │                                           │ │
    ├──────────────┤                                           │ │
    │ id (PK)      │                               OneToMany───┘ │
    │ offer_code   │                                             │
    │ lender_id    │◄────── ManyToOne (Lender)   OneToOne────────┘
    │ minAmount    │
    │ maxAmount    │                              ┌──────────────┐
    │ interestRate │                              │ EMI_SCHEDULES│
    │ tenure       │                              ├──────────────┤
    │ status       │                              │ id (PK)      │
    │ accepted...  │◄─── ManyToOne (Borrower)    │ loan_id (FK) │
    │ sanctioned..│◄─── OneToOne (Loan)          │ month        │
    │ createdAt    │                              │ emiAmount    │
    │ updatedAt    │                              │ principal    │
    └──────────────┘                              │ interest     │
                                                   │ balance      │
                                                   │ status       │
            ┌──────────────┐                       │ createdAt    │
            │   PAYMENTS   │                       │ updatedAt    │
            ├──────────────┤                       └──────────────┘
            │ id (PK)      │
            │ payment_id   │
            │ loan_id (FK) │◄──────────────────────┘
            │ emi_sched_id │◄──────────────────────┘
            │ amount       │
            │ paymentDate  │
            │ method       │
            │ status       │
            │ createdAt    │
            │ updatedAt    │
            └──────────────┘


                        ┌──────────────────┐
                        │  RISK_REPORTS    │
                        ├──────────────────┤
                        │ id (PK)          │
                        │ loan_id (FK)     │◄──── OneToOne (LOANS)
                        │ riskScore        │
                        │ defaultProbability│
                        │ createdAt        │
                        │ updatedAt        │
                        └──────────────────┘

                        ┌──────────────────┐
                        │ SECURITY_LOGS    │
                        ├──────────────────┤
                        │ id (PK)          │
                        │ log_id           │
                        │ action           │
                        │ performedBy      │
                        │ severity         │
                        │ timestamp        │
                        │ createdAt        │
                        └──────────────────┘
```

---

## Database Schema

### Complete Schema Overview

The LoanFlow database consists of **10 main tables** that support the peer-to-peer lending ecosystem:

1. **users** - User authentication and profile management
2. **borrowers** - Borrower-specific information
3. **lenders** - Lender-specific information
4. **loans** - Active loan records
5. **loan_requests** - Loan requests from borrowers
6. **loan_offers** - Loan offers from lenders
7. **payments** - Payment records
8. **emi_schedules** - EMI schedule for each loan
9. **risk_reports** - Risk analysis for loans
10. **security_logs** - System security audit trails

---

## Tables and Fields

### 1. **USERS Table**

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'LENDER', 'BORROWER', 'ANALYST') NOT NULL,
    created_at BIGINT,
    updated_at BIGINT,
    INDEX idx_email (email),
    INDEX idx_role (role)
);
```

| Field | Type | Constraints | Description |
|-------|------|-----------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique user identifier |
| full_name | VARCHAR(255) | NOT NULL | User's full name |
| email | VARCHAR(255) | NOT NULL, UNIQUE | User's email (login) |
| password | VARCHAR(255) | NOT NULL | Encrypted password |
| role | ENUM | NOT NULL | User role (ADMIN, LENDER, BORROWER, ANALYST) |
| created_at | BIGINT | - | Timestamp of creation |
| updated_at | BIGINT | - | Timestamp of last update |

---

### 2. **BORROWERS Table**

```sql
CREATE TABLE borrowers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    active_loans INT,
    risk_level ENUM('LOW', 'MEDIUM', 'HIGH', 'VERY_HIGH') NOT NULL,
    credit_score INT,
    kyc_verified BOOLEAN,
    created_at BIGINT,
    updated_at BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_risk_level (risk_level)
);
```

| Field | Type | Constraints | Description |
|-------|------|-----------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique borrower identifier |
| user_id | BIGINT | UNIQUE, FOREIGN KEY | Reference to Users table |
| active_loans | INT | - | Count of active loans |
| risk_level | ENUM | NOT NULL | Risk classification (LOW, MEDIUM, HIGH, VERY_HIGH) |
| credit_score | INT | - | Credit score (0-900) |
| kyc_verified | BOOLEAN | - | KYC verification status |
| created_at | BIGINT | - | Creation timestamp |
| updated_at | BIGINT | - | Last update timestamp |

---

### 3. **LENDERS Table**

```sql
CREATE TABLE lenders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    company_name VARCHAR(255),
    active_loans INT,
    total_disbursed DOUBLE,
    created_at BIGINT,
    updated_at BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_company_name (company_name)
);
```

| Field | Type | Constraints | Description |
|-------|------|-----------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique lender identifier |
| user_id | BIGINT | UNIQUE, FOREIGN KEY | Reference to Users table |
| company_name | VARCHAR(255) | - | Lender company name |
| active_loans | INT | - | Count of active loans |
| total_disbursed | DOUBLE | - | Total amount disbursed |
| created_at | BIGINT | - | Creation timestamp |
| updated_at | BIGINT | - | Last update timestamp |

---

### 4. **LOANS Table**

```sql
CREATE TABLE loans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id VARCHAR(255) UNIQUE,
    borrower_id BIGINT NOT NULL,
    lender_id BIGINT,
    amount DOUBLE NOT NULL,
    interest_rate DOUBLE NOT NULL,
    tenure INT NOT NULL,
    purpose VARCHAR(100),
    status ENUM('PENDING', 'ACTIVE', 'CLOSED', 'DEFAULTED') NOT NULL,
    start_date BIGINT,
    next_payment_date BIGINT,
    created_at BIGINT,
    updated_at BIGINT,
    FOREIGN KEY (borrower_id) REFERENCES borrowers(id) ON DELETE CASCADE,
    FOREIGN KEY (lender_id) REFERENCES lenders(id),
    INDEX idx_borrower_id (borrower_id),
    INDEX idx_lender_id (lender_id),
    INDEX idx_status (status),
    INDEX idx_loan_id (loan_id)
);
```

| Field | Type | Constraints | Description |
|-------|------|-----------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique loan identifier |
| loan_id | VARCHAR(255) | UNIQUE | Business loan identifier |
| borrower_id | BIGINT | NOT NULL, FOREIGN KEY | Reference to Borrowers table |
| lender_id | BIGINT | FOREIGN KEY | Reference to Lenders table |
| amount | DOUBLE | NOT NULL | Loan amount |
| interest_rate | DOUBLE | NOT NULL | Annual interest rate (%) |
| tenure | INT | NOT NULL | Loan duration (months) |
| purpose | VARCHAR(100) | - | Loan purpose |
| status | ENUM | NOT NULL | Loan status (PENDING, ACTIVE, CLOSED, DEFAULTED) |
| start_date | BIGINT | - | Loan start date (timestamp) |
| next_payment_date | BIGINT | - | Next payment due date (timestamp) |
| created_at | BIGINT | - | Creation timestamp |
| updated_at | BIGINT | - | Last update timestamp |

---

### 5. **LOAN_REQUESTS Table**

```sql
CREATE TABLE loan_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_code VARCHAR(255) UNIQUE,
    borrower_id BIGINT NOT NULL,
    amount DOUBLE NOT NULL,
    tenure INT NOT NULL,
    purpose VARCHAR(100) NOT NULL,
    interest_rate DOUBLE NOT NULL,
    status ENUM('OPEN', 'APPROVED', 'REJECTED', 'CANCELLED') NOT NULL,
    approved_by_lender_id BIGINT,
    sanctioned_loan_id BIGINT UNIQUE,
    created_at BIGINT,
    updated_at BIGINT,
    FOREIGN KEY (borrower_id) REFERENCES borrowers(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by_lender_id) REFERENCES lenders(id),
    FOREIGN KEY (sanctioned_loan_id) REFERENCES loans(id),
    INDEX idx_borrower_id (borrower_id),
    INDEX idx_status (status),
    INDEX idx_request_code (request_code)
);
```

| Field | Type | Constraints | Description |
|-------|------|-----------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique request identifier |
| request_code | VARCHAR(255) | UNIQUE | Business request identifier |
| borrower_id | BIGINT | NOT NULL, FOREIGN KEY | Reference to Borrowers table |
| amount | DOUBLE | NOT NULL | Requested loan amount |
| tenure | INT | NOT NULL | Requested loan duration (months) |
| purpose | VARCHAR(100) | NOT NULL | Loan purpose |
| interest_rate | DOUBLE | NOT NULL | Requested interest rate |
| status | ENUM | NOT NULL | Request status (OPEN, APPROVED, REJECTED, CANCELLED) |
| approved_by_lender_id | BIGINT | FOREIGN KEY | Reference to Lenders table |
| sanctioned_loan_id | BIGINT | UNIQUE, FOREIGN KEY | Reference to Loans table |
| created_at | BIGINT | - | Creation timestamp |
| updated_at | BIGINT | - | Last update timestamp |

---

### 6. **LOAN_OFFERS Table**

```sql
CREATE TABLE loan_offers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    offer_code VARCHAR(255) UNIQUE,
    lender_id BIGINT NOT NULL,
    min_amount DOUBLE NOT NULL,
    max_amount DOUBLE NOT NULL,
    interest_rate DOUBLE NOT NULL,
    tenure INT NOT NULL,
    status ENUM('OPEN', 'ACCEPTED', 'REJECTED', 'CANCELLED') NOT NULL,
    accepted_by_borrower_id BIGINT,
    sanctioned_loan_id BIGINT UNIQUE,
    created_at BIGINT,
    updated_at BIGINT,
    FOREIGN KEY (lender_id) REFERENCES lenders(id) ON DELETE CASCADE,
    FOREIGN KEY (accepted_by_borrower_id) REFERENCES borrowers(id),
    FOREIGN KEY (sanctioned_loan_id) REFERENCES loans(id),
    INDEX idx_lender_id (lender_id),
    INDEX idx_status (status),
    INDEX idx_offer_code (offer_code)
);
```

| Field | Type | Constraints | Description |
|-------|------|-----------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique offer identifier |
| offer_code | VARCHAR(255) | UNIQUE | Business offer identifier |
| lender_id | BIGINT | NOT NULL, FOREIGN KEY | Reference to Lenders table |
| min_amount | DOUBLE | NOT NULL | Minimum loan amount offered |
| max_amount | DOUBLE | NOT NULL | Maximum loan amount offered |
| interest_rate | DOUBLE | NOT NULL | Interest rate offered |
| tenure | INT | NOT NULL | Loan tenure offered (months) |
| status | ENUM | NOT NULL | Offer status (OPEN, ACCEPTED, REJECTED, CANCELLED) |
| accepted_by_borrower_id | BIGINT | FOREIGN KEY | Reference to Borrowers table |
| sanctioned_loan_id | BIGINT | UNIQUE, FOREIGN KEY | Reference to Loans table |
| created_at | BIGINT | - | Creation timestamp |
| updated_at | BIGINT | - | Last update timestamp |

---

### 7. **PAYMENTS Table**

```sql
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id VARCHAR(255) UNIQUE,
    loan_id BIGINT NOT NULL,
    emi_schedule_id BIGINT,
    amount DOUBLE NOT NULL,
    payment_date BIGINT,
    method ENUM('BANK_TRANSFER', 'CREDIT_CARD', 'DEBIT_CARD', 'WALLET') NOT NULL,
    status ENUM('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED') NOT NULL,
    created_at BIGINT,
    updated_at BIGINT,
    FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE,
    FOREIGN KEY (emi_schedule_id) REFERENCES emi_schedules(id),
    INDEX idx_loan_id (loan_id),
    INDEX idx_status (status),
    INDEX idx_payment_id (payment_id)
);
```

| Field | Type | Constraints | Description |
|-------|------|-----------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique payment identifier |
| payment_id | VARCHAR(255) | UNIQUE | Business payment identifier |
| loan_id | BIGINT | NOT NULL, FOREIGN KEY | Reference to Loans table |
| emi_schedule_id | BIGINT | FOREIGN KEY | Reference to EmiSchedules table |
| amount | DOUBLE | NOT NULL | Payment amount |
| payment_date | BIGINT | - | Payment date (timestamp) |
| method | ENUM | NOT NULL | Payment method |
| status | ENUM | NOT NULL | Payment status (PENDING, COMPLETED, FAILED, CANCELLED) |
| created_at | BIGINT | - | Creation timestamp |
| updated_at | BIGINT | - | Last update timestamp |

---

### 8. **EMI_SCHEDULES Table**

```sql
CREATE TABLE emi_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    month INT NOT NULL,
    emi_amount DOUBLE NOT NULL,
    principal DOUBLE NOT NULL,
    interest DOUBLE NOT NULL,
    balance DOUBLE NOT NULL,
    status ENUM('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED') NOT NULL,
    created_at BIGINT,
    updated_at BIGINT,
    FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE,
    INDEX idx_loan_id (loan_id),
    INDEX idx_status (status),
    UNIQUE KEY unique_loan_month (loan_id, month)
);
```

| Field | Type | Constraints | Description |
|-------|------|-----------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique schedule identifier |
| loan_id | BIGINT | NOT NULL, FOREIGN KEY | Reference to Loans table |
| month | INT | NOT NULL | EMI month number |
| emi_amount | DOUBLE | NOT NULL | Monthly EMI amount |
| principal | DOUBLE | NOT NULL | Principal component |
| interest | DOUBLE | NOT NULL | Interest component |
| balance | DOUBLE | NOT NULL | Remaining balance |
| status | ENUM | NOT NULL | Schedule status (PENDING, COMPLETED, FAILED, CANCELLED) |
| created_at | BIGINT | - | Creation timestamp |
| updated_at | BIGINT | - | Last update timestamp |

---

### 9. **RISK_REPORTS Table**

```sql
CREATE TABLE risk_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id BIGINT UNIQUE NOT NULL,
    risk_score INT NOT NULL,
    default_probability DOUBLE NOT NULL,
    created_at BIGINT,
    updated_at BIGINT,
    FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE,
    INDEX idx_loan_id (loan_id),
    INDEX idx_risk_score (risk_score)
);
```

| Field | Type | Constraints | Description |
|-------|------|-----------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique report identifier |
| loan_id | BIGINT | UNIQUE, NOT NULL, FOREIGN KEY | Reference to Loans table |
| risk_score | INT | NOT NULL | Risk score (0-100) |
| default_probability | DOUBLE | NOT NULL | Default probability (0.0-1.0) |
| created_at | BIGINT | - | Creation timestamp |
| updated_at | BIGINT | - | Last update timestamp |

---

### 10. **SECURITY_LOGS Table**

```sql
CREATE TABLE security_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    log_id VARCHAR(255) UNIQUE,
    action VARCHAR(255) NOT NULL,
    performed_by VARCHAR(100),
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'VERY_HIGH'),
    timestamp BIGINT,
    created_at BIGINT,
    INDEX idx_log_id (log_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_severity (severity)
);
```

| Field | Type | Constraints | Description |
|-------|------|-----------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique log identifier |
| log_id | VARCHAR(255) | UNIQUE | Business log identifier |
| action | VARCHAR(255) | NOT NULL | Action performed |
| performed_by | VARCHAR(100) | - | User who performed action |
| severity | ENUM | - | Severity level (LOW, MEDIUM, HIGH, VERY_HIGH) |
| timestamp | BIGINT | - | Action timestamp |
| created_at | BIGINT | - | Log creation timestamp |

---

## Relationships

### Entity Relationships Summary

| From | To | Type | Cardinality | FK Column | Description |
|------|----|----|------------|-----------|-------------|
| USERS | BORROWERS | OneToOne | 1:1 | user_id | One user can be one borrower |
| USERS | LENDERS | OneToOne | 1:1 | user_id | One user can be one lender |
| BORROWERS | LOANS | OneToMany | 1:N | borrower_id | One borrower can have multiple loans |
| LENDERS | LOANS | ManyToOne | N:1 | lender_id | Multiple loans from one lender |
| BORROWERS | LOAN_REQUESTS | OneToMany | 1:N | borrower_id | One borrower can create multiple requests |
| LENDERS | LOAN_REQUESTS | ManyToOne | N:1 | approved_by_lender_id | One lender approves many requests |
| LOAN_REQUESTS | LOANS | OneToOne | 1:1 | sanctioned_loan_id | Request converted to loan |
| LENDERS | LOAN_OFFERS | OneToMany | 1:N | lender_id | One lender creates multiple offers |
| BORROWERS | LOAN_OFFERS | ManyToOne | N:1 | accepted_by_borrower_id | Borrower accepts offers |
| LOAN_OFFERS | LOANS | OneToOne | 1:1 | sanctioned_loan_id | Offer converted to loan |
| LOANS | PAYMENTS | OneToMany | 1:N | loan_id | One loan has multiple payments |
| LOANS | EMI_SCHEDULES | OneToMany | 1:N | loan_id | One loan has multiple EMI schedules |
| EMI_SCHEDULES | PAYMENTS | OneToMany | 1:N | emi_schedule_id | One schedule has multiple payments |
| LOANS | RISK_REPORTS | OneToOne | 1:1 | loan_id | One loan has one risk report |

---

## Database Diagrams

### 1. Detailed Entity Relationship Diagram (Visual)

```
                        ┌──────────────┐
                        │    USERS     │
                        │              │
                        │ PK: id       │
                        │ email (UQ)   │
                        │ role (ENUM)  │
                        └──────────────┘
                             ▲    ▲
                    ┌────────┘    └────────┐
              1:1   │                      │   1:1
                    │ user_id              │ user_id
                    │                      │
            ┌───────────────┐        ┌──────────────┐
            │  BORROWERS    │        │   LENDERS    │
            │               │        │              │
            │ PK: id        │        │ PK: id       │
            │ FK: user_id   │        │ FK: user_id  │
            │ riskLevel     │        │ companyName  │
            │ creditScore   │        │ totalDisbursed│
            └───────────────┘        └──────────────┘
                    │                         ▲
            1:N     │                         │ N:1
         borrower_id│                         │ lender_id
                    │                         │
                    └───────────┬─────────────┘
                                │
                        ┌───────────────┐
                        │    LOANS      │
                        │               │
                        │ PK: id        │
                        │ FK: borrower_id
                        │ FK: lender_id │
                        │ status        │
                        │ amount        │
                        │ tenure        │
                        └───────────────┘
                         ▲   ▲   ▲    ▲
                         │   │   │    │
            ┌────────────┘   │   │    └──────────────┐
            │                │   │                   │
            │           1:1  │   │  1:N              │
            │  sanctioned..  │   │ loan_id           │
            │                │   │                   │
   ┌─────────────────┐    ┌──────────────┐  ┌──────────────┐
   │ LOAN_REQUESTS   │    │ LOAN_OFFERS  │  │   PAYMENTS   │
   │                 │    │              │  │              │
   │ PK: id          │    │ PK: id       │  │ PK: id       │
   │ FK: borrower_id │    │ FK: lender_id  │ FK: loan_id    │
   │ FK: approved..  │    │ FK: accepted..│  │ FK: emi_..   │
   │ FK: sanctioned..│    │ FK: sanctioned│  │ method       │
   │ status          │    │ status       │  │ status       │
   └─────────────────┘    └──────────────┘  └──────────────┘
                                                    ▲
                                                    │ N:1
                                                    │ emi_schedule_id
                                                    │
                                          ┌──────────────────┐
                                          │ EMI_SCHEDULES    │
                                          │                  │
                                          │ PK: id           │
                                          │ FK: loan_id      │
                                          │ month            │
                                          │ emiAmount        │
                                          │ principal        │
                                          │ interest         │
                                          │ balance          │
                                          │ status           │
                                          └──────────────────┘

                    ┌──────────────┐
                    │ RISK_REPORTS │
                    │              │
                    │ PK: id       │
                    │ FK: loan_id  │
                    │ riskScore    │
                    │ defaultProb  │
                    └──────────────┘
                           ▲
                        1:1 │
                    loan_id │
                           └─── (from LOANS)


                    ┌──────────────────┐
                    │ SECURITY_LOGS    │
                    │                  │
                    │ PK: id           │
                    │ log_id           │
                    │ action           │
                    │ performedBy      │
                    │ severity         │
                    │ timestamp        │
                    └──────────────────┘
                    (Audit logging table)
```

### 2. Functional Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    LOAN LIFECYCLE FLOW                          │
└─────────────────────────────────────────────────────────────────┘

                        ┌─────────────┐
                        │ Borrower    │
                        │ (USERS +    │
                        │ BORROWERS)  │
                        └──────┬──────┘
                               │
                        Creates│
                               ▼
                    ┌──────────────────┐
                    │  LOAN_REQUESTS   │
                    │  Status: OPEN    │
                    └────────┬─────────┘
                             │
                   Reviewed by│ Lender
                             ▼
                    ┌──────────────────┐
                    │  LOAN_REQUESTS   │
                    │ Status: APPROVED │
                    └────────┬─────────┘
                             │
               Sanctioned as  │
                             ▼
                    ┌──────────────────┐
                    │    LOANS         │
                    │ Status: ACTIVE   │
                    │ amount, tenure,  │
                    │ interestRate     │
                    └────────┬─────────┘
                             │
          ┌──────────────────┴──────────────────┐
          │                                     │
          ▼                                     ▼
   ┌──────────────┐                  ┌──────────────────┐
   │ RISK_REPORTS │                  │  EMI_SCHEDULES   │
   │              │                  │                  │
   │ riskScore    │                  │ month 1, 2, 3..N │
   │ defaultProb  │                  │ emiAmount        │
   └──────────────┘                  │ principal        │
                                     │ interest         │
                                     │ balance          │
                                     └────────┬─────────┘
                                              │
                                    For each  │
                                    month     ▼
                                     ┌──────────────────┐
                                     │    PAYMENTS      │
                                     │                  │
                                     │ paymentDate      │
                                     │ amount           │
                                     │ method           │
                                     │ status           │
                                     └────────┬─────────┘
                                              │
                                     When all │
                                   paid       ▼
                                     ┌──────────────────┐
                                     │    LOANS         │
                                     │ Status: CLOSED   │
                                     └──────────────────┘
```

### 3. Alternative Loan Creation Path

```
┌─────────────────────────────────────────────────────────────────┐
│                  ALTERNATIVE LOAN CREATION PATH                 │
└─────────────────────────────────────────────────────────────────┘

                        ┌─────────────┐
                        │  Lender     │
                        │ (USERS +    │
                        │ LENDERS)    │
                        └──────┬──────┘
                               │
                        Creates│
                               ▼
                    ┌──────────────────┐
                    │  LOAN_OFFERS     │
                    │  Status: OPEN    │
                    └────────┬─────────┘
                             │
                  Reviewed by │ Borrower
                             ▼
                    ┌──────────────────┐
                    │  LOAN_OFFERS     │
                    │ Status: ACCEPTED │
                    └────────┬─────────┘
                             │
               Sanctioned as  │
                             ▼
                    ┌──────────────────┐
                    │    LOANS         │
                    │ Status: ACTIVE   │
                    └──────────────────┘
```

### 4. Data Access Patterns

```
┌──────────────────────────────────────────────────────────┐
│              COMMON DATA ACCESS PATTERNS                 │
└──────────────────────────────────────────────────────────┘

1. GET USER PROFILE WITH ROLE
   USER (id, role) → BORROWERS/LENDERS (user_id)

2. GET BORROWER'S LOANS
   BORROWERS (id) → LOANS (borrower_id)

3. GET LENDER'S ACTIVE OFFERS
   LENDERS (id) → LOAN_OFFERS (lender_id, status='OPEN')

4. GET LOAN WITH EMI SCHEDULE
   LOANS (id) → EMI_SCHEDULES (loan_id) → PAYMENTS (emi_schedule_id)

5. GET LOAN REQUEST STATUS
   LOAN_REQUESTS (id) → LOANS (sanctioned_loan_id)

6. GET RISK ASSESSMENT
   LOANS (id) → RISK_REPORTS (loan_id)

7. CALCULATE TOTAL PAYMENTS
   LOANS (id) → PAYMENTS (loan_id) where status='COMPLETED'

8. GET AUDIT TRAIL
   SECURITY_LOGS (performed_by, timestamp) → Users (id)
```

---

## Connection Details

### JDBC Connection URL
```
jdbc:mysql://localhost:3306/loan_management
```

### Connection Pool Settings
- **Driver**: `com.mysql.cj.jdbc.Driver`
- **Pool Size**: Default Hikari settings
- **Auto Commit**: Enabled
- **Connection Timeout**: 10 seconds

### ORM Configuration
```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

### Key Indexes for Performance

```sql
-- User lookups
CREATE INDEX idx_email ON users(email);
CREATE INDEX idx_role ON users(role);

-- Borrower queries
CREATE INDEX idx_borrower_risk_level ON borrowers(risk_level);

-- Loan queries
CREATE INDEX idx_loan_borrower ON loans(borrower_id);
CREATE INDEX idx_loan_lender ON loans(lender_id);
CREATE INDEX idx_loan_status ON loans(status);
CREATE INDEX idx_loan_id ON loans(loan_id);

-- Request queries
CREATE INDEX idx_request_borrower ON loan_requests(borrower_id);
CREATE INDEX idx_request_status ON loan_requests(status);
CREATE INDEX idx_request_code ON loan_requests(request_code);

-- Offer queries
CREATE INDEX idx_offer_lender ON loan_offers(lender_id);
CREATE INDEX idx_offer_status ON loan_offers(status);

-- Payment queries
CREATE INDEX idx_payment_loan ON payments(loan_id);
CREATE INDEX idx_payment_status ON payments(status);

-- EMI Schedule queries
CREATE INDEX idx_emi_loan ON emi_schedules(loan_id);
CREATE INDEX idx_emi_status ON emi_schedules(status);

-- Risk Report queries
CREATE INDEX idx_risk_score ON risk_reports(risk_score);

-- Security Log queries
CREATE INDEX idx_security_timestamp ON security_logs(timestamp);
CREATE INDEX idx_security_severity ON security_logs(severity);
```

### Sample SQL Queries

#### Find Active Loans for a Borrower
```sql
SELECT l.* FROM loans l
WHERE l.borrower_id = ? AND l.status = 'ACTIVE'
ORDER BY l.created_at DESC;
```

#### Get EMI Schedule for a Loan
```sql
SELECT es.* FROM emi_schedules es
WHERE es.loan_id = ?
ORDER BY es.month ASC;
```

#### Get Payment History
```sql
SELECT p.* FROM payments p
WHERE p.loan_id = ? AND p.status = 'COMPLETED'
ORDER BY p.payment_date DESC;
```

#### Get Pending EMIs
```sql
SELECT es.* FROM emi_schedules es
WHERE es.loan_id = ? AND es.status = 'PENDING'
ORDER BY es.month ASC;
```

#### Get Lender's Total Disbursed Amount
```sql
SELECT SUM(l.amount) as total_disbursed FROM loans l
WHERE l.lender_id = ? AND l.status IN ('ACTIVE', 'CLOSED');
```

---

## Entity Enums Reference

### Role Enum
```java
ADMIN      - System administrator
LENDER     - Loan provider
BORROWER   - Loan applicant
ANALYST    - Risk analyst
```

### RiskLevel Enum
```java
LOW        - Low risk borrower
MEDIUM     - Medium risk borrower
HIGH       - High risk borrower
VERY_HIGH  - Very high risk borrower
```

### LoanStatus Enum
```java
PENDING    - Awaiting lender approval
ACTIVE     - Loan is active, payments ongoing
CLOSED     - Loan fully paid
DEFAULTED  - Loan defaulted
```

### RequestStatus Enum
```java
OPEN       - Request created, awaiting lender response
APPROVED   - Approved by lender
REJECTED   - Rejected by lender
CANCELLED  - Cancelled by borrower
```

### OfferStatus Enum
```java
OPEN       - Offer created, awaiting borrower response
ACCEPTED   - Accepted by borrower
REJECTED   - Rejected by borrower
CANCELLED  - Cancelled by lender
```

### PaymentStatus Enum
```java
PENDING    - Payment awaiting completion
COMPLETED  - Payment successfully completed
FAILED     - Payment failed
CANCELLED  - Payment cancelled
```

### PaymentMethod Enum
```java
BANK_TRANSFER - Direct bank transfer
CREDIT_CARD   - Credit card payment
DEBIT_CARD    - Debit card payment
WALLET        - Digital wallet payment
```

---

## Database Constraints and Integrity

### Primary Key Constraints
- All tables have an `id` field as the primary key with `AUTO_INCREMENT`

### Unique Constraints
- `users.email` - Must be unique
- `loans.loan_id` - Unique business identifier
- `loan_requests.request_code` - Unique business identifier
- `loan_offers.offer_code` - Unique business identifier
- `payments.payment_id` - Unique business identifier
- `security_logs.log_id` - Unique business identifier
- `borrowers.user_id` - One user = one borrower
- `lenders.user_id` - One user = one lender

### Foreign Key Constraints
- All foreign keys have `ON DELETE CASCADE` for data integrity
- Child records are automatically deleted when parent is deleted

### Check Constraints (Business Logic)
- Credit score: 0-900
- Interest rate: Positive value
- Tenure: Positive integer (months)
- Risk score: 0-100
- Default probability: 0.0 to 1.0

---

## Backup and Recovery

### Backup Strategy
```bash
# Full database backup
mysqldump -u root -p loan_management > loan_management_backup.sql

# Backup with timestamp
mysqldump -u root -p loan_management > loan_management_$(date +%Y%m%d_%H%M%S).sql
```

### Restore from Backup
```bash
mysql -u root -p loan_management < loan_management_backup.sql
```

---

## Performance Optimization Tips

1. **Index Strategy**: Use composite indexes for frequently joined columns
2. **Query Optimization**: Use appropriate WHERE clauses to reduce result sets
3. **Pagination**: Implement pagination for large result sets
4. **Connection Pooling**: Use Hikari connection pool (default in Spring Boot)
5. **Caching**: Consider caching frequently accessed data
6. **Lazy Loading**: Use appropriate fetch strategies in JPA

---

## Security Considerations

1. **Password**: Encrypted before storage in the database
2. **JWT Tokens**: Manage JWT tokens in application layer
3. **SQL Injection**: Prevented through parameterized queries (JPA)
4. **Access Control**: Role-based access control enforced at service layer
5. **Audit Logging**: All critical operations logged in `security_logs` table

---

## Maintenance Tasks

### Regular Tasks
- Monitor database size
- Check for slow queries
- Verify backup integrity
- Review security logs monthly
- Analyze index usage

### Quarterly Tasks
- Optimize table structures
- Archive old payment records
- Review and update indexes
- Analyze borrower risk distribution

---

## Troubleshooting

### Connection Issues
```
Error: Can't connect to MySQL server on 'localhost:3306'
Solution: 
1. Verify MySQL is running
2. Check port 3306 is not blocked
3. Verify credentials in application.properties
```

### Foreign Key Constraint Errors
```
Error: Cannot delete or update a parent row
Solution: 
1. Delete child records first
2. Check for CASCADE delete rules
```

### Performance Issues
```
Solution:
1. Check query execution plans
2. Verify indexes are being used
3. Monitor connection pool utilization
4. Check database disk space
```

---

## Contact & Support

For database-related issues or queries, refer to the main project documentation or contact the development team.

**Last Updated**: April 7, 2026  
**Version**: 1.0  
**Database Version**: MySQL 5.7+

