package com.cybershield.service;

import com.cybershield.repository.DatabaseManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class DashboardService {

    public Map<String, Integer> getLiveStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
             
            // Total Incidents
            ResultSet rs = stmt.executeQuery("SELECT count(*) FROM incidents");
            if (rs.next()) stats.put("totalIncidents", rs.getInt(1));
            
            // Critical Incidents
            rs = stmt.executeQuery("SELECT count(*) FROM incidents WHERE severity = 'CRITICAL'");
            if (rs.next()) stats.put("criticalIncidents", rs.getInt(1));
            
            // Open/Queued Incidents
            rs = stmt.executeQuery("SELECT count(*) FROM incidents WHERE status IN ('REPORTED', 'QUEUED')");
            if (rs.next()) stats.put("openIncidents", rs.getInt(1));
            
            // Investigating Incidents
            rs = stmt.executeQuery("SELECT count(*) FROM incidents WHERE status = 'INVESTIGATING'");
            if (rs.next()) stats.put("investigatingIncidents", rs.getInt(1));
            
            // Resolved Incidents
            rs = stmt.executeQuery("SELECT count(*) FROM incidents WHERE status IN ('RESOLVED', 'CLOSED')");
            if (rs.next()) stats.put("resolvedIncidents", rs.getInt(1));
            
            // Available Analysts
            rs = stmt.executeQuery("SELECT count(*) FROM analysts WHERE status = 'AVAILABLE'");
            if (rs.next()) stats.put("availableAnalysts", rs.getInt(1));
            
            // Analysts On Mission
            rs = stmt.executeQuery("SELECT count(*) FROM analysts WHERE status = 'ON_MISSION'");
            if (rs.next()) stats.put("analystsOnMission", rs.getInt(1));
            
            // Compromised Systems
            rs = stmt.executeQuery("SELECT count(*) FROM affected_systems WHERE status = 'COMPROMISED'");
            if (rs.next()) stats.put("compromisedSystems", rs.getInt(1));
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return stats;
    }
}
