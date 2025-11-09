package tn.example.backdeclitech.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.example.backdeclitech.entities.AuditActionType;
import tn.example.backdeclitech.entities.AuditEntityType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private AuditActionType actionType;
    private AuditEntityType entityType;
    private Long entityId;
    private String entityName;
    private Long userId;
    private String userName;
    private String userEmail;
    private LocalDateTime timestamp;
    private String oldValue;
    private String newValue;
    private List<AuditChange> changes;
    private String ipAddress;
    private String userAgent;
    private String description;
    private Integer affectedRecords;
}