package tn.example.backdeclitech.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tn.example.backdeclitech.DTO.ChildResponse;
import tn.example.backdeclitech.DTO.UpdateChildRequest;
import tn.example.backdeclitech.entities.Child;
import tn.example.backdeclitech.entities.User;
import tn.example.backdeclitech.services.ChildService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/children")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4300"})
@RequiredArgsConstructor
@Slf4j
public class ChildController {

    private final ChildService childService;

    @GetMapping("/by-parent")
    public ResponseEntity<?> getChildrenByParent() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() ||
                    authentication.getPrincipal().equals("anonymousUser")) {
                log.warn("⚠️ Unauthorized access attempt");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Vous devez être connecté pour effectuer cette action.");
            }

            User parent = (User) authentication.getPrincipal();
            List<ChildResponse> children = childService.getChildDTOsByParent(parent);
            return ResponseEntity.ok(children);

        } catch (Exception e) {
            log.error("❌ Erreur récupération enfants: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération des enfants : " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllChildren() {
        try {
            List<ChildResponse> children = childService.getAllChildren();
            return ResponseEntity.ok(children);
        } catch (Exception e) {
            log.error("❌ Erreur récupération tous les enfants: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération des enfants : " + e.getMessage());
        }
    }

    @GetMapping("/parents")
    public ResponseEntity<?> getAllParents() {
        try {
            List<User> parents = childService.getAllParents();

            List<Map<String, Object>> parentDtos = parents.stream()
                    .map(parent -> {
                        Map<String, Object> dto = new HashMap<>();
                        dto.put("id", parent.getId());
                        dto.put("firstName", parent.getFirstName());
                        dto.put("lastName", parent.getLastName());
                        dto.put("email", parent.getEmail());
                        dto.put("phone", parent.getPhone());
                        dto.put("registrationDate", parent.getRegistrationDate());
                        dto.put("expirationDate", parent.getAccountExpirationDate());
                        dto.put("active", parent.isActive());

                        List<Map<String, Object>> childrenDto = new ArrayList<>();
                        if (parent.getChildren() != null) {
                            childrenDto = parent.getChildren().stream()
                                    .map(child -> {
                                        Map<String, Object> childDto = new HashMap<>();
                                        childDto.put("id", child.getId());
                                        childDto.put("firstName", child.getFirstName());
                                        childDto.put("lastName", child.getLastName());
                                        childDto.put("sexe", child.getSexe());
                                        childDto.put("age", child.getAge());
                                        return childDto;
                                    })
                                    .collect(Collectors.toList());
                        }
                        dto.put("children", childrenDto);

                        return dto;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(parentDtos);

        } catch (Exception e) {
            log.error("❌ Erreur getAllParents: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération des parents: " + e.getMessage());
        }
    }

    @PostMapping("/parents/{parentId}/children")
    public ResponseEntity<?> addChild(@PathVariable Long parentId, @RequestBody Child child) {
        try {
            Child savedChild = childService.addChildToParent(parentId, child);
            return new ResponseEntity<>(savedChild, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("❌ Erreur ajout enfant: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/parents/{parentId}/children")
    public ResponseEntity<?> getChildrenByParent(@PathVariable Long parentId) {
        try {
            List<Child> children = childService.getChildrenByParent(parentId);
            return ResponseEntity.ok(children);
        } catch (Exception e) {
            log.error("❌ Erreur récupération enfants du parent: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{childId}")
    public ResponseEntity<?> updateChild(
            @PathVariable Long childId,
            @RequestBody UpdateChildRequest request) {
        try {
            Child updatedChild = childService.updateChild(childId, request);
            return ResponseEntity.ok(updatedChild);
        } catch (Exception e) {
            log.error("❌ Erreur mise à jour enfant: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}