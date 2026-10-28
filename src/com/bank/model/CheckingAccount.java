package com.bank.model;

import com.bank.exception.BankingException;
import com.bank.exception.InsufficientFundsException;
import com.bank.exception.InvalidAmountException;
import com.bank.util.SecurityUtil;

/**
 * Checking / Current Account specialization:
 *  - Provides overdraft protection limit.
 *  - Permits negative balance up to overdraft facility.
 *  - Imposes overdraft transaction fee when utilizing credit line.
 */
public class CheckingAccount extends Account {
    private static final long serialVersionUID = 1L;

    private double overdraftLimit;
    private double overdraftFee;

    public CheckingAccount(String accountNumber, Customer customer, double initialDeposit, String pin) {
        this(accountNumber, customer, initialDeposit, pin, 2000.0, 25.0);
    }

    public CheckingAccount(String accountNumber, Customer customer, double initialDeposit, String pin,
                           double overdraftLimit, double overdraftFee) {
        super(accountNumber, customer, initialDeposit, pin);
        this.overdraftLimit = overdraftLimit;
        this.overdraftFee = overdraftFee;
    }

    @Override
    public String getAccountType() {
        return "CHECKING";
    }

    @Override
    public synchronized void withdraw(double amount, String description) throws BankingException {
        ensureAccountIsActive();
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be strictly positive. Supplied: " + amount);
        }

        double maxAvailable = this.balance + this.overdraftLimit;
        if (amount > maxAvailable) {
            throw new InsufficientFundsException(
                    String.format("Overdraft limit exceeded! Requested: %s, Current Balance: %s, Allowed Overdraft: %s",
                            SecurityUtil.formatCurrency(amount),
                            SecurityUtil.formatCurrency(balance),
                            SecurityUtil.formatCurrency(overdraftLimit)),
                    maxAvailable,
                    amount
            );
        }

        boolean willUseOverdraft = (this.balance - amount < 0);
        this.balance -= amount;

        addTransaction(new Transaction(
                SecurityUtil.generateTransactionId(),
                TransactionType.WITHDRAWAL,
                amount,
                this.balance,
                description + (willUseOverdraft ? " [Overdraft Line Used]" : ""),
                null
        ));

        // Deduct overdraft fee if dipping into negative territory
        if (willUseOverdraft && this.overdraftFee > 0) {
            this.balance -= this.overdraftFee;
            addTransaction(new Transaction(
                    SecurityUtil.generateTransactionId(),
                    TransactionType.PENALTY_FEE,
                    this.overdraftFee,
                    this.balance,
                    "Overdraft protection fee charged",
                    null
            ));
        }
    }

    @Override
    public synchronized void applyMonthlyMaintenanceOrInterest() {
        // If account is overdrawn, apply 1.5% monthly overdraft interest on negative balance
        if (this.balance < 0) {
            double penalty = Math.abs(this.balance) * 0.015;
            this.balance -= penalty;
            addTransaction(new Transaction(
                    SecurityUtil.generateTransactionId(),
                    TransactionType.PENALTY_FEE,
                    penalty,
                    this.balance,
                    "Monthly overdraft interest surcharge (1.5%)",
                    null
            ));
        }
    }

    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(double overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    public double getOverdraftFee() {
        return overdraftFee;
    }

    public void setOverdraftFee(double overdraftFee) {
        this.overdraftFee = overdraftFee;
    }
}
