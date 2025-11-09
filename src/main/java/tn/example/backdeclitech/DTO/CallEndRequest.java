package tn.example.backdeclitech.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallEndRequest {
    @NotNull(message = "Call ID is required")
    private String callId;
}