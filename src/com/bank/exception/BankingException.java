package com.bank.exception;

/**
 * Base custom checked exception for all banking operations.
 */
public class BankingException extends Exception {
    public BankingException(String message) {
        super(message);
    }

    public BankingException(String message, Throwable cause) {
        super(message, cause);
    }
}
