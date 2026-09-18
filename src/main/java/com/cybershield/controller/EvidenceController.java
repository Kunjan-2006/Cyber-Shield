package com.cybershield.controller;

import com.cybershield.model.Evidence;
import com.cybershield.model.enums.EvidenceStatus;
import com.cybershield.model.enums.EvidenceType;
import com.cybershield.service.EvidenceService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class EvidenceController {

    @FXML private TextField txtIncidentId;
    @FXML private ComboBox<EvidenceType> cbType;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtCollectedBy;

    @FXML private TableView<Evidence> tableEvidence;
    @FXML private TableColumn<Evidence, String> colId;
    @FXML private TableColumn<Evidence, String> colIncidentId;
    @FXML private TableColumn<Evidence, String> colType;
    @FXML private TableColumn<Evidence, String> colDescription;
    @FXML private TableColumn<Evidence, String> colCollectedBy;
    @FXML private TableColumn<Evidence, String> colTime;
    @FXML private TableColumn<Evidence, String> colStatus;

    private EvidenceService evidenceService;

    public EvidenceController() {
        this.evidenceService = new EvidenceService();
    }

    @FXML
    public void initialize() {
        cbType.getItems().setAll(EvidenceType.values());

        colId.setCellValueFactory(new PropertyValueFactory<>("evidenceId"));
        colIncidentId.setCellValueFactory(new PropertyValueFactory<>("incidentId"));
        colType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEvidenceType().name()));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCollectedBy.setCellValueFactory(new PropertyValueFactory<>("collectedBy"));
        colTime.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCollectionTime().toString()));
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().name()));

        loadEvidence();
    }

    @FXML
    private void addEvidence() {
        if (txtIncidentId.getText().isEmpty() || cbType.getValue() == null || txtCollectedBy.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Incident ID, Type, and Collected By are required.");
            return;
        }

        Evidence evidence = new Evidence(
                "EV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                txtIncidentId.getText(),
                cbType.getValue(),
                txtDescription.getText(),
                txtCollectedBy.getText(),
                LocalDateTime.now(),
                EvidenceStatus.COLLECTED
        );

        evidenceService.addEvidence(evidence);
        clearForm();
        loadEvidence();
    }

    @FXML
    private void filterByIncident() {
        String incidentId = txtIncidentId.getText();
        if (incidentId != null && !incidentId.trim().isEmpty()) {
            List<Evidence> evidenceList = evidenceService.getEvidenceByIncident(incidentId);
            tableEvidence.setItems(FXCollections.observableArrayList(evidenceList));
        } else {
            loadEvidence();
        }
    }

    @FXML
    private void refreshTable() {
        loadEvidence();
    }

    private void loadEvidence() {
        List<Evidence> evidenceList = evidenceService.getEvidence();
        ObservableList<Evidence> data = FXCollections.observableArrayList(evidenceList);
        tableEvidence.setItems(data);
    }

    private void clearForm() {
        txtIncidentId.clear();
        cbType.setValue(null);
        txtDescription.clear();
        txtCollectedBy.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
