package tn.example.backdeclitech.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FeedBack implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    String content;
    int rating;
    LocalDateTime submittedAt;
    private boolean processed = false;

    @Enumerated(EnumType.STRING)
    private Sentiment sentiment;


    @ManyToOne
    private User parent;

    @ManyToOne(fetch = FetchType.EAGER)
    private Module module;

    @OneToOne
    private Reservation reservation;
}