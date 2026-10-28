package com.bank.ui;

import com.bank.exception.BankingException;
import com.bank.model.*;
import com.bank.service.BankService;
import com.bank.service.BankServiceImpl;
import com.bank.util.SecurityUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive Command Line Interface for Customers and Bank Administrators.
 */
public class BankCLI {

    private final BankService bankService;
    private final Scanner scanner;
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin123";

    public BankCLI() {
        this.bankService = new BankServiceImpl();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        while (running) {
            printHeader("APEX SECURE BANKING SYSTEM - (COURSE: 25CE076)");
            System.out.println(" 1.  Customer Banking Portal (Login with Account No)");
            System.out.println(" 2.  Bank Administrator Portal");
            System.out.println(" 3.  Financial Calculators (Loan EMI & Compound Interest)");
            System.out.println(" 4.  Demo Test Accounts & Quick Start Info");
            System.out.println(" 5.  OOP Architectural Overview (25CE076)");
            System.out.println(" 6.  Exit Application");
            System.out.println("==================================================================");
            int choice = readInt("Enter your selection (1-6): ", 1, 6);

            switch (choice) {
                case 1:
                    handleCustomerLogin();
                    break;
                case 2:
                    handleAdminLogin();
                    break;
                case 3:
                    handleCalculatorsMenu();
                    break;
                case 4:
                    displayDemoAccounts();
                    break;
                case 5:
                    displayOopArchitecture();
                    break;
                case 6:
                    running = false;
                    System.out.println("\nThank you for banking with Apex Secure Banking System. Goodbye!\n");
                    break;
            }
        }
    }

    // =========================================================================
    // CUSTOMER PORTAL
    // =========================================================================

    private void handleCustomerLogin() {
        printHeader("CUSTOMER AUTHENTICATION");
        System.out.print("Enter Account Number (e.g. ACC1001): ");
        String accNo = scanner.nextLine().trim();

        try {
            Account account = bankService.getAccount(accNo);
            System.out.print("Enter 4-digit Account PIN: ");
            String pin = scanner.nextLine().trim();

            // Verify PIN (raises InvalidPinException / AccountFrozenException if bad)
            account.verifyPin(pin);
            System.out.println("\n>>> Authentication Successful! Welcome, " + account.getCustomer().getFullName() + ".");
            customerSession(account, pin);
        } catch (BankingException e) {
            printError("Login Failed: " + e.getMessage());
        }
    }

    private void customerSession(Account account, String pin) {
        boolean active = true;
        while (active) {
            printHeader("CUSTOMER DASHBOARD: " + account.getCustomer().getFullName() + " [" + account.getAccountNumber() + "]");
            System.out.println(" 1.  View Account Summary & Balance");
            System.out.println(" 2.  Deposit Funds");
            System.out.println(" 3.  Withdraw Cash");
            System.out.println(" 4.  Transfer Funds (Inter-Account)");
            System.out.println(" 5.  Utility Bill Payment & Mobile Recharge");
            System.out.println(" 6.  Mini Statement (Recent 5 Transactions)");
            System.out.println(" 7.  Full Account Statement & Export to File");
            System.out.println(" 8.  Loan Management (Apply / View / Repay EMI)");
            System.out.println(" 9.  Interest & Maturity Projections");
            System.out.println(" 10. Change Account PIN");
            System.out.println(" 11. Logout");
            System.out.println("==================================================================");
            int choice = readInt("Select operation (1-11): ", 1, 11);

            switch (choice) {
                case 1:
                    viewAccountSummary(account);
                    break;
                case 2:
                    handleDeposit(account);
                    break;
                case 3:
                    pin = handleWithdrawal(account, pin);
                    break;
                case 4:
                    handleTransfer(account, pin);
                    break;
                case 5:
                    handleBillPayment(account, pin);
                    break;
                case 6:
                    System.out.println(account.generateMiniStatement());
                    pauseForEnter();
                    break;
                case 7:
                    handleStatementExport(account);
                    break;
                case 8:
                    handleLoanSubmenu(account, pin);
                    break;
                case 9:
                    displayInterestProjection(account);
                    break;
                case 10:
                    pin = handleChangePin(account, pin);
                    break;
                case 11:
                    active = false;
                    System.out.println("Logged out successfully from account " + account.getAccountNumber());
                    break;
            }
        }
    }

