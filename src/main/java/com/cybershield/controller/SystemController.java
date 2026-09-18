package com.cybershield.controller;

import com.cybershield.model.AffectedSystem;
import com.cybershield.model.enums.SystemStatus;
import com.cybershield.service.SystemService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class SystemController {

    @FXML private TextField txtId;
    @FXML private TextField txtName;
    @FXML private TextField txtDepartment;
    @FXML private TextField txtIp;
    @FXML private TextField txtType;
    @FXML private ComboBox<SystemStatus> cbStatus;

    @FXML private TableView<AffectedSystem> tableSystems;
    @FXML private TableColumn<AffectedSystem, String> colId;
    @FXML private TableColumn<AffectedSystem, String> colName;
    @FXML private TableColumn<AffectedSystem, String> colDepartment;
    @FXML private TableColumn<AffectedSystem, String> colIp;
    @FXML private TableColumn<AffectedSystem, String> colType;
    @FXML private TableColumn<AffectedSystem, String> colStatus;

    private SystemService systemService;

    public SystemController() {
        this.systemService = new SystemService();
    }

    @FXML
    public void initialize() {
        cbStatus.getItems().setAll(SystemStatus.values());
        cbStatus.setValue(SystemStatus.NORMAL);

        colId.setCellValueFactory(new PropertyValueFactory<>("systemId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("systemName"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));
        colIp.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        colType.setCellValueFactory(new PropertyValueFactory<>("systemType"));
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().name()));

        loadSystems();
    }

    @FXML
    private void addSystem() {
        if (txtId.getText().isEmpty() || txtName.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "ID and Name are required.");
            return;
        }

        AffectedSystem system = new AffectedSystem(
                txtId.getText(),
                txtName.getText(),
                txtDepartment.getText(),
                txtIp.getText(),
                txtType.getText(),
                cbStatus.getValue()
        );

        systemService.addSystem(system);
        clearForm();
        loadSystems();
    }

    @FXML
    private void refreshTable() {
        loadSystems();
    }

    private void loadSystems() {
        List<AffectedSystem> systems = systemService.getSystems();
        ObservableList<AffectedSystem> data = FXCollections.observableArrayList(systems);
        tableSystems.setItems(data);
    }

    private void clearForm() {
        txtId.clear();
        txtName.clear();
        txtDepartment.clear();
        txtIp.clear();
        txtType.clear();
        cbStatus.setValue(SystemStatus.NORMAL);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
