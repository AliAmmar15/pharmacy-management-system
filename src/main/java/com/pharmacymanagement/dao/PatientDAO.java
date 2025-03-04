package com.pharmacymanagement.dao;

import com.pharmacymanagement.model.Patient;
import com.pharmacymanagement.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PatientDAO {
    private static final Logger logger = LoggerFactory.getLogger(PatientDAO.class);

    public Patient save(Patient patient) {
        String sql = "INSERT INTO patients (first_name, last_name, email, phone_number, date_of_birth, allergies, medical_history) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, patient.getFirstName());
            pstmt.setString(2, patient.getLastName());
            pstmt.setString(3, patient.getEmail());
            pstmt.setString(4, patient.getPhoneNumber());
            pstmt.setDate(5, Date.valueOf(patient.getDateOfBirth()));
            pstmt.setString(6, patient.getAllergies());
            pstmt.setString(7, patient.getMedicalHistory());

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
        String sql = "UPDATE patients SET first_name = ?, last_name = ?, email = ?, phone_number = ?, " +
                    "date_of_birth = ?, allergies = ?, medical_history = ? WHERE patient_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patient.getFirstName());
            pstmt.setString(2, patient.getLastName());
            pstmt.setString(3, patient.getEmail());
            pstmt.setString(4, patient.getPhoneNumber());
            pstmt.setDate(5, Date.valueOf(patient.getDateOfBirth()));
            pstmt.setString(6, patient.getAllergies());
            pstmt.setString(7, patient.getMedicalHistory());
            pstmt.setLong(8, patient.getPatientId());

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
        String sql = "SELECT * FROM patients ORDER BY first_name, last_name";
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
        patient.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
        patient.setAllergies(rs.getString("allergies"));
        patient.setMedicalHistory(rs.getString("medical_history"));

        return patient;
    }
}