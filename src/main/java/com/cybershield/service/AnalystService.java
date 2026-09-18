package com.cybershield.service;

import com.cybershield.model.Analyst;
import com.cybershield.model.enums.AnalystStatus;
import com.cybershield.repository.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AnalystService {

    public void addAnalyst(Analyst analyst) {
        String sql = "INSERT INTO analysts (analystId, name, skill, experienceLevel, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, analyst.getAnalystId());
            pstmt.setString(2, analyst.getName());
            pstmt.setString(3, analyst.getSkill());
            pstmt.setString(4, analyst.getExperienceLevel());
            pstmt.setString(5, analyst.getStatus().name());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Analyst> getAnalysts() {
        List<Analyst> analysts = new ArrayList<>();
        String sql = "SELECT * FROM analysts";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                analysts.add(new Analyst(
                        rs.getString("analystId"),
                        rs.getString("name"),
                        rs.getString("skill"),
                        rs.getString("experienceLevel"),
                        AnalystStatus.valueOf(rs.getString("status"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return analysts;
    }

    public void updateAnalystStatus(String analystId, AnalystStatus status) {
        String sql = "UPDATE analysts SET status = ? WHERE analystId = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status.name());
            pstmt.setString(2, analystId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Optional<Analyst> findAvailableAnalyst(String requiredSkill) {
        String sql = "SELECT * FROM analysts WHERE status = 'AVAILABLE' AND skill = ? ORDER BY analystId ASC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, requiredSkill);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Analyst(
                            rs.getString("analystId"),
                            rs.getString("name"),
                            rs.getString("skill"),
                            rs.getString("experienceLevel"),
                            AnalystStatus.valueOf(rs.getString("status"))
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
