package com.bank.model;

import com.bank.util.SecurityUtil;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Immutable ledger entry representing an individual financial transaction.
 */
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String transactionId;
    private final LocalDateTime timestamp;
    private final TransactionType type;
    private final double amount;
    private final double balanceAfter;
    private final String description;
    private final String counterpartyAccount;

    public Transaction(String transactionId, TransactionType type, double amount,
                       double balanceAfter, String description, String counterpartyAccount) {
        this.transactionId = transactionId;
        this.timestamp = LocalDateTime.now();
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.counterpartyAccount = counterpartyAccount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public TransactionType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public String getDescription() {
        return description;
    }

    public String getCounterpartyAccount() {
        return counterpartyAccount;
    }

    @Override
    public String toString() {
        return String.format("[%s] %-18s | %-16s | Amt: %10s | Bal: %10s | %s",
                SecurityUtil.formatDateTime(timestamp),
                transactionId,
                type,
                SecurityUtil.formatCurrency(amount),
                SecurityUtil.formatCurrency(balanceAfter),
                description + (counterpartyAccount != null ? " (Ref: " + counterpartyAccount + ")" : ""));
    }
}
