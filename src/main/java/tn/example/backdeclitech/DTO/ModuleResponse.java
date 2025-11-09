package tn.example.backdeclitech.DTO;

import lombok.Data;
import java.util.List;

@Data
public class ModuleResponse {
    private Long id;
    private String title;
    private String description;
    private String jour;
    private Boolean actif;
    private Boolean annule;
    private int enrolledCount;
    private Long coBuildSpaceId;
    private String coBuildSpaceName;
    private List<String> instructeurs;
}