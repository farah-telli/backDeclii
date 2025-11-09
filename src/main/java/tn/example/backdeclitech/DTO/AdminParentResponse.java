package tn.example.backdeclitech.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminParentResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String username;
    private boolean active;
    private boolean isVerified;
    private LocalDateTime registrationDate;
    private LocalDate expirationDate;
    private String notes;
    private List<AdminChildResponse> children;
    private int childrenCount;
    private String createdBy;
    private LocalDateTime lastLoginDate;
    private String status;
}