package tn.example.backdeclitech.services;

import tn.example.backdeclitech.DTO.OrganisateurPresenceDto;
import tn.example.backdeclitech.DTO.Page;

public interface OrganisateurService {
    Page<OrganisateurPresenceDto> getAttendance(
            String type,
            String searchTerm,
            String selectedCoBuildSpace,
            String selectedTitre,
            String startDate,
            String endDate,
            int page,
            int size
    );
}
