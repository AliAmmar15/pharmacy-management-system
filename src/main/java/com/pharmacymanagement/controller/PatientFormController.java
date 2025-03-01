package com.pharmacymanagement.controller;

import com.pharmacymanagement.model.Patient;
import com.pharmacymanagement.service.PatientService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

public class PatientFormController {
    private static final Logger logger = LoggerFactory.getLogger(PatientFormController.class);
    private final PatientService patientService = new PatientService();

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private DatePicker dateOfBirthPicker;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private TextField phoneNumberField;
    @FXML private TextField emailField;
    @FXML private TextField addressField;
    @FXML private TextArea allergiesArea;
    @FXML private TextArea medicalHistoryArea;
    @FXML private Label errorLabel;

    private Patient patient;
    private boolean isEditMode = false;
    private Runnable onSaveCallback;

    @FXML
    public void initialize() {
        setupGenderComboBox();
        setupValidation();
        errorLabel.setVisible(false);
    }

    private void setupGenderComboBox() {
        genderComboBox.getItems().addAll("Male", "Female", "Other");
    }

    private void setupValidation() {
        // Phone number validation
        phoneNumberField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*([\\-()])?\\d*")) {
                phoneNumberField.setText(oldVal);
            }
        });

        // Prevent future dates in date of birth
        dateOfBirthPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(LocalDate.now()));
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
            dateOfBirthPicker.setValue(patient.getDateOfBirth());
            genderComboBox.setValue(patient.getGender());
            phoneNumberField.setText(patient.getPhoneNumber());
            emailField.setText(patient.getEmail());
            addressField.setText(patient.getAddress());
            allergiesArea.setText(patient.getAllergies());
            medicalHistoryArea.setText(patient.getMedicalHistory());
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
        patient.setDateOfBirth(dateOfBirthPicker.getValue());
        patient.setGender(genderComboBox.getValue());
        patient.setPhoneNumber(phoneNumberField.getText().trim());
        patient.setEmail(emailField.getText().trim());
        patient.setAddress(addressField.getText().trim());
        patient.setAllergies(allergiesArea.getText().trim());
        patient.setMedicalHistory(medicalHistoryArea.getText().trim());
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

        if (phoneNumberField.getText().trim().isEmpty()) {
            showError("Phone number is required");
            return false;
        }

        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Invalid email format");
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