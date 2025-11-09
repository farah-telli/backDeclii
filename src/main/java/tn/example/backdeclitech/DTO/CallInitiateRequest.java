package tn.example.backdeclitech.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallInitiateRequest {
    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    private String sdpOffer;
}
