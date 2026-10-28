package com.bank.model;

/**
 * Enumeration of supported transaction classifications.
 */
public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER_SENT,
    TRANSFER_RECEIVED,
    INTEREST_CREDIT,
    BILL_PAYMENT,
    LOAN_DISBURSAL,
    LOAN_EMI_DEDUCTION,
    PENALTY_FEE
}
