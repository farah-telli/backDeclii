package tn.example.backdeclitech.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    // Nouveaux champs pour les messages vocaux
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, columnDefinition = "ENUM('TEXT','VOICE')")
    private MessageType messageType = MessageType.TEXT;

    @Column(name = "audio_file_path", length = 500)
    private String audioFilePath;

    @Column(name = "audio_duration")
    private Integer audioDuration; // Durée en secondes

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
        if (messageType == null) {
            messageType = MessageType.TEXT;
        }
    }

    public enum MessageType {
        TEXT,
        VOICE
    }

    @Column(nullable = false)
    private Boolean isEdited = false;

    @Column
    private LocalDateTime editedAt;
}