package com.bank.model;

import com.bank.exception.BankingException;

/**
 * Interface defining transactional operations (Demonstrating Abstraction).
 */
public interface Transactable {
    void deposit(double amount) throws BankingException;
    void deposit(double amount, String description) throws BankingException;
    void withdraw(double amount) throws BankingException;
    void withdraw(double amount, String description) throws BankingException;
    double getBalance();
}
