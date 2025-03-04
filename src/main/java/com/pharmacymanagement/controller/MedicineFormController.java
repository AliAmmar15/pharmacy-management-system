package com.pharmacymanagement.controller;

import com.pharmacymanagement.model.Medicine;
import com.pharmacymanagement.service.MedicineService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.math.BigDecimal;

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
    @FXML private TextField batchNumberField;
    @FXML private DatePicker expiryDatePicker;
    @FXML private Label errorLabel;

    private Medicine medicine;
    private boolean isEditMode = false;
    private Runnable onSaveCallback;

    @FXML
    public void initialize() {
        setupCategoryComboBox();
        setupValidation();
        errorLabel.setVisible(false);
    }

    private void setupCategoryComboBox() {
        categoryComboBox.getItems().addAll(
            "Tablet",
            "Capsule",
            "Syrup",
            "Injection",
            "Cream",
            "Ointment",
            "Drops",
            "Inhaler",
            "Other"
        );
    }

    private void setupValidation() {
        // Numeric validation for price
        unitPriceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                unitPriceField.setText(oldVal);
            }
        });

        // Integer validation for quantities
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
        this.isEditMode = true;
        populateFields();
    }

    private void populateFields() {
        if (medicine != null) {
            nameField.setText(medicine.getName());
            genericNameField.setText(medicine.getGenericName());
            manufacturerField.setText(medicine.getManufacturer());
            categoryComboBox.setValue(medicine.getCategory());
            unitPriceField.setText(String.valueOf(medicine.getUnitPrice()));
            stockQuantityField.setText(String.valueOf(medicine.getStockQuantity()));
            minimumStockField.setText(String.valueOf(medicine.getMinimumStockLevel()));
            batchNumberField.setText(medicine.getBatchNumber());
            expiryDatePicker.setValue(medicine.getExpiryDate());
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

            Medicine medicineToSave = isEditMode ? medicine : new Medicine();
            updateMedicineFromFields(medicineToSave);

            if (isEditMode) {
                medicineService.updateMedicine(medicineToSave);
                logger.info("Medicine updated successfully: {}", medicineToSave.getName());
            } else {
                medicineService.addMedicine(medicineToSave);
                logger.info("Medicine added successfully: {}", medicineToSave.getName());
            }

            if (onSaveCallback != null) {
                onSaveCallback.run();
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
        medicine.setUnitPrice(new BigDecimal(unitPriceField.getText().trim()));
        medicine.setStockQuantity(Integer.parseInt(stockQuantityField.getText().trim()));
        medicine.setMinimumStockLevel(Integer.parseInt(minimumStockField.getText().trim()));
        medicine.setBatchNumber(batchNumberField.getText().trim());
        medicine.setExpiryDate(expiryDatePicker.getValue());
    }

    private boolean validateInput() {
        if (nameField.getText().trim().isEmpty()) {
            showError("Medicine name is required");
            return false;
        }

        if (genericNameField.getText().trim().isEmpty()) {
            showError("Generic name is required");
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