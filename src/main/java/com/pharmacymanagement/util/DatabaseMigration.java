package com.pharmacymanagement.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseMigration {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigration.class);

    public static void migrate(Connection conn) throws SQLException {
        createMigrationsTable(conn);
        migrateToVersion1(conn);
        migrateToVersion2(conn);
    }

    private static void createMigrationsTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS schema_migrations (
                version INTEGER PRIMARY KEY,
                applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private static boolean isVersionApplied(Connection conn, int version) throws SQLException {
        var sql = "SELECT COUNT(*) FROM schema_migrations WHERE version = ?";
        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, version);
            var rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private static void markVersionApplied(Connection conn, int version) throws SQLException {
        var sql = "INSERT INTO schema_migrations (version) VALUES (?)";
        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, version);
            stmt.executeUpdate();
        }
    }

    private static void migrateToVersion1(Connection conn) throws SQLException {
        if (isVersionApplied(conn, 1)) {
            return;
        }

        logger.info("Applying migration version 1 - Creating medicines table");
        
        String sql = """
            CREATE TABLE IF NOT EXISTS medicines (
                medicine_id INTEGER PRIMARY KEY AUTOINCREMENT,
                name VARCHAR(100) NOT NULL,
                generic_name VARCHAR(100),
                manufacturer VARCHAR(100),
                category VARCHAR(50),
                unit_price DECIMAL(10,2) NOT NULL,
                stock_quantity INTEGER NOT NULL,
                minimum_stock_level INTEGER NOT NULL,
                expiry_date DATE,
                batch_number VARCHAR(50),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            
            CREATE INDEX IF NOT EXISTS idx_medicine_name ON medicines(name);
            CREATE INDEX IF NOT EXISTS idx_medicine_category ON medicines(category);
            CREATE INDEX IF NOT EXISTS idx_medicine_expiry ON medicines(expiry_date);
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            markVersionApplied(conn, 1);
            logger.info("Migration version 1 completed successfully");
        }
    }

    private static void migrateToVersion2(Connection conn) throws SQLException {
        if (isVersionApplied(conn, 2)) {
            return;
        }

        logger.info("Applying migration version 2 - Creating patients table");
        
        String sql = """
            CREATE TABLE IF NOT EXISTS patients (
                patient_id INTEGER PRIMARY KEY AUTOINCREMENT,
                first_name VARCHAR(50) NOT NULL,
                last_name VARCHAR(50) NOT NULL,
                email VARCHAR(100),
                phone_number VARCHAR(20) NOT NULL,
                address TEXT,
                date_of_birth DATE,
                gender VARCHAR(10),
                allergies TEXT,
                medical_history TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            
            CREATE INDEX IF NOT EXISTS idx_patient_name ON patients(last_name, first_name);
            CREATE INDEX IF NOT EXISTS idx_patient_phone ON patients(phone_number);
            CREATE INDEX IF NOT EXISTS idx_patient_email ON patients(email);
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            markVersionApplied(conn, 2);
            logger.info("Migration version 2 completed successfully");
        }
    }
}