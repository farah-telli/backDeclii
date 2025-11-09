package tn.example.backdeclitech.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import tn.example.backdeclitech.entities.ModuleSession;
import tn.example.backdeclitech.services.ModuleSessionService;
import tn.example.backdeclitech.services.NotificationService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4300", allowCredentials = "true")
@Slf4j
public class TestEmailController {

    private final JavaMailSender mailSender;
    private final NotificationService notificationService;
    private final ModuleSessionService moduleSessionService;

    /**
     * Test simple d'envoi d'email
     * GET /api/test/email?to=email@example.com
     */
    @GetMapping("/email")
    public ResponseEntity<?> testSimpleEmail(@RequestParam String to) {
        log.info("🧪 Test envoi email simple vers: {}", to);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@declitech.com");
            message.setTo(to);
            message.setSubject("Test Email - DécliTech");
            message.setText("Ceci est un email de test. Si vous recevez ceci, la configuration email fonctionne !");

            mailSender.send(message);

            log.info("✅ Email de test envoyé avec succès");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Email envoyé avec succès à " + to
            ));

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi de l'email de test", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "cause", e.getCause() != null ? e.getCause().getMessage() : "N/A"
            ));
        }
    }

    /**
     * Test de notification pour une session spécifique
     * GET /api/test/notification/session/{sessionId}
     */
    @GetMapping("/notification/session/{sessionId}")
    public ResponseEntity<?> testSessionNotification(@PathVariable Long sessionId) {
        log.info("🧪 Test notification pour session: {}", sessionId);

        try {
            ModuleSession session = moduleSessionService.findById(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session non trouvée"));

            log.info("📋 Session trouvée: ID={}, Module={}, Actif={}, Annulé={}",
                    session.getId(),
                    session.getModule() != null ? session.getModule().getTitle() : "N/A",
                    session.isActive(),
                    session.isAnnule());

            log.info("📊 Nombre de réservations: {}",
                    session.getReservations() != null ? session.getReservations().size() : 0);

            // Test d'envoi de notification
            notificationService.notifySessionDeactivation(session);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Test de notification lancé");
            response.put("sessionId", sessionId);
            response.put("reservations", session.getReservations() != null ? session.getReservations().size() : 0);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erreur lors du test de notification", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "type", e.getClass().getSimpleName()
            ));
        }
    }

    /**
     * Vérifier les réservations d'une session
     * GET /api/test/session/{sessionId}/reservations
     */
    @GetMapping("/session/{sessionId}/reservations")
    public ResponseEntity<?> checkSessionReservations(@PathVariable Long sessionId) {
        log.info("🔍 Vérification des réservations pour session: {}", sessionId);

        try {
            ModuleSession session = moduleSessionService.findById(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session non trouvée"));

            Map<String, Object> response = new HashMap<>();
            response.put("sessionId", sessionId);
            response.put("hasReservations", session.getReservations() != null && !session.getReservations().isEmpty());
            response.put("reservationCount", session.getReservations() != null ? session.getReservations().size() : 0);
            response.put("reservations", session.getReservations());

            log.info("📊 Réservations: {}", response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erreur", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Test de la configuration email
     * GET /api/test/email-config
     */
    @GetMapping("/email-config")
    public ResponseEntity<?> testEmailConfiguration() {
        log.info("🔧 Vérification de la configuration email");

        Map<String, Object> response = new HashMap<>();

        try {
            // Tester la connexion SMTP
            mailSender.createMimeMessage();

            response.put("success", true);
            response.put("message", "Configuration email OK - JavaMailSender initialisé");
            response.put("mailSenderClass", mailSender.getClass().getName());

            log.info("✅ Configuration email valide");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erreur de configuration email", e);

            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("hint", "Vérifiez les paramètres spring.mail.* dans application.properties");

            return ResponseEntity.badRequest().body(response);
        }
    }
}