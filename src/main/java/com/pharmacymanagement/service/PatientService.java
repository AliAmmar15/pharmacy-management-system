package com.pharmacymanagement.service;

import com.pharmacymanagement.dao.PatientDAO;
import com.pharmacymanagement.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class PatientService {
    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);
    private final PatientDAO patientDAO;

    public PatientService() {
        this.patientDAO = new PatientDAO();
    }

    public Patient addPatient(Patient patient) {
        validatePatient(patient);
        return patientDAO.save(patient);
    }

    public void updatePatient(Patient patient) {
        validatePatient(patient);
        patientDAO.update(patient);
    }

    public Optional<Patient> getPatientById(Long id) {
        return patientDAO.findById(id);
    }

    public List<Patient> getAllPatients() {
        return patientDAO.findAll();
    }

    public void deletePatient(Long id) {
        patientDAO.delete(id);
    }

    private void validatePatient(Patient patient) {
        if (patient.getFirstName() == null || patient.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }

        if (patient.getLastName() == null || patient.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }

        if (patient.getPhoneNumber() == null || patient.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        // Email validation (basic)
        if (patient.getEmail() != null && !patient.getEmail().isEmpty() && 
            !patient.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Phone validation (basic)
        if (!patient.getPhoneNumber().matches("^[0-9()-+\\s]*$")) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
    }
}