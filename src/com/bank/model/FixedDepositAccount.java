package com.bank.model;

import com.bank.exception.BankingException;
import com.bank.exception.InvalidAmountException;
import com.bank.util.SecurityUtil;

import java.time.LocalDateTime;

/**
 * Fixed Deposit (FD) / Term Deposit Account specialization:
 *  - Fixed investment tenure with high guaranteed interest.
 *  - No recurring deposits permitted.
 *  - Penalizes premature liquidation before maturity date.
 */
public class FixedDepositAccount extends Account implements InterestBearing {
    private static final long serialVersionUID = 1L;

    private final double principalAmount;
    private final int tenureMonths;
    private final double annualInterestRate;
    private final LocalDateTime maturityDate;
    private boolean isLiquidated;

    public FixedDepositAccount(String accountNumber, Customer customer, double initialDeposit, String pin,
                               int tenureMonths, double annualInterestRate) {
        super(accountNumber, customer, initialDeposit, pin);
        this.principalAmount = initialDeposit;
        this.tenureMonths = tenureMonths;
        this.annualInterestRate = annualInterestRate;
        this.maturityDate = getCreatedAt().plusMonths(tenureMonths);
        this.isLiquidated = false;
    }

    @Override
    public String getAccountType() {
        return "FIXED_DEPOSIT";
    }

    @Override
    public void deposit(double amount, String description) throws BankingException {
        throw new BankingException("Fixed Deposit accounts do not accept recurring deposits. Please open a new FD account.");
    }

    @Override
    public synchronized void withdraw(double amount, String description) throws BankingException {
        ensureAccountIsActive();
        if (isLiquidated) {
            throw new BankingException("This Fixed Deposit account has already been liquidated.");
        }
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be strictly positive.");
        }

        boolean isPremature = LocalDateTime.now().isBefore(maturityDate);
        double totalAccrued = calculateMaturityPayout();
        double finalPayout = totalAccrued;

        if (isPremature) {
            // 20% penalty deduction on accrued interest if prematurely closed
            double accruedInterest = totalAccrued - principalAmount;
            double penalty = accruedInterest * 0.20;
            finalPayout = totalAccrued - penalty;
            addTransaction(new Transaction(
                    SecurityUtil.generateTransactionId(),
                    TransactionType.PENALTY_FEE,
                    penalty,
                    this.balance,
                    "Premature FD liquidation penalty (20% of accrued interest)",
                    null
            ));
        }

        this.balance = 0.0;
        this.isLiquidated = true;
        this.setStatus(AccountStatus.CLOSED);

        addTransaction(new Transaction(
                SecurityUtil.generateTransactionId(),
                TransactionType.WITHDRAWAL,
                finalPayout,
                0.0,
                description + (isPremature ? " [PREMATURE LIQUIDATION]" : " [FULL MATURITY PAYOUT]"),
                null
        ));
    }

    @Override
    public double calculateInterest() {
        // Compound interest formula: A = P(1 + r/n)^(nt)
        double r = (annualInterestRate / 100.0);
        double t = tenureMonths / 12.0;
        return principalAmount * Math.pow(1 + (r / 4.0), 4 * t) - principalAmount;
    }

    public double calculateMaturityPayout() {
        return principalAmount + calculateInterest();
    }

    @Override
    public synchronized void applyInterest() {
        // In FD, interest is credited at maturity
        double interest = calculateInterest();
        this.balance = principalAmount + interest;
        addTransaction(new Transaction(
                SecurityUtil.generateTransactionId(),
                TransactionType.INTEREST_CREDIT,
                interest,
                this.balance,
                String.format("Compounded FD Interest Credit (Tenure: %d months)", tenureMonths),
                null
        ));
    }

    @Override
    public void applyMonthlyMaintenanceOrInterest() {
        if (!isLiquidated && LocalDateTime.now().isAfter(maturityDate)) {
            applyInterest();
        }
    }

    @Override
    public double getInterestRate() {
        return annualInterestRate;
    }

    public double getPrincipalAmount() {
        return principalAmount;
    }

    public int getTenureMonths() {
        return tenureMonths;
    }

    public LocalDateTime getMaturityDate() {
        return maturityDate;
    }

    public boolean isLiquidated() {
        return isLiquidated;
    }
}
