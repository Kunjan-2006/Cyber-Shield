package com.cybershield.util;

import com.cybershield.exception.InvalidIncidentException;
import com.cybershield.model.CyberIncident;

public class ValidationUtil {

    public static void validateIncident(CyberIncident incident) throws InvalidIncidentException {
        if (incident == null) {
            throw new InvalidIncidentException("Incident cannot be null.");
        }
        if (incident.getReporter() == null || incident.getReporter().trim().isEmpty()) {
            throw new InvalidIncidentException("Reporter is required.");
        }
        if (incident.getType() == null) {
            throw new InvalidIncidentException("Incident type is required.");
        }
        if (incident.getSeverity() == null) {
            throw new InvalidIncidentException("Incident severity is required.");
        }
        if (incident.getDescription() == null || incident.getDescription().trim().isEmpty()) {
            throw new InvalidIncidentException("Incident description is required.");
        }
        if (incident.getRequiredSkill() == null || incident.getRequiredSkill().trim().isEmpty()) {
            throw new InvalidIncidentException("Required skill is missing.");
        }
    }
}
