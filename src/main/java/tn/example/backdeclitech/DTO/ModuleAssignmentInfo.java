package tn.example.backdeclitech.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class ModuleAssignmentInfo {
        private Long moduleId;
        private String moduleTitle;
        private String moduleDescription;
        private String jour;
        private Boolean actif;
        private Boolean annule;
        private Long coBuildSpaceId;
        private String coBuildSpaceName;
    }

