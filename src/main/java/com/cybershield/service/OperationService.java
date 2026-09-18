package com.cybershield.service;

import com.cybershield.exception.InvalidOperationException;
import com.cybershield.model.ResponseOperation;
import com.cybershield.model.enums.OperationStatus;
import com.cybershield.repository.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OperationService {

    public void createOperation(ResponseOperation operation) {
        String sql = "INSERT INTO response_operations (operationId, incidentId, analystId, startTime, endTime, status, resolutionNotes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, operation.getOperationId());
            pstmt.setString(2, operation.getIncidentId());
            pstmt.setString(3, operation.getAnalystId());
            pstmt.setString(4, operation.getStartTime() != null ? operation.getStartTime().toString() : null);
            pstmt.setString(5, operation.getEndTime() != null ? operation.getEndTime().toString() : null);
            pstmt.setString(6, operation.getStatus().name());
            pstmt.setString(7, operation.getResolutionNotes());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ResponseOperation> getOperations() {
        List<ResponseOperation> operations = new ArrayList<>();
        String sql = "SELECT * FROM response_operations";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                LocalDateTime startTime = rs.getString("startTime") != null ? LocalDateTime.parse(rs.getString("startTime")) : null;
                LocalDateTime endTime = rs.getString("endTime") != null ? LocalDateTime.parse(rs.getString("endTime")) : null;
                
                operations.add(new ResponseOperation(
                        rs.getString("operationId"),
                        rs.getString("incidentId"),
                        rs.getString("analystId"),
                        startTime,
                        endTime,
                        OperationStatus.valueOf(rs.getString("status")),
                        rs.getString("resolutionNotes")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return operations;
    }

    public void updateOperationStatus(String operationId, OperationStatus newStatus, String resolutionNotes) throws InvalidOperationException {
        Optional<ResponseOperation> opOpt = getOperationById(operationId);
        if (opOpt.isEmpty()) {
            throw new InvalidOperationException("Operation not found.");
        }
        
        ResponseOperation operation = opOpt.get();
        OperationStatus currentStatus = operation.getStatus();
        validateStatusTransition(currentStatus, newStatus);
        
        String sql = "UPDATE response_operations SET status = ?, resolutionNotes = ? WHERE operationId = ?";
        if (newStatus == OperationStatus.CLOSED || newStatus == OperationStatus.RESOLVED) {
             sql = "UPDATE response_operations SET status = ?, resolutionNotes = ?, endTime = ? WHERE operationId = ?";
        }
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newStatus.name());
            pstmt.setString(2, resolutionNotes != null ? resolutionNotes : "");
            
            if (newStatus == OperationStatus.CLOSED || newStatus == OperationStatus.RESOLVED) {
                pstmt.setString(3, LocalDateTime.now().toString());
                pstmt.setString(4, operationId);
            } else {
                pstmt.setString(3, operationId);
            }
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (newStatus == OperationStatus.CLOSED) {
            updateIncidentStatusToClosed(operation.getIncidentId());
            if (!hasActiveOperations(operation.getAnalystId())) {
                updateAnalystStatusToAvailable(operation.getAnalystId());
            }
        }
    }

    private void updateIncidentStatusToClosed(String incidentId) {
        String sql = "UPDATE incidents SET status = 'CLOSED' WHERE incidentId = ? AND status != 'CLOSED'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, incidentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean hasActiveOperations(String analystId) {
        String sql = "SELECT count(*) FROM response_operations WHERE analystId = ? AND status != 'CLOSED'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, analystId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void updateAnalystStatusToAvailable(String analystId) {
        String sql = "UPDATE analysts SET status = 'AVAILABLE' WHERE analystId = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, analystId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Optional<ResponseOperation> getOperationById(String operationId) {
        String sql = "SELECT * FROM response_operations WHERE operationId = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, operationId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    LocalDateTime startTime = rs.getString("startTime") != null ? LocalDateTime.parse(rs.getString("startTime")) : null;
                    LocalDateTime endTime = rs.getString("endTime") != null ? LocalDateTime.parse(rs.getString("endTime")) : null;
                    return Optional.of(new ResponseOperation(
                            rs.getString("operationId"),
                            rs.getString("incidentId"),
                            rs.getString("analystId"),
                            startTime,
                            endTime,
                            OperationStatus.valueOf(rs.getString("status")),
                            rs.getString("resolutionNotes")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void validateStatusTransition(OperationStatus current, OperationStatus next) throws InvalidOperationException {
        boolean valid = false;
        switch (current) {
            case ASSIGNED:
                valid = (next == OperationStatus.INVESTIGATING || next == OperationStatus.CLOSED);
                break;
            case INVESTIGATING:
                valid = (next == OperationStatus.CONTAINED || next == OperationStatus.RESOLVED);
                break;
            case CONTAINED:
                valid = (next == OperationStatus.RESOLVED);
                break;
            case RESOLVED:
                valid = (next == OperationStatus.CLOSED);
                break;
            case CLOSED:
                valid = false; // Cannot transition out of CLOSED
                break;
        }
        if (!valid) {
            throw new InvalidOperationException("Invalid transition from " + current + " to " + next);
        }
    }
}
