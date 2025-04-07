package dev.zanex.mvc.model;

import dev.zanex.Main;
import dev.zanex.utils.MySQLHandler;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {
    private final MySQLHandler mySQLHandler = Main.getMySQLHandler();

    public TransactionRepository() {
        // Constructor is intentionally empty
    }

    public void addTransaction(Transaction t) throws SQLException {
        String sql = "INSERT INTO transactions (type, amount, description, date, created_at) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = mySQLHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, t.getType().name());
            stmt.setDouble(2, t.getAmount());
            stmt.setString(3, t.getDescription());
            stmt.setDate(4, Date.valueOf(t.getDate()));
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating transaction failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    t.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating transaction failed, no ID obtained.");
                }
            }
        }
    }

    public void updateTransaction(Transaction t) throws SQLException {
        String sql = "UPDATE transactions SET type = ?, amount = ?, description = ?, date = ? WHERE id = ?";

        try (Connection conn = mySQLHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, t.getType().name());
            stmt.setDouble(2, t.getAmount());
            stmt.setString(3, t.getDescription());
            stmt.setDate(4, Date.valueOf(t.getDate()));
            stmt.setInt(5, t.getId());

            stmt.executeUpdate();
        }
    }

    public List<Transaction> getTransactions(LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT id, type, amount, description, date, created_at FROM transactions " +
                "WHERE date BETWEEN ? AND ? ORDER BY date DESC, id DESC";

        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = mySQLHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(from));
            stmt.setDate(2, Date.valueOf(to));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = new Transaction();
                    transaction.setId(rs.getInt("id"));
                    transaction.setType(TransactionType.valueOf(rs.getString("type")));
                    transaction.setAmount(rs.getDouble("amount"));
                    transaction.setDescription(rs.getString("description"));
                    transaction.setDate(rs.getDate("date").toLocalDate());
                    transaction.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                    transactions.add(transaction);
                }
            }
        }

        return transactions;
    }

    public void deleteTransactionIfToday(int id) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ? AND date = CURRENT_DATE()";

        try (Connection conn = mySQLHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Transaction could not be deleted. It may not exist or not be from today.");
            }
        }
    }

    public Transaction getTransactionById(int id) throws SQLException {
        String sql = "SELECT id, type, amount, description, date, created_at FROM transactions WHERE id = ?";

        try (Connection conn = mySQLHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Transaction transaction = new Transaction();
                    transaction.setId(rs.getInt("id"));
                    transaction.setType(TransactionType.valueOf(rs.getString("type")));
                    transaction.setAmount(rs.getDouble("amount"));
                    transaction.setDescription(rs.getString("description"));
                    transaction.setDate(rs.getDate("date").toLocalDate());
                    transaction.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                    return transaction;
                } else {
                    return null;
                }
            }
        }
    }

    public void deleteTransaction(int id) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ?";

        try (Connection conn = mySQLHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}