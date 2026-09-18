package com.cybershield;

import com.cybershield.exception.InvalidIncidentException;
import com.cybershield.exception.NoAnalystAvailableException;
import com.cybershield.model.Analyst;
import com.cybershield.model.CyberIncident;
import com.cybershield.model.enums.AnalystStatus;
import com.cybershield.model.enums.IncidentSeverity;
import com.cybershield.model.enums.IncidentStatus;
import com.cybershield.model.enums.IncidentType;
import com.cybershield.repository.DatabaseManager;
import com.cybershield.service.AnalystService;
import com.cybershield.service.IncidentService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class IncidentServiceTest {

    private IncidentService incidentService;
    private AnalystService analystService;

    @BeforeAll
    public static void setupDb() {
        // Initialize SQLite in test environment (same DB for simplicity in this project)
        DatabaseManager.initializeDatabase();
    }

    @BeforeEach
    public void setup() {
        incidentService = new IncidentService();
        analystService = new AnalystService();
        // Clear the queue for isolated tests
        incidentService.getIncidentQueue().clear();
    }

    // 1. Critical incident gets highest priority
    @Test
    public void testPriorityQueue_CriticalOverHigh() {
        CyberIncident low = createDummyIncident(IncidentSeverity.LOW);
        CyberIncident critical = createDummyIncident(IncidentSeverity.CRITICAL);
        
        incidentService.queueIncident(low);
        incidentService.queueIncident(critical);
        
        assertEquals(IncidentSeverity.CRITICAL, incidentService.getIncidentQueue().peek().getSeverity());
    }

    // 2. High priority comes before medium
    @Test
    public void testPriorityQueue_HighOverMedium() {
        CyberIncident medium = createDummyIncident(IncidentSeverity.MEDIUM);
        CyberIncident high = createDummyIncident(IncidentSeverity.HIGH);
        
        incidentService.queueIncident(medium);
        incidentService.queueIncident(high);
        
        assertEquals(IncidentSeverity.HIGH, incidentService.getIncidentQueue().peek().getSeverity());
    }

    // 3. Medium comes before low
    @Test
    public void testPriorityQueue_MediumOverLow() {
        CyberIncident low = createDummyIncident(IncidentSeverity.LOW);
        CyberIncident medium = createDummyIncident(IncidentSeverity.MEDIUM);
        
        incidentService.queueIncident(low);
        incidentService.queueIncident(medium);
        
        assertEquals(IncidentSeverity.MEDIUM, incidentService.getIncidentQueue().peek().getSeverity());
    }

    // 4. Same severity tie-breaking (earlier time comes first)
    @Test
    public void testPriorityQueue_SameSeverityTieBreaker() throws InterruptedException {
        CyberIncident first = createDummyIncident(IncidentSeverity.HIGH);
        first.setReportedTime(LocalDateTime.now().minusMinutes(10));
        
        CyberIncident second = createDummyIncident(IncidentSeverity.HIGH);
        second.setReportedTime(LocalDateTime.now());
        
        incidentService.queueIncident(second);
        incidentService.queueIncident(first);
        
        assertEquals(first.getIncidentId(), incidentService.getIncidentQueue().peek().getIncidentId());
    }

    // 5. Invalid incident validation (null reporter)
    @Test
    public void testValidation_NullReporter() {
        CyberIncident invalid = createDummyIncident(IncidentSeverity.HIGH);
        invalid.setReporter(null);
        
        assertThrows(InvalidIncidentException.class, () -> incidentService.validateIncident(invalid));
    }

    // 6. Invalid incident validation (empty description)
    @Test
    public void testValidation_EmptyDescription() {
        CyberIncident invalid = createDummyIncident(IncidentSeverity.HIGH);
        invalid.setDescription("   ");
        
        assertThrows(InvalidIncidentException.class, () -> incidentService.validateIncident(invalid));
    }

    // 7. No analyst available exception & 8. Incident remains QUEUED
    @Test
    public void testProcessNext_NoAnalystAvailable() {
        CyberIncident incident = createDummyIncident(IncidentSeverity.CRITICAL);
        incident.setRequiredSkill("Quantum Cryptography"); // Extremely unlikely skill
        incidentService.queueIncident(incident);
        
        assertThrows(NoAnalystAvailableException.class, () -> incidentService.processNextIncident());
        
        // Ensure incident is still in queue
        assertFalse(incidentService.getIncidentQueue().isEmpty());
        assertEquals(IncidentStatus.QUEUED, incidentService.getIncidentQueue().peek().getStatus());
    }

    // 9. Correct analyst skill matching & 10. Incident becomes ASSIGNED
    @Test
    public void testProcessNext_AnalystAvailable() throws Exception {
        // Create an affected system first to avoid foreign key failure
        com.cybershield.service.SystemService systemService = new com.cybershield.service.SystemService();
        systemService.addSystem(new com.cybershield.model.AffectedSystem(
                "SYS-1", "Test System", "IT", "192.168.1.1", "Server", com.cybershield.model.enums.SystemStatus.NORMAL
        ));

        // Setup Analyst
        Analyst analyst = new Analyst("T-ANALYST-1", "Test Analyst", "Malware Analysis", "Mid", AnalystStatus.AVAILABLE);
        analystService.addAnalyst(analyst);
        
        // Ensure analyst is available
        analystService.updateAnalystStatus("T-ANALYST-1", AnalystStatus.AVAILABLE);

        CyberIncident incident = createDummyIncident(IncidentSeverity.CRITICAL);
        incident.setRequiredSkill("Malware Analysis");
        
        incidentService.createIncident(incident); // This also queues it
        
        // This should not throw an exception, it should assign it
        incidentService.processNextIncident();
        
        // Queue should be empty for this skill now if it was the only one
        // Check DB for status
        CyberIncident updated = incidentService.getIncidentById(incident.getIncidentId()).orElse(null);
        assertNotNull(updated);
        assertEquals(IncidentStatus.ASSIGNED, updated.getStatus());
        
        // Clean up analyst status for other tests
        analystService.updateAnalystStatus("T-ANALYST-1", AnalystStatus.UNAVAILABLE);
    }
    
    // 11. Analyst unavailable behavior (Wrong skill)
    @Test
    public void testProcessNext_WrongSkillAnalystAvailable() {
        // Analyst has Network Security, Incident needs Forensics
        Analyst analyst = new Analyst("T-ANALYST-2", "Net Analyst", "Network Security", "Mid", AnalystStatus.AVAILABLE);
        analystService.addAnalyst(analyst);
        analystService.updateAnalystStatus("T-ANALYST-2", AnalystStatus.AVAILABLE);

        CyberIncident incident = createDummyIncident(IncidentSeverity.HIGH);
        incident.setRequiredSkill("Digital Forensics");
        incidentService.queueIncident(incident);
        
        assertThrows(NoAnalystAvailableException.class, () -> incidentService.processNextIncident());
        assertEquals(IncidentStatus.QUEUED, incidentService.getIncidentQueue().peek().getStatus());
    }

    private CyberIncident createDummyIncident(IncidentSeverity severity) {
        return new CyberIncident(
                "TEST-" + System.nanoTime(),
                "John Doe",
                IncidentType.MALWARE,
                severity,
                "SYS-1",
                "Test Description",
                LocalDateTime.now(),
                "Malware Analysis",
                IncidentStatus.QUEUED
        );
    }
}
