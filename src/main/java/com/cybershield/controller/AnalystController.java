package com.cybershield.controller;

import com.cybershield.model.Analyst;
import com.cybershield.model.enums.AnalystStatus;
import com.cybershield.service.AnalystService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class AnalystController {

    @FXML private TextField txtId;
    @FXML private TextField txtName;
    @FXML private TextField txtSkill;
    @FXML private TextField txtExperience;
    @FXML private ComboBox<AnalystStatus> cbStatus;

    @FXML private TableView<Analyst> tableAnalysts;
    @FXML private TableColumn<Analyst, String> colId;
    @FXML private TableColumn<Analyst, String> colName;
    @FXML private TableColumn<Analyst, String> colSkill;
    @FXML private TableColumn<Analyst, String> colExperience;
    @FXML private TableColumn<Analyst, String> colStatus;

    private AnalystService analystService;

    public AnalystController() {
        this.analystService = new AnalystService();
    }

    @FXML
    public void initialize() {
        cbStatus.getItems().setAll(AnalystStatus.values());
        cbStatus.setValue(AnalystStatus.AVAILABLE);

        colId.setCellValueFactory(new PropertyValueFactory<>("analystId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSkill.setCellValueFactory(new PropertyValueFactory<>("skill"));
        colExperience.setCellValueFactory(new PropertyValueFactory<>("experienceLevel"));
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().name()));

        loadAnalysts();
    }

    @FXML
    private void addAnalyst() {
        if (txtId.getText().isEmpty() || txtName.getText().isEmpty() || txtSkill.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "ID, Name, and Skill are required.");
            return;
        }

        Analyst analyst = new Analyst(
                txtId.getText(),
                txtName.getText(),
                txtSkill.getText(),
                txtExperience.getText(),
                cbStatus.getValue()
        );

        analystService.addAnalyst(analyst);
        clearForm();
        loadAnalysts();
    }

    @FXML
    private void refreshTable() {
        loadAnalysts();
    }

    private void loadAnalysts() {
        List<Analyst> analysts = analystService.getAnalysts();
        ObservableList<Analyst> data = FXCollections.observableArrayList(analysts);
        tableAnalysts.setItems(data);
    }

    private void clearForm() {
        txtId.clear();
        txtName.clear();
        txtSkill.clear();
        txtExperience.clear();
        cbStatus.setValue(AnalystStatus.AVAILABLE);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
