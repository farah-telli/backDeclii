package tn.example.backdeclitech.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CodeVerificationRequest {
    private String phoneNumber;
    private String code;
}
