package com.cybershield.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;

public class DatabaseManager {
    
    // SQLite connection string
    private static final String URL = "jdbc:sqlite:data/cybershield.db";
    
    /**
     * Get a connection to the SQLite database.
     * Ensure the foreign keys are enabled per connection.
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Initialize the database by creating necessary directories and tables.
     */
    public static void initializeDatabase() {
        // Ensure data directory exists
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create users table
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "username TEXT PRIMARY KEY, " +
                    "role TEXT NOT NULL" +
                    ");";
            stmt.execute(createUsersTable);

            // Create analysts table
            String createAnalystsTable = "CREATE TABLE IF NOT EXISTS analysts (" +
                    "analystId TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "skill TEXT NOT NULL, " +
                    "experienceLevel TEXT, " +
                    "status TEXT NOT NULL" +
                    ");";
            stmt.execute(createAnalystsTable);

            // Create affected_systems table
            String createSystemsTable = "CREATE TABLE IF NOT EXISTS affected_systems (" +
                    "systemId TEXT PRIMARY KEY, " +
                    "systemName TEXT NOT NULL, " +
                    "department TEXT, " +
                    "ipAddress TEXT, " +
                    "systemType TEXT, " +
                    "status TEXT NOT NULL" +
                    ");";
            stmt.execute(createSystemsTable);

            // Create incidents table
            String createIncidentsTable = "CREATE TABLE IF NOT EXISTS incidents (" +
                    "incidentId TEXT PRIMARY KEY, " +
                    "reporter TEXT NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "severity TEXT NOT NULL, " +
                    "affectedSystemId TEXT, " +
                    "description TEXT, " +
                    "reportedTime TEXT NOT NULL, " +
                    "requiredSkill TEXT NOT NULL, " +
                    "status TEXT NOT NULL, " +
                    "FOREIGN KEY (affectedSystemId) REFERENCES affected_systems(systemId)" +
                    ");";
            stmt.execute(createIncidentsTable);

            // Create evidence table
            String createEvidenceTable = "CREATE TABLE IF NOT EXISTS evidence (" +
                    "evidenceId TEXT PRIMARY KEY, " +
                    "incidentId TEXT NOT NULL, " +
                    "evidenceType TEXT NOT NULL, " +
                    "description TEXT, " +
                    "collectedBy TEXT NOT NULL, " +
                    "collectionTime TEXT NOT NULL, " +
                    "status TEXT NOT NULL, " +
                    "FOREIGN KEY (incidentId) REFERENCES incidents(incidentId)" +
                    ");";
            stmt.execute(createEvidenceTable);

            // Create response_operations table
            String createOperationsTable = "CREATE TABLE IF NOT EXISTS response_operations (" +
                    "operationId TEXT PRIMARY KEY, " +
                    "incidentId TEXT NOT NULL, " +
                    "analystId TEXT NOT NULL, " +
                    "startTime TEXT NOT NULL, " +
                    "endTime TEXT, " +
                    "status TEXT NOT NULL, " +
                    "resolutionNotes TEXT, " +
                    "FOREIGN KEY (incidentId) REFERENCES incidents(incidentId), " +
                    "FOREIGN KEY (analystId) REFERENCES analysts(analystId)" +
                    ");";
            stmt.execute(createOperationsTable);

            System.out.println("Database tables initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
