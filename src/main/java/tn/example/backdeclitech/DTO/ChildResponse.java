package tn.example.backdeclitech.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChildResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String sexe;
    private int age;
    private Long parentId;

    private String cobuildSpace;
    private String titre;
    private String classe;
    private String codeClasse;
    private String pseudonyme;
    private String motDePasse;
    private List<String> projets = new ArrayList<>();
    private String dateDerniereSeance;
    private String informations;
}