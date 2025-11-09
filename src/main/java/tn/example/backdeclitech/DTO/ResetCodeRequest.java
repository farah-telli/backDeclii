package tn.example.backdeclitech.DTO;

import lombok.Data;

@Data
public class ResetCodeRequest {
    private String phoneNumber;
    private String code;
    private String confirmCode;
}
