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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientListController {
    private static final Logger logger = LoggerFactory.getLogger(PatientListController.class);
    private final PatientService patientService = new PatientService();

    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, Long> idColumn;
    @FXML private TableColumn<Patient, String> nameColumn;
    @FXML private TableColumn<Patient, String> phoneColumn;
    @FXML private TableColumn<Patient, String> emailColumn;
    @FXML private TableColumn<Patient, LocalDate> dobColumn;
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
        nameColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName()
            )
        );
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        dobColumn.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        
        // Format date of birth
        dobColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });

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
                    return patient.getFirstName().toLowerCase().contains(lowerCaseFilter)
                        || patient.getLastName().toLowerCase().contains(lowerCaseFilter)
                        || patient.getPhoneNumber().toLowerCase().contains(lowerCaseFilter)
                        || (patient.getEmail() != null && 
                            patient.getEmail().toLowerCase().contains(lowerCaseFilter));
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

    @FXML
    private void handleAddNewPatient() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/patient-form.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Patient");
            stage.setScene(new Scene(root));
            
            PatientFormController controller = loader.getController();
            controller.setOnSaveCallback(this::onPatientSaved);
            
            stage.showAndWait();
        } catch (IOException e) {
            logger.error("Error opening patient form", e);
            updateStatus("Error opening patient form: " + e.getMessage());
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
            controller.setOnSaveCallback(this::onPatientSaved);
            
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

    private void onPatientSaved() {
        loadPatients();
        updateStatus("Patient saved successfully");
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private void updateTotalPatientsLabel() {
        int total = filteredPatients.size();
        totalPatientsLabel.setText(String.format("Total Patients: %d", total));
    }
}