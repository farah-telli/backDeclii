package tn.example.backdeclitech.entities;

import java.time.LocalDate;
import jakarta.persistence.Convert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.example.backdeclitech.presence_managment.entities.Presence;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModuleSession implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    private Date date;

    private String dayName;

    LocalTime StartTime;
    LocalTime EndTime;
    int Capcity;
    int EnrolledCount;
    boolean isActive;
    boolean isAnnule;

    @Column(name = "date_annulation")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateAnnulation;

    @Min(value = 0, message = "L'âge minimum doit être positif")
    @Max(value = 17, message = "L'âge minimum ne peut pas dépasser 17")
    @Column(name = "tranche_age_min")
    private int trancheAgeMin;

    @Min(value = 0, message = "L'âge maximum doit être positif")
    @Max(value = 17, message = "L'âge maximum ne peut pas dépasser 17")
    @Column(name = "tranche_age_max")
    private int trancheAgeMax;

    @ManyToOne
    @JsonIgnoreProperties({"moduleSessions", "reservations", "feedbacks", "newsList", "reclamations", "notifications", "coBuildSpace", "instructors"})
    private Module module;

    @OneToMany(mappedBy = "session", fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"session", "module", "child", "parent", "OrganizingTeam"})
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "session")
    @JsonIgnore
    private List<Presence> presences = new ArrayList<>();

    @ManyToOne
    @JsonIgnoreProperties({"modules"})
    private CoBuildSpace coBuildSpace;

    public String getFormattedTranche() {
        return trancheAgeMin + "-" + trancheAgeMax;
    }


    @JsonIgnore
    public List<User> getEnrolledUsers() {
        if (reservations == null || reservations.isEmpty()) {
            return new ArrayList<>();
        }

        return reservations.stream()
                .filter(reservation -> reservation != null)
                .map(reservation -> {
                    try {
                        return reservation.getParent();
                    } catch (Exception e) {
                        try {
                            // Si getParent() n'existe pas, essayer getUser()
                            return (User) reservation.getClass().getMethod("getUser").invoke(reservation);
                        } catch (Exception ex) {
                            return null;
                        }
                    }
                })
                .filter(user -> user != null)
                .distinct()
                .collect(Collectors.toList());
    }


    @JsonIgnore
    public int getActualEnrolledCount() {
        if (reservations == null) {
            return 0;
        }
        return (int) reservations.stream()
                .filter(r -> r != null && r.getStatus() != StatusReservation.CANCELED)
                .count();
    }


    @JsonIgnore
    public boolean isFull() {
        return getActualEnrolledCount() >= Capcity;
    }

    @JsonIgnore
    public int getAvailableSpots() {
        return Math.max(0, Capcity - getActualEnrolledCount());
    }

    public String getFormattedTime() {
        if (StartTime != null && EndTime != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return StartTime.format(formatter) + " - " + EndTime.format(formatter);
        }
        return "N/A";
    }
}