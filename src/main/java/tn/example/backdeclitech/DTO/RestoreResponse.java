package tn.example.backdeclitech.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestoreResponse {
    private boolean success;
    private String message;
    private Object restoredEntity;
    private Long newAuditLogId;
}
