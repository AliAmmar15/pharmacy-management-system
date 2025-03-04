package com.pharmacymanagement.controller;

import com.pharmacymanagement.model.Patient;
import com.pharmacymanagement.service.PatientService;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class PatientListController {
    private static final Logger logger = LoggerFactory.getLogger(PatientListController.class);
    private final PatientService patientService = new PatientService();

    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, Long> idColumn;
    @FXML private TableColumn<Patient, String> firstNameColumn;
    @FXML private TableColumn<Patient, String> lastNameColumn;
    @FXML private TableColumn<Patient, String> emailColumn;
    @FXML private TableColumn<Patient, String> phoneNumberColumn;
    @FXML private TableColumn<Patient, LocalDate> dateOfBirthColumn;
    @FXML private TableColumn<Patient, String> allergiesColumn;
    @FXML private TableColumn<Patient, String> medicalHistoryColumn;
    @FXML private TableColumn<Patient, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private Label totalPatientsLabel;

    private FilteredList<Patient> filteredPatients;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupSearch();
        loadPatients();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        dateOfBirthColumn.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        allergiesColumn.setCellValueFactory(new PropertyValueFactory<>("allergies"));
        medicalHistoryColumn.setCellValueFactory(new PropertyValueFactory<>("medicalHistory"));

        // Setup actions column
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                editButton.getStyleClass().add("button-secondary");
                deleteButton.getStyleClass().add("button-danger");

                editButton.setOnAction(event -> {
                    Patient patient = getTableRow().getItem();
                    if (patient != null) {
                        handleEditPatient(patient);
                    }
                });

                deleteButton.setOnAction(event -> {
                    Patient patient = getTableRow().getItem();
                    if (patient != null) {
                        handleDeletePatient(patient);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filteredPatients != null) {
                filteredPatients.setPredicate(patient -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }

                    String lowerCaseFilter = newValue.toLowerCase();
                    return patient.getFullName().toLowerCase().contains(lowerCaseFilter)
                            || patient.getEmail().toLowerCase().contains(lowerCaseFilter)
                            || patient.getPhoneNumber().toLowerCase().contains(lowerCaseFilter);
                });
                updateTotalPatientsLabel();
            }
        });
    }

    private void loadPatients() {
        try {
            List<Patient> patients = patientService.getAllPatients();
            filteredPatients = new FilteredList<>(FXCollections.observableArrayList(patients));
            patientTable.setItems(filteredPatients);
            updateTotalPatientsLabel();
            updateStatus("Patients loaded successfully");
        } catch (Exception e) {
            logger.error("Error loading patients", e);
            updateStatus("Error loading patients: " + e.getMessage());
        }
    }

    private void handleEditPatient(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/patient-form.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Patient");
            stage.setScene(new Scene(root));

            PatientFormController controller = loader.getController();
            controller.setPatient(patient);
            controller.setOnSaveCallback(this::loadPatients);

            stage.showAndWait();
        } catch (IOException e) {
            logger.error("Error opening patient form", e);
            updateStatus("Error opening patient form: " + e.getMessage());
        }
    }

    private void handleDeletePatient(Patient patient) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Patient");
        alert.setContentText("Are you sure you want to delete " + patient.getFullName() + "?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                patientService.deletePatient(patient.getPatientId());
                loadPatients();
                updateStatus("Patient deleted successfully");
            } catch (Exception e) {
                logger.error("Error deleting patient", e);
                updateStatus("Error deleting patient: " + e.getMessage());
            }
        }
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private void updateTotalPatientsLabel() {
        int total = filteredPatients.size();
        totalPatientsLabel.setText(String.format("Total Patients: %d", total));
    }
}