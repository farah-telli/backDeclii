package tn.example.backdeclitech.presence_managment.controllers;

import lombok.RequiredArgsConstructor;
import tn.example.backdeclitech.presence_managment.dto.ApiResponse;
import tn.example.backdeclitech.presence_managment.dto.ModuleSessionPresenceDTO;
import tn.example.backdeclitech.presence_managment.entities.ModuleSessionPresence;
import tn.example.backdeclitech.presence_managment.mappers.ModuleSessionPresenceMapper;
import tn.example.backdeclitech.presence_managment.services.ModuleSessionPresenceService;
import tn.example.backdeclitech.presence_managment.utils.filter.GenericFilterRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/module-session-presences")
@RequiredArgsConstructor
public class ModuleSessionPresenceController {

    private final ModuleSessionPresenceService service;
    private final ModuleSessionPresenceMapper mapper;

    @PostMapping
    public ResponseEntity<ApiResponse<ModuleSessionPresenceDTO>> create(@RequestBody ModuleSessionPresenceDTO dto) {
        ModuleSessionPresence entity = mapper.toEntity(dto);
        ModuleSessionPresence saved = service.createModuleSessionPresence(entity);
        return ResponseEntity.ok(ApiResponse.success("Module session presence created", mapper.toDTO(saved)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ModuleSessionPresenceDTO>> getById(@PathVariable Long id) {
        ModuleSessionPresence entity = service.getModuleSessionPresenceById(id);
        return ResponseEntity.ok(ApiResponse.success("Module session presence found", mapper.toDTO(entity)));
    }

    @GetMapping
    public ResponseEntity<List<ModuleSessionPresenceDTO>> getAll(Pageable pageable) {
        Page<ModuleSessionPresence> page = service.getAllModuleSessionPresences(pageable);
        return ResponseEntity.ok(page.map(mapper::toDTO).getContent());
    }
    

    @GetMapping("/date")
    public ResponseEntity<Page<ModuleSessionPresenceDTO>> getByDate(@RequestParam LocalDate date,  Pageable pageable) {
        
        Page<ModuleSessionPresence> modules = service.fetchModuleSessionPresenceByDate(date, pageable);
        return ResponseEntity.ok(modules.map(mapper::toDTO));
    }
    


    @PostMapping("/filter")
    public ResponseEntity<Page<ModuleSessionPresenceDTO>> filter(
            @RequestBody GenericFilterRequest filter,
            Pageable pageable) {
        Page<ModuleSessionPresence> page = service.filterModuleSessionPresences(filter, pageable);
        return ResponseEntity.ok(page.map(mapper::toDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ModuleSessionPresenceDTO>> update(
            @PathVariable Long id,
            @RequestBody ModuleSessionPresenceDTO dto) {
        ModuleSessionPresence entity = mapper.toEntity(dto);
        ModuleSessionPresence updated = service.updateModuleSessionPresence(id, entity);
        return ResponseEntity.ok(ApiResponse.success("Module session presence updated", mapper.toDTO(updated)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ModuleSessionPresenceDTO>> partialUpdate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        ModuleSessionPresence updated = service.partialUpdateModuleSessionPresence(id, updates);
        return ResponseEntity.ok(ApiResponse.success("Module session presence partially updated", mapper.toDTO(updated)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.deleteModuleSessionPresence(id);
        return ResponseEntity.ok(ApiResponse.success("Module session presence deleted"));
    }
}