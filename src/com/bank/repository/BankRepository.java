package com.bank.repository;

import com.bank.model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles in-memory storage and disk persistence for banking entities.
 * Automatically loads saved records or seeds sample accounts on initial run.
 */
public class BankRepository implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String DATA_FILE = "data/bank_data.dat";

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    private final Map<String, Customer> customers = new ConcurrentHashMap<>();
    private final List<Loan> loans = Collections.synchronizedList(new ArrayList<>());

    private static BankRepository instance;

    public static synchronized BankRepository getInstance() {
        if (instance == null) {
            instance = loadFromDisk();
        }
        return instance;
    }

    public BankRepository() {
        // Public constructor
    }

    public synchronized void addAccount(Account account) {
        accounts.put(account.getAccountNumber(), account);
        if (account.getCustomer() != null) {
            customers.put(account.getCustomer().getCustomerId(), account.getCustomer());
        }
        saveToDisk();
    }

    public Account getAccount(String accountNumber) {
        if (accountNumber == null) return null;
        return accounts.get(accountNumber.trim().toUpperCase());
    }

    public Collection<Account> getAllAccounts() {
        return Collections.unmodifiableCollection(accounts.values());
    }

    public synchronized boolean removeAccount(String accountNumber) {
        if (accountNumber == null) return false;
        Account removed = accounts.remove(accountNumber.trim().toUpperCase());
        if (removed != null) {
            saveToDisk();
            return true;
        }
        return false;
    }

    public synchronized void addLoan(Loan loan) {
        loans.add(loan);
        saveToDisk();
    }

    public List<Loan> getLoansForAccount(String accountNumber) {
        List<Loan> result = new ArrayList<>();
        synchronized (loans) {
            for (Loan l : loans) {
                if (l.getAccountNumber().equalsIgnoreCase(accountNumber)) {
                    result.add(l);
                }
            }
        }
        return result;
    }

    public List<Loan> getAllLoans() {
        return Collections.unmodifiableList(new ArrayList<>(loans));
    }

    public synchronized void saveToDisk() {
        try {
            Path path = Paths.get(DATA_FILE);
            if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
                oos.writeObject(new ArrayList<>(accounts.values()));
                oos.writeObject(new ArrayList<>(customers.values()));
                oos.writeObject(new ArrayList<>(loans));
            }
        } catch (IOException e) {
            System.err.println("[WARN] Failed to persist bank data to disk: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static BankRepository loadFromDisk() {
        BankRepository repo = new BankRepository();
        File file = new File(DATA_FILE);
        if (file.exists() && file.length() > 0) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                List<Account> accountList = (List<Account>) ois.readObject();
                List<Customer> customerList = (List<Customer>) ois.readObject();
                List<Loan> loanList = (List<Loan>) ois.readObject();

                for (Customer c : customerList) {
                    repo.customers.put(c.getCustomerId(), c);
                }
                for (Account a : accountList) {
                    repo.accounts.put(a.getAccountNumber(), a);
                }
                repo.loans.addAll(loanList);
                return repo;
            } catch (Exception e) {
                System.err.println("[WARN] Could not load persisted data file, initializing fresh demo data: " + e.getMessage());
            }
        }
        // Initialize rich demo dataset if empty or missing
        repo.seedDemoData();
        return repo;
    }

    private void seedDemoData() {
        // Customer 1: Diya Patel (Savings)
        Customer c1 = new Customer("CUST101", "Diya Patel", "pateldiya8821@gmail.com", "+91 9876543210", "IND-25CE076", "742 Evergreen Terrace");
        SavingsAccount sa = new SavingsAccount("ACC1001", c1, 15000.00, "1234", 4.5, 500.0, 6);
        try {
            sa.deposit(2500.00, "Salary Direct Deposit");
        } catch (com.bank.exception.BankingException ignored) {}
        accounts.put(sa.getAccountNumber(), sa);
        customers.put(c1.getCustomerId(), c1);

        // Customer 2: Alex Morgan (Checking with Overdraft)
        Customer c2 = new Customer("CUST102", "Alex Morgan", "alex.morgan@techcorp.com", "+1 555-0198", "US-SSN-9021", "100 Silicon Way, Tech City");
        CheckingAccount ca = new CheckingAccount("ACC1002", c2, 8500.00, "2345", 3000.0, 25.0);
        accounts.put(ca.getAccountNumber(), ca);
        customers.put(c2.getCustomerId(), c2);

        // Customer 3: Sarah Jenkins (Fixed Deposit)
        Customer c3 = new Customer("CUST103", "Sarah Jenkins", "sarah.j@investments.org", "+1 555-0842", "CAN-SIN-7731", "45 Wall Street, Financial Dist");
        FixedDepositAccount fda = new FixedDepositAccount("ACC1003", c3, 25000.00, "3456", 24, 7.5);
        accounts.put(fda.getAccountNumber(), fda);
        customers.put(c3.getCustomerId(), c3);

        // Seed a sample approved loan for Alex Morgan
        Loan sampleLoan = new Loan("LOAN-501", "ACC1002", "PERSONAL", 5000.00, 9.5, 12);
        loans.add(sampleLoan);

        saveToDisk();
    }
}
