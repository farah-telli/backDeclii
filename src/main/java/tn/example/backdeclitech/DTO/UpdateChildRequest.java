package tn.example.backdeclitech.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateChildRequest {
    private String cobuildSpace;
    private String titre;
    private String classe;
    private String codeClasse;
    private String pseudonyme;
    private String motDePasse;
    private List<String> projets;
    private String dateDerniereSeance;
    private String informations;
}