package tn.example.backdeclitech.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tn.example.backdeclitech.DTO.*;
import tn.example.backdeclitech.entities.CodeDTO;
import tn.example.backdeclitech.entities.PhoneDTO;
import tn.example.backdeclitech.entities.Role;
import tn.example.backdeclitech.entities.User;
import tn.example.backdeclitech.repositories.UserRepository;
import tn.example.backdeclitech.services.IAuthenticationService;
import tn.example.backdeclitech.services.SmsService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final SmsService smsService;
    private final IAuthenticationService authenticationService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login-email")
    public ResponseEntity<?> loginWithEmail(@RequestBody EmailLoginRequest loginRequest) throws Exception {
        return ResponseEntity.ok(authenticationService.authenticateOthers(loginRequest));
    }

    @PostMapping("/login-phone")
    public ResponseEntity<?> loginWithPhone(@RequestBody PhoneLoginRequest loginRequest) throws Exception {
        return ResponseEntity.ok(authenticationService.authenticateParent(loginRequest));
    }

    @PostMapping("/verify-code-login")
    public ResponseEntity<?> loginWithCode(@RequestBody CodeVerificationRequest request) throws Exception {
        return ResponseEntity.ok(authenticationService.authenticateParentWithCode(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) throws Exception {
        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) throws Exception {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authenticationService.logout(token);
            return ResponseEntity.ok("Logout successful");
        }
        return ResponseEntity.badRequest().body("Invalid token");
    }

    @PostMapping(value = "/send-code", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> sendVerificationCode(@RequestBody PhoneDTO dto) {
        String phone = dto.getPhoneNumber().replaceAll("\\s+", "").trim();
        log.info("☎️ Numéro reçu : {}", phone);

        Optional<User> userOpt = userRepository.findByPhone(phone);

        if (userOpt.isEmpty()) {
            log.warn("❌ Utilisateur introuvable.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Numéro non trouvé");
        }

        User user = userOpt.get();
        log.info("👤 Utilisateur trouvé : {} {}", user.getFirstName(), user.getLastName());

        if (user.getRole() != Role.PARENT) {
            log.warn("⛔ Rôle invalide : {}", user.getRole());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Rôle invalide");
        }

        if (user.getCodeExpirationDate() != null &&
                user.getCodeExpirationDate().isAfter(LocalDateTime.now())) {
            log.warn("🚫 Un code est déjà actif jusqu'à : {}", user.getCodeExpirationDate());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Un code a déjà été envoyé. Veuillez réessayer plus tard.");
        }

        String code = generateSixDigitCode();
        user.setVerificationCode(code);
        user.setCodeExpirationDate(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        String smsContent = "Votre code de vérification Declitech est : " + code;
        smsService.sendSms(phone, smsContent);

        log.info("✅ Code envoyé à : {} | Code : {}", phone, code);
        return ResponseEntity.ok("Code envoyé par SMS");
    }

    private String generateSixDigitCode() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }

    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody CodeDTO dto) {
        String phone = dto.getPhoneNumber().replaceAll("\\s+", "").trim();
        log.info("🔍 Vérification du code pour : {}", phone);

        Optional<User> userOpt = userRepository.findByPhone(phone);

        if (userOpt.isEmpty()) {
            log.warn("❌ Utilisateur introuvable");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Numéro non trouvé");
        }

        User user = userOpt.get();

        if (user.getVerificationCode() != null &&
                user.getVerificationCode().equals(dto.getCode()) &&
                user.getCodeExpirationDate() != null &&
                user.getCodeExpirationDate().isAfter(LocalDateTime.now())) {

            user.setIsVerified(true);
            user.setVerificationCode(null);
            user.setCodeExpirationDate(null);
            userRepository.save(user);

            log.info("✅ Compte vérifié avec succès");
            return ResponseEntity.ok("Compte vérifié avec succès");
        } else {
            log.warn("❌ Code invalide ou expiré");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Code invalide ou expiré");
        }
    }

    @PostMapping("/reset-code")
    public ResponseEntity<String> resetCode(@RequestBody ResetCodeRequest request) {
        String phone = request.getPhoneNumber().replaceAll("\\s+", "").trim();
        log.info("🔄 Demande de réinitialisation pour : {}", phone);

        if (!request.getCode().equals(request.getConfirmCode())) {
            log.warn("❌ Les codes ne correspondent pas");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Les codes ne correspondent pas");
        }

        if (request.getCode().length() < 6) {
            log.warn("❌ Code trop court");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Le code doit faire au moins 6 caractères");
        }

        Optional<User> userOpt = userRepository.findByPhone(phone);

        if (userOpt.isEmpty()) {
            log.warn("❌ Utilisateur introuvable pour : {}", phone);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Numéro non trouvé");
        }

        User user = userOpt.get();
        log.info("👤 Utilisateur trouvé : {} {}", user.getFirstName(), user.getLastName());

        if (!user.getIsVerified()) {
            log.warn("⛔ Utilisateur non vérifié");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Compte non vérifié");
        }

        user.setPassword(passwordEncoder.encode(request.getCode()));
        user.setVerificationCode(null);
        user.setCodeExpirationDate(null);
        userRepository.save(user);

        log.info("✅ Code d'accès réinitialisé pour : {}", phone);
        return ResponseEntity.ok("Code d'accès réinitialisé avec succès");
    }
}