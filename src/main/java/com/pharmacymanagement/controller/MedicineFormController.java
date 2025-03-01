package com.pharmacymanagement.controller;

import com.pharmacymanagement.model.Medicine;
import com.pharmacymanagement.service.MedicineService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class MedicineFormController {
    private static final Logger logger = LoggerFactory.getLogger(MedicineFormController.class);
    private final MedicineService medicineService = new MedicineService();
    
    @FXML private TextField nameField;
    @FXML private TextField genericNameField;
    @FXML private TextField manufacturerField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField unitPriceField;
    @FXML private TextField stockQuantityField;
    @FXML private TextField minimumStockField;
    @FXML private DatePicker expiryDatePicker;
    @FXML private TextField batchNumberField;
    @FXML private Label errorLabel;

    private Medicine medicine;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        // Initialize category combo box
        List<String> categories = Arrays.asList(
            "Tablets", "Capsules", "Syrups", "Injections", "Ointments",
            "Drops", "Inhalers", "Supplements"
        );
        categoryComboBox.getItems().addAll(categories);

        // Add validation listeners
        unitPriceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                unitPriceField.setText(oldVal);
            }
        });

        stockQuantityField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                stockQuantityField.setText(oldVal);
            }
        });

        minimumStockField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                minimumStockField.setText(oldVal);
            }
        });
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
        this.isEditMode = true;
        populateFields();
    }

    private void populateFields() {
        if (medicine != null) {
            nameField.setText(medicine.getName());
            genericNameField.setText(medicine.getGenericName());
            manufacturerField.setText(medicine.getManufacturer());
            categoryComboBox.setValue(medicine.getCategory());
            unitPriceField.setText(medicine.getUnitPrice().toString());
            stockQuantityField.setText(medicine.getStockQuantity().toString());
            minimumStockField.setText(medicine.getMinimumStockLevel().toString());
            expiryDatePicker.setValue(medicine.getExpiryDate());
            batchNumberField.setText(medicine.getBatchNumber());
        }
    }

    @FXML
    private void handleSave() {
        try {
            if (!validateInput()) {
                return;
            }

            Medicine medicineToSave = isEditMode ? medicine : new Medicine();
            updateMedicineFromFields(medicineToSave);

            if (isEditMode) {
                medicineService.updateMedicine(medicineToSave);
                logger.info("Medicine updated successfully: {}", medicineToSave.getName());
            } else {
                medicineService.addMedicine(medicineToSave);
                logger.info("Medicine added successfully: {}", medicineToSave.getName());
            }

            closeDialog();
        } catch (Exception e) {
            logger.error("Error saving medicine", e);
            showError("Error saving medicine: " + e.getMessage());
        }
    }

    private void updateMedicineFromFields(Medicine medicine) {
        medicine.setName(nameField.getText().trim());
        medicine.setGenericName(genericNameField.getText().trim());
        medicine.setManufacturer(manufacturerField.getText().trim());
        medicine.setCategory(categoryComboBox.getValue());
        medicine.setUnitPrice(new BigDecimal(unitPriceField.getText()));
        medicine.setStockQuantity(Integer.parseInt(stockQuantityField.getText()));
        medicine.setMinimumStockLevel(Integer.parseInt(minimumStockField.getText()));
        medicine.setExpiryDate(expiryDatePicker.getValue());
        medicine.setBatchNumber(batchNumberField.getText().trim());
    }

    private boolean validateInput() {
        if (nameField.getText().trim().isEmpty()) {
            showError("Medicine name is required");
            return false;
        }

        if (unitPriceField.getText().trim().isEmpty()) {
            showError("Unit price is required");
            return false;
        }

        if (stockQuantityField.getText().trim().isEmpty()) {
            showError("Stock quantity is required");
            return false;
        }

        if (minimumStockField.getText().trim().isEmpty()) {
            showError("Minimum stock level is required");
            return false;
        }

        if (expiryDatePicker.getValue() != null && 
            expiryDatePicker.getValue().isBefore(LocalDate.now())) {
            showError("Expiry date cannot be in the past");
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}