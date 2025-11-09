package tn.example.backdeclitech.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4300", allowCredentials = "true")
@Slf4j
public class NotificationController {


    @GetMapping
    public ResponseEntity<?> getAllNotifications() {
        log.info("📋 Récupération de toutes les notifications");


        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("notifications", List.of());
        response.put("count", 0);
        response.put("message", "Endpoint /api/notifications créé - Implémentation à venir");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserNotifications(@PathVariable Long userId) {
        log.info("🔍 Récupération des notifications pour l'utilisateur: {}", userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", userId);
        response.put("notifications", List.of());
        response.put("count", 0);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        log.info("✅ Marquer notification {} comme lue", id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Notification marquée comme lue");
        response.put("notificationId", id);

        return ResponseEntity.ok(response);
    }


    @PatchMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        log.info("✅ Marquer toutes les notifications comme lues");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Toutes les notifications ont été marquées comme lues");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        log.info("🗑️ Suppression de la notification: {}", id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Notification supprimée");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount() {
        log.info("📊 Récupération du nombre de notifications non lues");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("unreadCount", 0);

        return ResponseEntity.ok(response);
    }
}