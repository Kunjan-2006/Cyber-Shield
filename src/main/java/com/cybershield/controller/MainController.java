package com.cybershield.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainController {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    public void initialize() {
        loadView("/com/cybershield/dashboard.fxml");
    }

    @FXML
    private void showDashboard() {
        loadView("/com/cybershield/dashboard.fxml");
    }

    @FXML
    private void showIncidents() {
        loadView("/com/cybershield/incidents.fxml");
    }

    @FXML
    private void showAnalysts() {
        loadView("/com/cybershield/analysts.fxml");
    }

    @FXML
    private void showSystems() {
        loadView("/com/cybershield/systems.fxml");
    }

    @FXML
    private void showEvidence() {
        loadView("/com/cybershield/evidence.fxml");
    }

    @FXML
    private void showOperations() {
        loadView("/com/cybershield/operations.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
