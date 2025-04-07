package dev.zanex.utils;

import dev.zanex.Main;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorHandler {
    /**
     * Shows an error dialog and logs the exception
     *
     * @param parent The parent component for the dialog
     * @param ex The exception to display and log
     */
    public static void showError(Component parent, Exception ex) {
        // Get stack trace as string
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String stackTrace = sw.toString();

        // Log the error
        Logger logger = Main.getLogger();
        if (logger != null) {
            logger.log("ERROR", ex.getMessage());
            logger.log("DEBUG", stackTrace);
        }

        // Show dialog to user
        JOptionPane.showMessageDialog(
                parent,
                "Ein Fehler ist aufgetreten: " + ex.getMessage(),
                "Fehler",
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Shows an error dialog with custom message and logs the exception
     *
     * @param parent The parent component for the dialog
     * @param message Custom error message to display
     * @param ex The exception to log
     */
    public static void showError(Component parent, String message, Exception ex) {
        // Get stack trace as string
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String stackTrace = sw.toString();

        // Log the error
        Logger logger = Main.getLogger();
        if (logger != null) {
            logger.log("ERROR", message);
            logger.log("DEBUG", stackTrace);
        }

        // Show dialog to user
        JOptionPane.showMessageDialog(
                parent,
                message,
                "Fehler",
                JOptionPane.ERROR_MESSAGE
        );
    }
}