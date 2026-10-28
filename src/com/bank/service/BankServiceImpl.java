package com.bank.service;

import com.bank.exception.*;
import com.bank.model.*;
import com.bank.repository.BankRepository;
import com.bank.util.SecurityUtil;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Concrete implementation of the BankService.
 * Enforces business rules, concurrency safety, and transaction atomicity.
 */
public class BankServiceImpl implements BankService {

    private final BankRepository repository;
    private final AtomicInteger accountSequence = new AtomicInteger(1004);
    private final AtomicInteger customerSequence = new AtomicInteger(104);
    private final AtomicInteger loanSequence = new AtomicInteger(502);

    public BankServiceImpl() {
        this.repository = BankRepository.getInstance();
    }

    private synchronized String nextAccountNumber() {
        return "ACC" + accountSequence.getAndIncrement();
    }

    private synchronized String nextCustomerId() {
        return "CUST" + customerSequence.getAndIncrement();
    }

    private synchronized String nextLoanId() {
        return "LOAN-" + loanSequence.getAndIncrement();
    }

    @Override
    public SavingsAccount openSavingsAccount(String customerName, String email, String phone, String nationalId,
                                            String address, double initialDeposit, String pin, double interestRate,
                                            double minBalance) throws BankingException {
        if (initialDeposit < minBalance) {
            throw new BankingException("Initial deposit cannot be less than the minimum balance requirement: " + minBalance);
        }
        Customer customer = new Customer(nextCustomerId(), customerName, email, phone, nationalId, address);
        SavingsAccount account = new SavingsAccount(nextAccountNumber(), customer, initialDeposit, pin, interestRate, minBalance, 6);
        repository.addAccount(account);
        return account;
    }

    @Override
    public CheckingAccount openCheckingAccount(String customerName, String email, String phone, String nationalId,
                                              String address, double initialDeposit, String pin,
                                              double overdraftLimit) throws BankingException {
        if (initialDeposit < 0) {
            throw new InvalidAmountException("Initial deposit cannot be negative.");
        }
        Customer customer = new Customer(nextCustomerId(), customerName, email, phone, nationalId, address);
        CheckingAccount account = new CheckingAccount(nextAccountNumber(), customer, initialDeposit, pin, overdraftLimit, 25.0);
        repository.addAccount(account);
        return account;
    }

    @Override
    public FixedDepositAccount openFixedDepositAccount(String customerName, String email, String phone, String nationalId,
                                                      String address, double initialDeposit, String pin,
                                                      int tenureMonths, double annualRate) throws BankingException {
        if (initialDeposit < 1000.0) {
            throw new BankingException("Minimum initial deposit for Fixed Deposit is $1,000.00.");
        }
        if (tenureMonths < 3) {
            throw new BankingException("Minimum tenure for Fixed Deposit is 3 months.");
        }
        Customer customer = new Customer(nextCustomerId(), customerName, email, phone, nationalId, address);
        FixedDepositAccount account = new FixedDepositAccount(nextAccountNumber(), customer, initialDeposit, pin, tenureMonths, annualRate);
        repository.addAccount(account);
        return account;
    }

    @Override
    public Account getAccount(String accountNumber) throws AccountNotFoundException {
        Account account = repository.getAccount(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException(accountNumber);
        }
        return account;
    }

    @Override
    public Collection<Account> getAllAccounts() {
        return repository.getAllAccounts();
    }

    @Override
    public void deposit(String accountNumber, double amount, String description) throws BankingException {
        Account account = getAccount(accountNumber);
        synchronized (account) {
            account.deposit(amount, description);
            repository.saveToDisk();
        }
    }

    @Override
    public void withdraw(String accountNumber, String pin, double amount, String description) throws BankingException {
        Account account = getAccount(accountNumber);
        synchronized (account) {
            account.verifyPin(pin);
            account.withdraw(amount, description);
            repository.saveToDisk();
        }
    }

