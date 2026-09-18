package com.cybershield.model;

import com.cybershield.model.enums.EvidenceType;
import com.cybershield.model.enums.EvidenceStatus;
import java.time.LocalDateTime;

public class Evidence {
    private String evidenceId;
    private String incidentId;
    private EvidenceType evidenceType;
    private String description;
    private String collectedBy;
    private LocalDateTime collectionTime;
    private EvidenceStatus status;

    public Evidence() {}

    public Evidence(String evidenceId, String incidentId, EvidenceType evidenceType, String description,
                    String collectedBy, LocalDateTime collectionTime, EvidenceStatus status) {
        this.evidenceId = evidenceId;
        this.incidentId = incidentId;
        this.evidenceType = evidenceType;
        this.description = description;
        this.collectedBy = collectedBy;
        this.collectionTime = collectionTime;
        this.status = status;
    }

    public String getEvidenceId() { return evidenceId; }
    public void setEvidenceId(String evidenceId) { this.evidenceId = evidenceId; }

    public String getIncidentId() { return incidentId; }
    public void setIncidentId(String incidentId) { this.incidentId = incidentId; }

    public EvidenceType getEvidenceType() { return evidenceType; }
    public void setEvidenceType(EvidenceType evidenceType) { this.evidenceType = evidenceType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCollectedBy() { return collectedBy; }
    public void setCollectedBy(String collectedBy) { this.collectedBy = collectedBy; }

    public LocalDateTime getCollectionTime() { return collectionTime; }
    public void setCollectionTime(LocalDateTime collectionTime) { this.collectionTime = collectionTime; }

    public EvidenceStatus getStatus() { return status; }
    public void setStatus(EvidenceStatus status) { this.status = status; }
}
