package tn.example.backdeclitech.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.example.backdeclitech.DTO.ModuleSessionResponse;
import tn.example.backdeclitech.entities.ModuleSession;
import tn.example.backdeclitech.exception.ResourceNotFoundException;
import tn.example.backdeclitech.mappers.ModuleSessionMapper;
import tn.example.backdeclitech.repositories.ModuleSessionRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
@RequiredArgsConstructor
public class ModuleSessionService implements IModuleSessionService {

    private final ModuleSessionRepository moduleSessionRepository;
    private final NotificationService notificationService;

    public List<ModuleSession> findAll() {
        return (List<ModuleSession>) moduleSessionRepository.findAll();
    }

    public Optional<ModuleSession> findById(Long id) {
        return moduleSessionRepository.findById(id);
    }

    public ModuleSession save(ModuleSession session) {
        return moduleSessionRepository.save(session);
    }

    public ModuleSession update(Long id, ModuleSession updatedSession) {
        updatedSession.setId(id);
        return moduleSessionRepository.save(updatedSession);
    }

    public void delete(Long id) {
        moduleSessionRepository.deleteById(id);
    }

    public List<ModuleSession> getSessionsByModuleIdSimple(Long moduleId) {
        return moduleSessionRepository.findByModuleId(moduleId);
    }

    public List<ModuleSession> getSessionsByModuleName(String moduleName) {
        return moduleSessionRepository.findByModuleTitleContainingIgnoreCase(moduleName);
    }

    public List<ModuleSession> getSessionsByDayName(String dayName) {
        String normalized = dayName.substring(0, 1).toUpperCase() + dayName.substring(1).toLowerCase();
        return moduleSessionRepository.findByDayName(normalized);
    }

    public List<ModuleSession> getSessionsByCoBuildSpaceId(Long coBuildSpaceId) {
        return moduleSessionRepository.findByCoBuildSpace_SpaceId(coBuildSpaceId);
    }

    public List<ModuleSession> getSessionsByChildAge(int age) {
        return moduleSessionRepository.findByChildAge(age);
    }

    public List<ModuleSession> getActiveSessions() {
        return moduleSessionRepository.findByIsActiveTrue();
    }

    public List<ModuleSession> getFilteredSessions(List<String> days, List<Long> spaceIds,
                                                   List<Long> moduleIds, String moduleName) {
        if ((days == null || days.isEmpty()) &&
                (spaceIds == null || spaceIds.isEmpty()) &&
                (moduleIds == null || moduleIds.isEmpty()) &&
                (moduleName == null || moduleName.trim().isEmpty())) {
            return findAll();
        }

        return moduleSessionRepository.findFilteredSessions(days, spaceIds, moduleIds, moduleName);
    }

    // ✅ CORRECTION PRINCIPALE : Utiliser findAllWithInstructors
    public List<ModuleSessionResponse> getAllSessionResponses() {
        log.info("📋 Récupération de toutes les sessions avec instructeurs");
        List<ModuleSession> sessions = moduleSessionRepository.findAllWithInstructors();
        log.info("✅ {} sessions récupérées de la base de données", sessions.size());

        List<ModuleSessionResponse> responses = sessions.stream()
                .map(ModuleSessionMapper::toDto)
                .collect(Collectors.toList());

        log.info("✅ {} réponses mappées", responses.size());
        return responses;
    }

