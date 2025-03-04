package com.pharmacymanagement.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);
    private static final String DATABASE_DIRECTORY = "data";
    private static final String DATABASE_FILE = "pharmacy.db";
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE_DIRECTORY + File.separator + DATABASE_FILE;
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            // Create data directory if it doesn't exist
            File dataDir = new File(DATABASE_DIRECTORY);
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            
            connection = DriverManager.getConnection(DATABASE_URL);
            connection.setAutoCommit(true);
            logger.info("Database connection established");
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    logger.info("Database connection closed successfully");
                }
            } catch (SQLException e) {
                logger.error("Error closing database connection", e);
            } finally {
                connection = null;
            }
        }
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            DatabaseMigration.migrate(conn);
            logger.info("Database initialization completed successfully");
        } catch (SQLException e) {
            logger.error("Error initializing database", e);
            throw new RuntimeException("Error initializing database", e);
        }
    }
}