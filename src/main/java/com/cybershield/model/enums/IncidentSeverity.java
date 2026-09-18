package com.cybershield.model.enums;

public enum IncidentSeverity {
    CRITICAL(4),
    HIGH(3),
    MEDIUM(2),
    LOW(1);

    private final int level;

    IncidentSeverity(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
