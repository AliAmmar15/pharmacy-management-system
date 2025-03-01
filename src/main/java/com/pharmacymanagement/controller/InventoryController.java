package com.pharmacymanagement.controller;

import com.pharmacymanagement.model.Medicine;
import com.pharmacymanagement.service.MedicineService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class InventoryController {
    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);
    private final MedicineService medicineService = new MedicineService();

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private TableView<Medicine> medicineTable;
    @FXML private TableColumn<Medicine, Long> idColumn;
    @FXML private TableColumn<Medicine, String> nameColumn;
    @FXML private TableColumn<Medicine, String> genericNameColumn;
    @FXML private TableColumn<Medicine, String> categoryColumn;
    @FXML private TableColumn<Medicine, String> manufacturerColumn;
    @FXML private TableColumn<Medicine, Integer> stockColumn;
    @FXML private TableColumn<Medicine, BigDecimal> unitPriceColumn;
    @FXML private TableColumn<Medicine, LocalDate> expiryDateColumn;
    @FXML private TableColumn<Medicine, Void> actionsColumn;
    @FXML private Label totalItemsLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label statusLabel;

    private ObservableList<Medicine> medicineList;
    private FilteredList<Medicine> filteredMedicines;

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadMedicines();
        updateStats();
    }

    private void setupTable() {
        // Initialize columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("medicineId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        genericNameColumn.setCellValueFactory(new PropertyValueFactory<>("genericName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        manufacturerColumn.setCellValueFactory(new PropertyValueFactory<>("manufacturer"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        expiryDateColumn.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));

        // Setup actions column
        setupActionsColumn();

        // Add row coloring for low stock
        medicineTable.setRowFactory(tv -> new TableRow<Medicine>() {
            @Override
            protected void updateItem(Medicine medicine, boolean empty) {
                super.updateItem(medicine, empty);
                if (medicine == null || empty) {
                    setStyle("");
                } else if (medicine.isLowStock()) {
                    setStyle("-fx-background-color: #ffebee;"); // Light red for low stock
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final Button adjustBtn = new Button("Adjust");
            private final HBox buttons = new HBox(5, editBtn, adjustBtn, deleteBtn);

            {
                editBtn.setOnAction(event -> {
                    Medicine medicine = getTableView().getItems().get(getIndex());
                    handleEditMedicine(medicine);
                });

                deleteBtn.setOnAction(event -> {
                    Medicine medicine = getTableView().getItems().get(getIndex());
                    handleDeleteMedicine(medicine);
                });

                adjustBtn.setOnAction(event -> {
                    Medicine medicine = getTableView().getItems().get(getIndex());
                    handleAdjustStock(medicine);
                });

                buttons.setStyle("-fx-alignment: CENTER;");
                editBtn.setStyle("-fx-font-size: 10px;");
                deleteBtn.setStyle("-fx-font-size: 10px;");
                adjustBtn.setStyle("-fx-font-size: 10px;");
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

    private void setupFilters() {
        // Setup category filter
        List<String> categories = Arrays.asList(
            "All", "Tablets", "Capsules", "Syrups", "Injections",
            "Ointments", "Drops", "Inhalers", "Supplements"
        );
        categoryFilter.getItems().addAll(categories);
        categoryFilter.setValue("All");

        // Setup search and category filtering
        medicineList = FXCollections.observableArrayList();
        filteredMedicines = new FilteredList<>(medicineList);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterMedicines());
        categoryFilter.valueProperty().addListener((observable, oldValue, newValue) -> filterMedicines());

        medicineTable.setItems(filteredMedicines);
    }

    private void filterMedicines() {
        filteredMedicines.setPredicate(medicine -> {
            String searchText = searchField.getText().toLowerCase();
            String category = categoryFilter.getValue();

            boolean matchesSearch = medicine.getName().toLowerCase().contains(searchText) ||
                                 medicine.getGenericName().toLowerCase().contains(searchText) ||
                                 medicine.getManufacturer().toLowerCase().contains(searchText);

            boolean matchesCategory = category.equals("All") || medicine.getCategory().equals(category);

            return matchesSearch && matchesCategory;
        });

        updateStats();
    }

    private void loadMedicines() {
        try {
            List<Medicine> medicines = medicineService.getAllMedicines();
            medicineList.setAll(medicines);
            updateStatus("Medicines loaded successfully");
        } catch (Exception e) {
            logger.error("Error loading medicines", e);
            updateStatus("Error loading medicines");
        }
    }

    @FXML
    private void handleAddMedicine() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/medicine-form.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add Medicine");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadMedicines(); // Refresh the list
        } catch (IOException e) {
            logger.error("Error opening medicine form", e);
            updateStatus("Error opening medicine form");
        }
    }

    private void handleEditMedicine(Medicine medicine) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/medicine-form.fxml"));
            Parent root = loader.load();

            MedicineFormController controller = loader.getController();
            controller.setMedicine(medicine);

            Stage stage = new Stage();
            stage.setTitle("Edit Medicine");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadMedicines(); // Refresh the list
        } catch (IOException e) {
            logger.error("Error opening medicine form", e);
            updateStatus("Error opening medicine form");
        }
    }

    private void handleDeleteMedicine(Medicine medicine) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Medicine");
        alert.setHeaderText("Delete " + medicine.getName());
        alert.setContentText("Are you sure you want to delete this medicine?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                medicineService.deleteMedicine(medicine.getMedicineId());
                loadMedicines(); // Refresh the list
                updateStatus("Medicine deleted successfully");
            } catch (Exception e) {
                logger.error("Error deleting medicine", e);
                updateStatus("Error deleting medicine");
            }
        }
    }

    private void handleAdjustStock(Medicine medicine) {
        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Adjust Stock");
        dialog.setHeaderText("Adjust stock for " + medicine.getName());
        dialog.setContentText("Enter quantity (positive to add, negative to remove):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(quantityStr -> {
            try {
                int quantity = Integer.parseInt(quantityStr);
                medicineService.updateStock(medicine.getMedicineId(), quantity);
                loadMedicines(); // Refresh the list
                updateStatus("Stock adjusted successfully");
            } catch (NumberFormatException e) {
                updateStatus("Invalid quantity entered");
            } catch (Exception e) {
                logger.error("Error adjusting stock", e);
                updateStatus("Error adjusting stock");
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadMedicines();
    }

    private void updateStats() {
        int totalItems = filteredMedicines.size();
        long lowStockCount = filteredMedicines.stream().filter(Medicine::isLowStock).count();

        totalItemsLabel.setText(String.format("Total Items: %d", totalItems));
        lowStockLabel.setText(String.format("Low Stock Items: %d", lowStockCount));
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
        logger.info(message);
    }
}