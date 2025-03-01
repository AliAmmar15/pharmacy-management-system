package com.pharmacymanagement.controller;

import com.pharmacymanagement.model.Medicine;
import com.pharmacymanagement.service.MedicineService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    private final MedicineService medicineService = new MedicineService();

    @FXML private TableView<Medicine> lowStockTable;
    @FXML private TableColumn<Medicine, String> lowStockNameColumn;
    @FXML private TableColumn<Medicine, Integer> lowStockQuantityColumn;
    @FXML private TableColumn<Medicine, Integer> lowStockMinimumColumn;
    
    @FXML private TableView<Object> recentSalesTable;
    @FXML private TableColumn<Object, String> saleTimeColumn;
    @FXML private TableColumn<Object, String> saleAmountColumn;
    @FXML private TableColumn<Object, String> saleItemsColumn;
    
    @FXML private Label statusLabel;
    @FXML private Label currentTimeLabel;
    @FXML private Label userLabel;
    @FXML private Label lowStockCount;
    @FXML private Label todaysSalesCount;

    private Timeline clockTimeline;

    @FXML
    private void initialize() {
        setupLowStockTable();
        setupRecentSalesTable();
        setupStatusBar();
        startClock();
        refreshData();
        
        // Set up automatic refresh every 5 minutes
        Timeline refreshTimeline = new Timeline(
            new KeyFrame(Duration.minutes(5), event -> refreshData())
        );
        refreshTimeline.setCycleCount(Animation.INDEFINITE);
        refreshTimeline.play();
    }

    private void setupLowStockTable() {
        lowStockNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        lowStockQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        lowStockMinimumColumn.setCellValueFactory(new PropertyValueFactory<>("minimumStockLevel"));
    }

    private void setupRecentSalesTable() {
        // TODO: Implement when Sales functionality is added
    }

    private void setupStatusBar() {
        userLabel.setText("User: " + System.getProperty("user.name"));
    }

    private void startClock() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            LocalDateTime now = LocalDateTime.now();
            currentTimeLabel.setText(timeFormatter.format(now));
        }));
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();
    }

    @FXML
    private void handleSettings() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Settings");
        alert.setHeaderText("Settings");
        alert.setContentText("Settings functionality will be implemented in future updates.");
        alert.showAndWait();
        updateStatus("Settings dialog opened");
    }

    @FXML
    private void handleExit() {
        Platform.exit();
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
            stage.setMinWidth(500);
            stage.setMinHeight(600);
            
            stage.showAndWait();
            refreshData();
            
        } catch (IOException e) {
            logger.error("Error loading medicine form", e);
            updateStatus("Error loading medicine form");
        }
    }

    @FXML
    private void handleViewInventory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/inventory-view.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Inventory Management");
            stage.setScene(new Scene(root));
            stage.setMinWidth(1200);
            stage.setMinHeight(600);
            
            stage.show();
            
        } catch (IOException e) {
            logger.error("Error loading inventory view", e);
            updateStatus("Error loading inventory view");
        }
    }

    @FXML
    private void handleStockAlerts() {
        updateStatus("Opening stock alerts...");
        refreshData();
    }

    @FXML
    private void handleNewSale() {
        updateStatus("New sale functionality coming soon");
    }

    @FXML
    private void handleSalesHistory() {
        updateStatus("Sales history functionality coming soon");
    }

    @FXML
    private void handleAddPatient() {
        updateStatus("Add patient functionality coming soon");
    }

    @FXML
    private void handleViewPatients() {
        updateStatus("View patients functionality coming soon");
    }

    @FXML
    private void handleSalesReport() {
        updateStatus("Sales report functionality coming soon");
    }

    @FXML
    private void handleInventoryReport() {
        updateStatus("Inventory report functionality coming soon");
    }

    private void refreshData() {
        updateLowStockTable();
        updateRecentSales();
        updateStatus("Dashboard updated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    private void updateLowStockTable() {
        try {
            List<Medicine> lowStockMedicines = medicineService.getLowStockMedicines();
            lowStockTable.setItems(FXCollections.observableArrayList(lowStockMedicines));
            lowStockCount.setText("(" + lowStockMedicines.size() + ")");
            
            if (!lowStockMedicines.isEmpty()) {
                updateStatus("Warning: " + lowStockMedicines.size() + " items are low in stock");
            }
        } catch (Exception e) {
            logger.error("Error updating low stock table", e);
            updateStatus("Error updating low stock information");
        }
    }

    private void updateRecentSales() {
        // TODO: Implement when Sales functionality is added
        todaysSalesCount.setText("(0 today)");
    }

    private void updateStatus(String message) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            logger.info("Status updated: {}", message);
        });
    }
}