    private void viewAccountSummary(Account a) {
        printHeader("ACCOUNT SUMMARY");
        System.out.printf("  Account Number   : %s\n", a.getAccountNumber());
        System.out.printf("  Account Holder   : %s\n", a.getCustomer().getFullName());
        System.out.printf("  Account Type     : %s\n", a.getAccountType());
        System.out.printf("  Account Status   : %s\n", a.getStatus());
        System.out.printf("  Available Balance: %s\n", SecurityUtil.formatCurrency(a.getBalance()));
        if (a instanceof SavingsAccount) {
            SavingsAccount sa = (SavingsAccount) a;
            System.out.printf("  Annual Interest  : %.2f%%\n", sa.getInterestRate());
            System.out.printf("  Minimum Balance  : %s\n", SecurityUtil.formatCurrency(sa.getMinimumBalance()));
            System.out.printf("  Monthly Free Txns: %d used of %d limit\n", sa.getMonthlyWithdrawalCount(), sa.getMonthlyWithdrawalLimit());
        } else if (a instanceof CheckingAccount) {
            CheckingAccount ca = (CheckingAccount) a;
            System.out.printf("  Overdraft Limit  : %s\n", SecurityUtil.formatCurrency(ca.getOverdraftLimit()));
            System.out.printf("  Overdraft Fee    : %s\n", SecurityUtil.formatCurrency(ca.getOverdraftFee()));
        } else if (a instanceof FixedDepositAccount) {
            FixedDepositAccount fda = (FixedDepositAccount) a;
            System.out.printf("  Principal Locked : %s\n", SecurityUtil.formatCurrency(fda.getPrincipalAmount()));
            System.out.printf("  Tenure           : %d Months\n", fda.getTenureMonths());
            System.out.printf("  Interest Rate    : %.2f%%\n", fda.getInterestRate());
            System.out.printf("  Maturity Date    : %s\n", SecurityUtil.formatDateTime(fda.getMaturityDate()));
            System.out.printf("  Maturity Payout  : %s\n", SecurityUtil.formatCurrency(fda.calculateMaturityPayout()));
        }
        System.out.println("==================================================================");
        pauseForEnter();
    }

    private void handleDeposit(Account account) {
        printHeader("DEPOSIT FUNDS");
        double amount = readDouble("Enter deposit amount: $", 0.01, 10_000_000.0);
        System.out.print("Enter deposit remarks/reference (or press ENTER for default): ");
        String note = scanner.nextLine().trim();
        if (note.isEmpty()) note = "Counter Cash Deposit";

        try {
            bankService.deposit(account.getAccountNumber(), amount, note);
            printSuccess(String.format("Successfully deposited %s. New Balance: %s",
                    SecurityUtil.formatCurrency(amount), SecurityUtil.formatCurrency(account.getBalance())));
        } catch (BankingException e) {
            printError(e.getMessage());
        }
        pauseForEnter();
    }

    private String handleWithdrawal(Account account, String pin) {
        printHeader("WITHDRAW CASH");
        double amount = readDouble("Enter withdrawal amount: $", 0.01, 1_000_000.0);
        System.out.print("Confirm Account PIN: ");
        String enteredPin = scanner.nextLine().trim();

        try {
            bankService.withdraw(account.getAccountNumber(), enteredPin, amount, "ATM Cash Withdrawal");
            printSuccess(String.format("Successfully withdrew %s. Remaining Balance: %s",
                    SecurityUtil.formatCurrency(amount), SecurityUtil.formatCurrency(account.getBalance())));
        } catch (BankingException e) {
            printError(e.getMessage());
        }
        pauseForEnter();
        return pin;
    }

