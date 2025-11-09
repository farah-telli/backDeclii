package tn.example.backdeclitech.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.example.backdeclitech.entities.AuditActionType;
import tn.example.backdeclitech.entities.AuditEntityType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogFilter {
    private AuditActionType actionType;
    private AuditEntityType entityType;
    private Long userId;
    private Long entityId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String searchTerm;
}