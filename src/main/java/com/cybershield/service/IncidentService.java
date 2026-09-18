package com.cybershield.service;

import com.cybershield.exception.InvalidIncidentException;
import com.cybershield.exception.NoAnalystAvailableException;
import com.cybershield.model.Analyst;
import com.cybershield.model.CyberIncident;
import com.cybershield.model.ResponseOperation;
import com.cybershield.model.enums.AnalystStatus;
import com.cybershield.model.enums.IncidentSeverity;
import com.cybershield.model.enums.IncidentStatus;
import com.cybershield.model.enums.IncidentType;
import com.cybershield.model.enums.OperationStatus;
import com.cybershield.repository.DatabaseManager;
import com.cybershield.util.ValidationUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.UUID;

public class IncidentService {
    
    // The core PriorityQueue as requested
    private PriorityQueue<CyberIncident> incidentQueue;
    private AnalystService analystService;
    private OperationService operationService;

    public IncidentService() {
        this.incidentQueue = new PriorityQueue<>();
        this.analystService = new AnalystService();
        this.operationService = new OperationService();
        loadQueuedIncidents();
    }

    private void loadQueuedIncidents() {
        List<CyberIncident> allIncidents = getIncidents();
        for (CyberIncident incident : allIncidents) {
            if (incident.getStatus() == IncidentStatus.QUEUED) {
                incidentQueue.add(incident);
            }
        }
    }

    public void validateIncident(CyberIncident incident) throws InvalidIncidentException {
        ValidationUtil.validateIncident(incident);
    }

    public void createIncident(CyberIncident incident) throws InvalidIncidentException {
        validateIncident(incident);
        incident.setStatus(IncidentStatus.REPORTED);
        
        String sql = "INSERT INTO incidents (incidentId, reporter, type, severity, affectedSystemId, description, reportedTime, requiredSkill, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, incident.getIncidentId());
            pstmt.setString(2, incident.getReporter());
            pstmt.setString(3, incident.getType().name());
            pstmt.setString(4, incident.getSeverity().name());
            pstmt.setString(5, incident.getAffectedSystemId());
            pstmt.setString(6, incident.getDescription());
            pstmt.setString(7, incident.getReportedTime().toString());
            pstmt.setString(8, incident.getRequiredSkill());
            pstmt.setString(9, incident.getStatus().name());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        queueIncident(incident);
    }

    public void queueIncident(CyberIncident incident) {
        incident.setStatus(IncidentStatus.QUEUED);
        updateIncidentStatus(incident.getIncidentId(), IncidentStatus.QUEUED);
        incidentQueue.add(incident);
    }

    public void processNextIncident() throws NoAnalystAvailableException {
        if (incidentQueue.isEmpty()) {
            return;
        }

        // Peek first, do not remove until an analyst is successfully found
        CyberIncident highestPriorityIncident = incidentQueue.peek();
        
        Optional<Analyst> availableAnalystOpt = analystService.findAvailableAnalyst(highestPriorityIncident.getRequiredSkill());
        
        if (availableAnalystOpt.isEmpty()) {
            // Throw exception as requested, leaving incident in QUEUED state in the PriorityQueue
            throw new NoAnalystAvailableException("No available analyst found with skill: " + highestPriorityIncident.getRequiredSkill());
        }

        // If analyst available, poll the incident from the queue
        incidentQueue.poll();
        Analyst assignedAnalyst = availableAnalystOpt.get();

        // Update Analyst Status
        analystService.updateAnalystStatus(assignedAnalyst.getAnalystId(), AnalystStatus.ON_MISSION);

        // Update Incident Status
        updateIncidentStatus(highestPriorityIncident.getIncidentId(), IncidentStatus.ASSIGNED);

        // Create Response Operation
        ResponseOperation operation = new ResponseOperation(
                "OP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                highestPriorityIncident.getIncidentId(),
                assignedAnalyst.getAnalystId(),
                LocalDateTime.now(),
                null,
                OperationStatus.ASSIGNED,
                ""
        );
        operationService.createOperation(operation);
    }

    public void updateIncidentStatus(String incidentId, IncidentStatus status) {
        String sql = "UPDATE incidents SET status = ? WHERE incidentId = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status.name());
            pstmt.setString(2, incidentId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<CyberIncident> getIncidents() {
        List<CyberIncident> incidents = new ArrayList<>();
        String sql = "SELECT * FROM incidents ORDER BY reportedTime DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                incidents.add(new CyberIncident(
                        rs.getString("incidentId"),
                        rs.getString("reporter"),
                        IncidentType.valueOf(rs.getString("type")),
                        IncidentSeverity.valueOf(rs.getString("severity")),
                        rs.getString("affectedSystemId"),
                        rs.getString("description"),
                        LocalDateTime.parse(rs.getString("reportedTime")),
                        rs.getString("requiredSkill"),
                        IncidentStatus.valueOf(rs.getString("status"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return incidents;
    }

    public Optional<CyberIncident> getIncidentById(String incidentId) {
        String sql = "SELECT * FROM incidents WHERE incidentId = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, incidentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new CyberIncident(
                            rs.getString("incidentId"),
                            rs.getString("reporter"),
                            IncidentType.valueOf(rs.getString("type")),
                            IncidentSeverity.valueOf(rs.getString("severity")),
                            rs.getString("affectedSystemId"),
                            rs.getString("description"),
                            LocalDateTime.parse(rs.getString("reportedTime")),
                            rs.getString("requiredSkill"),
                            IncidentStatus.valueOf(rs.getString("status"))
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    // Exposed for testing
    public PriorityQueue<CyberIncident> getIncidentQueue() {
        return incidentQueue;
    }
}
