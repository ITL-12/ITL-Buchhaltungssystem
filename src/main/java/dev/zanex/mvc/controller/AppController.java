package dev.zanex.mvc.controller;

import dev.zanex.Main;
import dev.zanex.mvc.model.TransactionType;
import dev.zanex.utils.ErrorHandler;
import dev.zanex.utils.MySQLHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AppController {
    private LocalDate fromDate;
    private LocalDate toDate;
    private MySQLHandler dbHandler;

    public AppController() {
        // Default filter is current month
        this.fromDate = LocalDate.now().withDayOfMonth(1);
        this.toDate = LocalDate.now();
        this.dbHandler = Main.getMySQLHandler();
    }

    public void setDateFilter(LocalDate fromDate, LocalDate toDate) {
        if (fromDate.isAfter(toDate)) {
            Main.getLogger().log("ERROR", "Invalid date range: " + fromDate + " to " + toDate);
            throw new IllegalArgumentException("From date must be before to date");
        }

        this.fromDate = fromDate;
        this.toDate = toDate;
        Main.getLogger().log("INFO", "Date filter set: " + fromDate + " to " + toDate);
    }

    public void resetDateFilter() {
        this.fromDate = LocalDate.now().withDayOfMonth(1);
        this.toDate = LocalDate.now();
        Main.getLogger().log("INFO", "Date filter reset to current month");
    }

    public List<Transaction> getTransactions() {
        List<Transaction> transactions = new ArrayList<>();

        try {
            Main.getLogger().log("INFO", "Fetching transactions from " + fromDate + " to " + toDate);
            Connection conn = dbHandler.getConnection();

            // Fix: Changed 'transactions.date' to 'transactions.transaction_date' to match the schema
            String query = "SELECT transactions.id, transactions.transaction_date, transactions.description, transactions.amount, categories.name as category FROM transactions JOIN categories ON transactions.category_id = categories.id WHERE transactions.transaction_date BETWEEN ? AND ? ORDER BY transactions.transaction_date DESC";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setObject(1, fromDate);
            stmt.setObject(2, toDate);

            ResultSet rs = stmt.executeQuery();
            int count = 0;

            while (rs.next()) {
                Transaction transaction = new Transaction(
                        rs.getInt("id"),
                        rs.getObject("transaction_date", LocalDate.class), // Also update here
                        rs.getString("description"),
                        rs.getDouble("amount"),
                        rs.getString("category")
                );
                transactions.add(transaction);
                count++;
            }

            Main.getLogger().log("INFO", "Retrieved " + count + " transactions");
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            Main.getLogger().log("ERROR", "Database error retrieving transactions: " + e.getMessage());
            ErrorHandler.showError(null, "Error retrieving transactions", e);
        }

        return transactions;
    }

    public void addTransaction(dev.zanex.mvc.model.Transaction transaction) {
        try {
            Main.getLogger().log("INFO", "Adding new transaction: " + transaction.getDescription());
            Connection conn = dbHandler.getConnection();

            // First get the default category ID for this transaction type
            int categoryId = getDefaultCategoryId(transaction.getType());

            String query = "INSERT INTO transactions (category_id, amount, description, transaction_date, created_at) " +
                    "VALUES (?, ?, ?, ?, NOW())";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, categoryId);
            stmt.setDouble(2, transaction.getAmount());
            stmt.setString(3, transaction.getDescription());
            stmt.setObject(4, transaction.getDate());

            int rowsAffected = stmt.executeUpdate();
            stmt.close();

            if (rowsAffected > 0) {
                Main.getLogger().log("INFO", "Transaction added successfully");
            } else {
                Main.getLogger().log("WARN", "No rows affected when adding transaction");
            }
        } catch (SQLException e) {
            Main.getLogger().log("ERROR", "Database error adding transaction: " + e.getMessage());
            ErrorHandler.showError(null, "Error adding transaction", e);
        }
    }

    public void updateTransaction(dev.zanex.mvc.model.Transaction transaction) {
        try {
            Main.getLogger().log("INFO", "Updating transaction ID " + transaction.getId());
            Connection conn = dbHandler.getConnection();

            // Get category ID for this transaction type if it's needed
            int categoryId = transaction.getId() > 0 ?
                    getCurrentCategoryId(transaction.getId()) :
                    getDefaultCategoryId(transaction.getType());

            String query = "UPDATE transactions SET category_id = ?, amount = ?, description = ?, " +
                    "transaction_date = ? WHERE id = ?";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, categoryId);
            stmt.setDouble(2, transaction.getAmount());
            stmt.setString(3, transaction.getDescription());
            stmt.setObject(4, transaction.getDate());
            stmt.setInt(5, transaction.getId());

            int rowsAffected = stmt.executeUpdate();
            stmt.close();

            if (rowsAffected > 0) {
                Main.getLogger().log("INFO", "Transaction updated successfully");
            } else {
                Main.getLogger().log("WARN", "No rows affected when updating transaction ID " + transaction.getId());
            }
        } catch (SQLException e) {
            Main.getLogger().log("ERROR", "Database error updating transaction: " + e.getMessage());
            ErrorHandler.showError(null, "Error updating transaction", e);
        }
    }

    // Helper method to get the default category ID for a transaction type
    private int getDefaultCategoryId(TransactionType type) throws SQLException {
        Connection conn = dbHandler.getConnection();
        String query = "SELECT id FROM categories WHERE type = ? LIMIT 1";

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, type.name());

        ResultSet rs = stmt.executeQuery();
        int categoryId = 1; // Default fallback

        if (rs.next()) {
            categoryId = rs.getInt("id");
        } else {
            Main.getLogger().log("WARN", "No default category found for type " + type + ", using ID 1");
        }

        rs.close();
        stmt.close();
        return categoryId;
    }

    // Helper method to get the current category ID for an existing transaction
    private int getCurrentCategoryId(int transactionId) throws SQLException {
        Connection conn = dbHandler.getConnection();
        String query = "SELECT category_id FROM transactions WHERE id = ?";

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, transactionId);

        ResultSet rs = stmt.executeQuery();
        int categoryId = 1; // Default fallback

        if (rs.next()) {
            categoryId = rs.getInt("category_id");
        }

        rs.close();
        stmt.close();
        return categoryId;
    }

    // Inner class to represent a transaction
    public static class Transaction {
        private int id;
        private LocalDate date;
        private String description;
        private double amount;
        private String category;

        public Transaction(int id, LocalDate date, String description, double amount, String category) {
            this.id = id;
            this.date = date;
            this.description = description;
            this.amount = amount;
            this.category = category;
        }

        public int getId() {
            return id;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getDescription() {
            return description;
        }

        public double getAmount() {
            return amount;
        }

        public String getCategory() {
            return category;
        }
    }
}