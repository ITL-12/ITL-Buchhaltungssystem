package dev.zanex.utils;

import java.util.List;
import java.util.Map;

public class Logger {
    private static OutputHandler outputHandler;

    private final Map<String, String> prefixes = Map.of(
            "INFO", "&b(i) &a» &f",
            "ERROR", "&c(✘) &a» &f",
            "DEBUG", "&9(➤) &a» &f",
            "WARN", "&e(⚠) &a» &f",
            "SUCCESS", "&a(✔) &a» &f"
    );

    public Logger() {
        outputHandler = new OutputHandler();
        outputHandler.println(prefixes.get("INFO") + "Logger initialized.");
    }

    public void log(String level, String message) {
        String prefix = prefixes.getOrDefault(level.toUpperCase(), "&f");
        outputHandler.println(prefix + message);
    }

    public OutputHandler getOutputHandler() {
        return outputHandler;
    }
}
