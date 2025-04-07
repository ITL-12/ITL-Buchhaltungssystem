package dev.zanex.mvc.view;

import dev.zanex.Main;
import dev.zanex.mvc.controller.AppController;
import dev.zanex.mvc.model.Transaction;
import dev.zanex.mvc.model.TransactionType;
import dev.zanex.utils.ErrorHandler;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class MainFrame extends JFrame {
    private final AppController controller;
    private JTable transactionsTable;
    private TransactionTableModel tableModel;
    private FilterPanel filterPanel;
    private TransactionFormPanel transactionFormPanel;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    public MainFrame(AppController controller) {
        this.controller = controller;

        setTitle("Transaktionsverwaltung");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        if(Main.getRunArgs().contains("debug")) {
            Main.getLogger().log("DEBUG", "Running in debug mode");
            setAlwaysOnTop(true);
            setResizable(false);
        }

        initComponents();
    }

    private void initComponents() {
        // Create main layout
        setLayout(new BorderLayout());

        // Top panel with filter
        filterPanel = new FilterPanel(controller);
        add(filterPanel, BorderLayout.NORTH);

        // Create transaction form panel (right side)
        transactionFormPanel = new TransactionFormPanel(controller);

        // Create table model and table for transactions
        tableModel = new TransactionTableModel();
        transactionsTable = new JTable(tableModel);
        setupTable();

        // Create a split pane with table and form
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(transactionsTable), transactionFormPanel);
        splitPane.setDividerLocation(650);
        add(splitPane, BorderLayout.CENTER);

        // Add button panel at the bottom
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);

        // Load initial data
        refreshTransactions();
    }

    private void setupTable() {
        // Set table appearance and behavior
        transactionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionsTable.setRowHeight(25);
        transactionsTable.getTableHeader().setReorderingAllowed(false);

        // Custom renderers for currency and date columns
        transactionsTable.getColumnModel().getColumn(1).setCellRenderer(new DateCellRenderer());
        transactionsTable.getColumnModel().getColumn(3).setCellRenderer(new CurrencyCellRenderer());

        // Add selection listener
        transactionsTable.getSelectionModel().addListSelectionListener(this::onTableSelectionChanged);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton newButton = new JButton("Neue Transaktion");
        newButton.addActionListener(this::createNewTransaction);
        buttonPanel.add(newButton);

        JButton editButton = new JButton("Bearbeiten");
        editButton.addActionListener(this::editSelectedTransaction);
        buttonPanel.add(editButton);

        JButton deleteButton = new JButton("Löschen");
        deleteButton.addActionListener(this::deleteSelectedTransaction);
        buttonPanel.add(deleteButton);

        JButton refreshButton = new JButton("Aktualisieren");
        refreshButton.addActionListener(e -> refreshTransactions());
        buttonPanel.add(refreshButton);

        return buttonPanel;
    }

    private void onTableSelectionChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            int selectedRow = transactionsTable.getSelectedRow();
            if (selectedRow >= 0) {
                // Convert to model index in case of sorting
                int modelRow = transactionsTable.convertRowIndexToModel(selectedRow);
                AppController.Transaction selectedTransaction = tableModel.getTransactionAt(modelRow);

                // Convert controller transaction to model transaction for editing
                Transaction modelTransaction = convertToModelTransaction(selectedTransaction);
                transactionFormPanel.setTransaction(modelTransaction);
            }
        }
    }

    private Transaction convertToModelTransaction(AppController.Transaction ctrlTransaction) {
        Transaction transaction = new Transaction();
        transaction.setId(ctrlTransaction.getId());
        transaction.setDate(ctrlTransaction.getDate());
        transaction.setDescription(ctrlTransaction.getDescription());
        transaction.setAmount(ctrlTransaction.getAmount());

        // Determine transaction type from category or amount
        if (ctrlTransaction.getAmount() > 0) {
            transaction.setType(TransactionType.INCOME);
        } else {
            transaction.setType(TransactionType.EXPENSE);
        }

        return transaction;
    }

    private void createNewTransaction(ActionEvent e) {
        transactionFormPanel.setTransaction(null); // Clear form for new transaction
        transactionsTable.clearSelection();
    }

    private void editSelectedTransaction(ActionEvent e) {
        int selectedRow = transactionsTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = transactionsTable.convertRowIndexToModel(selectedRow);
            AppController.Transaction selectedTransaction = tableModel.getTransactionAt(modelRow);
            Transaction modelTransaction = convertToModelTransaction(selectedTransaction);
            transactionFormPanel.setTransaction(modelTransaction);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Bitte wählen Sie eine Transaktion aus der Liste aus.",
                    "Keine Auswahl",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelectedTransaction(ActionEvent e) {
        int selectedRow = transactionsTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = transactionsTable.convertRowIndexToModel(selectedRow);
            AppController.Transaction transaction = tableModel.getTransactionAt(modelRow);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Möchten Sie die Transaktion '" + transaction.getDescription() + "' wirklich löschen?",
                    "Transaktion löschen",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    // Delete transaction and refresh
                    // This would need to be implemented in your controller
                    // controller.deleteTransaction(transaction.getId());
                    JOptionPane.showMessageDialog(this,
                            "Diese Funktion ist noch nicht implementiert.",
                            "Information",
                            JOptionPane.INFORMATION_MESSAGE);
                    refreshTransactions();
                } catch (Exception ex) {
                    ErrorHandler.showError(this, "Fehler beim Löschen der Transaktion", ex);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Bitte wählen Sie eine Transaktion aus der Liste aus.",
                    "Keine Auswahl",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void refreshTransactions() {
        try {
            Main.getLogger().log("INFO", "Refreshing transaction display");
            // Fetch transactions using controller and update table model
            List<AppController.Transaction> transactions = controller.getTransactions();
            tableModel.setTransactions(transactions);
            Main.getLogger().log("SUCCESS", "Transaction display updated with " + transactions.size() + " records");
        } catch (Exception e) {
            ErrorHandler.showError(this, "Failed to refresh transactions", e);
        }
    }

    // Inner class for the table model
    private static class TransactionTableModel extends AbstractTableModel {
        private List<AppController.Transaction> transactions;
        private final String[] columnNames = {"ID", "Datum", "Beschreibung", "Betrag", "Kategorie"};

        public void setTransactions(List<AppController.Transaction> transactions) {
            this.transactions = transactions;
            fireTableDataChanged();
        }

        public AppController.Transaction getTransactionAt(int row) {
            return transactions.get(row);
        }

        @Override
        public int getRowCount() {
            return transactions == null ? 0 : transactions.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (transactions == null || transactions.isEmpty()) {
                return Object.class;
            }

            return switch (columnIndex) {
                case 0 -> Integer.class;
                case 1 -> LocalDate.class;
                case 3 -> Double.class;
                default -> String.class;
            };
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (transactions == null || rowIndex >= transactions.size()) {
                return null;
            }

            AppController.Transaction transaction = transactions.get(rowIndex);

            return switch (columnIndex) {
                case 0 -> transaction.getId();
                case 1 -> transaction.getDate();
                case 2 -> transaction.getDescription();
                case 3 -> transaction.getAmount();
                case 4 -> transaction.getCategory();
                default -> null;
            };
        }
    }

    // Custom renderer for date cells
    private class DateCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value instanceof LocalDate date) {
                setText(dateFormatter.format(date));
            }

            return this;
        }
    }

    // Custom renderer for currency cells
    private class CurrencyCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value instanceof Double amount) {
                setText(currencyFormat.format(amount));

                // Color positive/negative values
                if (!isSelected) {
                    setForeground(amount >= 0 ? new Color(0, 128, 0) : new Color(192, 0, 0));
                }
            }

            setHorizontalAlignment(SwingConstants.RIGHT);
            return this;
        }
    }
}