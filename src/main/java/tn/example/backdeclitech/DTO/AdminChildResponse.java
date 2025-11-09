package tn.example.backdeclitech.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminChildResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String sexe;
    private int age;
    private Long parentId;
    private String parentName;
    private String notes;
    private String medicalInfo;
    private LocalDateTime createdDate;
    private String createdBy;
    private boolean isBlocked;

    private int presenceCount;
    private LocalDateTime lastPresence;
}