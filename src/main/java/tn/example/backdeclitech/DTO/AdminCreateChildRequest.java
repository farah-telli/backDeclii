package tn.example.backdeclitech.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateChildRequest {
    @NotBlank(message = "Le prénom de l'enfant est obligatoire")
    @Size(max = 50, message = "Le prénom ne peut pas dépasser 50 caractères")
    private String firstName;

    @NotBlank(message = "Le nom de l'enfant est obligatoire")
    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    private String lastName;

    @NotBlank(message = "Le sexe est obligatoire")
    @Pattern(regexp = "^(Masculin|Féminin|M|F)$", message = "Sexe invalide (Masculin/Féminin)")
    private String sexe;

    @NotNull(message = "L'âge est obligatoire")
    @Min(value = 0, message = "L'âge doit être positif")
    @Max(value = 18, message = "L'âge doit être inférieur ou égal à 18")
    private Integer age;




    private String notes;
    private String medicalInfo;
}