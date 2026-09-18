package com.cybershield;

import com.cybershield.exception.InvalidOperationException;
import com.cybershield.model.ResponseOperation;
import com.cybershield.model.enums.OperationStatus;
import com.cybershield.repository.DatabaseManager;
import com.cybershield.service.OperationService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class OperationServiceTest {

    private OperationService operationService;

    @BeforeAll
    public static void setupDb() {
        DatabaseManager.initializeDatabase();
    }

    @BeforeEach
    public void setup() {
        operationService = new OperationService();
    }

    // 12. Valid operation status transition (ASSIGNED -> INVESTIGATING)
    @Test
    public void testValidTransition_AssignedToInvestigating() {
        assertDoesNotThrow(() -> {
            operationService.validateStatusTransition(OperationStatus.ASSIGNED, OperationStatus.INVESTIGATING);
        });
    }

    // 13. Invalid operation status transition (ASSIGNED -> RESOLVED)
    @Test
    public void testInvalidTransition_AssignedToResolved() {
        assertThrows(InvalidOperationException.class, () -> {
            operationService.validateStatusTransition(OperationStatus.ASSIGNED, OperationStatus.RESOLVED);
        });
    }

    // 14. Invalid operation status transition (CLOSED -> ANY)
    @Test
    public void testInvalidTransition_ClosedToAnything() {
        assertThrows(InvalidOperationException.class, () -> {
            operationService.validateStatusTransition(OperationStatus.CLOSED, OperationStatus.INVESTIGATING);
        });
    }
    
    // 15. Create operation and test valid update
    @Test
    public void testOperationLifecycle() throws Exception {
        String opId = "OP-TEST-" + System.nanoTime();
        
        // Create dummy system, analyst, and incident to satisfy foreign keys
        com.cybershield.service.SystemService sysService = new com.cybershield.service.SystemService();
        sysService.addSystem(new com.cybershield.model.AffectedSystem("SYS-OP", "Sys", "IT", "1.1.1.1", "Server", com.cybershield.model.enums.SystemStatus.NORMAL));
        
        com.cybershield.service.AnalystService anService = new com.cybershield.service.AnalystService();
        anService.addAnalyst(new com.cybershield.model.Analyst("A-TEST", "A", "Skill", "Mid", com.cybershield.model.enums.AnalystStatus.AVAILABLE));
        
        com.cybershield.service.IncidentService incService = new com.cybershield.service.IncidentService();
        com.cybershield.model.CyberIncident inc = new com.cybershield.model.CyberIncident("INC-TEST", "Rep", com.cybershield.model.enums.IncidentType.MALWARE, com.cybershield.model.enums.IncidentSeverity.HIGH, "SYS-OP", "Desc", LocalDateTime.now(), "Skill", com.cybershield.model.enums.IncidentStatus.REPORTED);
        incService.createIncident(inc);

        ResponseOperation operation = new ResponseOperation(
                opId,
                "INC-TEST",
                "A-TEST",
                LocalDateTime.now(),
                null,
                OperationStatus.ASSIGNED,
                ""
        );
        
        operationService.createOperation(operation);
        
        // Test update to INVESTIGATING
        assertDoesNotThrow(() -> {
            operationService.updateOperationStatus(opId, OperationStatus.INVESTIGATING, "Started investigation");
        });
        
        // We cannot test the next part easily without re-querying the DB, but since we trust the service:
        assertThrows(InvalidOperationException.class, () -> {
            // Because it's now INVESTIGATING, going back to ASSIGNED should fail
            operationService.updateOperationStatus(opId, OperationStatus.ASSIGNED, "Invalid step back");
        });
    }

    // 16. Verify closing an operation releases its analyst and closes the incident
    @Test
    public void testAnalystReleasedOnOperationClosed() throws Exception {
        String sysId = "SYS-TEST-16-" + System.nanoTime();
        String anId = "A-TEST-16-" + System.nanoTime();
        String incId = "INC-TEST-16-" + System.nanoTime();
        String opId = "OP-TEST-16-" + System.nanoTime();

        com.cybershield.service.SystemService sysService = new com.cybershield.service.SystemService();
        sysService.addSystem(new com.cybershield.model.AffectedSystem(sysId, "Sys", "IT", "1.1.1.1", "Server", com.cybershield.model.enums.SystemStatus.NORMAL));

        com.cybershield.service.AnalystService anService = new com.cybershield.service.AnalystService();
        anService.addAnalyst(new com.cybershield.model.Analyst(anId, "A16", "Skill", "Mid", com.cybershield.model.enums.AnalystStatus.ON_MISSION));

        com.cybershield.service.IncidentService incService = new com.cybershield.service.IncidentService();
        com.cybershield.model.CyberIncident inc = new com.cybershield.model.CyberIncident(incId, "Rep", com.cybershield.model.enums.IncidentType.MALWARE, com.cybershield.model.enums.IncidentSeverity.HIGH, sysId, "Desc", LocalDateTime.now(), "Skill", com.cybershield.model.enums.IncidentStatus.REPORTED);
        incService.createIncident(inc);

        ResponseOperation operation = new ResponseOperation(opId, incId, anId, LocalDateTime.now(), null, OperationStatus.ASSIGNED, "");
        operationService.createOperation(operation);

        // Move to CLOSED
        operationService.updateOperationStatus(opId, OperationStatus.INVESTIGATING, "");
        operationService.updateOperationStatus(opId, OperationStatus.CONTAINED, "");
        operationService.updateOperationStatus(opId, OperationStatus.RESOLVED, "");
        operationService.updateOperationStatus(opId, OperationStatus.CLOSED, "");

        // Verify Analyst is AVAILABLE
        com.cybershield.model.Analyst updatedAnalyst = anService.getAnalysts().stream().filter(a -> a.getAnalystId().equals(anId)).findFirst().get();
        assertEquals(com.cybershield.model.enums.AnalystStatus.AVAILABLE, updatedAnalyst.getStatus(), "Analyst should be AVAILABLE after operation is closed.");

        // Verify Incident is CLOSED
        com.cybershield.model.CyberIncident updatedIncident = incService.getIncidentById(incId).get();
        assertEquals(com.cybershield.model.enums.IncidentStatus.CLOSED, updatedIncident.getStatus(), "Incident should be CLOSED after operation is closed.");
    }

    // 17. Verify analyst with another active operation remains ON_MISSION
    @Test
    public void testAnalystNotReleasedIfOtherActiveOperations() throws Exception {
        String sysId = "SYS-TEST-17-" + System.nanoTime();
        String anId = "A-TEST-17-" + System.nanoTime();
        String incId1 = "INC-TEST-17A-" + System.nanoTime();
        String incId2 = "INC-TEST-17B-" + System.nanoTime();
        String opId1 = "OP-TEST-17A-" + System.nanoTime();
        String opId2 = "OP-TEST-17B-" + System.nanoTime();

        com.cybershield.service.SystemService sysService = new com.cybershield.service.SystemService();
        sysService.addSystem(new com.cybershield.model.AffectedSystem(sysId, "Sys", "IT", "1.1.1.1", "Server", com.cybershield.model.enums.SystemStatus.NORMAL));

        com.cybershield.service.AnalystService anService = new com.cybershield.service.AnalystService();
        anService.addAnalyst(new com.cybershield.model.Analyst(anId, "A17", "Skill", "Mid", com.cybershield.model.enums.AnalystStatus.ON_MISSION));

        com.cybershield.service.IncidentService incService = new com.cybershield.service.IncidentService();
        com.cybershield.model.CyberIncident inc1 = new com.cybershield.model.CyberIncident(incId1, "Rep", com.cybershield.model.enums.IncidentType.MALWARE, com.cybershield.model.enums.IncidentSeverity.HIGH, sysId, "Desc", LocalDateTime.now(), "Skill", com.cybershield.model.enums.IncidentStatus.REPORTED);
        incService.createIncident(inc1);
        com.cybershield.model.CyberIncident inc2 = new com.cybershield.model.CyberIncident(incId2, "Rep", com.cybershield.model.enums.IncidentType.MALWARE, com.cybershield.model.enums.IncidentSeverity.HIGH, sysId, "Desc", LocalDateTime.now(), "Skill", com.cybershield.model.enums.IncidentStatus.REPORTED);
        incService.createIncident(inc2);

        ResponseOperation op1 = new ResponseOperation(opId1, incId1, anId, LocalDateTime.now(), null, OperationStatus.ASSIGNED, "");
        operationService.createOperation(op1);
        ResponseOperation op2 = new ResponseOperation(opId2, incId2, anId, LocalDateTime.now(), null, OperationStatus.ASSIGNED, "");
        operationService.createOperation(op2);

        // Move op1 to CLOSED
        operationService.updateOperationStatus(opId1, OperationStatus.INVESTIGATING, "");
        operationService.updateOperationStatus(opId1, OperationStatus.CONTAINED, "");
        operationService.updateOperationStatus(opId1, OperationStatus.RESOLVED, "");
        operationService.updateOperationStatus(opId1, OperationStatus.CLOSED, "");

        // Analyst should still be ON_MISSION because op2 is active
        com.cybershield.model.Analyst updatedAnalyst = anService.getAnalysts().stream().filter(a -> a.getAnalystId().equals(anId)).findFirst().get();
        assertEquals(com.cybershield.model.enums.AnalystStatus.ON_MISSION, updatedAnalyst.getStatus(), "Analyst should still be ON_MISSION since op2 is active.");

        // Move op2 to CLOSED
        operationService.updateOperationStatus(opId2, OperationStatus.INVESTIGATING, "");
        operationService.updateOperationStatus(opId2, OperationStatus.CONTAINED, "");
        operationService.updateOperationStatus(opId2, OperationStatus.RESOLVED, "");
        operationService.updateOperationStatus(opId2, OperationStatus.CLOSED, "");

        // Now Analyst should be AVAILABLE
        updatedAnalyst = anService.getAnalysts().stream().filter(a -> a.getAnalystId().equals(anId)).findFirst().get();
        assertEquals(com.cybershield.model.enums.AnalystStatus.AVAILABLE, updatedAnalyst.getStatus(), "Analyst should be AVAILABLE after all operations are closed.");
    }
}
