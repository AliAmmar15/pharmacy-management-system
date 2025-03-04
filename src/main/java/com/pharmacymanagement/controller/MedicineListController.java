package com.pharmacymanagement.controller;

import com.pharmacymanagement.model.Medicine;
import com.pharmacymanagement.service.MedicineService;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MedicineListController {
    private static final Logger logger = LoggerFactory.getLogger(MedicineListController.class);
    private final MedicineService medicineService = new MedicineService();

    @FXML private TableView<Medicine> medicineTable;
    @FXML private TableColumn<Medicine, Long> idColumn;
    @FXML private TableColumn<Medicine, String> nameColumn;
    @FXML private TableColumn<Medicine, String> genericNameColumn;
    @FXML private TableColumn<Medicine, String> categoryColumn;
    @FXML private TableColumn<Medicine, Integer> stockColumn;
    @FXML private TableColumn<Medicine, Double> priceColumn;
    @FXML private TableColumn<Medicine, LocalDate> expiryDateColumn;
    @FXML private TableColumn<Medicine, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private Label totalMedicinesLabel;
    @FXML private Label currentDateTimeLabel;
    @FXML private VBox lowStockAlert;
    @FXML private Label lowStockLabel;
    @FXML private VBox expiryAlert;
    @FXML private Label expiryLabel;

    private FilteredList<Medicine> filteredMedicines;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupSearch();
        setupDateTime();
        loadMedicines();
        checkAlerts();
    }

    private void setupDateTime() {
        updateDateTime();
        // Update date/time every minute
        java.util.Timer timer = new java.util.Timer(true);
        timer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> updateDateTime());
            }
        }, 60000, 60000);
    }

    private void updateDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        currentDateTimeLabel.setText("Current Date and Time: " + LocalDateTime.now().format(formatter));
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("medicineId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        genericNameColumn.setCellValueFactory(new PropertyValueFactory<>("genericName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        expiryDateColumn.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        
        // Format price column
        priceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", price));
                }
            }
        });

        // Format date column
        expiryDateColumn.setCellFactory(column -> new TableCell<>() {
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
                    Medicine medicine = getTableRow().getItem();
                    if (medicine != null) {
                        handleEditMedicine(medicine);
                    }
                });

                deleteButton.setOnAction(event -> {
                    Medicine medicine = getTableRow().getItem();
                    if (medicine != null) {
                        handleDeleteMedicine(medicine);
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
            if (filteredMedicines != null) {
                filteredMedicines.setPredicate(medicine -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }

                    String lowerCaseFilter = newValue.toLowerCase();
                    return medicine.getName().toLowerCase().contains(lowerCaseFilter)
                        || medicine.getGenericName().toLowerCase().contains(lowerCaseFilter)
                        || medicine.getCategory().toLowerCase().contains(lowerCaseFilter)
                        || medicine.getManufacturer().toLowerCase().contains(lowerCaseFilter);
                });
                updateTotalMedicinesLabel();
            }
        });
    }

    private void loadMedicines() {
        try {
            List<Medicine> medicines = medicineService.getAllMedicines();
            filteredMedicines = new FilteredList<>(FXCollections.observableArrayList(medicines));
            medicineTable.setItems(filteredMedicines);
            updateTotalMedicinesLabel();
            updateStatus("Medicines loaded successfully");
        } catch (Exception e) {
            logger.error("Error loading medicines", e);
            updateStatus("Error loading medicines: " + e.getMessage());
        }
    }

    private void checkAlerts() {
        // Check low stock
        List<Medicine> lowStockMedicines = medicineService.getLowStockMedicines();
        if (!lowStockMedicines.isEmpty()) {
            lowStockAlert.setVisible(true);
            lowStockAlert.setManaged(true);
            lowStockLabel.setText(String.format("%d medicines are low in stock", lowStockMedicines.size()));
        }

        // Check expiring medicines (within 30 days)
        List<Medicine> expiringMedicines = medicineService.getExpiringMedicines(30);
        if (!expiringMedicines.isEmpty()) {
            expiryAlert.setVisible(true);
            expiryAlert.setManaged(true);
            expiryLabel.setText(String.format("%d medicines are expiring soon", expiringMedicines.size()));
        }
    }

    @FXML
    private void handleAddNewMedicine() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/medicine-form.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Medicine");
            stage.setScene(new Scene(root));
            
            MedicineFormController controller = loader.getController();
            controller.setOnSaveCallback(() -> {
                loadMedicines();
                checkAlerts();
            });
            
            stage.showAndWait();
        } catch (IOException e) {
            logger.error("Error opening medicine form", e);
            updateStatus("Error opening medicine form: " + e.getMessage());
        }
    }

    private void handleEditMedicine(Medicine medicine) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/medicine-form.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Medicine");
            stage.setScene(new Scene(root));
            
            MedicineFormController controller = loader.getController();
            controller.setMedicine(medicine);
            controller.setOnSaveCallback(() -> {
                loadMedicines();
                checkAlerts();
            });
            
            stage.showAndWait();
        } catch (IOException e) {
            logger.error("Error opening medicine form", e);
            updateStatus("Error opening medicine form: " + e.getMessage());
        }
    }

    private void handleDeleteMedicine(Medicine medicine) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Medicine");
        alert.setContentText("Are you sure you want to delete " + medicine.getName() + "?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                medicineService.deleteMedicine(medicine.getMedicineId());
                loadMedicines();
                checkAlerts();
                updateStatus("Medicine deleted successfully");
            } catch (Exception e) {
                logger.error("Error deleting medicine", e);
                updateStatus("Error deleting medicine: " + e.getMessage());
            }
        }
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private void updateTotalMedicinesLabel() {
        int total = filteredMedicines.size();
        totalMedicinesLabel.setText(String.format("Total Medicines: %d", total));
    }
}