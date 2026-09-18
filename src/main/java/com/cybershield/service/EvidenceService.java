package com.cybershield.service;

import com.cybershield.exception.EvidenceNotFoundException;
import com.cybershield.model.Evidence;
import com.cybershield.model.enums.EvidenceStatus;
import com.cybershield.model.enums.EvidenceType;
import com.cybershield.repository.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EvidenceService {

    public void addEvidence(Evidence evidence) {
        String sql = "INSERT INTO evidence (evidenceId, incidentId, evidenceType, description, collectedBy, collectionTime, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, evidence.getEvidenceId());
            pstmt.setString(2, evidence.getIncidentId());
            pstmt.setString(3, evidence.getEvidenceType().name());
            pstmt.setString(4, evidence.getDescription());
            pstmt.setString(5, evidence.getCollectedBy());
            pstmt.setString(6, evidence.getCollectionTime().toString());
            pstmt.setString(7, evidence.getStatus().name());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Evidence> getEvidence() {
        List<Evidence> evidenceList = new ArrayList<>();
        String sql = "SELECT * FROM evidence";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                evidenceList.add(new Evidence(
                        rs.getString("evidenceId"),
                        rs.getString("incidentId"),
                        EvidenceType.valueOf(rs.getString("evidenceType")),
                        rs.getString("description"),
                        rs.getString("collectedBy"),
                        LocalDateTime.parse(rs.getString("collectionTime")),
                        EvidenceStatus.valueOf(rs.getString("status"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return evidenceList;
    }

    public List<Evidence> getEvidenceByIncident(String incidentId) {
        List<Evidence> evidenceList = new ArrayList<>();
        String sql = "SELECT * FROM evidence WHERE incidentId = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, incidentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    evidenceList.add(new Evidence(
                            rs.getString("evidenceId"),
                            rs.getString("incidentId"),
                            EvidenceType.valueOf(rs.getString("evidenceType")),
                            rs.getString("description"),
                            rs.getString("collectedBy"),
                            LocalDateTime.parse(rs.getString("collectionTime")),
                            EvidenceStatus.valueOf(rs.getString("status"))
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return evidenceList;
    }

    public void updateEvidenceStatus(String evidenceId, EvidenceStatus status) throws EvidenceNotFoundException {
        String checkSql = "SELECT count(*) FROM evidence WHERE evidenceId = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, evidenceId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    throw new EvidenceNotFoundException("Evidence not found with ID: " + evidenceId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sql = "UPDATE evidence SET status = ? WHERE evidenceId = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status.name());
            pstmt.setString(2, evidenceId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