    private void handleTransfer(Account account, String pin) {
        printHeader("INTER-ACCOUNT FUNDS TRANSFER");
        System.out.print("Enter Beneficiary Destination Account Number (e.g. ACC1002): ");
        String destAccNo = scanner.nextLine().trim();
        double amount = readDouble("Enter transfer amount: $", 0.01, 1_000_000.0);
        System.out.print("Enter Transfer Purpose / Remarks: ");
        String remarks = scanner.nextLine().trim();
        System.out.print("Confirm Account PIN to Authorize: ");
        String enteredPin = scanner.nextLine().trim();

        try {
            bankService.transferFunds(account.getAccountNumber(), enteredPin, destAccNo, amount, remarks);
            printSuccess(String.format("Successfully transferred %s to %s. New Balance: %s",
                    SecurityUtil.formatCurrency(amount), destAccNo, SecurityUtil.formatCurrency(account.getBalance())));
        } catch (BankingException e) {
            printError(e.getMessage());
        }
        pauseForEnter();
    }

    private void handleBillPayment(Account account, String pin) {
        printHeader("UTILITY BILL PAYMENT & RECHARGE");
        System.out.println(" Select Utility Category:");
        System.out.println("  1. Electricity Bill");
        System.out.println("  2. Water Board Bill");
        System.out.println("  3. Mobile Prepaid/Postpaid Recharge");
        System.out.println("  4. High-Speed Internet / Fiber Broadband");
        System.out.println("  5. Highway Fastag Toll Recharge");
        int cat = readInt("Select option (1-5): ", 1, 5);

        String[] types = {"Electricity", "Water Board", "Mobile Recharge", "Broadband", "Fastag Toll"};
        String billType = types[cat - 1];

        System.out.print("Enter Biller / Provider Name (e.g. PowerCorp, AT&T, Jio): ");
        String biller = scanner.nextLine().trim();
        System.out.print("Enter Customer Consumer ID / Reference Number: ");
        String refNo = scanner.nextLine().trim();
        double amount = readDouble("Enter payment amount: $", 1.0, 50000.0);
        System.out.print("Confirm Account PIN to Pay: ");
        String enteredPin = scanner.nextLine().trim();

        try {
            bankService.payBill(account.getAccountNumber(), enteredPin, biller, billType, amount, refNo);
            printSuccess(String.format("Payment of %s for %s (%s) processed successfully!\nTransaction ID generated.",
                    SecurityUtil.formatCurrency(amount), billType, biller));
        } catch (BankingException e) {
            printError(e.getMessage());
        }
        pauseForEnter();
    }

    private void handleStatementExport(Account account) {
        printHeader("ACCOUNT STATEMENT");
        System.out.println(account.generateFullStatement());

        System.out.print("Would you like to export this statement to a text file? (y/n): ");
        String ans = scanner.nextLine().trim().toLowerCase();
        if (ans.startsWith("y")) {
            String fileName = "statement_" + account.getAccountNumber() + ".txt";
            try {
                account.exportStatementToFile(fileName);
                File f = new File(fileName);
                printSuccess("Statement successfully exported to file: " + f.getAbsolutePath());
            } catch (IOException e) {
                printError("Could not export statement: " + e.getMessage());
            }
        }
        pauseForEnter();
    }

