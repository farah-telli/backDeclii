package tn.example.backdeclitech.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationPartnerDTO {

    private Long userId;
    private String fullName;
    private String role;
    private Long unreadCount;
    private LocalDateTime lastMessageTime;
}