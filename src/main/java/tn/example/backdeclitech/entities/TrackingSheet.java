package tn.example.backdeclitech.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrackingSheet {
    @Id
    @GeneratedValue
     Long id;
     LocalDateTime trackingDate;
     String Activities;
     Date DateDernierSeance;
     String Level;

    @ManyToOne
    private Child child;

    @ManyToOne
    private Module module;

    @ManyToOne
    private User instructor;

}
