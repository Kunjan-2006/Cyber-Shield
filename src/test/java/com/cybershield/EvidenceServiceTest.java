package com.cybershield;

import com.cybershield.exception.EvidenceNotFoundException;
import com.cybershield.model.Evidence;
import com.cybershield.model.enums.EvidenceStatus;
import com.cybershield.model.enums.EvidenceType;
import com.cybershield.repository.DatabaseManager;
import com.cybershield.service.EvidenceService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EvidenceServiceTest {

    private EvidenceService evidenceService;

    @BeforeAll
    public static void setupDb() {
        DatabaseManager.initializeDatabase();
    }

    @BeforeEach
    public void setup() {
        evidenceService = new EvidenceService();
    }

    // 16. Evidence creation
    @Test
    public void testAddAndRetrieveEvidence() {
        String evId = "EV-TEST-" + System.nanoTime();
        
        try {
            // Create dummy system and incident to satisfy foreign keys
            com.cybershield.service.SystemService sysService = new com.cybershield.service.SystemService();
            sysService.addSystem(new com.cybershield.model.AffectedSystem("SYS-EV", "Sys", "IT", "1.1.1.1", "Server", com.cybershield.model.enums.SystemStatus.NORMAL));
            
            com.cybershield.service.IncidentService incService = new com.cybershield.service.IncidentService();
            com.cybershield.model.CyberIncident inc = new com.cybershield.model.CyberIncident("INC-123", "Rep", com.cybershield.model.enums.IncidentType.MALWARE, com.cybershield.model.enums.IncidentSeverity.HIGH, "SYS-EV", "Desc", LocalDateTime.now(), "Skill", com.cybershield.model.enums.IncidentStatus.REPORTED);
            incService.createIncident(inc);
        } catch (Exception e) {}

        Evidence evidence = new Evidence(
                evId,
                "INC-123",
                EvidenceType.LOG,
                "Server Logs",
                "Analyst Bob",
                LocalDateTime.now(),
                EvidenceStatus.COLLECTED
        );
        
        evidenceService.addEvidence(evidence);
        
        List<Evidence> retrieved = evidenceService.getEvidenceByIncident("INC-123");
        boolean found = retrieved.stream().anyMatch(e -> e.getEvidenceId().equals(evId));
        assertTrue(found, "Evidence should be retrievable after creation");
    }

    // 17. Evidence not found behavior
    @Test
    public void testUpdateEvidence_NotFound() {
        String nonExistentId = "EV-UNKNOWN-" + System.nanoTime();
        
        assertThrows(EvidenceNotFoundException.class, () -> {
            evidenceService.updateEvidenceStatus(nonExistentId, EvidenceStatus.VERIFIED);
        });
    }
}
