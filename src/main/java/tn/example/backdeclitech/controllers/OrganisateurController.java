package tn.example.backdeclitech.controllers;

import org.springframework.web.bind.annotation.*;
import tn.example.backdeclitech.DTO.OrganisateurPresenceDto;
import tn.example.backdeclitech.DTO.Page;
import tn.example.backdeclitech.services.OrganisateurService;

@RestController
@RequestMapping("/api/organisateur")
public class OrganisateurController {

    private final OrganisateurService service;

    public OrganisateurController(OrganisateurService service) {
        this.service = service;
    }

    @GetMapping("/attendance")
    public Page<OrganisateurPresenceDto> getAttendance(
            @RequestParam String type,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String selectedCoBuildSpace,
            @RequestParam(required = false) String selectedTitre,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.getAttendance(type, searchTerm, selectedCoBuildSpace, selectedTitre, startDate, endDate, page, size);
    }
}
