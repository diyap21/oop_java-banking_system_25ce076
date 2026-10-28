package com.bank.model;

import com.bank.util.SecurityUtil;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Loan entity handling principal, interest, EMI amortization and repayment status.
 */
public class Loan implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String loanId;
    private final String accountNumber;
    private final String loanType; // Personal, Home, Education, Auto
    private final double principalAmount;
    private final double annualInterestRate;
    private final int tenureMonths;
    private final double monthlyEmi;
    private double remainingBalance;
    private boolean isApproved;
    private boolean isFullyRepaid;
    private final LocalDateTime appliedDate;

    public Loan(String loanId, String accountNumber, String loanType,
                double principalAmount, double annualInterestRate, int tenureMonths) {
        this.loanId = loanId;
        this.accountNumber = accountNumber;
        this.loanType = loanType;
        this.principalAmount = principalAmount;
        this.annualInterestRate = annualInterestRate;
        this.tenureMonths = tenureMonths;
        this.monthlyEmi = calculateEmi(principalAmount, annualInterestRate, tenureMonths);
        this.remainingBalance = this.monthlyEmi * tenureMonths;
        this.isApproved = true;
        this.isFullyRepaid = false;
        this.appliedDate = LocalDateTime.now();
    }

    public static double calculateEmi(double principal, double annualRate, int months) {
        if (months <= 0) return 0;
        double monthlyRate = (annualRate / 12.0) / 100.0;
        if (monthlyRate == 0) return principal / months;
        return (principal * monthlyRate * Math.pow(1 + monthlyRate, months)) /
               (Math.pow(1 + monthlyRate, months) - 1);
    }

    public void repayInstallment(double amount) {
        this.remainingBalance -= amount;
        if (this.remainingBalance <= 0.01) {
            this.remainingBalance = 0.0;
            this.isFullyRepaid = true;
        }
    }

    public String getLoanId() {
        return loanId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getLoanType() {
        return loanType;
    }

    public double getPrincipalAmount() {
        return principalAmount;
    }

    public double getAnnualInterestRate() {
        return annualInterestRate;
    }

    public int getTenureMonths() {
        return tenureMonths;
    }

    public double getMonthlyEmi() {
        return monthlyEmi;
    }

    public double getRemainingBalance() {
        return remainingBalance;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public boolean isFullyRepaid() {
        return isFullyRepaid;
    }

    public LocalDateTime getAppliedDate() {
        return appliedDate;
    }

    @Override
    public String toString() {
        return String.format("Loan[%s | %s | Principal: %s | EMI: %s/mo | Tenure: %d mos | Remaining: %s | Status: %s]",
                loanId, loanType,
                SecurityUtil.formatCurrency(principalAmount),
                SecurityUtil.formatCurrency(monthlyEmi),
                tenureMonths,
                SecurityUtil.formatCurrency(remainingBalance),
                isFullyRepaid ? "PAID OFF" : (isApproved ? "ACTIVE" : "PENDING"));
    }
}
