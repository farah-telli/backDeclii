package tn.example.backdeclitech.presence_managment.controllers;

import lombok.RequiredArgsConstructor;
import tn.example.backdeclitech.presence_managment.dto.ApiResponse;
import tn.example.backdeclitech.presence_managment.dto.SiteExitPresenceDTO;
import tn.example.backdeclitech.presence_managment.entities.SiteExitPresence;
import tn.example.backdeclitech.presence_managment.mappers.SiteExitPresenceMapper;
import tn.example.backdeclitech.presence_managment.services.SiteExitPresenceService;
import tn.example.backdeclitech.presence_managment.utils.filter.GenericFilterRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/site-exit-presences")
@RequiredArgsConstructor
public class SiteExitPresenceController {

    private final SiteExitPresenceService service;
    private final SiteExitPresenceMapper mapper;

    @PostMapping
    public ResponseEntity<ApiResponse<SiteExitPresenceDTO>> create(@RequestBody SiteExitPresenceDTO dto) {
        SiteExitPresence entity = mapper.toEntity(dto);
        SiteExitPresence saved = service.createSiteExitPresence(entity);
        return ResponseEntity.ok(ApiResponse.success("Site exit presence created", mapper.toDTO(saved)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SiteExitPresenceDTO>> getById(@PathVariable Long id) {
        SiteExitPresence entity = service.getSiteExitPresenceById(id);
        return ResponseEntity.ok(ApiResponse.success("Site exit presence found", mapper.toDTO(entity)));
    }

    @GetMapping
    public ResponseEntity<List<SiteExitPresenceDTO>> getAll(Pageable pageable) {
        Page<SiteExitPresence> page = service.getAllSiteExitPresences(pageable);
        return ResponseEntity.ok(page.map(mapper::toDTO).getContent());
    }

    @PostMapping("/filter")
    public ResponseEntity<Page<SiteExitPresenceDTO>> filter(
            @RequestBody GenericFilterRequest filter,
            Pageable pageable) {
        Page<SiteExitPresence> page = service.filterSiteExitPresences(filter, pageable);
        return ResponseEntity.ok(page.map(mapper::toDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SiteExitPresenceDTO>> update(
            @PathVariable Long id,
            @RequestBody SiteExitPresenceDTO dto) {
        SiteExitPresence entity = mapper.toEntity(dto);
        SiteExitPresence updated = service.updateSiteExitPresence(id, entity);
        return ResponseEntity.ok(ApiResponse.success("Site exit presence updated", mapper.toDTO(updated)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<SiteExitPresenceDTO>> partialUpdate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        SiteExitPresence updated = service.partialUpdateSiteExitPresence(id, updates);
        return ResponseEntity.ok(ApiResponse.success("Site exit presence updated", mapper.toDTO(updated)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.deleteSiteExitPresence(id);
        return ResponseEntity.ok(ApiResponse.success("Site exit presence deleted"));
    }
    @GetMapping("/date")
    public ResponseEntity<Page<SiteExitPresenceDTO>> getByDate(@RequestParam LocalDate date, Pageable pageable) {
        Page<SiteExitPresence> site = service.fetchDailyExitPage(date, pageable);
        return ResponseEntity.ok(site.map(mapper::toDTO));
    }

    @GetMapping("/between-dates")
    public ResponseEntity<Page<SiteExitPresenceDTO>> getBetweenDates(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate, Pageable pageable) {
        Page<SiteExitPresence> page = service.fetchSiteExitPresencesBetweenDates(startDate, endDate, pageable);
        return ResponseEntity.ok(page.map(mapper::toDTO));
    }
}