    private void handleLoanSubmenu(Account account, String pin) {
        printHeader("LOAN SERVICES DASHBOARD");
        System.out.println(" 1. View My Active Loans");
        System.out.println(" 2. Apply for New Loan");
        System.out.println(" 3. Repay Monthly Loan EMI");
        System.out.println(" 4. Back to Dashboard");
        int sub = readInt("Select option (1-4): ", 1, 4);

        if (sub == 1) {
            List<Loan> myLoans = bankService.getCustomerLoans(account.getAccountNumber());
            if (myLoans.isEmpty()) {
                System.out.println("\nNo loans currently associated with this account.\n");
            } else {
                System.out.println("\n--- Active Loan Records ---");
                for (Loan l : myLoans) {
                    System.out.println(l);
                }
            }
            pauseForEnter();
        } else if (sub == 2) {
            System.out.println("\nLoan Categories available: PERSONAL, HOME, EDUCATION, AUTO");
            System.out.print("Enter Desired Loan Type: ");
            String type = scanner.nextLine().trim().toUpperCase();
            double amount = readDouble("Enter Loan Principal Amount: $", 500.0, 1000000.0);
            int tenure = readInt("Enter Tenure in Months (6 - 120): ", 6, 120);

            try {
                Loan approvedLoan = bankService.applyLoan(account.getAccountNumber(), type, amount, tenure);
                printSuccess("Loan Application Approved!");
                System.out.printf("  Loan ID       : %s\n", approvedLoan.getLoanId());
                System.out.printf("  Principal     : %s\n", SecurityUtil.formatCurrency(approvedLoan.getPrincipalAmount()));
                System.out.printf("  Interest Rate : %.2f%% p.a.\n", approvedLoan.getAnnualInterestRate());
                System.out.printf("  Monthly EMI   : %s\n", SecurityUtil.formatCurrency(approvedLoan.getMonthlyEmi()));
                System.out.printf("  Tenure        : %d Months\n", approvedLoan.getTenureMonths());
                System.out.println("Funds have been disbursed into your account balance.");
            } catch (BankingException e) {
                printError(e.getMessage());
            }
            pauseForEnter();
        } else if (sub == 3) {
            List<Loan> myLoans = bankService.getCustomerLoans(account.getAccountNumber());
            if (myLoans.isEmpty()) {
                System.out.println("\nNo active loans found to repay.\n");
            } else {
                System.out.println("\nSelect Loan to Repay EMI:");
                for (int i = 0; i < myLoans.size(); i++) {
                    System.out.printf("  [%d] %s (Remaining: %s, EMI: %s)\n",
                            i + 1, myLoans.get(i).getLoanId(),
                            SecurityUtil.formatCurrency(myLoans.get(i).getRemainingBalance()),
                            SecurityUtil.formatCurrency(myLoans.get(i).getMonthlyEmi()));
                }
                int idx = readInt("Select loan index: ", 1, myLoans.size()) - 1;
                Loan target = myLoans.get(idx);

                System.out.print("Confirm Account PIN to Deduct EMI: ");
                String enteredPin = scanner.nextLine().trim();

                try {
                    bankService.repayLoanEmi(account.getAccountNumber(), enteredPin, target.getLoanId());
                    printSuccess(String.format("EMI payment of %s deducted successfully! Remaining Loan Balance: %s",
                            SecurityUtil.formatCurrency(target.getMonthlyEmi()),
                            SecurityUtil.formatCurrency(target.getRemainingBalance())));
                } catch (BankingException e) {
                    printError(e.getMessage());
                }
            }
            pauseForEnter();
        }
    }

    private void displayInterestProjection(Account account) {
        printHeader("INTEREST & GROWTH PROJECTION");
        if (account instanceof InterestBearing) {
            InterestBearing ib = (InterestBearing) account;
            double rate = ib.getInterestRate();
            double projectedMonthly = ib.calculateInterest();
            System.out.printf("  Nominal Interest Rate : %.2f%% per annum\n", rate);
            System.out.printf("  Monthly Interest Yield: %s\n", SecurityUtil.formatCurrency(projectedMonthly));
            System.out.printf("  Annual Interest Yield : %s (Simple estimate)\n", SecurityUtil.formatCurrency(projectedMonthly * 12));
            if (account instanceof FixedDepositAccount) {
                FixedDepositAccount fda = (FixedDepositAccount) account;
                System.out.printf("  Fixed Deposit Maturity Payout: %s on %s\n",
                        SecurityUtil.formatCurrency(fda.calculateMaturityPayout()),
                        SecurityUtil.formatDateTime(fda.getMaturityDate()));
            }
        } else {
            System.out.println("Checking / Current accounts do not earn interest on deposits.");
            System.out.println("Interest is applicable on Savings and Fixed Deposit accounts.");
        }
        pauseForEnter();
    }

