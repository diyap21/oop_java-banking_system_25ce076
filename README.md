# 🏛️ Apex Global Secure Banking System (Java OOP)

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Build](https://img.shields.io/badge/Build-Passing-brightgreen?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)
![Course](https://img.shields.io/badge/Course-25CE076-orange?style=for-the-badge)

A enterprise-grade, object-oriented console Banking Management & Transaction Processing System developed in **Java 17**. Built with strict adherence to **Core Object-Oriented Programming (OOP) Principles**, clean architecture, thread safety, data persistence, and banking-grade security features.

---

## ⚡ Quick Start: Clone & Run in Terminal

Anyone can clone and run this repository directly from their terminal:

### 📥 Direct ZIP Download
You can download the bundled project package directly: **[`oop_java-banking_system_25ce076.zip`](oop_java-banking_system_25ce076.zip)** (or from GitHub: [Download ZIP](https://github.com/diyap21/oop_java-banking_system_25ce076/raw/main/oop_java-banking_system_25ce076.zip)).

### 1. Clone the Repository
```bash
git clone https://github.com/diyap21/oop_java-banking_system_25ce076.git
cd oop_java-banking_system_25ce076
```

### 2. Build and Run

#### 🔹 On Windows (PowerShell):
```powershell
.\compile.ps1
.\run.ps1
```
*(Or simply run `.\run.ps1` — it will automatically build if not already compiled)*

#### 🔹 On Windows (Command Prompt):
```cmd
compile.bat
run.bat
```

#### 🔹 On Linux / macOS (Bash):
```bash
chmod +x compile.sh run.sh
./run.sh
```

#### 🔹 Standard Java CLI (Any Platform):
```bash
# Compile:
javac -d bin -encoding UTF-8 $(find src -name "*.java")

# Run:
java -cp bin com.bank.Main
```

---

## 🔑 Demo Test Accounts (Pre-seeded for Instant Evaluation)

The system automatically loads test accounts on first launch so you can test all operations immediately:

| Account No | Account Holder | Type | PIN | Starting Balance | Features / Notes |
|:---|:---|:---|:---|:---|:---|
| **`ACC1001`** | **Diya Patel** | Savings | `1234` | **$17,500.00** | 4.5% Interest, $500 Min Balance, 6 Free Withdrawals/mo |
| **`ACC1002`** | **Alex Morgan** | Checking | `2345` | **$8,500.00** | $3,000 Overdraft Protection, Overdraft Fee logic |
| **`ACC1003`** | **Sarah Jenkins** | Fixed Deposit | `3456` | **$25,000.00** | 24-Month Term @ 7.5% p.a., Premature Liquidation Penalty |
| **Admin** | System Admin | Administrator | `admin123` *(user: `admin`)* | — | Vault Control, Audit Reports, Account Freezing, Interest Accrual |

---

## 📐 Object-Oriented Programming (OOP) Design

This project demonstrates all fundamental and advanced OOP paradigms:

### 1. 🛡️ Encapsulation
- All sensitive states (`balance`, `pinHash`, `status`, `transactions`, `customer`) are strictly encapsulated with `private` and `protected` access specifiers.
- **Cryptographic Security**: Passwords and PINs are hashed using **SHA-256** (`SecurityUtil.java`). Plaintext PINs are never stored or logged in memory or disk.
- Read/write operations are mediated through validation methods (`verifyPin()`, `deposit()`, `withdraw()`).

### 2. 🧬 Inheritance
- **Base Class**: `com.bank.model.Account` (Abstract base class encapsulating common identity, ledger history, statement generation, and lifecycle management).
- **Subclasses**:
  - `SavingsAccount extends Account implements InterestBearing`
  - `CheckingAccount extends Account`
  - `FixedDepositAccount extends Account implements InterestBearing`

### 3. 🔄 Polymorphism
- **Dynamic Method Dispatch (Runtime Polymorphism)**:
  - Overridden `withdraw()` in each subclass:
    - `SavingsAccount.withdraw()` enforces strict minimum balance limits and charges excessive withdrawal fees after quota exhaustion.
    - `CheckingAccount.withdraw()` checks available balance + overdraft limit and charges overdraft protection fees when negative.
    - `FixedDepositAccount.withdraw()` assesses premature penalty deductions on accrued interest.
  - Overridden `applyMonthlyMaintenanceOrInterest()` tailored to each account type.
- **Method Overloading (Compile-Time Polymorphism)**:
  - `deposit(double amount)` and `deposit(double amount, String description)`
  - `withdraw(double amount)` and `withdraw(double amount, String description)`
  - Overloaded constructors across models.

### 4. 🧩 Abstraction & Interfaces
- Separates high-level business contracts from implementation details:
  - `Transactable`: Defines core financial mutation specifications (`deposit`, `withdraw`, `getBalance`).
  - `Reportable`: Defines audit statements and file export methods.
  - `InterestBearing`: Contract for accounts eligible for interest accrual (`calculateInterest`, `applyInterest`).
  - `BankService`: Interface decoupling business logic from UI/CLI presentation layer.

---

## 🌟 Advanced & Extra Features Added

Beyond standard deposit/withdraw operations, the system includes:

1. **Deadlock-Free Fund Transfer**:
   - Inter-account atomic transfer with deterministic locking order based on account numbers to avoid thread deadlocks during concurrent operations.
2. **Loan Management & EMI Amortization**:
   - Customer loan applications with automated eligibility checking based on account balance.
   - Standard banking amortization formula:
     $$EMI = \frac{P \cdot r \cdot (1+r)^n}{(1+r)^n - 1}$$
   - Direct loan disbursal into account balance and monthly installment EMI repayment.
3. **Utility Bill Payments & Mobile Recharge**:
   - Integrated billing module for Electricity, Water, Broadband, Mobile, and Fastag tolls with instant transaction receipts.
4. **Security & Anti-Brute-Force Lockout**:
   - Accounts are automatically **LOCKED** after 3 consecutive wrong PIN entries.
   - Admin unlock facility and account freeze/unfreeze capability for fraud prevention.
5. **Statement Export to Physical Text File**:
   - View formatted mini statements or export full formal account statements to file (e.g., `statement_ACC1001.txt`).
6. **Data Persistence**:
   - Data is automatically saved to and loaded from `data/bank_data.dat` using Java Object Serialization, preserving all state between application restarts.
7. **Dual-Role Management**:
   - Customer self-service dashboard vs. Bank Administrator executive control panel.

---

## 🗂️ Project Directory Structure

```
oop_java-banking_system_25ce076/
├── src/
│   └── com/
│       └── bank/
│           ├── Main.java                         # Application Entry Point
│           ├── model/
│           │   ├── Account.java                  # Abstract Base Account
│           │   ├── SavingsAccount.java           # Savings specialization
│           │   ├── CheckingAccount.java          # Checking with Overdraft
│           │   ├── FixedDepositAccount.java      # Term Deposit specialization
│           │   ├── Customer.java                 # Customer details entity
│           │   ├── Transaction.java              # Immutable Ledger Entry
│           │   ├── TransactionType.java          # Transaction classification Enum
│           │   ├── AccountStatus.java            # Account status Enum
│           │   ├── Loan.java                     # Loan amortization entity
│           │   ├── Transactable.java             # Transaction Interface
│           │   ├── Reportable.java               # Reporting Interface
│           │   └── InterestBearing.java          # Interest Interface
│           ├── service/
│           │   ├── BankService.java              # Core Service Interface
│           │   └── BankServiceImpl.java          # Business Logic & Rules
│           ├── repository/
│           │   └── BankRepository.java           # Data Storage & Persistence
│           ├── exception/
│           │   ├── BankingException.java         # Base custom checked exception
│           │   ├── InsufficientFundsException.java
│           │   ├── AccountNotFoundException.java
│           │   ├── InvalidPinException.java
│           │   ├── AccountFrozenException.java
│           │   └── InvalidAmountException.java
│           ├── ui/
│           │   └── BankCLI.java                  # Interactive Console CLI
│           └── util/
│               └── SecurityUtil.java             # SHA-256 Hashing & Formatting
├── data/                                         # Persistent storage directory
│   └── .gitkeep
├── bin/                                          # Compiled bytecode output (.class)
├── compile.bat                                   # Windows CMD compilation script
├── run.bat                                       # Windows CMD run script
├── compile.ps1                                   # Windows PowerShell build script
├── run.ps1                                       # Windows PowerShell run script
├── compile.sh                                    # Unix/macOS build script
├── run.sh                                        # Unix/macOS run script
├── .gitignore                                    # Git exclusion rules
└── README.md                                     # Full documentation
```

---

## 💻 System Requirements

- **Java Development Kit (JDK)**: Version 11 or higher (Tested on Temurin OpenJDK 17).
- **Operating System**: Windows, macOS, or Linux.
- **Git**: Installed and accessible in your command line.

---

## 👤 Author & Academic Details

- **Student / Author**: Diya Patel
- **Course / ID**: 25CE076 (Object-Oriented Programming with Java)
- **GitHub Repository**: [https://github.com/diyap21/oop_java-banking_system_25ce076.git](https://github.com/diyap21/oop_java-banking_system_25ce076.git)
