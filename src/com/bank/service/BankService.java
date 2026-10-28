package com.bank.service;

import com.bank.exception.AccountNotFoundException;
import com.bank.exception.BankingException;
import com.bank.model.*;

import java.util.Collection;
import java.util.List;

/**
 * Service Contract defining core banking business operations.
 */
public interface BankService {
    SavingsAccount openSavingsAccount(String customerName, String email, String phone, String nationalId,
                                      String address, double initialDeposit, String pin, double interestRate,
                                      double minBalance) throws BankingException;

    CheckingAccount openCheckingAccount(String customerName, String email, String phone, String nationalId,
                                        String address, double initialDeposit, String pin,
                                        double overdraftLimit) throws BankingException;

    FixedDepositAccount openFixedDepositAccount(String customerName, String email, String phone, String nationalId,
                                                String address, double initialDeposit, String pin,
                                                int tenureMonths, double annualRate) throws BankingException;

    Account getAccount(String accountNumber) throws AccountNotFoundException;

    Collection<Account> getAllAccounts();

    void deposit(String accountNumber, double amount, String description) throws BankingException;

    void withdraw(String accountNumber, String pin, double amount, String description) throws BankingException;

    void transferFunds(String sourceAccNo, String pin, String destAccNo, double amount, String remarks) throws BankingException;

    void payBill(String accountNumber, String pin, String billerName, String billType, double amount, String billRefNumber) throws BankingException;

    void changePin(String accountNumber, String oldPin, String newPin) throws BankingException;

    void freezeAccount(String accountNumber) throws AccountNotFoundException;

    void unfreezeAccount(String accountNumber) throws AccountNotFoundException;

    void unlockAccount(String accountNumber) throws AccountNotFoundException;

    boolean closeAccount(String accountNumber) throws AccountNotFoundException;

    double getTotalBankDeposits();

    List<Account> searchAccounts(String query);

    void applySystemWideInterest();

    String generateAuditReport();

    Loan applyLoan(String accountNumber, String loanType, double amount, int tenureMonths) throws BankingException;

    void repayLoanEmi(String accountNumber, String pin, String loanId) throws BankingException;

    List<Loan> getCustomerLoans(String accountNumber);

    List<Loan> getAllLoans();
}
