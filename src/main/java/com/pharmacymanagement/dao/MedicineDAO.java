package com.pharmacymanagement.dao;

import com.pharmacymanagement.model.Medicine;
import com.pharmacymanagement.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MedicineDAO {
    private static final Logger logger = LoggerFactory.getLogger(MedicineDAO.class);

    public Medicine save(Medicine medicine) {
        String sql = "INSERT INTO medicines (name, generic_name, manufacturer, category, " +
                    "unit_price, stock_quantity, minimum_stock_level, expiry_date, batch_number) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, medicine.getName());
            pstmt.setString(2, medicine.getGenericName());
            pstmt.setString(3, medicine.getManufacturer());
            pstmt.setString(4, medicine.getCategory());
            pstmt.setBigDecimal(5, medicine.getUnitPrice());
            pstmt.setInt(6, medicine.getStockQuantity());
            pstmt.setInt(7, medicine.getMinimumStockLevel());
            pstmt.setDate(8, medicine.getExpiryDate() != null ? 
                         Date.valueOf(medicine.getExpiryDate()) : null);
            pstmt.setString(9, medicine.getBatchNumber());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        medicine.setMedicineId(rs.getLong(1));
                    }
                }
            }

            logger.info("Medicine saved successfully: {}", medicine.getName());
            return medicine;

        } catch (SQLException e) {
            logger.error("Error saving medicine", e);
            throw new RuntimeException("Error saving medicine", e);
        }
    }

    public void update(Medicine medicine) {
        String sql = "UPDATE medicines SET name = ?, generic_name = ?, manufacturer = ?, " +
                    "category = ?, unit_price = ?, stock_quantity = ?, minimum_stock_level = ?, " +
                    "expiry_date = ?, batch_number = ?, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE medicine_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, medicine.getName());
            pstmt.setString(2, medicine.getGenericName());
            pstmt.setString(3, medicine.getManufacturer());
            pstmt.setString(4, medicine.getCategory());
            pstmt.setBigDecimal(5, medicine.getUnitPrice());
            pstmt.setInt(6, medicine.getStockQuantity());
            pstmt.setInt(7, medicine.getMinimumStockLevel());
            pstmt.setDate(8, medicine.getExpiryDate() != null ? 
                         Date.valueOf(medicine.getExpiryDate()) : null);
            pstmt.setString(9, medicine.getBatchNumber());
            pstmt.setLong(10, medicine.getMedicineId());

            pstmt.executeUpdate();
            logger.info("Medicine updated successfully: {}", medicine.getName());

        } catch (SQLException e) {
            logger.error("Error updating medicine", e);
            throw new RuntimeException("Error updating medicine", e);
        }
    }

    public Optional<Medicine> findById(Long id) {
        String sql = "SELECT * FROM medicines WHERE medicine_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToMedicine(rs));
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding medicine by ID", e);
            throw new RuntimeException("Error finding medicine by ID", e);
        }
    }

    public List<Medicine> findAll() {
        String sql = "SELECT * FROM medicines ORDER BY name";
        List<Medicine> medicines = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                medicines.add(mapResultSetToMedicine(rs));
            }

            return medicines;

        } catch (SQLException e) {
            logger.error("Error finding all medicines", e);
            throw new RuntimeException("Error finding all medicines", e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM medicines WHERE medicine_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.executeUpdate();
            logger.info("Medicine deleted successfully: ID {}", id);

        } catch (SQLException e) {
            logger.error("Error deleting medicine", e);
            throw new RuntimeException("Error deleting medicine", e);
        }
    }

    private Medicine mapResultSetToMedicine(ResultSet rs) throws SQLException {
        Medicine medicine = new Medicine();
        medicine.setMedicineId(rs.getLong("medicine_id"));
        medicine.setName(rs.getString("name"));
        medicine.setGenericName(rs.getString("generic_name"));
        medicine.setManufacturer(rs.getString("manufacturer"));
        medicine.setCategory(rs.getString("category"));
        medicine.setUnitPrice(rs.getBigDecimal("unit_price"));
        medicine.setStockQuantity(rs.getInt("stock_quantity"));
        medicine.setMinimumStockLevel(rs.getInt("minimum_stock_level"));
        
        Date expiryDate = rs.getDate("expiry_date");
        if (expiryDate != null) {
            medicine.setExpiryDate(expiryDate.toLocalDate());
        }
        
        medicine.setBatchNumber(rs.getString("batch_number"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            medicine.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            medicine.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return medicine;
    }
}