    private String handleChangePin(Account account, String currentPin) {
        printHeader("CHANGE ACCOUNT PIN");
        System.out.print("Enter Current PIN: ");
        String oldPin = scanner.nextLine().trim();
        System.out.print("Enter New 4-digit PIN: ");
        String newPin = scanner.nextLine().trim();
        System.out.print("Confirm New 4-digit PIN: ");
        String confirmPin = scanner.nextLine().trim();

        if (!newPin.equals(confirmPin)) {
            printError("New PIN entries do not match.");
            pauseForEnter();
            return currentPin;
        }

        try {
            bankService.changePin(account.getAccountNumber(), oldPin, newPin);
            printSuccess("Account PIN updated successfully! Remember to use your new PIN for future logins.");
            pauseForEnter();
            return newPin;
        } catch (BankingException e) {
            printError(e.getMessage());
            pauseForEnter();
            return currentPin;
        }
    }

    // =========================================================================
    // ADMIN PORTAL
    // =========================================================================

    private void handleAdminLogin() {
        printHeader("BANK ADMINISTRATOR AUTHENTICATION");
        System.out.print("Enter Admin Username: ");
        String user = scanner.nextLine().trim();
        System.out.print("Enter Admin Password: ");
        String pass = scanner.nextLine().trim();

        if (ADMIN_USER.equalsIgnoreCase(user) && ADMIN_PASS.equals(pass)) {
            printSuccess("Admin Authentication Verified! Welcome, System Administrator.");
            adminSession();
        } else {
            printError("Authentication Failed: Invalid admin credentials.");
            pauseForEnter();
        }
    }

    private void adminSession() {
        boolean active = true;
        while (active) {
            printHeader("ADMINISTRATOR EXECUTIVE CONTROL PANEL");
            System.out.println(" 1.  Open New Customer Account (Savings, Checking, FD)");
            System.out.println(" 2.  View All Bank Accounts & Vault Reserves");
            System.out.println(" 3.  Search Account (By Name, Account No, or Type)");
            System.out.println(" 4.  Freeze Customer Account");
            System.out.println(" 5.  Unfreeze Customer Account");
            System.out.println(" 6.  Unlock Locked Account (Reset Failed Attempts)");
            System.out.println(" 7.  Accrue & Distribute System-Wide Monthly Interest");
            System.out.println(" 8.  Generate Official Central Audit Report");
            System.out.println(" 9.  Inspect All Disbursed Bank Loans");
            System.out.println(" 10. Close / Deactivate Account");
            System.out.println(" 11. Logout to Main Menu");
            System.out.println("==================================================================");
            int choice = readInt("Select administrative operation (1-11): ", 1, 11);

            switch (choice) {
                case 1:
                    handleOpenNewAccount();
                    break;
                case 2:
                    handleViewAllAccounts();
                    break;
                case 3:
                    handleSearchAccounts();
                    break;
                case 4:
                    handleFreezeAccount();
                    break;
                case 5:
                    handleUnfreezeAccount();
                    break;
                case 6:
                    handleUnlockAccount();
                    break;
                case 7:
                    bankService.applySystemWideInterest();
                    printSuccess("System-wide monthly maintenance and interest accrual completed!");
                    pauseForEnter();
                    break;
                case 8:
                    System.out.println(bankService.generateAuditReport());
                    pauseForEnter();
                    break;
                case 9:
                    handleViewAllLoans();
                    break;
                case 10:
                    handleCloseAccount();
                    break;
                case 11:
                    active = false;
                    System.out.println("Admin logged out successfully.");
                    break;
            }
        }
    }

