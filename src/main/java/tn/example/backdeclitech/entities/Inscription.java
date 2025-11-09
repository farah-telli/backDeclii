package tn.example.backdeclitech.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Inscription implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dateInscription;

    private LocalDate dateExpiration;

    private boolean active;

    @Enumerated(EnumType.STRING)
    private StatusInscription statusInscription;

    @OneToOne
    @JoinColumn(name = "child_id", unique = true)
    private Child child;

}
