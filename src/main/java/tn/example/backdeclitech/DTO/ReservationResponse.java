package tn.example.backdeclitech.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.example.backdeclitech.entities.StatusReservation;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationResponse {
    private Long id;
    private LocalDateTime dateReservation;
    private StatusReservation status;
    private String childName;
    private String parentName;
    private Long childId;
    private String moduleName;
    private String phone;
    private Long moduleId;
    private Date sessionDate;
    private String sessionTime;
    private Long moduleSessionId;
    private String cobuildSpaceName;
}