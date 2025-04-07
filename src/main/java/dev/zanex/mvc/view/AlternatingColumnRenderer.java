package dev.zanex.mvc.view;

import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;

public class AlternatingColumnRenderer extends DefaultTableCellRenderer {
    private Color evenColumnColor = new Color(240, 240, 255); // Light blue
    private Color oddColumnColor = new Color(255, 255, 255);  // White

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Component component = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        if (!isSelected) {
            if (column % 2 == 0) {
                component.setBackground(evenColumnColor);
            } else {
                component.setBackground(oddColumnColor);
            }
        }

        return component;
    }

    // Optional: Add setters to customize colors
    public void setEvenColumnColor(Color color) {
        this.evenColumnColor = color;
    }

    public void setOddColumnColor(Color color) {
        this.oddColumnColor = color;
    }
}