package com.pharmacymanagement.dao;

import com.pharmacymanagement.model.Patient;
import com.pharmacymanagement.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PatientDAO {
    private static final Logger logger = LoggerFactory.getLogger(PatientDAO.class);

    public Patient save(Patient patient) {
        String sql = "INSERT INTO patients (first_name, last_name, email, phone_number, address, " +
                    "date_of_birth, gender, allergies, medical_history) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, patient.getFirstName());
            pstmt.setString(2, patient.getLastName());
            pstmt.setString(3, patient.getEmail());
            pstmt.setString(4, patient.getPhoneNumber());
            pstmt.setString(5, patient.getAddress());
            pstmt.setDate(6, patient.getDateOfBirth() != null ? 
                         Date.valueOf(patient.getDateOfBirth()) : null);
            pstmt.setString(7, patient.getGender());
            pstmt.setString(8, patient.getAllergies());
            pstmt.setString(9, patient.getMedicalHistory());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        patient.setPatientId(rs.getLong(1));
                    }
                }
            }

            logger.info("Patient saved successfully: {}", patient.getFullName());
            return patient;

        } catch (SQLException e) {
            logger.error("Error saving patient", e);
            throw new RuntimeException("Error saving patient", e);
        }
    }

    public void update(Patient patient) {
        String sql = "UPDATE patients SET first_name = ?, last_name = ?, email = ?, " +
                    "phone_number = ?, address = ?, date_of_birth = ?, gender = ?, " +
                    "allergies = ?, medical_history = ?, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE patient_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patient.getFirstName());
            pstmt.setString(2, patient.getLastName());
            pstmt.setString(3, patient.getEmail());
            pstmt.setString(4, patient.getPhoneNumber());
            pstmt.setString(5, patient.getAddress());
            pstmt.setDate(6, patient.getDateOfBirth() != null ? 
                         Date.valueOf(patient.getDateOfBirth()) : null);
            pstmt.setString(7, patient.getGender());
            pstmt.setString(8, patient.getAllergies());
            pstmt.setString(9, patient.getMedicalHistory());
            pstmt.setLong(10, patient.getPatientId());

            pstmt.executeUpdate();
            logger.info("Patient updated successfully: {}", patient.getFullName());

        } catch (SQLException e) {
            logger.error("Error updating patient", e);
            throw new RuntimeException("Error updating patient", e);
        }
    }

    public Optional<Patient> findById(Long id) {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToPatient(rs));
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding patient by ID", e);
            throw new RuntimeException("Error finding patient by ID", e);
        }
    }

    public List<Patient> findAll() {
        String sql = "SELECT * FROM patients ORDER BY last_name, first_name";
        List<Patient> patients = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                patients.add(mapResultSetToPatient(rs));
            }

            return patients;

        } catch (SQLException e) {
            logger.error("Error finding all patients", e);
            throw new RuntimeException("Error finding all patients", e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM patients WHERE patient_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.executeUpdate();
            logger.info("Patient deleted successfully: ID {}", id);

        } catch (SQLException e) {
            logger.error("Error deleting patient", e);
            throw new RuntimeException("Error deleting patient", e);
        }
    }

    private Patient mapResultSetToPatient(ResultSet rs) throws SQLException {
        Patient patient = new Patient();
        patient.setPatientId(rs.getLong("patient_id"));
        patient.setFirstName(rs.getString("first_name"));
        patient.setLastName(rs.getString("last_name"));
        patient.setEmail(rs.getString("email"));
        patient.setPhoneNumber(rs.getString("phone_number"));
        patient.setAddress(rs.getString("address"));
        
        Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            patient.setDateOfBirth(dob.toLocalDate());
        }
        
        patient.setGender(rs.getString("gender"));
        patient.setAllergies(rs.getString("allergies"));
        patient.setMedicalHistory(rs.getString("medical_history"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            patient.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            patient.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return patient;
    }
}