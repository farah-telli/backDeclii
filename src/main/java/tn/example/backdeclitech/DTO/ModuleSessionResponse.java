package tn.example.backdeclitech.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModuleSessionResponse {
    private Long id;
    private String jour;
    private String heure;
    private String coBuildSpaceName;
    private String moduleTitle;
    private List<String> instructeurs;
    private String tranche;
    private Boolean isActive;
    private boolean isAnnule;
    private int enrolledCount;
    private int capacity;

    private Long moduleId;
    private Long coBuildSpaceId;
}