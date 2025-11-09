package tn.example.backdeclitech.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.example.backdeclitech.DTO.*;
import tn.example.backdeclitech.entities.*;
import tn.example.backdeclitech.repositories.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final ModuleSessionRepository moduleSessionRepository;
    private final CoBuildSpaceRepository coBuildSpaceRepository;
    private final ObjectMapper objectMapper;

    /**
     * Créer un log d'audit
     */
    @Transactional
    public AuditLog createAuditLog(
            AuditActionType actionType,
            AuditEntityType entityType,
            Long entityId,
            String entityName,
            User currentUser,
            Object oldValue,
            Object newValue,
            String description,
            HttpServletRequest request
    ) {
        AuditLog log = new AuditLog();
        log.setActionType(actionType);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setEntityName(entityName);
        log.setUserId(currentUser.getId());
        log.setUserName(currentUser.getFirstName() + " " + currentUser.getLastName());
        log.setUserEmail(currentUser.getEmail());
        log.setTimestamp(LocalDateTime.now());
        log.setDescription(description);

        try {
            if (oldValue != null) {
                log.setOldValue(objectMapper.writeValueAsString(oldValue));
            }
            if (newValue != null) {
                log.setNewValue(objectMapper.writeValueAsString(newValue));
            }

            if (oldValue != null && newValue != null) {
                List<AuditChange> changes = calculateChanges(oldValue, newValue);
                log.setChanges(objectMapper.writeValueAsString(changes));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (request != null) {
            log.setIpAddress(getClientIpAddress(request));
            log.setUserAgent(request.getHeader("User-Agent"));
        }

        return auditLogRepository.save(log);
    }

    /**
     * Créer un log pour une action groupée
     */
    @Transactional
    public AuditLog createBulkAuditLog(
            AuditActionType actionType,
            AuditEntityType entityType,
            Integer affectedRecords,
            User currentUser,
            String description,
            HttpServletRequest request
    ) {
        AuditLog log = new AuditLog();
        log.setActionType(actionType);
        log.setEntityType(entityType);
        log.setEntityId(0L);
        log.setAffectedRecords(affectedRecords);
        log.setUserId(currentUser.getId());
        log.setUserName(currentUser.getFirstName() + " " + currentUser.getLastName());
        log.setUserEmail(currentUser.getEmail());
        log.setTimestamp(LocalDateTime.now());
        log.setDescription(description);

        if (request != null) {
            log.setIpAddress(getClientIpAddress(request));
            log.setUserAgent(request.getHeader("User-Agent"));
        }

        return auditLogRepository.save(log);
    }

    /**
     * Récupérer tous les logs avec filtres
     */
    public List<AuditLogResponse> getAllAuditLogs(AuditLogFilter filter) {
        List<AuditLog> logs;

        if (filter == null || isFilterEmpty(filter)) {
            logs = auditLogRepository.findAll();
        } else {
            logs = auditLogRepository.findWithFilters(
                    filter.getActionType(),
                    filter.getEntityType(),
                    filter.getUserId(),
                    filter.getEntityId(),
                    filter.getStartDate(),
                    filter.getEndDate(),
                    filter.getSearchTerm()
            );
        }

        return logs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les logs pour une entité
     */
    public List<AuditLogResponse> getAuditLogsForEntity(AuditEntityType entityType, Long entityId) {
        List<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(
                entityType, entityId
        );
        return logs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les logs pour un utilisateur
     */
    public List<AuditLogResponse> getAuditLogsForUser(Long userId) {
        List<AuditLog> logs = auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
        return logs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les statistiques
     */
    public AuditStatistics getStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        AuditStatistics stats = new AuditStatistics();

        stats.setTotalActions(auditLogRepository.countTotalActions(startDate, endDate));
        stats.setUniqueUsers(auditLogRepository.countUniqueUsers(startDate, endDate));
        stats.setModifications(auditLogRepository.countModifications(startDate, endDate));
        stats.setDeletions(auditLogRepository.countDeletions(startDate, endDate));
        stats.setCreations(auditLogRepository.countCreations(startDate, endDate));
        stats.setLogins(auditLogRepository.countLogins(startDate, endDate));

        List<Object[]> actionsByType = auditLogRepository.countByActionType(startDate, endDate);
        Map<AuditActionType, Long> actionsMap = actionsByType.stream()
                .collect(Collectors.toMap(
                        arr -> (AuditActionType) arr[0],
                        arr -> (Long) arr[1]
                ));
        stats.setActionsByType(actionsMap);

        List<Object[]> actionsByEntity = auditLogRepository.countByEntityType(startDate, endDate);
        Map<AuditEntityType, Long> entitiesMap = actionsByEntity.stream()
                .collect(Collectors.toMap(
                        arr -> (AuditEntityType) arr[0],
                        arr -> (Long) arr[1]
                ));
        stats.setActionsByEntity(entitiesMap);

        List<Object[]> topUsersData = auditLogRepository.findTopUsers(startDate, endDate);
        List<UserActivity> topUsers = topUsersData.stream()
                .limit(10)
                .map(arr -> new UserActivity(
                        (Long) arr[0],
                        (String) arr[1],
                        (String) arr[2],
                        (Long) arr[3]
                ))
                .collect(Collectors.toList());
        stats.setTopUsers(topUsers);

        return stats;
    }

    /**
     * Restaurer une entité à partir d'un log
     */
    @Transactional
    public RestoreResponse restoreFromAuditLog(RestoreRequest request, User currentUser, HttpServletRequest httpRequest) {
        RestoreResponse response = new RestoreResponse();

        try {
            AuditLog auditLog = auditLogRepository.findById(request.getAuditLogId())
                    .orElseThrow(() -> new RuntimeException("Log d'audit non trouvé"));

            if (auditLog.getOldValue() == null) {
                response.setSuccess(false);
                response.setMessage("Aucune donnée à restaurer pour ce log");
                return response;
            }

            Object restoredEntity = null;
            switch (request.getEntityType()) {
                case USER:
                    restoredEntity = restoreUser(auditLog, request.getEntityId());
                    break;
                case MODULE:
                    restoredEntity = restoreModule(auditLog, request.getEntityId());
                    break;
                case SESSION:
                    restoredEntity = restoreSession(auditLog, request.getEntityId());
                    break;
                case COBUILD_SPACE:
                    restoredEntity = restoreCoBuildSpace(auditLog, request.getEntityId());
                    break;
                default:
                    response.setSuccess(false);
                    response.setMessage("Type d'entité non pris en charge pour la restauration");
                    return response;
            }

            AuditLog restoreLog = createAuditLog(
                    AuditActionType.RESTORE,
                    request.getEntityType(),
                    request.getEntityId(),
                    auditLog.getEntityName(),
                    currentUser,
                    null,
                    restoredEntity,
                    "Restauration depuis log #" + request.getAuditLogId() +
                            (request.getReason() != null ? " - Raison: " + request.getReason() : ""),
                    httpRequest
            );

            response.setSuccess(true);
            response.setMessage("Restauration effectuée avec succès");
            response.setRestoredEntity(restoredEntity);
            response.setNewAuditLogId(restoreLog.getId());

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Erreur lors de la restauration: " + e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

    /**
     * Supprimer les logs anciens
     */
    @Transactional
    public int deleteOldAuditLogs(int olderThanDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(olderThanDays);
        return auditLogRepository.deleteOldLogs(cutoffDate);
    }

    // ========== MÉTHODES PRIVÉES ==========

    private boolean isFilterEmpty(AuditLogFilter filter) {
        return filter.getActionType() == null &&
                filter.getEntityType() == null &&
                filter.getUserId() == null &&
                filter.getEntityId() == null &&
                filter.getStartDate() == null &&
                filter.getEndDate() == null &&
                (filter.getSearchTerm() == null || filter.getSearchTerm().isEmpty());
    }

    private AuditLogResponse convertToResponse(AuditLog log) {
        AuditLogResponse response = new AuditLogResponse();
        response.setId(log.getId());
        response.setActionType(log.getActionType());
        response.setEntityType(log.getEntityType());
        response.setEntityId(log.getEntityId());
        response.setEntityName(log.getEntityName());
        response.setUserId(log.getUserId());
        response.setUserName(log.getUserName());
        response.setUserEmail(log.getUserEmail());
        response.setTimestamp(log.getTimestamp());
        response.setOldValue(log.getOldValue());
        response.setNewValue(log.getNewValue());
        response.setIpAddress(log.getIpAddress());
        response.setUserAgent(log.getUserAgent());
        response.setDescription(log.getDescription());
        response.setAffectedRecords(log.getAffectedRecords());

        if (log.getChanges() != null) {
            try {
                List<AuditChange> changes = objectMapper.readValue(
                        log.getChanges(),
                        new TypeReference<List<AuditChange>>() {}
                );
                response.setChanges(changes);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return response;
    }

    private List<AuditChange> calculateChanges(Object oldValue, Object newValue) {
        List<AuditChange> changes = new ArrayList<>();

        try {
            Field[] fields = oldValue.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object oldFieldValue = field.get(oldValue);
                Object newFieldValue = field.get(newValue);

                if (!Objects.equals(oldFieldValue, newFieldValue)) {
                    changes.add(new AuditChange(
                            field.getName(),
                            oldFieldValue,
                            newFieldValue
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return changes;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    // ========== MÉTHODES DE RESTAURATION ==========

    /**
     * Restaurer un utilisateur
     */
    private User restoreUser(AuditLog auditLog, Long userId) throws JsonProcessingException {
        User oldUser = objectMapper.readValue(auditLog.getOldValue(), User.class);
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        currentUser.setFirstName(oldUser.getFirstName());
        currentUser.setLastName(oldUser.getLastName());
        currentUser.setEmail(oldUser.getEmail());
        currentUser.setPhone(oldUser.getPhone());
        currentUser.setActive(oldUser.isActive());
        currentUser.setRole(oldUser.getRole());

        return userRepository.save(currentUser);
    }

    /**
     * Restaurer un module
     */
    private tn.example.backdeclitech.entities.Module restoreModule(AuditLog auditLog, Long moduleId) throws JsonProcessingException {
        tn.example.backdeclitech.entities.Module oldModule = objectMapper.readValue(
                auditLog.getOldValue(),
                tn.example.backdeclitech.entities.Module.class
        );
        tn.example.backdeclitech.entities.Module currentModule = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));

        // Restaurer les champs du module
        currentModule.setTitle(oldModule.getTitle());
        currentModule.setDescription(oldModule.getDescription());
        currentModule.setImageUrl(oldModule.getImageUrl());
        currentModule.setJour(oldModule.getJour());
        currentModule.setActif(oldModule.isActif());
        currentModule.setAnnulation(oldModule.isAnnulation());

        return moduleRepository.save(currentModule);
    }

    /**
     * Restaurer une session
     */
    private ModuleSession restoreSession(AuditLog auditLog, Long sessionId) throws JsonProcessingException {
        ModuleSession oldSession = objectMapper.readValue(auditLog.getOldValue(), ModuleSession.class);
        ModuleSession currentSession = moduleSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session non trouvée"));

        // Restaurer les champs de la session selon votre entité
        currentSession.setDate(oldSession.getDate());
        currentSession.setDayName(oldSession.getDayName());
        currentSession.setStartTime(oldSession.getStartTime());
        currentSession.setEndTime(oldSession.getEndTime());
        currentSession.setCapcity(oldSession.getCapcity());
        currentSession.setEnrolledCount(oldSession.getEnrolledCount());
        currentSession.setActive(oldSession.isActive());
        currentSession.setAnnule(oldSession.isAnnule());
        currentSession.setDateAnnulation(oldSession.getDateAnnulation());
        currentSession.setTrancheAgeMin(oldSession.getTrancheAgeMin());
        currentSession.setTrancheAgeMax(oldSession.getTrancheAgeMax());

        return moduleSessionRepository.save(currentSession);
    }

    /**
     * Restaurer un CoBuildSpace
     */
    private CoBuildSpace restoreCoBuildSpace(AuditLog auditLog, Long spaceId) throws JsonProcessingException {
        CoBuildSpace oldSpace = objectMapper.readValue(auditLog.getOldValue(), CoBuildSpace.class);
        CoBuildSpace currentSpace = coBuildSpaceRepository.findById(spaceId)
                .orElseThrow(() -> new RuntimeException("CoBuildSpace non trouvé"));

        currentSpace.setName(oldSpace.getName());
        currentSpace.setAddress(oldSpace.getAddress());
        currentSpace.setActif(oldSpace.isActif());

        return coBuildSpaceRepository.save(currentSpace);
    }
}