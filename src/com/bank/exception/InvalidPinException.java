package com.bank.exception;

/**
 * Thrown when authentication with PIN fails.
 */
public class InvalidPinException extends BankingException {
    private final int remainingAttempts;

    public InvalidPinException(String message, int remainingAttempts) {
        super(message);
        this.remainingAttempts = remainingAttempts;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }
}
