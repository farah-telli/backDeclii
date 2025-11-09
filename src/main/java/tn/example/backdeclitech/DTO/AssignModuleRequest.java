package tn.example.backdeclitech.DTO;

import lombok.Data;

@Data
public class AssignModuleRequest {
    private Long instructorId;
    private Long moduleId;
    private Long coBuildSpaceId;
    private String jour;
}

