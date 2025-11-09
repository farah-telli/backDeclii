package tn.example.backdeclitech.presence_managment.dto;

import lombok.*;
import tn.example.backdeclitech.DTO.CoBuildSpaceDTO;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresenceDTO {
    private Long id;
    private LocalDateTime dateTime;
    private LocalDate reservationDate;
    private String pupilName;
    private Boolean present;
    private String parentName;
    private LocalDateTime lastUpdated;
    private Long childId;
    private Long organizingTeamId;
    private Long parentId;
    private ModuleSessionDTO session;
    private CoBuildSpaceDTO site;
    private String status;
}