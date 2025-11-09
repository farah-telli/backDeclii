package tn.example.backdeclitech.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private Long id;
    private String type;
    private String title;
    private String message;
    private Long sessionId;
    private String moduleTitle;
    private String coBuildSpaceName;
    private String jour;
    private String heure;
    private LocalDateTime timestamp;
    private boolean read;

    public NotificationDTO(String type, String title, String message,
                           Long sessionId, String moduleTitle, String coBuildSpaceName,
                           String jour, String heure) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.sessionId = sessionId;
        this.moduleTitle = moduleTitle;
        this.coBuildSpaceName = coBuildSpaceName;
        this.jour = jour;
        this.heure = heure;
        this.timestamp = LocalDateTime.now();
        this.read = false;
    }
}