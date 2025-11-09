package tn.example.backdeclitech.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstructorModulesResponse {
    private Long instructorId;
    private String instructorName;
    private String instructorEmail;
    private List<ModuleAssignmentInfo> modules;
}