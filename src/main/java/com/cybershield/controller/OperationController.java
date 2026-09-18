package com.cybershield.controller;

import com.cybershield.exception.InvalidOperationException;
import com.cybershield.model.ResponseOperation;
import com.cybershield.model.enums.IncidentStatus;
import com.cybershield.model.enums.OperationStatus;
import com.cybershield.service.IncidentService;
import com.cybershield.service.OperationService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class OperationController {

    @FXML private TableView<ResponseOperation> tableOperations;
    @FXML private TableColumn<ResponseOperation, String> colId;
    @FXML private TableColumn<ResponseOperation, String> colIncidentId;
    @FXML private TableColumn<ResponseOperation, String> colAnalystId;
    @FXML private TableColumn<ResponseOperation, String> colStart;
    @FXML private TableColumn<ResponseOperation, String> colEnd;
    @FXML private TableColumn<ResponseOperation, String> colStatus;
    @FXML private TableColumn<ResponseOperation, String> colNotes;
    
    @FXML private ComboBox<OperationStatus> cbNewStatus;
    @FXML private TextArea txtResolutionNotes;

    private OperationService operationService;
    private IncidentService incidentService; // Needed to update incident status when operation completes

    public OperationController() {
        this.operationService = new OperationService();
        this.incidentService = new IncidentService();
    }

    @FXML
    public void initialize() {
        cbNewStatus.getItems().setAll(OperationStatus.values());

        colId.setCellValueFactory(new PropertyValueFactory<>("operationId"));
        colIncidentId.setCellValueFactory(new PropertyValueFactory<>("incidentId"));
        colAnalystId.setCellValueFactory(new PropertyValueFactory<>("analystId"));
        colStart.setCellValueFactory(cellData -> {
            return cellData.getValue().getStartTime() != null ? 
                    new SimpleStringProperty(cellData.getValue().getStartTime().toString()) : new SimpleStringProperty("");
        });
        colEnd.setCellValueFactory(cellData -> {
            return cellData.getValue().getEndTime() != null ? 
                    new SimpleStringProperty(cellData.getValue().getEndTime().toString()) : new SimpleStringProperty("");
        });
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().name()));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("resolutionNotes"));

        loadOperations();
    }

    @FXML
    private void updateOperationStatus() {
        ResponseOperation selectedOp = tableOperations.getSelectionModel().getSelectedItem();
        if (selectedOp == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select an operation from the table first.");
            return;
        }
        
        OperationStatus newStatus = cbNewStatus.getValue();
        if (newStatus == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a new status.");
            return;
        }

        try {
            operationService.updateOperationStatus(selectedOp.getOperationId(), newStatus, txtResolutionNotes.getText());
            
            // Sync incident status conceptually (e.g. if operation RESOLVED, incident is RESOLVED)
            if (newStatus == OperationStatus.RESOLVED || newStatus == OperationStatus.CLOSED || newStatus == OperationStatus.CONTAINED || newStatus == OperationStatus.INVESTIGATING) {
                try {
                    incidentService.updateIncidentStatus(selectedOp.getIncidentId(), IncidentStatus.valueOf(newStatus.name()));
                } catch(IllegalArgumentException e) {
                    // Ignore if enum names don't exactly match
                }
            }
            
            loadOperations();
            txtResolutionNotes.clear();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Status updated successfully.");
        } catch (InvalidOperationException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Transition", e.getMessage());
        }
    }

    @FXML
    private void refreshTable() {
        loadOperations();
    }

    private void loadOperations() {
        List<ResponseOperation> operations = operationService.getOperations();
        ObservableList<ResponseOperation> data = FXCollections.observableArrayList(operations);
        tableOperations.setItems(data);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