    private void handleOpenNewAccount() {
        printHeader("OPEN NEW BANK ACCOUNT");
        System.out.println(" Choose Account Classification:");
        System.out.println("  1. Savings Account (Interest, Min Balance)");
        System.out.println("  2. Checking Account (Overdraft Protection)");
        System.out.println("  3. Fixed Deposit Account (Term Investment)");
        int typeChoice = readInt("Selection (1-3): ", 1, 3);

        System.out.print("Customer Full Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Customer Email Address: ");
        String email = scanner.nextLine().trim();
        System.out.print("Customer Phone Number: ");
        String phone = scanner.nextLine().trim();
        System.out.print("Customer Government ID / Aadhaar / SSN: ");
        String nid = scanner.nextLine().trim();
        System.out.print("Customer Address: ");
        String address = scanner.nextLine().trim();
        System.out.print("Initial 4-digit PIN: ");
        String pin = scanner.nextLine().trim();

        try {
            if (typeChoice == 1) {
                double deposit = readDouble("Initial Deposit ($500 min): $", 500.0, 10000000.0);
                SavingsAccount sa = bankService.openSavingsAccount(name, email, phone, nid, address, deposit, pin, 4.5, 500.0);
                printSuccess("Savings Account Created Successfully! Account Number: " + sa.getAccountNumber());
            } else if (typeChoice == 2) {
                double deposit = readDouble("Initial Deposit ($0+): $", 0.0, 10000000.0);
                double overdraft = readDouble("Approved Overdraft Facility: $", 500.0, 50000.0);
                CheckingAccount ca = bankService.openCheckingAccount(name, email, phone, nid, address, deposit, pin, overdraft);
                printSuccess("Checking Account Created Successfully! Account Number: " + ca.getAccountNumber());
            } else {
                double deposit = readDouble("Initial FD Principal ($1000 min): $", 1000.0, 10000000.0);
                int months = readInt("Tenure in Months (3 - 120): ", 3, 120);
                FixedDepositAccount fda = bankService.openFixedDepositAccount(name, email, phone, nid, address, deposit, pin, months, 7.5);
                printSuccess("Fixed Deposit Account Created Successfully! Account Number: " + fda.getAccountNumber());
            }
        } catch (BankingException e) {
            printError(e.getMessage());
        }
        pauseForEnter();
    }

    private void handleViewAllAccounts() {
        printHeader("ALL REGISTERED BANK ACCOUNTS");
        Collection<Account> accounts = bankService.getAllAccounts();
        System.out.printf("Total Vault Reserves: %s across %d accounts\n\n",
                SecurityUtil.formatCurrency(bankService.getTotalBankDeposits()), accounts.size());
        for (Account a : accounts) {
            System.out.println(a);
        }
        pauseForEnter();
    }

    private void handleSearchAccounts() {
        printHeader("SEARCH ACCOUNTS");
        System.out.print("Enter Account Number, Customer Name, or Type: ");
        String q = scanner.nextLine().trim();
        List<Account> results = bankService.searchAccounts(q);
        if (results.isEmpty()) {
            System.out.println("No matching accounts found for query: " + q);
        } else {
            System.out.printf("Found %d matching account(s):\n", results.size());
            for (Account a : results) {
                System.out.println(a);
            }
        }
        pauseForEnter();
    }

    private void handleFreezeAccount() {
        printHeader("FREEZE ACCOUNT");
        System.out.print("Enter Account Number to Freeze: ");
        String acc = scanner.nextLine().trim();
        try {
            bankService.freezeAccount(acc);
            printSuccess("Account " + acc + " has been successfully FROZEN.");
        } catch (BankingException e) {
            printError(e.getMessage());
        }
        pauseForEnter();
    }

    private void handleUnfreezeAccount() {
        printHeader("UNFREEZE ACCOUNT");
        System.out.print("Enter Account Number to Unfreeze: ");
        String acc = scanner.nextLine().trim();
        try {
            bankService.unfreezeAccount(acc);
            printSuccess("Account " + acc + " has been UNFROZEN and restored to ACTIVE.");
        } catch (BankingException e) {
            printError(e.getMessage());
        }
        pauseForEnter();
    }

