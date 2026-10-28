package com.bank.model;

import com.bank.exception.AccountFrozenException;
import com.bank.exception.BankingException;
import com.bank.exception.InvalidAmountException;
import com.bank.exception.InvalidPinException;
import com.bank.util.SecurityUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base class representing a Bank Account.
 * Demonstrates:
 *  - Encapsulation: private/protected state, controlled access via methods
 *  - Abstraction: abstract methods for withdraw() and account lifecycle
 *  - Polymorphism: implemented differently by Savings, Checking, Fixed Deposit
 */
public abstract class Account implements Transactable, Reportable, Serializable {
    private static final long serialVersionUID = 1L;
    private static final int MAX_FAILED_PIN_ATTEMPTS = 3;

    private final String accountNumber;
    private Customer customer;
    protected double balance;
    private String pinHash;
    private AccountStatus status;
    private final List<Transaction> transactions;
    private final LocalDateTime createdAt;
    private int failedPinAttempts;

    public Account(String accountNumber, Customer customer, double initialDeposit, String pin) {
        this.accountNumber = accountNumber;
        this.customer = customer;
        this.balance = initialDeposit;
        this.pinHash = SecurityUtil.hashPin(pin);
        this.status = AccountStatus.ACTIVE;
        this.transactions = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.failedPinAttempts = 0;

        // Record opening balance transaction
        if (initialDeposit > 0) {
            this.transactions.add(new Transaction(
                    SecurityUtil.generateTransactionId(),
                    TransactionType.DEPOSIT,
                    initialDeposit,
                    initialDeposit,
                    "Initial Account Opening Deposit",
                    null
            ));
        }
    }

    public abstract String getAccountType();

    public abstract void applyMonthlyMaintenanceOrInterest();

    @Override
    public void deposit(double amount) throws BankingException {
        deposit(amount, "Standard Deposit");
    }

    @Override
    public synchronized void deposit(double amount, String description) throws BankingException {
        ensureAccountIsActive();
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be strictly positive. Supplied: " + amount);
        }
        this.balance += amount;
        Transaction tx = new Transaction(
                SecurityUtil.generateTransactionId(),
                TransactionType.DEPOSIT,
                amount,
                this.balance,
                description,
                null
        );
        this.transactions.add(tx);
    }

    @Override
    public void withdraw(double amount) throws BankingException {
        withdraw(amount, "Standard Withdrawal");
    }

    @Override
    public abstract void withdraw(double amount, String description) throws BankingException;

    public boolean verifyPin(String pin) throws InvalidPinException, AccountFrozenException {
        if (status == AccountStatus.LOCKED) {
            throw new AccountFrozenException("Account " + accountNumber + " is LOCKED due to excessive failed attempts. Contact Bank Admin.");
        }
        if (status == AccountStatus.FROZEN) {
            throw new AccountFrozenException("Account " + accountNumber + " is currently FROZEN. Contact Bank Admin.");
        }
        if (status == AccountStatus.CLOSED) {
            throw new AccountFrozenException("Account " + accountNumber + " is CLOSED.");
        }

        String inputHash = SecurityUtil.hashPin(pin);
        if (this.pinHash.equals(inputHash)) {
            this.failedPinAttempts = 0;
            return true;
        } else {
            this.failedPinAttempts++;
            int remaining = MAX_FAILED_PIN_ATTEMPTS - this.failedPinAttempts;
            if (this.failedPinAttempts >= MAX_FAILED_PIN_ATTEMPTS) {
                this.status = AccountStatus.LOCKED;
                throw new AccountFrozenException("Account " + accountNumber + " has been LOCKED after 3 consecutive invalid PIN entries!");
            }
            throw new InvalidPinException("Invalid PIN entered. Attempts remaining: " + remaining, remaining);
        }
    }

    public synchronized void changePin(String oldPin, String newPin) throws InvalidPinException, AccountFrozenException {
        verifyPin(oldPin);
        if (newPin == null || newPin.trim().length() < 4) {
            throw new IllegalArgumentException("New PIN must be at least 4 digits long.");
        }
        this.pinHash = SecurityUtil.hashPin(newPin.trim());
    }

    public synchronized void resetFailedAttempts() {
        this.failedPinAttempts = 0;
        if (this.status == AccountStatus.LOCKED) {
            this.status = AccountStatus.ACTIVE;
        }
    }

    protected void ensureAccountIsActive() throws AccountFrozenException {
        if (status != AccountStatus.ACTIVE) {
            throw new AccountFrozenException("Account " + accountNumber + " is not ACTIVE. Current status: " + status);
        }
    }

    public void addTransaction(Transaction tx) {
        this.transactions.add(tx);
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @Override
    public double getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getFailedPinAttempts() {
        return failedPinAttempts;
    }

    @Override
    public String generateMiniStatement() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n==================== MINI STATEMENT ====================\n");
        sb.append(String.format("Account: %-15s Type: %s\n", accountNumber, getAccountType()));
        sb.append(String.format("Customer: %-14s Balance: %s\n", customer.getFullName(), SecurityUtil.formatCurrency(balance)));
        sb.append("--------------------------------------------------------\n");
        int size = transactions.size();
        int count = Math.min(5, size);
        if (size == 0) {
            sb.append("No transactions on record.\n");
        } else {
            for (int i = size - count; i < size; i++) {
                sb.append(transactions.get(i).toString()).append("\n");
            }
        }
        sb.append("========================================================\n");
        return sb.toString();
    }

    @Override
    public String generateFullStatement() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================================================================\n");
        sb.append(String.format("                             OFFICIAL ACCOUNT STATEMENT\n"));
        sb.append("========================================================================================\n");
        sb.append(String.format("Account Number: %-25s Account Type: %s\n", accountNumber, getAccountType()));
        sb.append(String.format("Account Holder: %-25s Account Status: %s\n", customer.getFullName(), status));
        sb.append(String.format("Registered Phone: %-23s Email: %s\n", customer.getPhoneNumber(), customer.getEmail()));
        sb.append(String.format("Current Balance: %-24s Statement Generated: %s\n",
                SecurityUtil.formatCurrency(balance), SecurityUtil.formatDateTime(LocalDateTime.now())));
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format("%-20s | %-12s | %-16s | %10s | %10s | %s\n",
                "Timestamp", "Txn ID", "Type", "Amount", "Balance", "Description"));
        sb.append("----------------------------------------------------------------------------------------\n");
        if (transactions.isEmpty()) {
            sb.append(" No transactions recorded.\n");
        } else {
            for (Transaction tx : transactions) {
                sb.append(String.format("%-20s | %-12s | %-16s | %10s | %10s | %s\n",
                        SecurityUtil.formatDateTime(tx.getTimestamp()),
                        tx.getTransactionId(),
                        tx.getType(),
                        SecurityUtil.formatCurrency(tx.getAmount()),
                        SecurityUtil.formatCurrency(tx.getBalanceAfter()),
                        tx.getDescription() + (tx.getCounterpartyAccount() != null ? " [Ref:" + tx.getCounterpartyAccount() + "]" : "")));
            }
        }
        sb.append("========================================================================================\n");
        return sb.toString();
    }

    @Override
    public void exportStatementToFile(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(generateFullStatement());
        }
    }

    @Override
    public String toString() {
        return String.format("[%s] Acc: %s | Holder: %-15s | Bal: %10s | Status: %s",
                getAccountType(), accountNumber, customer.getFullName(), SecurityUtil.formatCurrency(balance), status);
    }
}
