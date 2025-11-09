package tn.example.backdeclitech.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.example.backdeclitech.entities.AuditEntityType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestoreRequest {
    private Long auditLogId;
    private AuditEntityType entityType;
    private Long entityId;
    private String reason;
}