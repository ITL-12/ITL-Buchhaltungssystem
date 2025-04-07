package dev.zanex;

import dev.zanex.mvc.controller.AppController;
import dev.zanex.mvc.view.MainFrame;
import dev.zanex.utils.Logger;
import dev.zanex.utils.MySQLHandler;
import dev.zanex.utils.OutputHandler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private static MySQLHandler mysqlHandler;
    private static Logger logger;
    private static AppController controller;

    public static void main(String[] args) {
        logger = new Logger();
        logger.log("INFO", "Starting application");

        try {
            // Connect to MySQL
            logger.log("INFO", "Connecting to database");
            mysqlHandler = new MySQLHandler("localhost", 3306, "buchhaltung", "root", "root");
            logger.log("SUCCESS", "Database connection established");

            // Initialize schema if needed
            initializeDatabase();

            // Initialize controller
            controller = new AppController();
            logger.log("INFO", "Controller initialized");

            // Initialize main frame
            logger.log("INFO", "Initializing UI");
            MainFrame mainFrame = new MainFrame(controller);
            mainFrame.setVisible(true);
            logger.log("SUCCESS", "Application started successfully");

        } catch (SQLException e) {
            logger.log("ERROR", "Database connection error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.log("ERROR", "Application initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initializeDatabase() {
        try {
            logger.log("INFO", "Checking/initializing database schema");

            // Load the schema SQL from resources
            InputStream schemaStream = Main.class.getResourceAsStream("/sql/SCHEMA.sql");

            if (schemaStream == null) {
                logger.log("ERROR", "Schema file not found in resources");
                throw new RuntimeException("Could not find SCHEMA.sql in resources");
            }

            // Read the SQL file content
            String schemaSql = new BufferedReader(new InputStreamReader(schemaStream))
                    .lines().collect(Collectors.joining("\n"));

            // Split by semicolons to execute each statement separately
            String[] statements = schemaSql.split(";");

            // Execute each statement
            for (String statement : statements) {
                if (!statement.trim().isEmpty()) {
                    try {
                        mysqlHandler.executeUpdate(statement);
                    } catch (SQLException e) {
                        logger.log("WARN", "Error executing schema statement: " + e.getMessage());
                    }
                }
            }

            // Add dummy data if needed
            loadDummyData();

            // Print database structure to console
            printDatabaseSchema();

            logger.log("SUCCESS", "Database schema initialized");
        } catch (Exception e) {
            logger.log("ERROR", "Failed to initialize database schema: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private static void loadDummyData() {
        try {
            InputStream dummyDataStream = Main.class.getResourceAsStream("/sql/DUMMY_DATA.sql");
            if (dummyDataStream != null) {
                logger.log("INFO", "Loading dummy data");

                // Read the SQL file content
                String dummySql = new BufferedReader(new InputStreamReader(dummyDataStream))
                        .lines().collect(Collectors.joining("\n"));

                // Split by semicolons
                String[] statements = dummySql.split(";");

                // Execute each statement
                for (String statement : statements) {
                    if (!statement.trim().isEmpty()) {
                        try {
                            mysqlHandler.executeUpdate(statement);
                        } catch (SQLException e) {
                            logger.log("WARN", "Error executing dummy data statement: " + e.getMessage());
                        }
                    }
                }

                logger.log("SUCCESS", "Dummy data loaded");
            }
        } catch (Exception e) {
            logger.log("WARN", "Failed to load dummy data: " + e.getMessage());
        }
    }

    private static void printDatabaseSchema() {
        try {
            OutputHandler output = new OutputHandler();
            List<String> tableInfo = new ArrayList<>();

            // Get all tables in the database
            ResultSet tables = mysqlHandler.getConnection().getMetaData().getTables(
                    null, null, "%", new String[]{"TABLE"});

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                tableInfo.add("&b" + tableName + "&r:");

                // Get table columns and their details
                ResultSet columns = mysqlHandler.getConnection().getMetaData().getColumns(
                        null, null, tableName, "%");

                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String dataType = columns.getString("TYPE_NAME");
                    String nullable = columns.getString("IS_NULLABLE").equals("Ja") ? "NULL" : "NOT NULL";
                    String key = "";

                    // Check if column is a primary key
                    ResultSet primaryKeys = mysqlHandler.getConnection().getMetaData().getPrimaryKeys(
                            null, null, tableName);
                    while (primaryKeys.next()) {
                        if (primaryKeys.getString("COLUMN_NAME").equals(columnName)) {
                            key = "PRIMARY KEY";
                            break;
                        }
                    }
                    primaryKeys.close();

                    tableInfo.add("  &7- &e" + columnName + "&7: &f" + dataType +
                            " &7" + nullable + (key.isEmpty() ? "" : " &c" + key));
                }
                columns.close();

                // Add blank line between tables
                tableInfo.add("");
            }
            tables.close();

            // Remove last empty line if exists
            if (!tableInfo.isEmpty() && tableInfo.get(tableInfo.size() - 1).isEmpty()) {
                tableInfo.remove(tableInfo.size() - 1);
            }

            // Print the table structure
            output.printTable("Database Schema", tableInfo);

        } catch (SQLException e) {
            logger.log("ERROR", "Failed to print database schema: " + e.getMessage());
        }
    }

    public static MySQLHandler getMySQLHandler() {
        return mysqlHandler;
    }

    public static AppController getController() {
        return controller;
    }

    public static String getRunArgs() {
        return String.join(" ", System.getProperty("sun.java.command").split(" "));
    }

    public static Logger getLogger() {
        return logger;
    }
}