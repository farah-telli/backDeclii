package tn.example.backdeclitech.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tn.example.backdeclitech.entities.ModuleSession;
import tn.example.backdeclitech.entities.Reservation;
import tn.example.backdeclitech.entities.User;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;
    private static final String FROM_EMAIL = "farah.telli@esprit.tn";

    @Async
    public void notifySessionCancellation(ModuleSession session) {
        log.info("🔔 Envoi des notifications d'annulation pour la session {}", session.getId());

        Set<String> parentEmails = getParentEmailsFromSession(session);

        if (parentEmails.isEmpty()) {
            log.warn("⚠️ Aucun parent inscrit pour la session {}", session.getId());
            return;
        }

        String subject = "🚫 Annulation de session - " + getModuleTitle(session);
        String htmlContent = buildCancellationEmailHtml(session);

        for (String email : parentEmails) {
            try {
                sendHtmlEmail(email, subject, htmlContent);
                log.info("✅ Email d'annulation envoyé à: {}", email);
            } catch (Exception e) {
                log.error("❌ Erreur lors de l'envoi de l'email à {}: {}", email, e.getMessage());
            }
        }

        log.info("✅ Notifications d'annulation envoyées à {} parent(s)", parentEmails.size());
    }


    @Async
    public void notifySessionReactivation(ModuleSession session) {
        log.info("🔔 Envoi des notifications de réactivation pour la session {}", session.getId());

        Set<String> parentEmails = getParentEmailsFromSession(session);

        if (parentEmails.isEmpty()) {
            log.warn("⚠️ Aucun parent inscrit pour la session {}", session.getId());
            return;
        }

        String subject = "✅ Réactivation de session - " + getModuleTitle(session);
        String htmlContent = buildReactivationEmailHtml(session);

        for (String email : parentEmails) {
            try {
                sendHtmlEmail(email, subject, htmlContent);
                log.info("✅ Email de réactivation envoyé à: {}", email);
            } catch (Exception e) {
                log.error("❌ Erreur lors de l'envoi de l'email à {}: {}", email, e.getMessage());
            }
        }

        log.info("✅ Notifications de réactivation envoyées à {} parent(s)", parentEmails.size());
    }


    @Async
    public void notifySessionDeactivation(ModuleSession session) {
        log.info("🔔 Envoi des notifications de désactivation pour la session {}", session.getId());

        Set<String> parentEmails = getParentEmailsFromSession(session);

        if (parentEmails.isEmpty()) {
            log.warn("⚠️ Aucun parent inscrit pour la session {}", session.getId());
            return;
        }

        String subject = "⏸️ Suspension temporaire de session - " + getModuleTitle(session);
        String htmlContent = buildDeactivationEmailHtml(session);

        for (String email : parentEmails) {
            try {
                sendHtmlEmail(email, subject, htmlContent);
                log.info("✅ Email de désactivation envoyé à: {}", email);
            } catch (Exception e) {
                log.error("❌ Erreur lors de l'envoi de l'email à {}: {}", email, e.getMessage());
            }
        }

        log.info("✅ Notifications de désactivation envoyées à {} parent(s)", parentEmails.size());
    }


    @Async
    public void notifyMultipleSessionsCancellation(int count, String moduleTitle, String coBuildSpaceName) {
        log.info("🔔 Envoi de notifications pour annulation de {} sessions", count);
        // Cette méthode peut être étendue selon vos besoins
    }

    private Set<String> getParentEmailsFromSession(ModuleSession session) {
        Set<String> emails = new HashSet<>();

        log.info("🔍 Recherche des parents pour la session ID: {}", session.getId());

        if (session.getReservations() == null || session.getReservations().isEmpty()) {
            log.warn("⚠️ Aucune réservation trouvée pour la session {}", session.getId());
            return emails;
        }

        log.info("📊 {} réservation(s) trouvée(s)", session.getReservations().size());

        for (Reservation reservation : session.getReservations()) {
            User parent = reservation.getParent();
            if (parent != null && parent.getEmail() != null && !parent.getEmail().isEmpty()) {
                emails.add(parent.getEmail());
                log.info("✉️ Email parent ajouté: {}", parent.getEmail());
            } else {
                log.warn("⚠️ Réservation sans parent ou email valide: ID {}", reservation.getId());
            }
        }

        log.info("✅ Total emails collectés: {}", emails.size());
        return emails;
    }

    private String buildCancellationEmailHtml(ModuleSession session) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

        String dateStr = session.getDate() != null ? dateFormat.format(session.getDate()) : "N/A";
        String startTime = session.getStartTime() != null ? session.getStartTime().format(timeFormat) : "N/A";
        String endTime = session.getEndTime() != null ? session.getEndTime().format(timeFormat) : "N/A";

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }
                    .header { background-color: #dc3545; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                    .content { background-color: white; padding: 30px; border-radius: 0 0 5px 5px; }
                    .info-box { background-color: #f8f9fa; padding: 15px; margin: 20px 0; border-left: 4px solid #dc3545; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    h1 { margin: 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🚫 Session Annulée</h1>
                    </div>
                    <div class="content">
                        <p>Cher(e) Parent,</p>
                        
                        <p>Nous vous informons que la session suivante a été <strong>annulée</strong> :</p>
                        
                        <div class="info-box">
                            <p><strong>Module :</strong> %s</p>
                            <p><strong>Espace :</strong> %s</p>
                            <p><strong>Date :</strong> %s</p>
                            <p><strong>Horaire :</strong> %s - %s</p>
                            <p><strong>Tranche d'âge :</strong> %s</p>
                        </div>
                        
                        <p>Nous nous excusons pour ce désagrément. Si vous avez des questions, n'hésitez pas à nous contacter.</p>
                        
                        <p>Cordialement,<br><strong>L'équipe DécliTech</strong></p>
                    </div>
                    <div class="footer">
                        <p>Cet email a été envoyé automatiquement. Merci de ne pas y répondre.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                getModuleTitle(session),
                getCoBuildSpaceName(session),
                dateStr,
                startTime,
                endTime,
                session.getFormattedTranche()
        );
    }


    private String buildReactivationEmailHtml(ModuleSession session) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

        String dateStr = session.getDate() != null ? dateFormat.format(session.getDate()) : "N/A";
        String startTime = session.getStartTime() != null ? session.getStartTime().format(timeFormat) : "N/A";
        String endTime = session.getEndTime() != null ? session.getEndTime().format(timeFormat) : "N/A";

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }
                    .header { background-color: #28a745; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                    .content { background-color: white; padding: 30px; border-radius: 0 0 5px 5px; }
                    .info-box { background-color: #f8f9fa; padding: 15px; margin: 20px 0; border-left: 4px solid #28a745; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    h1 { margin: 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Session Réactivée</h1>
                    </div>
                    <div class="content">
                        <p>Cher(e) Parent,</p>
                        
                        <p>Bonne nouvelle ! La session suivante a été <strong>réactivée</strong> :</p>
                        
                        <div class="info-box">
                            <p><strong>Module :</strong> %s</p>
                            <p><strong>Espace :</strong> %s</p>
                            <p><strong>Date :</strong> %s</p>
                            <p><strong>Horaire :</strong> %s - %s</p>
                            <p><strong>Tranche d'âge :</strong> %s</p>
                        </div>
                        
                        <p>Votre réservation est toujours valide. Nous vous attendons avec plaisir !</p>
                        
                        <p>Cordialement,<br><strong>L'équipe DécliTech</strong></p>
                    </div>
                    <div class="footer">
                        <p>Cet email a été envoyé automatiquement. Merci de ne pas y répondre.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                getModuleTitle(session),
                getCoBuildSpaceName(session),
                dateStr,
                startTime,
                endTime,
                session.getFormattedTranche()
        );
    }

    /**
     * Construit le contenu HTML pour l'email de désactivation
     */
    private String buildDeactivationEmailHtml(ModuleSession session) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

        String dateStr = session.getDate() != null ? dateFormat.format(session.getDate()) : "N/A";
        String startTime = session.getStartTime() != null ? session.getStartTime().format(timeFormat) : "N/A";
        String endTime = session.getEndTime() != null ? session.getEndTime().format(timeFormat) : "N/A";

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }
                    .header { background-color: #ffc107; color: #333; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                    .content { background-color: white; padding: 30px; border-radius: 0 0 5px 5px; }
                    .info-box { background-color: #f8f9fa; padding: 15px; margin: 20px 0; border-left: 4px solid #ffc107; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    h1 { margin: 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⏸️ Session Suspendue Temporairement</h1>
                    </div>
                    <div class="content">
                        <p>Cher(e) Parent,</p>
                        
                        <p>Nous vous informons que la session suivante a été <strong>temporairement suspendue</strong> :</p>
                        
                        <div class="info-box">
                            <p><strong>Module :</strong> %s</p>
                            <p><strong>Espace :</strong> %s</p>
                            <p><strong>Date :</strong> %s</p>
                            <p><strong>Horaire :</strong> %s - %s</p>
                            <p><strong>Tranche d'âge :</strong> %s</p>
                        </div>
                        
                        <p>Cette suspension est temporaire. Nous vous tiendrons informé(e) dès que la session sera de nouveau disponible.</p>
                        
                        <p>Cordialement,<br><strong>L'équipe DécliTech</strong></p>
                    </div>
                    <div class="footer">
                        <p>Cet email a été envoyé automatiquement. Merci de ne pas y répondre.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                getModuleTitle(session),
                getCoBuildSpaceName(session),
                dateStr,
                startTime,
                endTime,
                session.getFormattedTranche()
        );
    }

    /**
     * Envoie un email HTML
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(FROM_EMAIL);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    // Méthodes utilitaires
    private String getModuleTitle(ModuleSession session) {
        return session.getModule() != null ? session.getModule().getTitle() : "N/A";
    }

    private String getCoBuildSpaceName(ModuleSession session) {
        return session.getCoBuildSpace() != null ? session.getCoBuildSpace().getName() : "N/A";
    }
}