package com.bank.exception;

/**
 * Thrown when an invalid transaction amount is supplied (<= 0).
 */
public class InvalidAmountException extends BankingException {
    public InvalidAmountException(String message) {
        super(message);
    }
}
