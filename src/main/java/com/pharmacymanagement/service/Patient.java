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
        if (patient.getName() == null || patient.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Patient name is required");
        }

        if (patient.getAge() < 0) {
            throw new IllegalArgumentException("Patient age cannot be negative");
        }

        if (patient.getGender() == null || patient.getGender().trim().isEmpty()) {
            throw new IllegalArgumentException("Patient gender is required");
        }

        if (patient.getContactNumber() == null || patient.getContactNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Patient contact number is required");
        }

                if (patient.getAddress() == null || patient.getAddress().trim().isEmpty()) {
                    throw new IllegalArgumentException("Patient address is required");
                }
            }
        }

public class Patient {