package com.cybershield.model;

import com.cybershield.model.enums.IncidentSeverity;
import com.cybershield.model.enums.IncidentStatus;
import com.cybershield.model.enums.IncidentType;
import java.time.LocalDateTime;

public class CyberIncident implements Comparable<CyberIncident> {
    private String incidentId;
    private String reporter;
    private IncidentType type;
    private IncidentSeverity severity;
    private String affectedSystemId;
    private String description;
    private LocalDateTime reportedTime;
    private String requiredSkill;
    private IncidentStatus status;

    public CyberIncident() {}

    public CyberIncident(String incidentId, String reporter, IncidentType type, IncidentSeverity severity,
                         String affectedSystemId, String description, LocalDateTime reportedTime,
                         String requiredSkill, IncidentStatus status) {
        this.incidentId = incidentId;
        this.reporter = reporter;
        this.type = type;
        this.severity = severity;
        this.affectedSystemId = affectedSystemId;
        this.description = description;
        this.reportedTime = reportedTime;
        this.requiredSkill = requiredSkill;
        this.status = status;
    }

    // Getters and Setters
    public String getIncidentId() { return incidentId; }
    public void setIncidentId(String incidentId) { this.incidentId = incidentId; }

    public String getReporter() { return reporter; }
    public void setReporter(String reporter) { this.reporter = reporter; }

    public IncidentType getType() { return type; }
    public void setType(IncidentType type) { this.type = type; }

    public IncidentSeverity getSeverity() { return severity; }
    public void setSeverity(IncidentSeverity severity) { this.severity = severity; }

    public String getAffectedSystemId() { return affectedSystemId; }
    public void setAffectedSystemId(String affectedSystemId) { this.affectedSystemId = affectedSystemId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getReportedTime() { return reportedTime; }
    public void setReportedTime(LocalDateTime reportedTime) { this.reportedTime = reportedTime; }

    public String getRequiredSkill() { return requiredSkill; }
    public void setRequiredSkill(String requiredSkill) { this.requiredSkill = requiredSkill; }

    public IncidentStatus getStatus() { return status; }
    public void setStatus(IncidentStatus status) { this.status = status; }

    @Override
    public int compareTo(CyberIncident other) {
        // Higher severity level comes first
        int severityComparison = Integer.compare(other.severity.getLevel(), this.severity.getLevel());
        if (severityComparison != 0) {
            return severityComparison;
        }
        // If severity is the same, earlier reported time comes first
        if (this.reportedTime != null && other.reportedTime != null) {
            return this.reportedTime.compareTo(other.reportedTime);
        }
        return 0;
    }
}
