package com.bank.exception;

/**
 * Thrown when an operation is attempted on a frozen or locked account.
 */
public class AccountFrozenException extends BankingException {
    public AccountFrozenException(String message) {
        super(message);
    }
}
