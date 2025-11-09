package tn.example.backdeclitech.DTO;

public class CancelWeekRequest {
        private Long moduleSessionId;
        private Long moduleId;
        private Long coBuildSpaceId;
        private String startDate;
        private String endDate;


    public CancelWeekRequest() {}

    public String getStartDate() {
        return startDate;
    }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public Long getModuleId() {
        return moduleId;
    }
    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }

    public Long getCoBuildSpaceId() {
        return coBuildSpaceId;
    }
    public void setCoBuildSpaceId(Long coBuildSpaceId) {
        this.coBuildSpaceId = coBuildSpaceId;
    }
}