    @Override
    public void transferFunds(String sourceAccNo, String pin, String destAccNo, double amount, String remarks) throws BankingException {
        if (sourceAccNo.equalsIgnoreCase(destAccNo)) {
            throw new BankingException("Source and destination accounts cannot be identical.");
        }
        if (amount <= 0) {
            throw new InvalidAmountException("Transfer amount must be strictly greater than 0.");
        }

        Account source = getAccount(sourceAccNo);
        Account dest = getAccount(destAccNo);

        // Verify PIN and eligibility before locking
        source.verifyPin(pin);

        // Prevent Deadlock by ordering locks based on Account Number hash/lexicographical order
        Account firstLock = source.getAccountNumber().compareTo(dest.getAccountNumber()) < 0 ? source : dest;
        Account secondLock = source.getAccountNumber().compareTo(dest.getAccountNumber()) < 0 ? dest : source;

        synchronized (firstLock) {
            synchronized (secondLock) {
                // Deduct from source
                source.withdraw(amount, "Funds Transfer to " + destAccNo + (remarks != null && !remarks.isEmpty() ? " - " + remarks : ""));

                // Credit to destination
                dest.deposit(amount, "Funds Received from " + sourceAccNo + (remarks != null && !remarks.isEmpty() ? " - " + remarks : ""));

                repository.saveToDisk();
            }
        }
    }

    @Override
    public void payBill(String accountNumber, String pin, String billerName, String billType, double amount, String billRefNumber) throws BankingException {
        Account account = getAccount(accountNumber);
        synchronized (account) {
            account.verifyPin(pin);
            String desc = String.format("Utility Bill Payment (%s - %s) Ref: %s", billType, billerName, billRefNumber);
            account.withdraw(amount, desc);
            repository.saveToDisk();
        }
    }

    @Override
    public void changePin(String accountNumber, String oldPin, String newPin) throws BankingException {
        Account account = getAccount(accountNumber);
        synchronized (account) {
            account.changePin(oldPin, newPin);
            repository.saveToDisk();
        }
    }

    @Override
    public void freezeAccount(String accountNumber) throws AccountNotFoundException {
        Account account = getAccount(accountNumber);
        account.setStatus(AccountStatus.FROZEN);
        repository.saveToDisk();
    }

    @Override
    public void unfreezeAccount(String accountNumber) throws AccountNotFoundException {
        Account account = getAccount(accountNumber);
        account.setStatus(AccountStatus.ACTIVE);
        repository.saveToDisk();
    }

    @Override
    public void unlockAccount(String accountNumber) throws AccountNotFoundException {
        Account account = getAccount(accountNumber);
        account.resetFailedAttempts();
        repository.saveToDisk();
    }

    @Override
    public boolean closeAccount(String accountNumber) throws AccountNotFoundException {
        Account account = getAccount(accountNumber);
        account.setStatus(AccountStatus.CLOSED);
        repository.saveToDisk();
        return true;
    }

    @Override
    public double getTotalBankDeposits() {
        double total = 0.0;
        for (Account a : repository.getAllAccounts()) {
            if (a.getStatus() != AccountStatus.CLOSED) {
                total += Math.max(0, a.getBalance());
            }
        }
        return total;
    }

