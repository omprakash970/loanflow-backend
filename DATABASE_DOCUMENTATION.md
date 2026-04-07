# LoanFlow Backend - Database Documentation

## 📋 Table of Contents
1. [Database Overview](#database-overview)
2. [Database Connections](#database-connections)
3. [Entity-Relationship Model (ER Model)](#entity-relationship-model)
4. [Database Schema](#database-schema)
5. [Tables & Columns](#tables--columns)
6. [Relationships & Constraints](#relationships--constraints)
7. [Data Types & Validation](#data-types--validation)
8. [Indexes & Performance](#indexes--performance)
9. [Sample Data](#sample-data)
10. [Database Diagrams](#database-diagrams)
11. [Connection Configuration](#connection-configuration)
12. [Backup & Recovery](#backup--recovery)
13. [Troubleshooting](#troubleshooting)

---

## 🗄️ Database Overview

**Database Name:** `loan_management`  
**Database Type:** MySQL 8.0+  
**Character Set:** UTF-8  
**Collation:** utf8mb4_unicode_ci  
**Engine:** InnoDB  

The LoanFlow system uses a relational MySQL database to manage users, loans, borrowers, lenders, EMI schedules, and related data for a comprehensive loan issuance and management system.

### Key Features
- **Transaction Support:** InnoDB engine ensures ACID compliance
- **Relationships:** Foreign key constraints maintain referential integrity
- **Scalability:** Optimized for high-volume transaction processing
- **Security:** Encrypted password storage and role-based data access

---

## 🔌 Database Connections

### Connection Configuration

**File:** `src/main/resources/application.properties`

```properties
# MySQL Database Connection
spring.datasource.url=jdbc:mysql://localhost:3306/loan_management
spring.datasource.username=root
spring.datasource.password=Nancy123abc@
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Connection Pool Settings
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true
```

### Connection Parameters

| Parameter | Value | Description |
|-----------|-------|-------------|
| **Host** | localhost | Database server address |
| **Port** | 3306 | MySQL default port |
| **Database** | loan_management | Database name |
| **Username** | root | Database user (default) |
| **Password** | Nancy123abc@ | Database password |
| **Driver** | com.mysql.cj.jdbc.Driver | MySQL Connector/J driver |

### Connection Pool Configuration

- **Maximum Pool Size:** 10 connections
- **Minimum Idle Connections:** 5
- **Connection Timeout:** 30 seconds
- **Idle Connection Timeout:** 10 minutes

### Connection String Format

```
jdbc:mysql://[host]:[port]/[database]?useSSL=false&serverTimezone=UTC
```

---

## 🏗️ Entity-Relationship Model (ER Model)

### ER Diagram (Textual Representation)

```
┌─────────────────┐
│     Users       │
├─────────────────┤
│ id (PK)         │
│ fullName        │
│ email (UNIQUE)  │
│ password        │◄──┐
│ role            │   │
│ createdAt       │   │
│ updatedAt       │   │
└─────────────────┘   │
         │            │
         │ 1:1        │
    ┌────┴────┐       │
    │          │       │
┌───┴───┐  ┌──┴────┐  │
│Borrower│  │Lender │  │
├────────┤  ├───────┤  │
│id (PK) │  │id (PK)│  │
│userId◄─┼──┤userId◄┼──┘
│credits │  │funds  │
│...     │  │...    │
└────────┘  └───────┘
    │           │
    │ 1:M       │ 1:M
    │           │
    ▼           ▼
┌─────────────────────┐
│       Loans         │
├─────────────────────┤
│ id (PK)             │
│ borrowerId (FK)     │
│ lenderId (FK)       │
│ amount              │
│ interestRate        │
│ tenure              │
│ status              │
│ createdAt           │
│ updatedAt           │
└─────────────────────┘
         │
         │ 1:M
         │
    ┌────┴─────────┐
    │              │
    ▼              ▼
┌──────────────┐ ┌──────────────┐
│   Payments   │ │ EMI Schedule │
├──────────────┤ ├──────────────┤
│ id (PK)      │ │ id (PK)      │
│ loanId (FK)  │ │ loanId (FK)  │
│ amount       │ │ emiNumber    │
│ paymentDate  │ │ dueDate      │
│ status       │ │ amount       │
│ ...          │ │ status       │
└──────────────┘ └──────────────┘
```

### Relationship Summary

| Relationship | Type | Description |
|--------------|------|-------------|
| **Users → Borrower** | 1:1 | Each user can optionally have a borrower profile |
| **Users → Lender** | 1:1 | Each user can optionally have a lender profile |
| **Borrower → Loans** | 1:M | One borrower can have multiple loan applications |
| **Lender → Loans** | 1:M | One lender can finance multiple loans |
| **Loans → Payments** | 1:M | One loan can have multiple payments |
| **Loans → EMI Schedule** | 1:M | One loan can have multiple EMI installments |

---

## 📊 Database Schema

### DDL (Data Definition Language)

```sql
-- Create Database
CREATE DATABASE IF NOT EXISTS loan_management
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE loan_management;

-- Create Users Table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at BIGINT,
    updated_at BIGINT,
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Borrower Table
CREATE TABLE borrower (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    active_loans INT DEFAULT 0,
    risk_level VARCHAR(50),
    credit_score INT,
    kyc_verified BOOLEAN DEFAULT FALSE,
    created_at BIGINT,
    updated_at BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_risk_level (risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Lender Table
CREATE TABLE lender (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    company_name VARCHAR(255),
    active_loans INT DEFAULT 0,
    total_disbursed DECIMAL(15, 2) DEFAULT 0.00,
    created_at BIGINT,
    updated_at BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Loans Table
CREATE TABLE loans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    borrower_id BIGINT NOT NULL,
    lender_id BIGINT,
    amount DECIMAL(15, 2) NOT NULL,
    interest_rate DECIMAL(5, 2),
    tenure INT,
    purpose VARCHAR(255),
    status VARCHAR(50),
    created_at BIGINT,
    updated_at BIGINT,
    FOREIGN KEY (borrower_id) REFERENCES borrower(id) ON DELETE CASCADE,
    FOREIGN KEY (lender_id) REFERENCES lender(id) ON DELETE SET NULL,
    INDEX idx_borrower_id (borrower_id),
    INDEX idx_lender_id (lender_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Payments Table
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    payment_date BIGINT,
    transaction_id VARCHAR(255),
    status VARCHAR(50),
    created_at BIGINT,
    updated_at BIGINT,
    FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE,
    INDEX idx_loan_id (loan_id),
    INDEX idx_status (status),
    INDEX idx_payment_date (payment_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create EMI Schedule Table
CREATE TABLE emi_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    emi_number INT,
    due_date BIGINT,
    amount DECIMAL(15, 2) NOT NULL,
    principal DECIMAL(15, 2),
    interest DECIMAL(15, 2),
    status VARCHAR(50),
    paid_date BIGINT,
    created_at BIGINT,
    updated_at BIGINT,
    FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE,
    INDEX idx_loan_id (loan_id),
    INDEX idx_status (status),
    INDEX idx_due_date (due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Security Logs Table (for audit trail)
CREATE TABLE security_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(255),
    ip_address VARCHAR(50),
    timestamp BIGINT,
    details LONGTEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 📋 Tables & Columns

### 1. Users Table

**Purpose:** Stores authentication and user profile information

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| **id** | BIGINT | PK, AUTO_INCREMENT | Unique user identifier |
| **full_name** | VARCHAR(255) | NOT NULL | User's full name |
| **email** | VARCHAR(255) | NOT NULL, UNIQUE | Unique email address |
| **password** | VARCHAR(255) | NOT NULL | BCrypt hashed password |
| **role** | VARCHAR(50) | NOT NULL | User role (ADMIN, LENDER, BORROWER, ANALYST) |
| **created_at** | BIGINT | | Creation timestamp (milliseconds) |
| **updated_at** | BIGINT | | Last update timestamp (milliseconds) |

**Sample Row:**
```sql
INSERT INTO users VALUES (
    1, 
    'John Doe', 
    'john@example.com', 
    '$2a$10$abcdef...', 
    'BORROWER', 
    1711799796000, 
    1711799796000
);
```

---

### 2. Borrower Table

**Purpose:** Stores borrower-specific information and profile data

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| **id** | BIGINT | PK, AUTO_INCREMENT | Unique borrower identifier |
| **user_id** | BIGINT | FK, UNIQUE | Reference to users table |
| **active_loans** | INT | DEFAULT 0 | Count of active loans |
| **risk_level** | VARCHAR(50) | | Risk assessment (LOW, MEDIUM, HIGH) |
| **credit_score** | INT | | Credit score (300-850) |
| **kyc_verified** | BOOLEAN | DEFAULT FALSE | KYC verification status |
| **created_at** | BIGINT | | Creation timestamp |
| **updated_at** | BIGINT | | Last update timestamp |

---

### 3. Lender Table

**Purpose:** Stores lender/financial institution information

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| **id** | BIGINT | PK, AUTO_INCREMENT | Unique lender identifier |
| **user_id** | BIGINT | FK, UNIQUE | Reference to users table |
| **company_name** | VARCHAR(255) | | Name of lending company/institution |
| **active_loans** | INT | DEFAULT 0 | Count of active loan contracts |
| **total_disbursed** | DECIMAL(15,2) | DEFAULT 0.00 | Total amount disbursed |
| **created_at** | BIGINT | | Creation timestamp |
| **updated_at** | BIGINT | | Last update timestamp |

---

### 4. Loans Table

**Purpose:** Stores loan application and contract information

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| **id** | BIGINT | PK, AUTO_INCREMENT | Unique loan identifier |
| **borrower_id** | BIGINT | FK (NOT NULL) | Reference to borrower |
| **lender_id** | BIGINT | FK (NULLABLE) | Reference to lender (set after approval) |
| **amount** | DECIMAL(15,2) | NOT NULL | Loan amount (principal) |
| **interest_rate** | DECIMAL(5,2) | | Annual interest rate (e.g., 12.50) |
| **tenure** | INT | | Loan tenure in months |
| **purpose** | VARCHAR(255) | | Loan purpose (e.g., "Home Purchase") |
| **status** | VARCHAR(50) | | Status (PENDING, APPROVED, REJECTED, DISBURSED, CLOSED) |
| **created_at** | BIGINT | | Application creation date |
| **updated_at** | BIGINT | | Last update timestamp |

---

### 5. Payments Table

**Purpose:** Tracks all loan payments made by borrowers

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| **id** | BIGINT | PK, AUTO_INCREMENT | Unique payment identifier |
| **loan_id** | BIGINT | FK (NOT NULL) | Reference to loan |
| **amount** | DECIMAL(15,2) | NOT NULL | Payment amount |
| **payment_date** | BIGINT | | Payment timestamp |
| **transaction_id** | VARCHAR(255) | | Transaction reference (for audit) |
| **status** | VARCHAR(50) | | Payment status (PENDING, SUCCESS, FAILED) |
| **created_at** | BIGINT | | Record creation timestamp |
| **updated_at** | BIGINT | | Last update timestamp |

---

### 6. EMI Schedule Table

**Purpose:** Stores monthly EMI (Equated Monthly Installment) schedule

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| **id** | BIGINT | PK, AUTO_INCREMENT | Unique EMI record identifier |
| **loan_id** | BIGINT | FK (NOT NULL) | Reference to loan |
| **emi_number** | INT | | Installment number (1, 2, 3...) |
| **due_date** | BIGINT | | EMI due date timestamp |
| **amount** | DECIMAL(15,2) | NOT NULL | Total EMI amount |
| **principal** | DECIMAL(15,2) | | Principal component of EMI |
| **interest** | DECIMAL(15,2) | | Interest component of EMI |
| **status** | VARCHAR(50) | | EMI status (PENDING, PAID, OVERDUE, PARTIAL) |
| **paid_date** | BIGINT | | Actual payment date (if paid) |
| **created_at** | BIGINT | | Record creation timestamp |
| **updated_at** | BIGINT | | Last update timestamp |

---

### 7. Security Logs Table

**Purpose:** Maintains audit trail for security and compliance

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| **id** | BIGINT | PK, AUTO_INCREMENT | Unique log identifier |
| **user_id** | BIGINT | FK (NULLABLE) | Reference to user (if applicable) |
| **action** | VARCHAR(255) | | Action performed (LOGIN, REGISTER, PAYMENT, etc.) |
| **ip_address** | VARCHAR(50) | | Client IP address |
| **timestamp** | BIGINT | | Event timestamp |
| **details** | LONGTEXT | | Additional JSON/text details |

---

## 🔗 Relationships & Constraints

### Foreign Key Constraints

```sql
-- Users → Borrower (1:1)
ALTER TABLE borrower 
ADD CONSTRAINT fk_borrower_user 
FOREIGN KEY (user_id) REFERENCES users(id) 
ON DELETE CASCADE 
ON UPDATE CASCADE;

-- Users → Lender (1:1)
ALTER TABLE lender 
ADD CONSTRAINT fk_lender_user 
FOREIGN KEY (user_id) REFERENCES users(id) 
ON DELETE CASCADE 
ON UPDATE CASCADE;

-- Borrower → Loans (1:M)
ALTER TABLE loans 
ADD CONSTRAINT fk_loans_borrower 
FOREIGN KEY (borrower_id) REFERENCES borrower(id) 
ON DELETE CASCADE 
ON UPDATE CASCADE;

-- Lender → Loans (1:M)
ALTER TABLE loans 
ADD CONSTRAINT fk_loans_lender 
FOREIGN KEY (lender_id) REFERENCES lender(id) 
ON DELETE SET NULL 
ON UPDATE CASCADE;

-- Loans → Payments (1:M)
ALTER TABLE payments 
ADD CONSTRAINT fk_payments_loan 
FOREIGN KEY (loan_id) REFERENCES loans(id) 
ON DELETE CASCADE 
ON UPDATE CASCADE;

-- Loans → EMI Schedule (1:M)
ALTER TABLE emi_schedule 
ADD CONSTRAINT fk_emi_loan 
FOREIGN KEY (loan_id) REFERENCES loans(id) 
ON DELETE CASCADE 
ON UPDATE CASCADE;
```

### Cascade Rules

| Action | Rule | Description |
|--------|------|-------------|
| **ON DELETE CASCADE** | User deleted | Automatically delete borrower/lender profiles and related loans |
| **ON DELETE SET NULL** | Lender deleted | Set lender_id to NULL (loan remains but unassigned) |
| **ON UPDATE CASCADE** | ID updated | Automatically update all foreign key references |

---

## 📐 Data Types & Validation

### Numeric Types

| Type | Range | Usage |
|------|-------|-------|
| **INT** | -2,147,483,648 to 2,147,483,647 | Active loans count, credit score, tenure |
| **BIGINT** | -9.2×10¹⁸ to 9.2×10¹⁸ | IDs, timestamps |
| **DECIMAL(15,2)** | Up to 15 digits, 2 decimal places | Monetary amounts |

### Text Types

| Type | Size | Usage |
|------|------|-------|
| **VARCHAR(50)** | Up to 50 chars | Roles, status, IP addresses |
| **VARCHAR(255)** | Up to 255 chars | Names, emails, company names |
| **LONGTEXT** | Up to 4GB | Detailed audit logs |

### Boolean Type

| Type | Values | Usage |
|------|--------|-------|
| **BOOLEAN** | TRUE/FALSE | KYC verification status |

### Timestamp Type

**Format:** Unix timestamp in milliseconds (BIGINT)

Example: `1711799796000` = April 3, 2026, 10:29:56 UTC

---

## 🚀 Indexes & Performance

### Default Indexes

Primary Key Indexes (Automatic):
```sql
PRIMARY KEY (id)  -- Auto-indexed for all tables
```

### Custom Indexes

```sql
-- Users table indexes
CREATE INDEX idx_email ON users(email);          -- Fast email lookup
CREATE INDEX idx_role ON users(role);            -- Filter by role
CREATE INDEX idx_created_at ON users(created_at);-- Timeline queries

-- Borrower table indexes
CREATE INDEX idx_user_id ON borrower(user_id);   -- Fast user lookup
CREATE INDEX idx_risk_level ON borrower(risk_level); -- Filter by risk

-- Loans table indexes
CREATE INDEX idx_borrower_id ON loans(borrower_id);
CREATE INDEX idx_lender_id ON loans(lender_id);
CREATE INDEX idx_status ON loans(status);        -- Filter by status
CREATE INDEX idx_created_at ON loans(created_at);

-- Payments table indexes
CREATE INDEX idx_loan_id ON payments(loan_id);
CREATE INDEX idx_status ON payments(status);
CREATE INDEX idx_payment_date ON payments(payment_date);

-- EMI Schedule indexes
CREATE INDEX idx_loan_id ON emi_schedule(loan_id);
CREATE INDEX idx_status ON emi_schedule(status);
CREATE INDEX idx_due_date ON emi_schedule(due_date);

-- Security Logs indexes
CREATE INDEX idx_user_id ON security_logs(user_id);
CREATE INDEX idx_timestamp ON security_logs(timestamp);
```

### Query Performance Tips

1. **Use indexed columns in WHERE clauses** - Especially email, role, status
2. **Avoid selecting all columns** - Use specific column names
3. **Use LIMIT for large result sets** - Implement pagination
4. **Index foreign key columns** - Usually already done, but verify
5. **Analyze slow queries** - Use `EXPLAIN` command

---

## 💾 Sample Data

### Insert Sample Users

```sql
-- Admin user
INSERT INTO users (full_name, email, password, role, created_at, updated_at) VALUES
('Admin User', 'admin@loanflow.com', '$2a$10$...bcrypt_hash...', 'ADMIN', 1711799796000, 1711799796000);

-- Lender user
INSERT INTO users (full_name, email, password, role, created_at, updated_at) VALUES
('Bank XYZ', 'lender@bankxyz.com', '$2a$10$...bcrypt_hash...', 'LENDER', 1711799796000, 1711799796000);

-- Borrower user
INSERT INTO users (full_name, email, password, role, created_at, updated_at) VALUES
('John Doe', 'john@example.com', '$2a$10$...bcrypt_hash...', 'BORROWER', 1711799796000, 1711799796000);
```

### Insert Sample Borrower Profile

```sql
INSERT INTO borrower (user_id, active_loans, risk_level, credit_score, kyc_verified, created_at, updated_at)
VALUES (3, 1, 'LOW', 750, TRUE, 1711799796000, 1711799796000);
```

### Insert Sample Lender Profile

```sql
INSERT INTO lender (user_id, company_name, active_loans, total_disbursed, created_at, updated_at)
VALUES (2, 'Bank XYZ Ltd.', 5, 500000.00, 1711799796000, 1711799796000);
```

### Insert Sample Loan

```sql
INSERT INTO loans (borrower_id, lender_id, amount, interest_rate, tenure, purpose, status, created_at, updated_at)
VALUES (1, 1, 100000.00, 12.50, 60, 'Home Purchase', 'APPROVED', 1711799796000, 1711799796000);
```

---

## 📈 Database Diagrams

### Simplified Entity Relationship Diagram

```
┌─────────────────────────────────┐
│          USERS                  │
├─────────────────────────────────┤
│ PK: id                          │
│ - fullName                      │
│ - email (UNIQUE)                │
│ - password (BCrypt)             │
│ - role (ADMIN/LENDER/BORROWER)  │
│ - createdAt, updatedAt          │
└─────┬───────────────────────┬───┘
      │                       │
      │ 1:1 (Optional)        │ 1:1 (Optional)
      │                       │
      ▼                       ▼
┌──────────────┐         ┌──────────────┐
│  BORROWER    │         │    LENDER    │
├──────────────┤         ├──────────────┤
│ PK: id       │         │ PK: id       │
│ FK: userId   │         │ FK: userId   │
│ - activeLoans│         │ - company    │
│ - riskLevel  │         │ - totalDisb  │
└──────┬───────┘         └────┬─────────┘
       │                      │
       │ 1:M                  │ 1:M
       │                      │
       └──────────┬───────────┘
                  │
                  ▼
          ┌──────────────────┐
          │      LOANS       │
          ├──────────────────┤
          │ PK: id           │
          │ FK: borrowerId   │
          │ FK: lenderId     │
          │ - amount         │
          │ - interestRate   │
          │ - tenure         │
          │ - status         │
          └────┬──────────┬──┘
               │          │
        1:M ───┤          ├─── 1:M
               │          │
               ▼          ▼
      ┌──────────────┐  ┌─────────────────┐
      │   PAYMENTS   │  │  EMI_SCHEDULE   │
      ├──────────────┤  ├─────────────────┤
      │ PK: id       │  │ PK: id          │
      │ FK: loanId   │  │ FK: loanId      │
      │ - amount     │  │ - emiNumber     │
      │ - status     │  │ - amount        │
      └──────────────┘  │ - principal     │
                        │ - interest      │
                        │ - status        │
                        └─────────────────┘
```

### Table Dependency Chain

```
Users (Root Entity)
  ├── Borrower (depends on Users)
  │   └── Loans (depends on Borrower)
  │       ├── Payments (depends on Loans)
  │       └── EMI_Schedule (depends on Loans)
  │
  └── Lender (depends on Users)
      └── Loans (depends on Lender)
          ├── Payments (depends on Loans)
          └── EMI_Schedule (depends on Loans)

Security_Logs (depends on Users)
```

---

## 🔧 Connection Configuration

### Java Spring Boot Configuration

**File:** `src/main/resources/application.properties`

```properties
# Database URL
spring.datasource.url=jdbc:mysql://localhost:3306/loan_management?useSSL=false&serverTimezone=UTC

# Database credentials
spring.datasource.username=root
spring.datasource.password=Nancy123abc@

# Connection Pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# Hibernate/JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.show-sql=false
```

### MySQL Command Line Connection

```bash
# Connect to MySQL server
mysql -u root -p -h localhost

# Connect directly to database
mysql -u root -p loan_management

# Password prompt will appear - enter: Nancy123abc@
```

### Connection Pooling Strategy

The application uses HikariCP (default in Spring Boot):

- **Connection Acquisition:** < 250ms
- **Idle Timeout:** 10 minutes
- **Maximum Lifetime:** 30 minutes
- **Connection Validation:** Every 5 minutes

---

## 💾 Backup & Recovery

### Backup Procedures

#### Full Database Backup

```bash
# Create backup
mysqldump -u root -p loan_management > loan_management_backup.sql

# Backup with timestamp
mysqldump -u root -p loan_management > loan_management_backup_$(date +%Y%m%d_%H%M%S).sql

# All databases backup
mysqldump -u root -p --all-databases > all_databases_backup.sql
```

#### Restore from Backup

```bash
# Restore full database
mysql -u root -p loan_management < loan_management_backup.sql

# Restore specific table
mysql -u root -p loan_management < loan_management_backup.sql --tables users
```

### Backup Best Practices

1. **Schedule Regular Backups:** Daily or per business requirement
2. **Test Restores:** Monthly restore from backup to verify integrity
3. **Multiple Locations:** Store backups on different servers/cloud storage
4. **Encryption:** Encrypt sensitive backup files
5. **Version Control:** Maintain multiple backup versions
6. **Documentation:** Log all backup/restore operations

### Automated Backup Script

```bash
#!/bin/bash
# backup.sh - Automated daily backup

BACKUP_DIR="/backups/mysql"
DBNAME="loan_management"
USER="root"
PASS="Nancy123abc@"
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p $BACKUP_DIR

mysqldump -u $USER -p$PASS $DBNAME > $BACKUP_DIR/backup_$DATE.sql

# Keep only last 7 days
find $BACKUP_DIR -name "backup_*.sql" -mtime +7 -delete

echo "Backup completed: $BACKUP_DIR/backup_$DATE.sql"
```

---

## 🐛 Troubleshooting

### Connection Issues

#### Error: "Access denied for user 'root'@'localhost'"

```bash
# Verify credentials in application.properties
# Check MySQL is running
mysql -u root -p
# Verify password
echo "Nancy123abc@" | mysql -u root -p -e "SELECT 1;"
```

#### Error: "Unknown database 'loan_management'"

```sql
-- Create database
CREATE DATABASE loan_management 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Verify creation
SHOW DATABASES;
```

#### Error: "Communications link failure"

```bash
# Check MySQL service status
# Windows
net start MySQL80

# Linux
sudo systemctl start mysql

# Mac
brew services start mysql-server
```

### Performance Issues

#### Slow Queries

```sql
-- Enable query logging
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;

-- Check slow query log
SHOW VARIABLES LIKE 'slow_query%';

-- Analyze query
EXPLAIN SELECT * FROM loans WHERE status = 'PENDING';
```

#### High Memory Usage

```sql
-- Check buffer pool
SHOW VARIABLES LIKE 'innodb_buffer_pool_size';

-- Optimize table
OPTIMIZE TABLE users, borrower, lender, loans, payments;

-- Check table statistics
ANALYZE TABLE users, borrower, lender, loans, payments;
```

### Data Issues

#### Referential Integrity Errors

```sql
-- Check foreign key status
SELECT CONSTRAINT_NAME, CONSTRAINT_TYPE 
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_NAME = 'loans';

-- Disable/Enable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;
-- ... perform operations ...
SET FOREIGN_KEY_CHECKS = 1;
```

#### Duplicate Entry Errors

```sql
-- Check for duplicate emails
SELECT email, COUNT(*) 
FROM users 
GROUP BY email 
HAVING COUNT(*) > 1;

-- Remove duplicates
DELETE FROM users 
WHERE id NOT IN (
    SELECT MIN(id) FROM users GROUP BY email
);
```

### Validation

#### Check Database Integrity

```sql
-- Check all tables
CHECK TABLE users, borrower, lender, loans, payments, emi_schedule;

-- Repair if needed
REPAIR TABLE users;

-- Optimize all tables
OPTIMIZE TABLE users, borrower, lender, loans, payments, emi_schedule;
```

---

## 📞 Database Support

### Common Queries

#### User Statistics
```sql
SELECT role, COUNT(*) as count 
FROM users 
GROUP BY role;
```

#### Active Loans by Status
```sql
SELECT status, COUNT(*) as count 
FROM loans 
GROUP BY status;
```

#### Total Amount Disbursed
```sql
SELECT SUM(amount) as total_disbursed 
FROM loans 
WHERE status = 'DISBURSED';
```

#### Overdue EMI Schedule
```sql
SELECT * FROM emi_schedule 
WHERE status = 'OVERDUE' 
AND due_date < UNIX_TIMESTAMP() * 1000;
```

---

## 📚 Additional Resources

- [MySQL 8.0 Documentation](https://dev.mysql.com/doc/mysql-en/)
- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [Hibernate ORM Documentation](https://hibernate.org/orm/)
- [InnoDB Storage Engine](https://dev.mysql.com/doc/refman/8.0/en/innodb-storage-engine.html)

---

**Last Updated:** April 7, 2026  
**Version:** 1.0  
**Author:** LoanFlow Development Team

