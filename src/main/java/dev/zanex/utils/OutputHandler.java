package dev.zanex.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutputHandler {
    private final Map<Character, String> colors = new HashMap<>();

    public OutputHandler() {
        colors.put('0', "\u001B[30m"); // black
        colors.put('1', "\u001B[34m"); // dark_blue
        colors.put('2', "\u001B[32m"); // dark_green
        colors.put('3', "\u001B[36m"); // dark_aqua
        colors.put('4', "\u001B[31m"); // dark_red
        colors.put('5', "\u001B[35m"); // dark_purple
        colors.put('6', "\u001B[33m"); // gold
        colors.put('7', "\u001B[37m"); // gray
        colors.put('8', "\u001B[90m"); // dark_gray
        colors.put('9', "\u001B[94m"); // blue
        colors.put('a', "\u001B[92m"); // green
        colors.put('b', "\u001B[96m"); // aqua
        colors.put('c', "\u001B[91m"); // red
        colors.put('d', "\u001B[95m"); // light_purple
        colors.put('e', "\u001B[93m"); // yellow
        colors.put('f', "\u001B[97m"); // white
        colors.put('r', "\u001B[0m");  // reset
    }

    private String replaceColorCodes(String text) {
        for (Map.Entry<Character, String> entry : colors.entrySet()) {
            text = text.replace("&" + entry.getKey(), entry.getValue());
        }
        return text;
    }

    private String stripColorCodes(String text) {
        return text.replaceAll("&[0-9a-fr]", "");
    }

    public void println(String text) {
        System.out.println(replaceColorCodes(text));
    }

    public void print(String text) {
        System.out.print(replaceColorCodes(text));
    }

    public void printTable(String title, List<String> lines) {
        int maxContentLength = lines.stream()
                .mapToInt(line -> stripColorCodes(line).length())
                .max().orElse(0);

        int titleLength = stripColorCodes(title).length() + 4; // Including brackets [ ]
        int tableWidth = Math.max(maxContentLength, titleLength) + 4; // 4 for borders and padding

        String header = "&f╭" + "─".repeat((tableWidth - titleLength) / 2) + "&7[&b" + title + "&7]&f"
                + "─".repeat((tableWidth - titleLength + 1) / 2) + "╮";

        println(header);
        for (String line : lines) {
            String content = stripColorCodes(line);
            println("&f│ &7" + paddingRight(line, maxContentLength) + " &f│");
        }
        println("&f╰" + "─".repeat(tableWidth - 2) + "╯"); // -2 for the borders
    }

    private String paddingRight(String text, int length) {
        int visibleLength = stripColorCodes(text).length();
        return text + " ".repeat(length - visibleLength);
    }
}