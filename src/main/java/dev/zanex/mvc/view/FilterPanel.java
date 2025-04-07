package dev.zanex.mvc.view;

import dev.zanex.mvc.controller.AppController;
import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class FilterPanel extends JPanel {
    private AppController controller;

    private JXDatePicker fromDatePicker;
    private JXDatePicker toDatePicker;
    private JButton applyFilterButton;
    private JButton resetFilterButton;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public FilterPanel(AppController controller) {
        this.controller = controller;

        setBorder(BorderFactory.createTitledBorder("Zeitraum Filter"));
        setLayout(new GridBagLayout());

        initComponents();
    }

    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // From date
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        add(new JLabel("Von:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        fromDatePicker = new JXDatePicker();
        fromDatePicker.setDate(Date.from(LocalDate.now().withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant()));
        fromDatePicker.setFormats(new java.text.SimpleDateFormat("dd.MM.yyyy"));
        add(fromDatePicker, gbc);

        // To date
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        add(new JLabel("Bis:"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        toDatePicker = new JXDatePicker();
        toDatePicker.setDate(Date.from(LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault()).toInstant()));
        toDatePicker.setFormats(new java.text.SimpleDateFormat("dd.MM.yyyy"));
        add(toDatePicker, gbc);

        // Buttons
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.weightx = 0.0;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        applyFilterButton = new JButton("Filtern");
        applyFilterButton.addActionListener(this::applyFilter);
        buttonPanel.add(applyFilterButton);

        resetFilterButton = new JButton("Zurücksetzen");
        resetFilterButton.addActionListener(this::resetFilter);
        buttonPanel.add(resetFilterButton);

        add(buttonPanel, gbc);
    }

    private void applyFilter(ActionEvent e) {
        try {
            LocalDate fromDate = fromDatePicker.getDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate toDate = toDatePicker.getDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();

            if (fromDate.isAfter(toDate)) {
                throw new IllegalArgumentException("Das Von-Datum muss vor dem Bis-Datum liegen.");
            }

            controller.setDateFilter(fromDate, toDate);

            // Refresh parent frame
            refreshParentFrame();

        } catch (NullPointerException ex) {
            JOptionPane.showMessageDialog(this,
                    "Bitte wählen Sie gültige Datumsangaben aus.",
                    "Eingabefehler",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Eingabefehler",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetFilter(ActionEvent e) {
        fromDatePicker.setDate(Date.from(LocalDate.now().withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant()));
        toDatePicker.setDate(Date.from(LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault()).toInstant()));
        controller.resetDateFilter();

        // Refresh parent frame
        refreshParentFrame();
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

    public LocalDate getFromDate() {
        try {
            return fromDatePicker.getDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (Exception e) {
            return LocalDate.now().withDayOfMonth(1);
        }
    }

    public LocalDate getToDate() {
        try {
            return toDatePicker.getDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (Exception e) {
            return LocalDate.now();
        }
    }
}