package com.bank.model;

import java.io.IOException;

/**
 * Interface defining reporting and statement generation capabilities.
 */
public interface Reportable {
    String generateMiniStatement();
    String generateFullStatement();
    void exportStatementToFile(String filePath) throws IOException;
}
