package tn.example.backdeclitech.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private String content;
    private LocalDateTime sentAt;
    private Boolean isRead;
    private String messageType;
    private String audioUrl;
    private Integer audioDuration;
    private Boolean isEdited;
    private LocalDateTime editedAt;
}


