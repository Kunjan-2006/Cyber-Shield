package com.cybershield.model;

import com.cybershield.model.enums.SystemStatus;

public class AffectedSystem {
    private String systemId;
    private String systemName;
    private String department;
    private String ipAddress;
    private String systemType;
    private SystemStatus status;

    public AffectedSystem() {}

    public AffectedSystem(String systemId, String systemName, String department, String ipAddress,
                          String systemType, SystemStatus status) {
        this.systemId = systemId;
        this.systemName = systemName;
        this.department = department;
        this.ipAddress = ipAddress;
        this.systemType = systemType;
        this.status = status;
    }

    public String getSystemId() { return systemId; }
    public void setSystemId(String systemId) { this.systemId = systemId; }

    public String getSystemName() { return systemName; }
    public void setSystemName(String systemName) { this.systemName = systemName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getSystemType() { return systemType; }
    public void setSystemType(String systemType) { this.systemType = systemType; }

    public SystemStatus getStatus() { return status; }
    public void setStatus(SystemStatus status) { this.status = status; }
}
