package com.pharmacymanagement.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    
    @FXML private Label statusLabel;
    @FXML private Label userLabel;
    @FXML private Label dateTimeLabel;

    @FXML
    public void initialize() {
        updateDateTime();
        setupUserInfo();
        // Start a timer to update the date/time every minute
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
        dateTimeLabel.setText(LocalDateTime.now().format(formatter));
    }

    private void setupUserInfo() {
        // Get the current user's name from the system
        String username = System.getProperty("user.name");
        userLabel.setText("Welcome, " + username);
    }

    @FXML
    private void handleViewPatients() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/patient-list.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Patient Management");
            stage.setScene(new Scene(root));
            
            // Set minimum window size
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            
            stage.show();
            updateStatus("Opened patient management");
            
        } catch (IOException e) {
            logger.error("Error opening patient management", e);
            updateStatus("Error opening patient management");
        }
    }

    @FXML
    private void handleAddPatient() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/patient-form.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Patient");
            stage.setScene(new Scene(root));
            
            PatientFormController controller = loader.getController();
            controller.setOnSaveCallback(() -> updateStatus("Patient added successfully"));
            
            stage.showAndWait();
            
        } catch (IOException e) {
            logger.error("Error opening patient form", e);
            updateStatus("Error opening patient form");
        }
    }

    @FXML
    private void handleManageMedicines() {
        updateStatus("Medicine management - Coming soon");
    }

    @FXML
    private void handleViewInventory() {
        updateStatus("Inventory management - Coming soon");
    }

    @FXML
    private void handleViewReports() {
        updateStatus("Reports - Coming soon");
    }

    @FXML
    private void handleSettings() {
        updateStatus("Settings - Coming soon");
    }

    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Pharmacy Management System");
        alert.setHeaderText("Pharmacy Management System");
        alert.setContentText("Version 1.0\n© 2025 Your Company Name\n\nDeveloped by: Your Name");
        alert.showAndWait();
    }

    @FXML
    private void handleExit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Exit");
        alert.setHeaderText("Exit Application");
        alert.setContentText("Are you sure you want to exit?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Platform.exit();
        }
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
        logger.info(message);
    }
}