    private void handleUnlockAccount() {
        printHeader("UNLOCK LOCKED ACCOUNT");
        System.out.print("Enter Account Number to Unlock: ");
        String acc = scanner.nextLine().trim();
        try {
            bankService.unlockAccount(acc);
            printSuccess("Account " + acc + " unlocked. Failed attempts counter reset to 0.");
        } catch (BankingException e) {
            printError(e.getMessage());
        }
        pauseForEnter();
    }

    private void handleViewAllLoans() {
        printHeader("CENTRAL LOAN DISBURSAL REGISTRY");
        List<Loan> loans = bankService.getAllLoans();
        if (loans.isEmpty()) {
            System.out.println("No loans recorded in the registry.");
        } else {
            for (Loan l : loans) {
                System.out.println(l);
            }
        }
        pauseForEnter();
    }

    private void handleCloseAccount() {
        printHeader("CLOSE ACCOUNT");
        System.out.print("Enter Account Number to Close: ");
        String acc = scanner.nextLine().trim();
        System.out.print("Are you sure you want to permanently close account " + acc + "? (yes/no): ");
        String confirm = scanner.nextLine().trim();
        if (confirm.equalsIgnoreCase("yes")) {
            try {
                bankService.closeAccount(acc);
                printSuccess("Account " + acc + " closed successfully.");
            } catch (BankingException e) {
                printError(e.getMessage());
            }
        } else {
            System.out.println("Action cancelled.");
        }
        pauseForEnter();
    }

    // =========================================================================
    // HELPER TOOLS & PUBLIC MENUS
    // =========================================================================

    private void handleCalculatorsMenu() {
        printHeader("FINANCIAL CALCULATORS");
        System.out.println(" 1. Loan EMI Amortization Calculator");
        System.out.println(" 2. Compound Interest / Fixed Deposit Maturity Calculator");
        System.out.println(" 3. Back to Main Menu");
        int sub = readInt("Select option (1-3): ", 1, 3);

        if (sub == 1) {
            printHeader("LOAN EMI CALCULATOR");
            double principal = readDouble("Enter Principal Loan Amount: $", 100.0, 10000000.0);
            double annualRate = readDouble("Enter Annual Interest Rate (e.g. 8.5): ", 0.1, 50.0);
            int months = readInt("Enter Loan Tenure in Months: ", 1, 360);

            double emi = Loan.calculateEmi(principal, annualRate, months);
            double totalRepayment = emi * months;
            double totalInterest = totalRepayment - principal;

            System.out.println("\n--------------------------------------------------");
            System.out.printf("  Monthly EMI Payable  : %s\n", SecurityUtil.formatCurrency(emi));
            System.out.printf("  Total Interest Cost  : %s\n", SecurityUtil.formatCurrency(totalInterest));
            System.out.printf("  Total Repayment Sum  : %s\n", SecurityUtil.formatCurrency(totalRepayment));
            System.out.println("--------------------------------------------------");
            pauseForEnter();
        } else if (sub == 2) {
            printHeader("COMPOUND INTEREST CALCULATOR");
            double principal = readDouble("Enter Principal Investment: $", 100.0, 10000000.0);
            double annualRate = readDouble("Enter Annual Interest Rate (e.g. 7.5): ", 0.1, 50.0);
            int months = readInt("Enter Tenure in Months: ", 1, 360);

            double r = annualRate / 100.0;
            double t = months / 12.0;
            double maturity = principal * Math.pow(1 + (r / 4.0), 4 * t);
            double earned = maturity - principal;

            System.out.println("\n--------------------------------------------------");
            System.out.printf("  Total Interest Earned: %s\n", SecurityUtil.formatCurrency(earned));
            System.out.printf("  Maturity Payout Value: %s\n", SecurityUtil.formatCurrency(maturity));
            System.out.println("--------------------------------------------------");
            pauseForEnter();
        }
    }

