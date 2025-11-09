package tn.example.backdeclitech.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Penalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private String motif;

    private int dureeBlocage;
    

    @ManyToOne
    private Child child;


    public Penalty() {}

    public Penalty(String motif, int dureeBlocage, Child child) {
        this.date = LocalDate.now();
        this.motif = motif;
        this.dureeBlocage = dureeBlocage;
        this.child = child;
    }

    public boolean isActive() {
        return date.plusDays(dureeBlocage).isAfter(LocalDate.now());
    }
}

