package tn.example.backdeclitech.presence_managment.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.Map;

@Data
public class PresenceFilterRequest {
     private Long childId;
    private Long parentId;
    private Long siteId;
    private Long sessionId;
    private LocalDate date;
    private Boolean present;
    private String pupilName;
    private String parentName;

    private Map<String, Object> filters;
}