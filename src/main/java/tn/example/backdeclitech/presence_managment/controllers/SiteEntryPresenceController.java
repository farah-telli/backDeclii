package tn.example.backdeclitech.presence_managment.controllers;

import lombok.RequiredArgsConstructor;
import tn.example.backdeclitech.presence_managment.dto.ApiResponse;
import tn.example.backdeclitech.presence_managment.dto.SiteEntryPresenceDTO;
import tn.example.backdeclitech.presence_managment.entities.SiteEntryPresence;
import tn.example.backdeclitech.presence_managment.mappers.SiteEntryPresenceMapper;
import tn.example.backdeclitech.presence_managment.services.SiteEntryPresenceService;
import tn.example.backdeclitech.presence_managment.utils.filter.GenericFilterRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/site-entry-presences")
@RequiredArgsConstructor
public class SiteEntryPresenceController {

    private final SiteEntryPresenceService service;
    private final SiteEntryPresenceMapper mapper;

    @PostMapping
    public ResponseEntity<ApiResponse<SiteEntryPresenceDTO>> create(@RequestBody SiteEntryPresenceDTO dto) {
        SiteEntryPresence entity = mapper.toEntity(dto);
        SiteEntryPresence saved = service.createSiteEntryPresence(entity);
        return ResponseEntity.ok(ApiResponse.success("Site entry presence created", mapper.toDTO(saved)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SiteEntryPresenceDTO>> getById(@PathVariable Long id) {
        SiteEntryPresence entity = service.getSiteEntryPresenceById(id);
        return ResponseEntity.ok(ApiResponse.success("Site entry presence found", mapper.toDTO(entity)));
    }

    @GetMapping
    public ResponseEntity<List<SiteEntryPresenceDTO>> getAll(Pageable pageable) {
        Page<SiteEntryPresence> page = service.getAllSiteEntryPresences(pageable);
        return ResponseEntity.ok(page.map(mapper::toDTO).getContent());
    }

    @PostMapping("/filter")
    public ResponseEntity<Page<SiteEntryPresenceDTO>> filter(
            @RequestBody GenericFilterRequest filter,
            Pageable pageable) {
        Page<SiteEntryPresence> page = service.filterSiteEntryPresences(filter, pageable);
        return ResponseEntity.ok(page.map(mapper::toDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SiteEntryPresenceDTO>> update(
            @PathVariable Long id,
            @RequestBody SiteEntryPresenceDTO dto) {
        SiteEntryPresence entity = mapper.toEntity(dto);
        SiteEntryPresence updated = service.updateSiteEntryPresence(id, entity);
        return ResponseEntity.ok(ApiResponse.success("Site entry presence updated", mapper.toDTO(updated)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<SiteEntryPresenceDTO>> partialUpdate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        SiteEntryPresence updated = service.partialUpdateSiteEntryPresence(id, updates);
        return ResponseEntity.ok(ApiResponse.success("Site entry presence updated", mapper.toDTO(updated)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.deleteSiteEntryPresence(id);
        return ResponseEntity.ok(ApiResponse.success("Site entry presence deleted"));
    }

    @GetMapping("/date")
    public ResponseEntity<Page<SiteEntryPresenceDTO>> getByDate(@RequestParam LocalDate date, Pageable pageable) {
        Page<SiteEntryPresence> page = service.fetchDailyEntryPage(date, pageable);
        return ResponseEntity.ok(page.map(mapper::toDTO));
    }

    @GetMapping("/between-dates")
    public ResponseEntity<Page<SiteEntryPresenceDTO>> getBetweenDates(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate, Pageable pageable) {
        Page<SiteEntryPresence> page = service.fetchSiteEntryPresencesBetweenDates(startDate, endDate, pageable);
        return ResponseEntity.ok(page.map(mapper::toDTO));
    }
}