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
public class SiteEntryPresenceDTO {
    private Long id;
    private PresenceDTO presence;
    private String supplementaryInfo;
    private String disciplineReport;
}