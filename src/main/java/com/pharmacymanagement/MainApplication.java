package com.pharmacymanagement;

import com.pharmacymanagement.util.DatabaseUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);

    @Override
    public void start(Stage stage) {
        try {
            // Initialize database
            DatabaseUtil.initializeDatabase();
            
            // Load main view
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
            
            stage.setTitle("Pharmacy Management System");
            stage.setScene(scene);
            stage.show();
            
            logger.info("Application started successfully");
        } catch (Exception e) {
            logger.error("Error starting application", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        // Clean up resources
        DatabaseUtil.closeConnection();
        logger.info("Application stopped");
    }

    public static void main(String[] args) {
        launch();
    }
}