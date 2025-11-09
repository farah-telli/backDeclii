package tn.example.backdeclitech.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter

public class Reclamation {
    @Id
    @GeneratedValue
    long id;
    String subject;
    String description;
    LocalDateTime dateSoumission;

    @Enumerated(EnumType.STRING)
    private StatutReclamation statut;

    @ManyToOne
    private User parent;

    @ManyToOne
    private Module module;
}
