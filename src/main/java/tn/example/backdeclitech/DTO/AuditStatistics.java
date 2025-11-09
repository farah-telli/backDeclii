package tn.example.backdeclitech.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.example.backdeclitech.entities.AuditActionType;
import tn.example.backdeclitech.entities.AuditEntityType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditStatistics {
    private Long totalActions;
    private Long uniqueUsers;
    private Long modifications;
    private Long deletions;
    private Long creations;
    private Long logins;

    private java.util.Map<AuditActionType, Long> actionsByType;

    private java.util.Map<AuditEntityType, Long> actionsByEntity;

    private List<UserActivity> topUsers;
}