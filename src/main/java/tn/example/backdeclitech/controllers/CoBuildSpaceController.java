package tn.example.backdeclitech.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import tn.example.backdeclitech.DTO.CoBuildSpaceDTO;
import tn.example.backdeclitech.services.CoBuildSpaceService;
import tn.example.backdeclitech.entities.CoBuildSpace;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cobuildSpaces")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CoBuildSpaceController {

    private final CoBuildSpaceService service;

    @PostMapping
    public ResponseEntity<CoBuildSpace> create(@RequestBody CoBuildSpace space) {
        CoBuildSpace saved = service.save(space);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<CoBuildSpaceDTO>> getAll() {
        List<CoBuildSpace> spaces = service.getAll();
        List<CoBuildSpaceDTO> dtos = spaces.stream()
                .map(space -> new CoBuildSpaceDTO(
                    space.getSpaceId(),
                    space.getName(),
                    space.getAddress(),
                    space.isActif()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CoBuildSpaceDTO> getById(@PathVariable Long id) {
        CoBuildSpace space = service.getById(id);
        if (space == null) return ResponseEntity.notFound().build();

        CoBuildSpaceDTO dto = new CoBuildSpaceDTO(
                space.getSpaceId(),
                space.getName(),
                space.getAddress(),
                space.isActif()
        );
        return ResponseEntity.ok(dto);
    }


    @PutMapping("/{id}")
    public ResponseEntity<CoBuildSpace> update(@PathVariable Long id, @RequestBody CoBuildSpace space) {
        CoBuildSpace updated = service.update(id, space);
        return (updated != null) ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
