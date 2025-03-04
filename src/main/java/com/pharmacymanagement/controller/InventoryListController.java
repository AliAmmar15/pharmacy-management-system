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
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InventoryListController {
    private static final Logger logger = LoggerFactory.getLogger(InventoryListController.class);
    private final MedicineService medicineService = new MedicineService();

    @FXML private TableView<Medicine> inventoryTable;
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
    @FXML private Label totalInventoryLabel;

    private FilteredList<Medicine> filteredInventory;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupSearch();
        loadInventory();
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
                        handleEditInventory(medicine);
                    }
                });

                deleteButton.setOnAction(event -> {
                    Medicine medicine = getTableRow().getItem();
                    if (medicine != null) {
                        handleDeleteInventory(medicine);
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
            if (filteredInventory != null) {
                filteredInventory.setPredicate(medicine -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }

                    String lowerCaseFilter = newValue.toLowerCase();
                    return medicine.getName().toLowerCase().contains(lowerCaseFilter)
                        || medicine.getGenericName().toLowerCase().contains(lowerCaseFilter)
                        || medicine.getCategory().toLowerCase().contains(lowerCaseFilter)
                        || medicine.getManufacturer().toLowerCase().contains(lowerCaseFilter);
                });
                updateTotalInventoryLabel();
            }
        });
    }

    private void loadInventory() {
        try {
            List<Medicine> inventory = medicineService.getAllMedicines();
            filteredInventory = new FilteredList<>(FXCollections.observableArrayList(inventory));
            inventoryTable.setItems(filteredInventory);
            updateTotalInventoryLabel();
            updateStatus("Inventory loaded successfully");
        } catch (Exception e) {
            logger.error("Error loading inventory", e);
            updateStatus("Error loading inventory: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddNewStock() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/inventory-form.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Stock");
            stage.setScene(new Scene(root));
            
            InventoryFormController controller = loader.getController();
            controller.setOnSaveCallback(this::loadInventory);
            
            stage.showAndWait();
        } catch (IOException e) {
            logger.error("Error opening inventory form", e);
            updateStatus("Error opening inventory form: " + e.getMessage());
        }
    }

    private void handleEditInventory(Medicine medicine) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/inventory-form.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Inventory");
            stage.setScene(new Scene(root));
            
            InventoryFormController controller = loader.getController();
            controller.setMedicine(medicine);
            controller.setOnSaveCallback(this::loadInventory);
            
            stage.showAndWait();
        } catch (IOException e) {
            logger.error("Error opening inventory form", e);
            updateStatus("Error opening inventory form: " + e.getMessage());
        }
    }

    private void handleDeleteInventory(Medicine medicine) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Inventory");
        alert.setContentText("Are you sure you want to delete " + medicine.getName() + "?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                medicineService.deleteMedicine(medicine.getMedicineId());
                loadInventory();
                updateStatus("Inventory deleted successfully");
            } catch (Exception e) {
                logger.error("Error deleting inventory", e);
                updateStatus("Error deleting inventory: " + e.getMessage());
            }
        }
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private void updateTotalInventoryLabel() {
        int total = filteredInventory.size();
        totalInventoryLabel.setText(String.format("Total Inventory: %d", total));
    }
}