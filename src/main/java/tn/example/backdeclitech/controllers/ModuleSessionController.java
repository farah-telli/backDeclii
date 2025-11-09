package tn.example.backdeclitech.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.example.backdeclitech.DTO.CancelWeekRequest;
import tn.example.backdeclitech.DTO.ModuleSessionResponse;
import tn.example.backdeclitech.entities.Child;
import tn.example.backdeclitech.entities.ModuleSession;
import tn.example.backdeclitech.repositories.ChildRepository;
import tn.example.backdeclitech.services.ModuleSessionService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/moduleSessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4300", allowCredentials = "true")
@Slf4j
public class ModuleSessionController {
    private final ModuleSessionService service;
    private final ChildRepository childRepository;


    @GetMapping
    public ResponseEntity<List<ModuleSession>> getAll() {
        log.info("📋 Récupération de toutes les sessions");
        List<ModuleSession> sessions = service.findAll();
        log.info("✅ {} session(s) trouvée(s)", sessions.size());
        return ResponseEntity.ok(sessions);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ModuleSession> getById(@PathVariable Long id) {
        log.info("🔍 Recherche de la session avec l'ID: {}", id);
        return service.findById(id)
                .map(session -> {
                    log.info("✅ Session trouvée: {}", id);
                    return ResponseEntity.ok(session);
                })
                .orElseGet(() -> {
                    log.warn("⚠️ Session non trouvée avec l'ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }


    @PostMapping
    public ResponseEntity<ModuleSession> create(@RequestBody ModuleSession session) {
        log.info("➕ Création d'une nouvelle session");
        try {
            ModuleSession savedSession = service.save(session);
            log.info("✅ Session créée avec l'ID: {}", savedSession.getId());
            return ResponseEntity.ok(savedSession);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la création de la session", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<ModuleSession> update(@PathVariable Long id, @RequestBody ModuleSession session) {
        log.info("✏️ Mise à jour de la session avec l'ID: {}", id);
        try {
            ModuleSession updatedSession = service.update(id, session);
            log.info("✅ Session {} mise à jour avec succès", id);
            return ResponseEntity.ok(updatedSession);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la mise à jour de la session {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("🗑️ Suppression de la session avec l'ID: {}", id);
        try {
            service.delete(id);
            log.info("✅ Session {} supprimée avec succès", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("❌ Erreur lors de la suppression de la session {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/by-module/{moduleId}")
    public ResponseEntity<?> getSessionsByModule(@PathVariable Long moduleId) {
        log.info("🔍 Recherche des sessions pour le module: {}", moduleId);
        try {
            List<ModuleSession> sessions = service.getSessionsByModuleIdSimple(moduleId);
            log.info("✅ {} session(s) trouvée(s) pour le module {}", sessions.size(), moduleId);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la recherche par module {}", moduleId, e);
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }


    @GetMapping("/by-module-name/{moduleName}")
    public ResponseEntity<?> getSessionsByModuleName(@PathVariable String moduleName) {
        log.info("🔍 Recherche des sessions pour le module: {}", moduleName);
        try {
            List<ModuleSession> sessions = service.getSessionsByModuleName(moduleName);
            log.info("✅ {} session(s) trouvée(s) pour le module '{}'", sessions.size(), moduleName);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la recherche par nom de module '{}'", moduleName, e);
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }


    @GetMapping("/by-day/{day}")
    public ResponseEntity<?> getSessionsByDay(@PathVariable String day) {
        log.info("🔍 Recherche des sessions pour le jour: {}", day);
        try {
            List<ModuleSession> sessions = service.getSessionsByDayName(day);
            log.info("✅ {} session(s) trouvée(s) pour le jour '{}'", sessions.size(), day);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la recherche par jour '{}'", day, e);
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }

    @GetMapping("/by-cobuildspace/{coBuildSpaceId}")
    public ResponseEntity<?> getSessionsByCoBuildSpace(@PathVariable Long coBuildSpaceId) {
        log.info("🔍 Recherche des sessions pour le CoBuildSpace: {}", coBuildSpaceId);
        try {
            List<ModuleSession> sessions = service.getSessionsByCoBuildSpaceId(coBuildSpaceId);
            log.info("✅ {} session(s) trouvée(s) pour le CoBuildSpace {}", sessions.size(), coBuildSpaceId);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la recherche par CoBuildSpace {}", coBuildSpaceId, e);
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }


    @GetMapping("/byChild/{childId}")
    public ResponseEntity<?> getSessionsByChildAge(@PathVariable Long childId) {
        log.info("🔍 Recherche des sessions pour l'enfant avec l'ID: {}", childId);
        try {
            Child child = childRepository.findById(childId)
                    .orElseThrow(() -> new RuntimeException("Enfant non trouvé avec l'ID: " + childId));

            int childAge = child.getAge();
            log.info("👶 Âge de l'enfant: {} ans", childAge);

            List<ModuleSession> sessions = service.getSessionsByChildAge(childAge);
            log.info("✅ {} session(s) trouvée(s) pour l'âge {} ans", sessions.size(), childAge);

            return ResponseEntity.ok(sessions);
        } catch (RuntimeException e) {
            log.error("❌ {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Erreur lors de la recherche pour l'enfant {}", childId, e);
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }


    @GetMapping("/filtered")
    public ResponseEntity<?> getFilteredSessions(
            @RequestParam(required = false) List<String> days,
            @RequestParam(required = false) List<Long> spaceIds,
            @RequestParam(required = false) List<Long> moduleIds,
            @RequestParam(required = false) String moduleName) {
        try {
            log.info("🔍 Filtrage des sessions - Days: {}, SpaceIds: {}, ModuleIds: {}, ModuleName: {}",
                    days, spaceIds, moduleIds, moduleName);

            List<ModuleSession> sessions = service.getFilteredSessions(days, spaceIds, moduleIds, moduleName);

            log.info("✅ {} session(s) trouvée(s) après filtrage", sessions.size());
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("❌ Erreur lors du filtrage des sessions", e);
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }

    @GetMapping("/response")
    public ResponseEntity<List<ModuleSessionResponse>> getAllResponses() {
        log.info("📋 Récupération de toutes les sessions (format DTO)");
        List<ModuleSessionResponse> responses = service.getAllSessionResponses();
        log.info("✅ {} session(s) trouvée(s)", responses.size());
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/sessions/{id}/annule")
    public ResponseEntity<?> updateAnnuleStatus(@PathVariable Long id, @RequestParam boolean annule) {
        log.info("{} Session {} - Nouveau statut: {}",
                annule ? "❌" : "✅", id, annule ? "annulée" : "active");
        try {
            ModuleSession updated = service.updateAnnuleStatus(id, annule);

            String message = annule ?
                    "Session annulée avec succès (ID: " + updated.getId() + ")" :
                    "Session réactivée avec succès (ID: " + updated.getId() + ")";

            log.info("✅ {}", message);

            Map<String, Object> response = new HashMap<>();
            response.put("message", message);
            response.put("session", updated);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("❌ Erreur lors de la mise à jour du statut de la session {}", id, e);
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }


    @PatchMapping("/{id}/activate")
    public ResponseEntity<?> activateSession(@PathVariable Long id) {
        log.info("✅ Activation de la session avec l'ID: {}", id);
        try {
            ModuleSession updatedSession = service.activerSession(id);
            log.info("✅ Session {} activée avec succès", id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Session activée avec succès");
            response.put("session", updatedSession);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("❌ Erreur lors de l'activation de la session {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<?> desactiverSession(@PathVariable Long id) {
        log.info("❌ Désactivation de la session avec l'ID: {}", id);
        try {
            ModuleSession updatedSession = service.desactiverSession(id);
            log.info("✅ Session {} désactivée avec succès", id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Session désactivée avec succès");
            response.put("session", updatedSession);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("❌ Erreur lors de la désactivation de la session {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/sessions/cancelByWeek")
    public ResponseEntity<?> annulerSessionsDureeSemaine(@RequestBody CancelWeekRequest request) {
        log.info("📅 DÉBUT ANNULATION SESSIONS POUR UNE SEMAINE");

        try {
            log.info("📥 Requête reçue:");
            log.info("   - StartDate: {}", request.getStartDate());
            log.info("   - ModuleId: {}", request.getModuleId());
            log.info("   - CoBuildSpaceId: {}", request.getCoBuildSpaceId());

            if (request.getStartDate() == null || request.getStartDate().trim().isEmpty()) {
                log.error("❌ La date de début est manquante");
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "error", "La date de début est requise",
                                "field", "startDate"
                        ));
            }

            if (request.getModuleId() == null) {
                log.error("❌ L'ID du module est manquant");
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "error", "L'ID du module est requis",
                                "field", "moduleId"
                        ));
            }

            if (request.getCoBuildSpaceId() == null) {
                log.error("❌ L'ID du CoBuildSpace est manquant");
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "error", "L'ID du CoBuildSpace est requis",
                                "field", "coBuildSpaceId"
                        ));
            }

            log.info("✅ Validation des données réussie");

            List<ModuleSession> updatedSessions = service.annulerSessionsDureeSemaine(
                    request.getStartDate(),
                    request.getModuleId(),
                    request.getCoBuildSpaceId()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", updatedSessions.size() + " session(s) annulée(s) avec succès");
            response.put("count", updatedSessions.size());
            response.put("sessions", updatedSessions);
            response.put("notificationsSent", true);

            log.info("✅ ANNULATION TERMINÉE AVEC SUCCÈS");
            log.info("   📊 Nombre de sessions annulées: {}", updatedSessions.size());
            log.info("   📢 Notifications envoyées aux parents");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("❌ ERREUR LORS DE L'ANNULATION");
            log.error("   Type: {}", e.getClass().getSimpleName());
            log.error("   Message: {}", e.getMessage());
            e.printStackTrace();

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage(),
                            "type", e.getClass().getSimpleName(),
                            "cause", e.getCause() != null ? e.getCause().getMessage() : "N/A"
                    ));
        } catch (Exception e) {
            log.error("❌ ERREUR INATTENDUE");
            log.error("   Type: {}", e.getClass().getSimpleName());
            log.error("   Message: {}", e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Erreur interne du serveur : " + e.getMessage(),
                            "type", e.getClass().getSimpleName()
                    ));
        }
    }


    @GetMapping("/sessions/active")
    public ResponseEntity<List<ModuleSession>> getActiveSessions() {
        log.info("🔍 Récupération des sessions actives");
        List<ModuleSession> activeSessions = service.getActiveSessions();
        log.info("✅ {} session(s) active(s) trouvée(s)", activeSessions.size());
        return ResponseEntity.ok(activeSessions);
    }


    @PostMapping("/reactiver-automatique")
    public ResponseEntity<?> reactiverSessionsManually() {
        log.info("🔄 DÉBUT RÉACTIVATION AUTOMATIQUE");

        try {
            List<ModuleSession> reactivees = service.reactiverSessionsAutomatiquement();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", reactivees.size() + " session(s) réactivée(s)");
            response.put("count", reactivees.size());
            response.put("sessions", reactivees);
            response.put("notificationsSent", true);

            log.info("✅ RÉACTIVATION TERMINÉE AVEC SUCCÈS");
            log.info("   📊 Nombre de sessions réactivées: {}", reactivees.size());
            log.info("   📢 Notifications envoyées aux parents");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ ERREUR LORS DE LA RÉACTIVATION");
            log.error("   Message: {}", e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Erreur: " + e.getMessage()
                    ));
        }
    }
}