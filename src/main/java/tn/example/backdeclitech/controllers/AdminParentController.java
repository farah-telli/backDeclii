package tn.example.backdeclitech.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tn.example.backdeclitech.DTO.*;
import tn.example.backdeclitech.entities.User;
import tn.example.backdeclitech.repositories.UserRepository;
import tn.example.backdeclitech.services.AdminParentService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/parents")
@CrossOrigin(origins = "http://localhost:4300")
@RequiredArgsConstructor
@Validated
public class AdminParentController {

    private final AdminParentService adminParentService;
    private final UserRepository userRepository;

    @GetMapping("/by-single-date")
    public ResponseEntity<?> getParentsByCalendarDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "registrationDate"));
            Page<AdminParentResponse> parents = adminParentService.getParentsByDate(date, date, pageable);

            return ResponseEntity.ok(new PaginatedResponse<>(
                    parents.getContent(),
                    parents.getNumber(),
                    parents.getSize(),
                    parents.getTotalElements(),
                    parents.getTotalPages(),
                    parents.isFirst(),
                    parents.isLast()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors du filtrage par date: " + e.getMessage()));
        }
    }

    @GetMapping("/by-date")
    public ResponseEntity<?> getParentsByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "registrationDate"));
            Page<AdminParentResponse> parents = adminParentService.getParentsByDate(startDate, endDate, pageable);

            return ResponseEntity.ok(new PaginatedResponse<>(
                    parents.getContent(),
                    parents.getNumber(),
                    parents.getSize(),
                    parents.getTotalElements(),
                    parents.getTotalPages(),
                    parents.isFirst(),
                    parents.isLast()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors du filtrage par date: " + e.getMessage()));
        }
    }

    @PostMapping("/addParent")
    public ResponseEntity<?> createParent(@Valid @RequestBody AdminCreateParentRequest request) {
        try {
            String adminUsername = getCurrentAdminUsername();
            AdminParentResponse response = adminParentService.createParentWithChildren(request, adminUsername);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Erreur lors de la création du parent: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllParents(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "registrationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String search) {
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ?
                    Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<AdminParentResponse> parents = adminParentService.getAllParentsForAdmin(pageable, search);

            return ResponseEntity.ok(new PaginatedResponse<>(
                    parents.getContent(),
                    parents.getNumber(),
                    parents.getSize(),
                    parents.getTotalElements(),
                    parents.getTotalPages(),
                    parents.isFirst(),
                    parents.isLast()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la récupération des parents: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleParentStatus(@PathVariable Long id) {
        Optional<User> parentOpt = userRepository.findById(id);
        if (parentOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Parent not found");
        }

        User parent = parentOpt.get();
        parent.setActive(!parent.isActive());
        userRepository.save(parent);

        return ResponseEntity.ok(Map.of(
                "id", parent.getId(),
                "active", parent.isActive()
        ));
    }

    @GetMapping("/{parentId}")
    public ResponseEntity<?> getParentById(@PathVariable Long parentId) {
        try {
            AdminParentResponse parent = adminParentService.getParentByIdForAdmin(parentId);
            return ResponseEntity.ok(parent);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Parent introuvable: " + e.getMessage()));
        }
    }

    @PutMapping("/{parentId}")
    public ResponseEntity<?> updateParent(
            @PathVariable Long parentId,
            @Valid @RequestBody AdminUpdateParentRequest request) {
        try {
            String adminUsername = getCurrentAdminUsername();
            AdminParentResponse response = adminParentService.updateParent(parentId, request, adminUsername);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Erreur lors de la mise à jour: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{parentId}")
    public ResponseEntity<?> deleteParent(@PathVariable Long parentId) {
        try {
            String adminUsername = getCurrentAdminUsername();
            adminParentService.deleteParent(parentId, adminUsername);
            return ResponseEntity.ok(new SuccessResponse("Parent supprimé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Erreur lors de la suppression: " + e.getMessage()));
        }
    }

    @PostMapping("/{parentId}/children")
    public ResponseEntity<?> addChildToParent(
            @PathVariable Long parentId,
            @Valid @RequestBody AdminCreateChildRequest request) {
        try {
            String adminUsername = getCurrentAdminUsername();
            AdminChildResponse response = adminParentService.addChildToParent(parentId, request, adminUsername);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Erreur lors de l'ajout de l'enfant: " + e.getMessage()));
        }
    }

    @GetMapping("/{parentId}/children")
    public ResponseEntity<?> getChildrenByParent(@PathVariable Long parentId) {
        try {
            List<AdminChildResponse> children = adminParentService.getChildrenByParent(parentId);
            return ResponseEntity.ok(children);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Erreur: " + e.getMessage()));
        }
    }

    @DeleteMapping("/children/{childId}")
    public ResponseEntity<?> deleteChild(@PathVariable Long childId) {
        try {
            String adminUsername = getCurrentAdminUsername();
            adminParentService.deleteChild(childId, adminUsername);
            return ResponseEntity.ok(new SuccessResponse("Enfant supprimé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Erreur lors de la suppression: " + e.getMessage()));
        }
    }

    @PostMapping("/bulk-import")
    public ResponseEntity<?> bulkImportParents(@Valid @RequestBody AdminBulkParentRequest request) {
        try {
            String adminUsername = getCurrentAdminUsername();
            List<AdminParentResponse> results = adminParentService.bulkImportParents(request, adminUsername);

            return ResponseEntity.ok(new BulkImportResponse(
                    results.size(),
                    request.getParents().size() - results.size(),
                    results
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Erreur lors de l'import: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getParentStats() {
        try {
            AdminParentService.AdminStatsResponse stats = adminParentService.getParentStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la récupération des statistiques: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchParents(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            Page<AdminParentResponse> results = adminParentService.getAllParentsForAdmin(pageable, query);
            return ResponseEntity.ok(results.getContent());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la recherche: " + e.getMessage()));
        }
    }

    @GetMapping("/expiring-soon")
    public ResponseEntity<?> getExpiringParents(@RequestParam(defaultValue = "7") int days) {
        try {
            List<AdminParentResponse> expiringParents = adminParentService.getExpiringParents(days);
            return ResponseEntity.ok(expiringParents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur: " + e.getMessage()));
        }
    }

    @PostMapping("/deactivate-expired")
    public ResponseEntity<?> deactivateExpiredParents() {
        try {
            int count = adminParentService.deactivateExpiredParents();
            return ResponseEntity.ok(Map.of(
                    "message", "Parents expirés désactivés",
                    "count", count
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur: " + e.getMessage()));
        }
    }

    @PutMapping("/{parentId}/extend-expiration")
    public ResponseEntity<?> extendExpirationDate(
            @PathVariable Long parentId,
            @RequestBody Map<String, String> body) {
        try {
            String adminUsername = getCurrentAdminUsername();
            LocalDate newDate = LocalDate.parse(body.get("expirationDate"));
            AdminParentResponse response = adminParentService.extendExpirationDate(
                    parentId, newDate, adminUsername
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Erreur: " + e.getMessage()));
        }
    }

    @PutMapping("/{parentId}/add-days")
    public ResponseEntity<?> addDaysToExpiration(
            @PathVariable Long parentId,
            @RequestBody Map<String, Integer> body) {
        try {
            String adminUsername = getCurrentAdminUsername();
            Integer days = body.get("days");
            if (days == null || days <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Le nombre de jours doit être positif"));
            }
            AdminParentResponse response = adminParentService.addDaysToExpiration(
                    parentId, days, adminUsername
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Erreur: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{parentId}/expiration")
    public ResponseEntity<?> removeExpirationDate(@PathVariable Long parentId) {
        try {
            String adminUsername = getCurrentAdminUsername();
            AdminParentResponse response = adminParentService.removeExpirationDate(
                    parentId, adminUsername
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Erreur: " + e.getMessage()));
        }
    }

    private String getCurrentAdminUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return ((User) principal).getUsername();
            } else {
                return principal.toString();
            }
        }
        return "system";
    }
}