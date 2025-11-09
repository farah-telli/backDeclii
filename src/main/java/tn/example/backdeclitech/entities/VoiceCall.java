package tn.example.backdeclitech.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "voice_calls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoiceCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String callId;

    @ManyToOne
    @JoinColumn(name = "caller_id", nullable = false)
    private User caller;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CallStatus status = CallStatus.INITIATED;

    @Column(nullable = false)
    private LocalDateTime initiatedAt;

    @Column
    private LocalDateTime answeredAt;

    @Column
    private LocalDateTime endedAt;

    @Column
    private Integer duration; // Durée en secondes

    @Lob
    @Column(columnDefinition = "TEXT")
    private String sdpOffer;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String sdpAnswer;

    @PrePersist
    protected void onCreate() {
        initiatedAt = LocalDateTime.now();
        if (status == null) {
            status = CallStatus.INITIATED;
        }
    }

    public enum CallStatus {
        INITIATED,
        RINGING,
        ANSWERED,
        REJECTED,
        MISSED,
        ENDED,
        CANCELLED
    }
}