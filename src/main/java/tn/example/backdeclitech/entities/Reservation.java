package tn.example.backdeclitech.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Reservation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private LocalDateTime dateReservation;
    private boolean penalise;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Enumerated(EnumType.STRING)
    private StatusReservation status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JsonIgnoreProperties({"reservations", "presences", "module", "coBuildSpace"})
    private ModuleSession session;

    @ManyToOne
    @JsonIgnoreProperties({"parent"})
    private Child child;

    @ManyToOne
    @JsonIgnoreProperties({"reservationList", "reclamations", "notifications", "presences", "feedBacks"})
    private User parent;

    @ManyToOne
    @JsonIgnoreProperties({"moduleSessions", "reservations", "coBuildSpace"})
    private Module module;

    @ManyToOne
    private User organizingTeam;


    @ManyToOne
    @JsonIgnoreProperties({"reservations"})
    private User user;
    public User getUser() {
        return user;
    }


}
