package tn.example.backdeclitech.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.example.backdeclitech.presence_managment.entities.Presence;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "child")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Child implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String sexe;
    private int age;

    private String cobuildSpace;

    @Enumerated(EnumType.STRING)
    private Titre titre = Titre.EXPLORATEUR;

    private String classe = "Declitech-RI";
    private String codeClasse;
    private String pseudonyme;
    private String motDePasse;

    @ElementCollection
    @CollectionTable(name = "child_projets", joinColumns = @JoinColumn(name = "child_id"))
    @Column(name = "projet")
    private List<String> projets = new ArrayList<>();

    private LocalDate dateDerniereSeance;

    @Column(length = 1000)
    private String informations;

    @ManyToOne
    @JsonBackReference
    private User parent;

    @OneToMany(mappedBy = "child")
    private List<Presence> presences;

    @OneToMany(mappedBy = "child")
    private List<TrackingSheet> trackingSheets;

    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL)
    private List<Penalty> penalties;

    @OneToOne
    private Inscription inscription;

    public boolean isBlocked() {
        return penalties != null && penalties.stream().anyMatch(Penalty::isActive);
    }

    public enum Titre {
        LEADER,
        EXPLORATEUR,
        ARCHITECTE,
        INNOVATEUR;

        @JsonValue
        public String toJson() {
            return this.name().toLowerCase();
        }

        @JsonCreator
        public static Titre fromJson(String value) {
            if (value == null) {
                return EXPLORATEUR;
            }
            try {
                return Titre.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                return EXPLORATEUR; // Valeur par défaut
            }
        }
    }
}