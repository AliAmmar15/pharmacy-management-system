package com.pharmacymanagement.controller;

import com.pharmacymanagement.model.Patient;
import com.pharmacymanagement.service.PatientService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class PatientFormController {
    private static final Logger logger = LoggerFactory.getLogger(PatientFormController.class);
    private final PatientService patientService = new PatientService();

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneNumberField;
    @FXML private DatePicker dateOfBirthPicker;
    @FXML private TextArea allergiesField;
    @FXML private TextArea medicalHistoryField;
    @FXML private Label errorLabel;

    private Patient patient;
    private boolean isEditMode = false;
    private Runnable onSaveCallback;

    @FXML
    public void initialize() {
        setupValidation();
        errorLabel.setVisible(false);
    }

    private void setupValidation() {
        // Integer validation for phone number
        phoneNumberField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                phoneNumberField.setText(oldVal);
            }
        });
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
        this.isEditMode = true;
        populateFields();
    }

    private void populateFields() {
        if (patient != null) {
            firstNameField.setText(patient.getFirstName());
            lastNameField.setText(patient.getLastName());
            emailField.setText(patient.getEmail());
            phoneNumberField.setText(patient.getPhoneNumber());
            dateOfBirthPicker.setValue(patient.getDateOfBirth());
            allergiesField.setText(patient.getAllergies());
            medicalHistoryField.setText(patient.getMedicalHistory());
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    private void handleSave() {
        try {
            if (!validateInput()) {
                return;
            }

            Patient patientToSave = isEditMode ? patient : new Patient();
            updatePatientFromFields(patientToSave);

            if (isEditMode) {
                patientService.updatePatient(patientToSave);
                logger.info("Patient updated successfully: {}", patientToSave.getFullName());
            } else {
                patientService.addPatient(patientToSave);
                logger.info("Patient added successfully: {}", patientToSave.getFullName());
            }

            if (onSaveCallback != null) {
                onSaveCallback.run();
            }

            closeDialog();
        } catch (Exception e) {
            logger.error("Error saving patient", e);
            showError("Error saving patient: " + e.getMessage());
        }
    }

    private void updatePatientFromFields(Patient patient) {
        patient.setFirstName(firstNameField.getText().trim());
        patient.setLastName(lastNameField.getText().trim());
        patient.setEmail(emailField.getText().trim());
        patient.setPhoneNumber(phoneNumberField.getText().trim());
        patient.setDateOfBirth(dateOfBirthPicker.getValue());
        patient.setAllergies(allergiesField.getText().trim());
        patient.setMedicalHistory(medicalHistoryField.getText().trim());
        patient.setUpdatedAt(LocalDateTime.now());
    }

    private boolean validateInput() {
        if (firstNameField.getText().trim().isEmpty()) {
            showError("First name is required");
            return false;
        }

        if (lastNameField.getText().trim().isEmpty()) {
            showError("Last name is required");
            return false;
        }

        if (emailField.getText().trim().isEmpty()) {
            showError("Email is required");
            return false;
        }

        if (phoneNumberField.getText().trim().isEmpty()) {
            showError("Phone number is required");
            return false;
        }

        if (dateOfBirthPicker.getValue() == null) {
            showError("Date of birth is required");
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}