package com.bank.model;

/**
 * Interface implemented by accounts capable of bearing interest.
 */
public interface InterestBearing {
    double calculateInterest();
    void applyInterest();
    double getInterestRate();
}
