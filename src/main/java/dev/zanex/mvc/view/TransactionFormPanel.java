package dev.zanex.mvc.view;

import dev.zanex.mvc.controller.AppController;
import dev.zanex.mvc.model.Transaction;
import dev.zanex.mvc.model.TransactionType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

public class TransactionFormPanel extends JPanel {
    private final AppController controller;
    private Transaction currentTransaction;
    private boolean isEditMode = false;

    // Form components
    private final JComboBox<TransactionType> typeComboBox;
    private final JSpinner dateSpinner;
    private final JTextField descriptionField;
    private final JTextField amountField;
    private final JButton saveButton;
    private final JButton cancelButton;

    public TransactionFormPanel(AppController controller) {
        this.controller = controller;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Transaktion"));

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Type selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Typ:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        typeComboBox = new JComboBox<>(TransactionType.values());
        formPanel.add(typeComboBox, gbc);

        // Date picker
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Datum:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateModel.setValue(new Date());
        dateSpinner = new JSpinner(dateModel);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd.MM.yyyy"));
        formPanel.add(dateSpinner, gbc);

        // Description field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Beschreibung:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        descriptionField = new JTextField(20);
        formPanel.add(descriptionField, gbc);

        // Amount field
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Betrag (€):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        amountField = new JTextField(10);
        formPanel.add(amountField, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        saveButton = new JButton("Speichern");
        saveButton.addActionListener(this::saveTransaction);
        buttonPanel.add(saveButton);

        cancelButton = new JButton("Abbrechen");
        cancelButton.addActionListener(e -> clearForm());
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Initialize with empty form
        clearForm();
    }

    public void setTransaction(Transaction transaction) {
        this.currentTransaction = transaction;
        this.isEditMode = transaction != null;

        if (transaction != null) {
            // Populate form for editing
            typeComboBox.setSelectedItem(transaction.getType());
            dateSpinner.setValue(Date.from(transaction.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            descriptionField.setText(transaction.getDescription());
            amountField.setText(String.format("%.2f", transaction.getAmount()));
            saveButton.setText("Aktualisieren");
        } else {
            clearForm();
        }
    }

    private void saveTransaction(ActionEvent e) {
        try {
            // Validate inputs
            if (descriptionField.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("Bitte geben Sie eine Beschreibung ein.");
            }

            double amount;
            try {
                // Replace comma with dot for decimal parsing
                String amountText = amountField.getText().replace(',', '.');
                amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Bitte geben Sie einen gültigen positiven Betrag ein.");
            }

            TransactionType type = (TransactionType) Objects.requireNonNull(typeComboBox.getSelectedItem());
            LocalDate date = ((Date) dateSpinner.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            String description = descriptionField.getText().trim();

            if (isEditMode && currentTransaction != null) {
                // Update existing transaction
                currentTransaction.setType(type);
                currentTransaction.setDate(date);
                currentTransaction.setDescription(description);
                currentTransaction.setAmount(amount);

                controller.updateTransaction(currentTransaction);
            } else {
                // Create new transaction
                Transaction newTransaction = new Transaction();
                newTransaction.setType(type);
                newTransaction.setDate(date);
                newTransaction.setDescription(description);
                newTransaction.setAmount(amount);

                controller.addTransaction(newTransaction);
            }

            // Reset form after saving
            clearForm();

            // Refresh parent frame
            refreshParentFrame();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Eingabefehler",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        currentTransaction = null;
        isEditMode = false;

        typeComboBox.setSelectedItem(TransactionType.EXPENSE);
        dateSpinner.setValue(new Date());
        descriptionField.setText("");
        amountField.setText("");
        saveButton.setText("Speichern");
    }

    private void refreshParentFrame() {
        // Find the parent MainFrame and refresh it
        Container parent = getParent();
        while (parent != null && !(parent instanceof MainFrame)) {
            parent = parent.getParent();
        }

        if (parent instanceof MainFrame) {
            ((MainFrame) parent).refreshTransactions();
        }
    }
}