    public List<ModuleSessionResponse> getSessionsForParents() {
        log.info("📋 Récupération des sessions pour parents");
        return moduleSessionRepository.findAllWithInstructors().stream()
                .filter(session -> session.isActive() && !session.isAnnule())
                .map(ModuleSessionMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ModuleSessionResponse> getSessionsForChild(int childAge) {
        log.info("📋 Récupération des sessions pour enfant d'âge {}", childAge);
        return moduleSessionRepository.findByChildAge(childAge).stream()
                .filter(session -> session.isActive() && !session.isAnnule())
                .map(ModuleSessionMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ModuleSessionResponse> getSessionsForParentByCoBuildSpace(Long coBuildSpaceId) {
        log.info("📋 Récupération des sessions pour CoBuildSpace {}", coBuildSpaceId);
        return moduleSessionRepository.findByCoBuildSpace_SpaceId(coBuildSpaceId).stream()
                .filter(session -> session.isActive() && !session.isAnnule())
                .map(ModuleSessionMapper::toDto)
                .collect(Collectors.toList());
    }

    public ModuleSessionResponse convertToResponse(ModuleSession session) {
        return ModuleSessionMapper.toDto(session);
    }

    @Transactional
    public ModuleSession activerSession(Long id) {
        Optional<ModuleSession> sessionOptional = moduleSessionRepository.findById(id);
        if (sessionOptional.isPresent()) {
            ModuleSession session = sessionOptional.get();
            session.setActive(true);
            ModuleSession saved = moduleSessionRepository.save(session);
            log.info("Session {} activee", id);
            return saved;
        }
        throw new ResourceNotFoundException("Session introuvable avec l'id " + id);
    }

    @Transactional
    public ModuleSession desactiverSession(Long id) {
        ModuleSession session = moduleSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session non trouvee avec l'id " + id));
        session.setActive(false);
        ModuleSession saved = moduleSessionRepository.save(session);
        log.info("Session {} desactivee", id);

        try {
            notificationService.notifySessionDeactivation(saved);
            log.info("📧 Notifications de désactivation envoyées pour la session {}", id);
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi de la notification de désactivation", e);
        }

        return saved;
    }

    @Transactional
    public ModuleSession updateAnnuleStatus(Long sessionId, boolean isAnnule) {
        ModuleSession session = moduleSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session non trouvee avec l'id: " + sessionId));

        session.setAnnule(isAnnule);

        if (isAnnule) {
            session.setDateAnnulation(new Date());
            log.info("Session {} annulee le {}", sessionId, new Date());

            try {
                notificationService.notifySessionCancellation(session);
                log.info("📧 Notifications d'annulation envoyées pour la session {}", sessionId);
            } catch (Exception e) {
                log.error("❌ Erreur lors de l'envoi de la notification d'annulation", e);
            }
        } else {
            session.setDateAnnulation(null);
            log.info("Session {} reactivee", sessionId);

            try {
                notificationService.notifySessionReactivation(session);
                log.info("📧 Notifications de réactivation envoyées pour la session {}", sessionId);
            } catch (Exception e) {
                log.error("❌ Erreur lors de l'envoi de la notification de réactivation", e);
            }
        }

        return moduleSessionRepository.save(session);
    }

    @Transactional
    public List<ModuleSession> annulerSessionsDureeSemaine(String startDateStr, Long moduleId, Long coBuildSpaceId) {
        try {
            log.info("🔍 DEBUT annulerSessionsDureeSemaine");
            log.info("   StartDate: {}", startDateStr);
            log.info("   ModuleId: {}", moduleId);
            log.info("   CoBuildSpaceId: {}", coBuildSpaceId);

            LocalDate localStartDate = LocalDate.parse(startDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate localEndDate = localStartDate.plusDays(6);

            Date startDate = java.sql.Date.valueOf(localStartDate);
            Date endDate = java.sql.Date.valueOf(localEndDate);
            Date dateAnnulation = new Date();

            log.info("📅 Période calculée: {} à {}", startDate, endDate);

            List<ModuleSession> sessions = moduleSessionRepository
                    .findActiveSessionsForModuleAndCoBuildSpaceBetweenDates(moduleId, coBuildSpaceId, startDate, endDate);

            log.info("📊 Nombre de sessions trouvées: {}", sessions.size());

            if (sessions.isEmpty()) {
                log.warn("⚠️ Aucune session trouvee pour la periode {} - {} (Module: {}, CoBuildSpace: {})",
                        startDate, endDate, moduleId, coBuildSpaceId);
                return List.of();
            }

            for (ModuleSession session : sessions) {
                session.setAnnule(true);
                session.setDateAnnulation(dateAnnulation);
            }

            List<ModuleSession> savedSessions = StreamSupport.stream(
                            moduleSessionRepository.saveAll(sessions).spliterator(), false)
                    .collect(Collectors.toList());

            log.info("{} sessions annulees pour la semaine du {} au {}", savedSessions.size(), startDate, endDate);

            if (!savedSessions.isEmpty()) {
                try {
                    for (ModuleSession session : savedSessions) {
                        notificationService.notifySessionCancellation(session);
                    }
                    log.info("📧 Notifications d'annulation envoyées pour {} session(s)", savedSessions.size());
                } catch (Exception e) {
                    log.error("❌ Erreur lors de l'envoi des notifications d'annulation multiple", e);
                }
            }

            return savedSessions;

        } catch (DateTimeParseException e) {
            log.error("Format de date invalide: {}", startDateStr);
            throw new RuntimeException("Format de date invalide, attendu : yyyy-MM-dd");
        } catch (Exception e) {
            log.error("Erreur lors de l'annulation des sessions", e);
            throw new RuntimeException("Erreur lors de l'annulation des sessions : " + e.getMessage(), e);
        }
    }

    @Transactional
    public List<ModuleSession> reactiverSessionsAutomatiquement() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date uneSemaineAvant = calendar.getTime();

        List<ModuleSession> sessionsAReactiver = moduleSessionRepository.findSessionsAnnuleesAvant(uneSemaineAvant);

        if (sessionsAReactiver.isEmpty()) {
            log.info("Aucune session a reactiver automatiquement");
            return Collections.emptyList();
        }

        for (ModuleSession session : sessionsAReactiver) {
            session.setAnnule(false);
            session.setDateAnnulation(null);

            try {
                notificationService.notifySessionReactivation(session);
            } catch (Exception e) {
                log.error("❌ Erreur lors de l'envoi de la notification de réactivation pour la session {}",
                        session.getId(), e);
            }
        }

        List<ModuleSession> reactivees = (List<ModuleSession>) moduleSessionRepository.saveAll(sessionsAReactiver);
        log.info("✅ {} sessions reactivees automatiquement", reactivees.size());
        log.info("📧 Notifications de réactivation envoyées pour {} session(s)", reactivees.size());

        return reactivees;
    }
}