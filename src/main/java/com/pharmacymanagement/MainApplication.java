package com.pharmacymanagement;

import com.pharmacymanagement.util.DatabaseUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database
            DatabaseUtil.initializeDatabase();

            // Load the main view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
            Parent root = loader.load();

            // Configure the primary stage
            primaryStage.setTitle("Pharmacy Management System");
            primaryStage.setScene(new Scene(root));
            
            // Set minimum window size
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(800);

            // Add application icon if available
            try {
                primaryStage.getIcons().add(
                    new Image(getClass().getResourceAsStream("/images/icon.png"))
                );
            } catch (Exception e) {
                logger.warn("Application icon not found", e);
            }

            // Handle window close event
            primaryStage.setOnCloseRequest(event -> {
                Platform.exit();
            });

            primaryStage.show();
            logger.info("Application started successfully");

        } catch (Exception e) {
            logger.error("Error starting application", e);
            throw new RuntimeException("Error starting application", e);
        }
    }

    @Override
    public void stop() {
        try {
            DatabaseUtil.closeConnection();
            logger.info("Application stopped successfully");
        } catch (Exception e) {
            logger.error("Error stopping application", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}