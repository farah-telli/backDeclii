package tn.example.backdeclitech.controllers;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.example.backdeclitech.DTO.*;
import tn.example.backdeclitech.entities.AuditActionType;
import tn.example.backdeclitech.entities.AuditEntityType;
import tn.example.backdeclitech.entities.User;
import tn.example.backdeclitech.services.AuditLogService;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuditLogController {

    private final AuditLogService auditLogService;


    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAllAuditLogs(
            @RequestParam(required = false) AuditActionType actionType,
            @RequestParam(required = false) AuditEntityType entityType,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String search
    ) {
        AuditLogFilter filter = new AuditLogFilter();
        filter.setActionType(actionType);
        filter.setEntityType(entityType);
        filter.setUserId(userId);
        filter.setEntityId(entityId);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setSearchTerm(search);

        List<AuditLogResponse> logs = auditLogService.getAllAuditLogs(filter);
        return ResponseEntity.ok(logs);
    }


    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsForEntity(
            @PathVariable AuditEntityType entityType,
            @PathVariable Long entityId
    ) {
        List<AuditLogResponse> logs = auditLogService.getAuditLogsForEntity(entityType, entityId);
        return ResponseEntity.ok(logs);
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsForUser(@PathVariable Long userId) {
        List<AuditLogResponse> logs = auditLogService.getAuditLogsForUser(userId);
        return ResponseEntity.ok(logs);
    }


    @GetMapping("/statistics")
    public ResponseEntity<AuditStatistics> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        AuditStatistics stats = auditLogService.getStatistics(startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createAuditLog(
            @RequestBody AuditLogRequest request,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest httpRequest
    ) {
        try {
            auditLogService.createAuditLog(
                    request.getActionType(),
                    request.getEntityType(),
                    request.getEntityId(),
                    request.getEntityName(),
                    currentUser,
                    null,
                    null,
                    request.getDescription(),
                    httpRequest
            );

            Map<String, String> response = new HashMap<>();
            response.put("message", "Log d'audit créé avec succès");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la création du log: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    @PostMapping("/restore")
    public ResponseEntity<RestoreResponse> restoreFromAuditLog(
            @RequestBody RestoreRequest request,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest httpRequest
    ) {
        RestoreResponse response = auditLogService.restoreFromAuditLog(request, currentUser, httpRequest);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/export")
    public void exportAuditLogs(
            @RequestParam(required = false) AuditActionType actionType,
            @RequestParam(required = false) AuditEntityType entityType,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpServletResponse response
    ) throws IOException {
        AuditLogFilter filter = new AuditLogFilter();
        filter.setActionType(actionType);
        filter.setEntityType(entityType);
        filter.setUserId(userId);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);

        List<AuditLogResponse> logs = auditLogService.getAllAuditLogs(filter);

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"audit-logs-" + LocalDateTime.now() + ".csv\"");

        PrintWriter writer = response.getWriter();

        writer.println("ID,Type d'action,Type d'entité,ID Entité,Nom Entité,Utilisateur,Email,Date/Heure,Description,Enregistrements affectés");

        for (AuditLogResponse log : logs) {
            writer.println(String.format("%d,%s,%s,%d,%s,%s,%s,%s,\"%s\",%d",
                    log.getId(),
                    log.getActionType(),
                    log.getEntityType(),
                    log.getEntityId(),
                    escapeCsv(log.getEntityName()),
                    escapeCsv(log.getUserName()),
                    escapeCsv(log.getUserEmail()),
                    log.getTimestamp(),
                    escapeCsv(log.getDescription()),
                    log.getAffectedRecords() != null ? log.getAffectedRecords() : 0
            ));
        }

        writer.flush();
    }


    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> deleteOldAuditLogs(
            @RequestParam(defaultValue = "365") int olderThanDays
    ) {
        int deleted = auditLogService.deleteOldAuditLogs(olderThanDays);

        Map<String, Object> response = new HashMap<>();
        response.put("deleted", deleted);
        response.put("message", deleted + " logs supprimés");

        return ResponseEntity.ok(response);
    }


    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}

class AuditLogRequest {
    private AuditActionType actionType;
    private AuditEntityType entityType;
    private Long entityId;
    private String entityName;
    private String description;

    public AuditActionType getActionType() { return actionType; }
    public void setActionType(AuditActionType actionType) { this.actionType = actionType; }

    public AuditEntityType getEntityType() { return entityType; }
    public void setEntityType(AuditEntityType entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
