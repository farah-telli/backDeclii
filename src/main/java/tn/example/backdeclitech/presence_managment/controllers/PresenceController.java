package tn.example.backdeclitech.presence_managment.controllers;

import lombok.RequiredArgsConstructor;
import tn.example.backdeclitech.presence_managment.dto.ApiResponse;
import tn.example.backdeclitech.presence_managment.dto.PresenceDTO;
import tn.example.backdeclitech.presence_managment.entities.Presence;
import tn.example.backdeclitech.presence_managment.mappers.PresenceMapper;
import tn.example.backdeclitech.presence_managment.services.PresenceService;
import tn.example.backdeclitech.presence_managment.utils.filter.GenericFilterRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/presences")
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;
    private final PresenceMapper presenceMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<PresenceDTO>> create(@RequestBody PresenceDTO dto) {
        Presence entity = presenceMapper.toEntity(dto);
        Presence saved = presenceService.savePresence(entity);
        return ResponseEntity.ok(ApiResponse.success("Presence created", presenceMapper.toDTO(saved)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PresenceDTO>> getById(@PathVariable Long id) {
        Optional<Presence> presence = presenceService.findPresenceById(id);
        return presence.map(p -> ResponseEntity.ok(ApiResponse.success("Presence found", presenceMapper.toDTO(p))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<PresenceDTO>> getAll(Pageable pageable) {
        Page<Presence> page = presenceService.getAllPresences(pageable);
        return ResponseEntity.ok(page.map(presenceMapper::toDTO).getContent());
    }

    @PostMapping("/filter")
    public ResponseEntity<Page<PresenceDTO>> filter(
            @RequestBody GenericFilterRequest filter,
            Pageable pageable) {
        Page<Presence> page = presenceService.filterPresences(filter, pageable);
        return ResponseEntity.ok(page.map(presenceMapper::toDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PresenceDTO>> update(
            @PathVariable Long id,
            @RequestBody PresenceDTO dto) {
        Presence entity = presenceMapper.toEntity(dto);
        Presence updated = presenceService.updatePresence(id, entity);
        return ResponseEntity.ok(ApiResponse.success("Presence updated", presenceMapper.toDTO(updated)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<PresenceDTO>> partialUpdate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        Presence updated = presenceService.partialUpdate(id, updates);
        return ResponseEntity.ok(ApiResponse.success("Presence partially updated", presenceMapper.toDTO(updated)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        presenceService.deletePresence(id);
        return ResponseEntity.ok(ApiResponse.success("Presence deleted"));
    }
}