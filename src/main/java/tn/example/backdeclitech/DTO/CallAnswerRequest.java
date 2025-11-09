package tn.example.backdeclitech.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallAnswerRequest {
    @NotNull(message = "Call ID is required")
    private String callId;

    private String sdpAnswer;
}
