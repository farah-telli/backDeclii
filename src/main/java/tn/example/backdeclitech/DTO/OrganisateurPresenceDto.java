package tn.example.backdeclitech.DTO;

import java.util.Date;

public class OrganisateurPresenceDto {
    private String childName;
    private String parentName;
    private String parentPhone;
    private String moduleName;
    private String cobuildSpaceName;
    private String sessionTime;
    private Date sessionDate;
    private String status;

    public String getChildName() { return childName; }
    public void setChildName(String childName) { this.childName = childName; }
    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }
    public String getParentPhone() { return parentPhone; }
    public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }
    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }
    public String getCobuildSpaceName() { return cobuildSpaceName; }
    public void setCobuildSpaceName(String cobuildSpaceName) { this.cobuildSpaceName = cobuildSpaceName; }
    public String getSessionTime() { return sessionTime; }
    public void setSessionTime(String sessionTime) { this.sessionTime = sessionTime; }
    public Date getSessionDate() { return sessionDate; }
    public void setSessionDate(Date sessionDate) { this.sessionDate = sessionDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
