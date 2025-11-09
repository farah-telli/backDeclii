package tn.example.backdeclitech.presence_managment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteExitPresenceDTO {
    private Long id;
    private PresenceDTO presence;
    private String exitNotes;
    private Boolean authorizedPickup;
    private String lostItems;
    private Boolean idVerified;
    private String incidentReport;
    private Boolean pickupSignatureObtained;
}