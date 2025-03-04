package com.pharmacymanagement.controller;

import com.pharmacymanagement.model.Medicine;
import com.pharmacymanagement.service.MedicineService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

public class InventoryFormController {
    private static final Logger logger = LoggerFactory.getLogger(InventoryFormController.class);
    private final MedicineService medicineService = new MedicineService();

    @FXML private TextField medicineNameField;
    @FXML private TextField genericNameField;
    @FXML private TextField manufacturerField;
    @FXML private TextField currentStockField;
    @FXML private TextField addStockField;
    @FXML private TextField batchNumberField;
    @FXML private DatePicker expiryDatePicker;
    @FXML private Label errorLabel;

    private Medicine medicine;
    private Runnable onSaveCallback;

    @FXML
    public void initialize() {
        setupValidation();
        errorLabel.setVisible(false);
    }

    private void setupValidation() {
        // Integer validation for add stock
        addStockField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                addStockField.setText(oldVal);
            }
        });

        // Prevent past dates for expiry
        expiryDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
        populateFields();
    }

    private void populateFields() {
        if (medicine != null) {
            medicineNameField.setText(medicine.getName());
            genericNameField.setText(medicine.getGenericName());
            manufacturerField.setText(medicine.getManufacturer());
            currentStockField.setText(String.valueOf(medicine.getStockQuantity()));
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

            int addedStock = Integer.parseInt(addStockField.getText().trim());
            medicine.setStockQuantity(medicine.getStockQuantity() + addedStock);
            medicine.setBatchNumber(batchNumberField.getText().trim());
            medicine.setExpiryDate(expiryDatePicker.getValue());

            medicineService.updateMedicine(medicine);
            logger.info("Inventory updated successfully for medicine: {}", medicine.getName());

            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            
            closeDialog();
        } catch (Exception e) {
            logger.error("Error updating inventory", e);
            showError("Error updating inventory: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        if (addStockField.getText().trim().isEmpty()) {
            showError("Add stock quantity is required");
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) medicineNameField.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}