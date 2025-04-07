package dev.zanex.mvc.view;

import dev.zanex.mvc.controller.AppController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class SummaryPanel extends JPanel {
    private final AppController controller;
    private final JLabel periodLabel;
    private final JLabel incomeValueLabel;
    private final JLabel expensesValueLabel;
    private final JLabel balanceValueLabel;
    private final NumberFormat currencyFormatter;
    private final DateTimeFormatter dateFormatter;

    public SummaryPanel(AppController controller) {
        this.controller = controller;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        this.dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Zusammenfassung"));

        // Period panel
        JPanel periodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        periodPanel.add(new JLabel("Zeitraum:"));
        periodLabel = new JLabel();
        periodPanel.add(periodLabel);
        add(periodPanel, BorderLayout.NORTH);

        // Summary grid
        JPanel summaryGrid = new JPanel(new GridLayout(3, 2, 10, 5));
        summaryGrid.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Income row
        JLabel incomeLabel = new JLabel("Einnahmen:");
        incomeLabel.setForeground(new Color(0, 128, 0)); // Green
        incomeLabel.setFont(incomeLabel.getFont().deriveFont(Font.BOLD));
        summaryGrid.add(incomeLabel);

        incomeValueLabel = new JLabel();
        incomeValueLabel.setForeground(new Color(0, 128, 0)); // Green
        incomeValueLabel.setFont(incomeValueLabel.getFont().deriveFont(Font.BOLD));
        incomeValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        summaryGrid.add(incomeValueLabel);

        // Expenses row
        JLabel expensesLabel = new JLabel("Ausgaben:");
        expensesLabel.setForeground(new Color(192, 0, 0)); // Red
        expensesLabel.setFont(expensesLabel.getFont().deriveFont(Font.BOLD));
        summaryGrid.add(expensesLabel);

        expensesValueLabel = new JLabel();
        expensesValueLabel.setForeground(new Color(192, 0, 0)); // Red
        expensesValueLabel.setFont(expensesValueLabel.getFont().deriveFont(Font.BOLD));
        expensesValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        summaryGrid.add(expensesValueLabel);

        // Balance row
        JLabel balanceLabel = new JLabel("Saldo:");
        balanceLabel.setFont(balanceLabel.getFont().deriveFont(Font.BOLD, 14f));
        summaryGrid.add(balanceLabel);

        balanceValueLabel = new JLabel();
        balanceValueLabel.setFont(balanceValueLabel.getFont().deriveFont(Font.BOLD, 14f));
        balanceValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        summaryGrid.add(balanceValueLabel);

        add(summaryGrid, BorderLayout.CENTER);
    }

    public void updateSummary(double income, double expenses, LocalDate fromDate, LocalDate toDate) {
        // Update period label
        String periodText = dateFormatter.format(fromDate) + " - " + dateFormatter.format(toDate);
        periodLabel.setText(periodText);

        // Update financial values
        incomeValueLabel.setText(currencyFormatter.format(income));
        expensesValueLabel.setText(currencyFormatter.format(expenses));

        double balance = income - expenses;
        balanceValueLabel.setText(currencyFormatter.format(balance));

        // Set balance color based on value
        if (balance < 0) {
            balanceValueLabel.setForeground(new Color(192, 0, 0)); // Red for negative
        } else {
            balanceValueLabel.setForeground(new Color(0, 128, 0)); // Green for positive or zero
        }

        // Repaint to show changes
        revalidate();
        repaint();
    }
}