package com.cybershield.model;

import com.cybershield.model.enums.OperationStatus;
import java.time.LocalDateTime;

public class ResponseOperation {
    private String operationId;
    private String incidentId;
    private String analystId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private OperationStatus status;
    private String resolutionNotes;

    public ResponseOperation() {}

    public ResponseOperation(String operationId, String incidentId, String analystId, LocalDateTime startTime,
                             LocalDateTime endTime, OperationStatus status, String resolutionNotes) {
        this.operationId = operationId;
        this.incidentId = incidentId;
        this.analystId = analystId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.resolutionNotes = resolutionNotes;
    }

    public String getOperationId() { return operationId; }
    public void setOperationId(String operationId) { this.operationId = operationId; }

    public String getIncidentId() { return incidentId; }
    public void setIncidentId(String incidentId) { this.incidentId = incidentId; }

    public String getAnalystId() { return analystId; }
    public void setAnalystId(String analystId) { this.analystId = analystId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public OperationStatus getStatus() { return status; }
    public void setStatus(OperationStatus status) { this.status = status; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
}
