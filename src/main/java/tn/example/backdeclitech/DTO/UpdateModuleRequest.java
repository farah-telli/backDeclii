package tn.example.backdeclitech.DTO;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateModuleRequest {

    @Size(max = 100, message = "Le titre ne peut pas dépasser 100 caractères")
    private String title;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    @Size(max = 50, message = "Le jour ne peut pas dépasser 50 caractères")
    private String jour;


    private Boolean annulation;
    private List<Long> instructorIds;

    private Boolean actif;

    private Long coBuildSpaceId;
}