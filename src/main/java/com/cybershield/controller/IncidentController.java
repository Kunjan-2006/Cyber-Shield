package com.cybershield.controller;

import com.cybershield.exception.InvalidIncidentException;
import com.cybershield.exception.NoAnalystAvailableException;
import com.cybershield.model.CyberIncident;
import com.cybershield.model.enums.IncidentSeverity;
import com.cybershield.model.enums.IncidentStatus;
import com.cybershield.model.enums.IncidentType;
import com.cybershield.service.IncidentService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class IncidentController {

    @FXML private TextField txtReporter;
    @FXML private ComboBox<IncidentType> cbType;
    @FXML private ComboBox<IncidentSeverity> cbSeverity;
    @FXML private TextField txtSystemId;
    @FXML private TextField txtSkill;
    @FXML private TextArea txtDescription;
    
    @FXML private TableView<CyberIncident> tableIncidents;
    @FXML private TableColumn<CyberIncident, String> colId;
    @FXML private TableColumn<CyberIncident, String> colReporter;
    @FXML private TableColumn<CyberIncident, String> colType;
    @FXML private TableColumn<CyberIncident, String> colSeverity;
    @FXML private TableColumn<CyberIncident, String> colSkill;
    @FXML private TableColumn<CyberIncident, String> colStatus;
    
    private IncidentService incidentService;

    public IncidentController() {
        this.incidentService = new IncidentService();
    }

    @FXML
    public void initialize() {
        cbType.getItems().setAll(IncidentType.values());
        cbSeverity.getItems().setAll(IncidentSeverity.values());
        
        colId.setCellValueFactory(new PropertyValueFactory<>("incidentId"));
        colReporter.setCellValueFactory(new PropertyValueFactory<>("reporter"));
        colType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType().name()));
        colSeverity.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSeverity().name()));
        colSkill.setCellValueFactory(new PropertyValueFactory<>("requiredSkill"));
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().name()));
        
        loadIncidents();
    }

    @FXML
    private void createIncident() {
        try {
            CyberIncident incident = new CyberIncident(
                    "INC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    txtReporter.getText(),
                    cbType.getValue(),
                    cbSeverity.getValue(),
                    txtSystemId.getText(),
                    txtDescription.getText(),
                    LocalDateTime.now(),
                    txtSkill.getText(),
                    IncidentStatus.REPORTED
            );
            
            incidentService.createIncident(incident);
            clearForm();
            loadIncidents();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Incident created and queued successfully.");
        } catch (InvalidIncidentException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", e.getMessage());
        }
    }

    @FXML
    private void processNextIncident() {
        try {
            incidentService.processNextIncident();
            loadIncidents();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Highest priority incident processed and analyst assigned.");
        } catch (NoAnalystAvailableException e) {
            showAlert(Alert.AlertType.WARNING, "No Analyst Available", e.getMessage() + "\nIncident remains in QUEUE.");
        } catch (Exception e) {
            if(incidentService.getIncidentQueue().isEmpty()) {
                 showAlert(Alert.AlertType.INFORMATION, "Queue Empty", "No queued incidents to process.");
            } else {
                 showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    @FXML
    private void refreshTable() {
        loadIncidents();
    }

    private void loadIncidents() {
        List<CyberIncident> incidents = incidentService.getIncidents();
        ObservableList<CyberIncident> data = FXCollections.observableArrayList(incidents);
        tableIncidents.setItems(data);
    }

    private void clearForm() {
        txtReporter.clear();
        cbType.setValue(null);
        cbSeverity.setValue(null);
        txtSystemId.clear();
        txtSkill.clear();
        txtDescription.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
