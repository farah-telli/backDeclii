package tn.example.backdeclitech.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditEntityType entityType;

    @Column(nullable = false)
    private Long entityId;

    @Column(length = 255)
    private String entityName;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 255)
    private String userName;

    @Column(nullable = false, length = 255)
    private String userEmail;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String oldValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    @Column(columnDefinition = "TEXT")
    private String changes;

    @Column(length = 45)
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private Integer affectedRecords;

    // Méthode helper pour créer un log
    public static AuditLog createLog(
            AuditActionType actionType,
            AuditEntityType entityType,
            Long entityId,
            String entityName,
            User currentUser,
            String description
    ) {
        AuditLog log = new AuditLog();
        log.setActionType(actionType);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setEntityName(entityName);
        log.setUserId(currentUser.getId());
        log.setUserName(currentUser.getFirstName() + " " + currentUser.getLastName());
        log.setUserEmail(currentUser.getEmail());
        log.setDescription(description);
        log.setTimestamp(LocalDateTime.now());
        return log;
    }
}