    private void displayDemoAccounts() {
        printHeader("DEMO TEST ACCOUNTS FOR INSTANT EVALUATION");
        System.out.println(" You can test all features right away using these pre-seeded accounts:");
        System.out.println(" ------------------------------------------------------------------");
        System.out.println(" 1. SAVINGS ACCOUNT:");
        System.out.println("    Account Number : ACC1001");
        System.out.println("    Account Holder : Diya Patel (25CE076)");
        System.out.println("    PIN            : 1234");
        System.out.println("    Balance        : $17,500.00 (Min Balance: $500, Rate: 4.5%)");
        System.out.println();
        System.out.println(" 2. CHECKING ACCOUNT (WITH OVERDRAFT):");
        System.out.println("    Account Number : ACC1002");
        System.out.println("    Account Holder : Alex Morgan");
        System.out.println("    PIN            : 2345");
        System.out.println("    Balance        : $8,500.00 (Overdraft Facility: $3,000.00)");
        System.out.println();
        System.out.println(" 3. FIXED DEPOSIT ACCOUNT:");
        System.out.println("    Account Number : ACC1003");
        System.out.println("    Account Holder : Sarah Jenkins");
        System.out.println("    PIN            : 3456");
        System.out.println("    Principal      : $25,000.00 (Tenure: 24 Months @ 7.5%)");
        System.out.println();
        System.out.println(" 4. BANK ADMINISTRATOR LOGIN:");
        System.out.println("    Username       : admin");
        System.out.println("    Password       : admin123");
        System.out.println(" ------------------------------------------------------------------");
        pauseForEnter();
    }

    private void displayOopArchitecture() {
        printHeader("OBJECT-ORIENTED PROGRAMMING DESIGN (25CE076)");
        System.out.println(" 1. ENCAPSULATION:");
        System.out.println("    - Private state (balance, PIN hashes, transaction logs).");
        System.out.println("    - Cryptographic SHA-256 PIN hashing; PINs are never stored in plain text.");
        System.out.println("    - Strict business validation before any state mutation.");
        System.out.println();
        System.out.println(" 2. INHERITANCE:");
        System.out.println("    - Abstract Base Class: Account");
        System.out.println("    - Derived Subclasses: SavingsAccount, CheckingAccount, FixedDepositAccount.");
        System.out.println("    - Reuses core account identity, transaction recording, and reporting logic.");
        System.out.println();
        System.out.println(" 3. POLYMORPHISM:");
        System.out.println("    - Runtime Polymorphism: Dynamic method dispatch for withdraw(),");
        System.out.println("      getAccountType(), and applyMonthlyMaintenanceOrInterest().");
        System.out.println("    - Compile-time Polymorphism: Method overloading on deposit(), withdraw(),");
        System.out.println("      transferFunds(), and constructor variations.");
        System.out.println();
        System.out.println(" 4. ABSTRACTION & INTERFACES:");
        System.out.println("    - Transactable: Defines core financial transaction contract.");
        System.out.println("    - Reportable: Defines statement generation and file export contracts.");
        System.out.println("    - InterestBearing: Implemented by Savings and FixedDeposit accounts.");
        System.out.println("    - BankService: Decouples business rules from UI display.");
        System.out.println(" ==================================================================");
        pauseForEnter();
    }

    // =========================================================================
    // CONSOLE UTILITIES
    // =========================================================================

    private void printHeader(String title) {
        System.out.println("\n==================================================================");
        System.out.println("   " + title);
        System.out.println("==================================================================");
    }

    private void printSuccess(String msg) {
        System.out.println("\n[SUCCESS] " + msg);
    }

    private void printError(String msg) {
        System.out.println("\n[ERROR] " + msg);
    }

    private void pauseForEnter() {
        System.out.print("\nPress ENTER to continue...");
        scanner.nextLine();
    }

    private int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(input);
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.printf("Please enter a valid number between %d and %d.\n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid numeric input. Please try again.");
            }
        }
    }

    private double readDouble(String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                double val = Double.parseDouble(input);
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.printf("Please enter a valid value between %.2f and %.2f.\n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid decimal number. Please try again.");
            }
        }
    }
}
