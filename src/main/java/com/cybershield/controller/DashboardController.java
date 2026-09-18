package com.cybershield.controller;

import com.cybershield.service.DashboardService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class DashboardController {

    @FXML private Label lblTotalIncidents;
    @FXML private Label lblCriticalIncidents;
    @FXML private Label lblOpenIncidents;
    @FXML private Label lblInvestigatingIncidents;
    @FXML private Label lblResolvedIncidents;
    @FXML private Label lblAvailableAnalysts;
    @FXML private Label lblAnalystsOnMission;
    @FXML private Label lblCompromisedSystems;

    private DashboardService dashboardService;
    private Timer timer;

    public DashboardController() {
        dashboardService = new DashboardService();
    }

    @FXML
    public void initialize() {
        refreshDashboard();
        
        // Auto-refresh dashboard every 5 seconds
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> refreshDashboard());
            }
        }, 5000, 5000);
    }
    
    // Call this if navigating away, though simple enough to leave running in background for this academic project.

    private void refreshDashboard() {
        Map<String, Integer> stats = dashboardService.getLiveStatistics();
        
        lblTotalIncidents.setText(String.valueOf(stats.getOrDefault("totalIncidents", 0)));
        lblCriticalIncidents.setText(String.valueOf(stats.getOrDefault("criticalIncidents", 0)));
        lblOpenIncidents.setText(String.valueOf(stats.getOrDefault("openIncidents", 0)));
        lblInvestigatingIncidents.setText(String.valueOf(stats.getOrDefault("investigatingIncidents", 0)));
        lblResolvedIncidents.setText(String.valueOf(stats.getOrDefault("resolvedIncidents", 0)));
        lblAvailableAnalysts.setText(String.valueOf(stats.getOrDefault("availableAnalysts", 0)));
        lblAnalystsOnMission.setText(String.valueOf(stats.getOrDefault("analystsOnMission", 0)));
        lblCompromisedSystems.setText(String.valueOf(stats.getOrDefault("compromisedSystems", 0)));
    }
}
