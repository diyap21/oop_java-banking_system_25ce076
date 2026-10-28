package com.bank.model;

import com.bank.exception.BankingException;
import com.bank.exception.InsufficientFundsException;
import com.bank.exception.InvalidAmountException;
import com.bank.util.SecurityUtil;

/**
 * Savings Account specialization:
 *  - Enforces minimum balance maintenance.
 *  - Supports monthly compound interest accrual.
 *  - Tracks and enforces monthly withdrawal quotas.
 */
public class SavingsAccount extends Account implements InterestBearing {
    private static final long serialVersionUID = 1L;

    private double interestRate; // Annual interest rate in percentage (e.g., 4.5)
    private double minimumBalance;
    private int monthlyWithdrawalLimit;
    private int monthlyWithdrawalCount;

    public SavingsAccount(String accountNumber, Customer customer, double initialDeposit, String pin) {
        this(accountNumber, customer, initialDeposit, pin, 4.5, 500.0, 6);
    }

    public SavingsAccount(String accountNumber, Customer customer, double initialDeposit, String pin,
                          double interestRate, double minimumBalance, int monthlyWithdrawalLimit) {
        super(accountNumber, customer, initialDeposit, pin);
        this.interestRate = interestRate;
        this.minimumBalance = minimumBalance;
        this.monthlyWithdrawalLimit = monthlyWithdrawalLimit;
        this.monthlyWithdrawalCount = 0;
    }

    @Override
    public String getAccountType() {
        return "SAVINGS";
    }

    @Override
    public synchronized void withdraw(double amount, String description) throws BankingException {
        ensureAccountIsActive();
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be strictly positive. Supplied: " + amount);
        }

        if (this.balance - amount < this.minimumBalance) {
            throw new InsufficientFundsException(
                    String.format("Withdrawal denied: Minimum balance requirement of %s must be maintained. Current: %s, Requested: %s",
                            SecurityUtil.formatCurrency(minimumBalance),
                            SecurityUtil.formatCurrency(balance),
                            SecurityUtil.formatCurrency(amount)),
                    this.balance,
                    amount
            );
        }

        double fee = 0.0;
        if (monthlyWithdrawalCount >= monthlyWithdrawalLimit) {
            fee = 5.0; // Excessive withdrawal penalty fee
            if (this.balance - amount - fee < this.minimumBalance) {
                throw new InsufficientFundsException(
                        "Monthly withdrawal limit exceeded. Additional $5.00 fee cannot be accommodated without violating minimum balance.",
                        this.balance,
                        amount + fee
                );
            }
        }

        this.balance -= amount;
        monthlyWithdrawalCount++;

        addTransaction(new Transaction(
                SecurityUtil.generateTransactionId(),
                TransactionType.WITHDRAWAL,
                amount,
                this.balance,
                description + (fee > 0 ? " [Limit Exceeded Penalty Fee Applied]" : ""),
                null
        ));

        if (fee > 0) {
            this.balance -= fee;
            addTransaction(new Transaction(
                    SecurityUtil.generateTransactionId(),
                    TransactionType.PENALTY_FEE,
                    fee,
                    this.balance,
                    "Excessive monthly withdrawal fee",
                    null
            ));
        }
    }

    @Override
    public double calculateInterest() {
        // Monthly interest portion: (Balance * AnnualRate / 100) / 12
        return (this.balance * (this.interestRate / 100.0)) / 12.0;
    }

    @Override
    public synchronized void applyInterest() {
        if (this.balance <= 0) return;
        double monthlyInterest = calculateInterest();
        this.balance += monthlyInterest;
        addTransaction(new Transaction(
                SecurityUtil.generateTransactionId(),
                TransactionType.INTEREST_CREDIT,
                monthlyInterest,
                this.balance,
                String.format("Monthly interest credit at %.2f%% p.a.", interestRate),
                null
        ));
    }

    @Override
    public synchronized void applyMonthlyMaintenanceOrInterest() {
        applyInterest();
        this.monthlyWithdrawalCount = 0; // Reset monthly counter
    }

    @Override
    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public double getMinimumBalance() {
        return minimumBalance;
    }

    public void setMinimumBalance(double minimumBalance) {
        this.minimumBalance = minimumBalance;
    }

    public int getMonthlyWithdrawalLimit() {
        return monthlyWithdrawalLimit;
    }

    public int getMonthlyWithdrawalCount() {
        return monthlyWithdrawalCount;
    }
}
