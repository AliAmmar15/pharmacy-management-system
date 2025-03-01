package com.pharmacymanagement.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);
    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            logger.info("Database connection established");
        }
        return connection;
    }

    public static void initializeDatabase() {
        try {
            // Create database if it doesn't exist
            Connection conn = getConnection();
            
            // Read and execute schema.sql
            String schemaPath = DatabaseUtil.class.getResource("/db/schema.sql").getPath();
            // Handle Windows file path format
            if (schemaPath.startsWith("/")) {
                schemaPath = schemaPath.substring(1);
            }
            
            String schema = new String(Files.readAllBytes(Paths.get(schemaPath)));
            
            // Split the schema into individual statements
            String[] statements = schema.split(";");
            
            Statement stmt = conn.createStatement();
            for (String statement : statements) {
                if (!statement.trim().isEmpty()) {
                    stmt.execute(statement);
                }
            }
            
            logger.info("Database schema initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize database", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed");
            }
        } catch (SQLException e) {
            logger.error("Error closing database connection", e);
        }
    }
}