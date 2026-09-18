package com.cybershield.service;

import com.cybershield.model.AffectedSystem;
import com.cybershield.model.enums.SystemStatus;
import com.cybershield.repository.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SystemService {

    public void addSystem(AffectedSystem system) {
        String sql = "INSERT INTO affected_systems (systemId, systemName, department, ipAddress, systemType, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, system.getSystemId());
            pstmt.setString(2, system.getSystemName());
            pstmt.setString(3, system.getDepartment());
            pstmt.setString(4, system.getIpAddress());
            pstmt.setString(5, system.getSystemType());
            pstmt.setString(6, system.getStatus().name());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<AffectedSystem> getSystems() {
        List<AffectedSystem> systems = new ArrayList<>();
        String sql = "SELECT * FROM affected_systems";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                systems.add(new AffectedSystem(
                        rs.getString("systemId"),
                        rs.getString("systemName"),
                        rs.getString("department"),
                        rs.getString("ipAddress"),
                        rs.getString("systemType"),
                        SystemStatus.valueOf(rs.getString("status"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return systems;
    }

    public void updateSystemStatus(String systemId, SystemStatus status) {
        String sql = "UPDATE affected_systems SET status = ? WHERE systemId = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status.name());
            pstmt.setString(2, systemId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
