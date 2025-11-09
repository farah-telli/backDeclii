package tn.example.backdeclitech.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class VoiceMessageRequest {
    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    private Integer duration;
}