    @Override
    public List<Account> searchAccounts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(repository.getAllAccounts());
        }
        String q = query.trim().toLowerCase();
        List<Account> matches = new ArrayList<>();
        for (Account a : repository.getAllAccounts()) {
            boolean matchAcc = a.getAccountNumber().toLowerCase().contains(q);
            boolean matchName = a.getCustomer() != null && a.getCustomer().getFullName().toLowerCase().contains(q);
            boolean matchType = a.getAccountType().toLowerCase().contains(q);
            if (matchAcc || matchName || matchType) {
                matches.add(a);
            }
        }
        return matches;
    }

    @Override
    public void applySystemWideInterest() {
        for (Account a : repository.getAllAccounts()) {
            if (a.getStatus() == AccountStatus.ACTIVE) {
                a.applyMonthlyMaintenanceOrInterest();
            }
        }
        repository.saveToDisk();
    }

    @Override
    public String generateAuditReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================================================================\n");
        sb.append("                          BANK SYSTEM CENTRAL AUDIT REPORT\n");
        sb.append("========================================================================================\n");
        sb.append("Report Date/Time: ").append(SecurityUtil.formatDateTime(LocalDateTime.now())).append("\n");
        sb.append("Total Registered Accounts: ").append(repository.getAllAccounts().size()).append("\n");
        sb.append("Total Reserve Deposit Vault: ").append(SecurityUtil.formatCurrency(getTotalBankDeposits())).append("\n");
        sb.append("Total Disbursed Loans: ").append(repository.getAllLoans().size()).append("\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format("%-12s | %-20s | %-14s | %12s | %-10s\n",
                "Account No", "Customer Name", "Type", "Balance", "Status"));
        sb.append("----------------------------------------------------------------------------------------\n");
        for (Account a : repository.getAllAccounts()) {
            sb.append(String.format("%-12s | %-20s | %-14s | %12s | %-10s\n",
                    a.getAccountNumber(),
                    a.getCustomer() != null ? a.getCustomer().getFullName() : "N/A",
                    a.getAccountType(),
                    SecurityUtil.formatCurrency(a.getBalance()),
                    a.getStatus()));
        }
        sb.append("========================================================================================\n");
        return sb.toString();
    }

    @Override
    public Loan applyLoan(String accountNumber, String loanType, double amount, int tenureMonths) throws BankingException {
        Account account = getAccount(accountNumber);
        if (amount < 500) {
            throw new BankingException("Minimum loan amount is $500.00.");
        }
        if (tenureMonths < 6 || tenureMonths > 120) {
            throw new BankingException("Loan tenure must be between 6 and 120 months.");
        }

        // Eligibility criteria: loan amount cannot exceed 5x average balance
        double maxEligible = Math.max(1000.0, account.getBalance() * 5.0);
        if (amount > maxEligible) {
            throw new BankingException(String.format("Loan rejected: Requested amount %s exceeds maximum eligibility limit of %s based on current account balance.",
                    SecurityUtil.formatCurrency(amount), SecurityUtil.formatCurrency(maxEligible)));
        }

        double rate = 8.5; // Base annual rate
        if (loanType.equalsIgnoreCase("HOME")) rate = 7.0;
        else if (loanType.equalsIgnoreCase("EDUCATION")) rate = 6.5;
        else if (loanType.equalsIgnoreCase("AUTO")) rate = 8.0;

        Loan loan = new Loan(nextLoanId(), accountNumber, loanType.toUpperCase(), amount, rate, tenureMonths);
        repository.addLoan(loan);

        // Disburse loan funds immediately into customer account
        account.deposit(amount, "Loan Disbursal Approved [" + loan.getLoanId() + " (" + loan.getLoanType() + ")]");
        repository.saveToDisk();

        return loan;
    }

    @Override
    public void repayLoanEmi(String accountNumber, String pin, String loanId) throws BankingException {
        Account account = getAccount(accountNumber);
        account.verifyPin(pin);

        List<Loan> customerLoans = repository.getLoansForAccount(accountNumber);
        Loan targetLoan = null;
        for (Loan l : customerLoans) {
            if (l.getLoanId().equalsIgnoreCase(loanId)) {
                targetLoan = l;
                break;
            }
        }

        if (targetLoan == null) {
            throw new BankingException("No loan found matching ID " + loanId + " for this account.");
        }
        if (targetLoan.isFullyRepaid()) {
            throw new BankingException("Loan " + loanId + " has already been fully repaid!");
        }

        double emi = Math.min(targetLoan.getMonthlyEmi(), targetLoan.getRemainingBalance());
        account.withdraw(emi, "Monthly Loan EMI Installment Repayment [" + targetLoan.getLoanId() + "]");
        targetLoan.repayInstallment(emi);
        repository.saveToDisk();
    }

    @Override
    public List<Loan> getCustomerLoans(String accountNumber) {
        return repository.getLoansForAccount(accountNumber);
    }

    @Override
    public List<Loan> getAllLoans() {
        return repository.getAllLoans();
    }
}
