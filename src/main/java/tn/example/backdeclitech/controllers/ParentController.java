package tn.example.backdeclitech.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.example.backdeclitech.entities.*;
import tn.example.backdeclitech.repositories.UserRepository;
import tn.example.backdeclitech.services.SmsService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/controller")
@AllArgsConstructor
@CrossOrigin
public class ParentController {

    private final UserRepository userRepository;
    private final SmsService smsService;


    @PostMapping(value = "/send-code", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> sendVerificationCode(@RequestBody PhoneDTO dto) {
        String phone = dto.getPhoneNumber().replaceAll("\\s+", "").trim();
        System.out.println("☎️ Numéro reçu : " + phone);

        Optional<User> userOpt = userRepository.findByPhone(phone);

        if (userOpt.isEmpty()) {
            System.out.println("❌ Utilisateur introuvable.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Numéro non trouvé");
        }

        User user = userOpt.get();
        System.out.println("👤 Utilisateur trouvé : " + user.getFirstName() + " " + user.getLastName());

        if (user.getRole() != Role.PARENT) {
            System.out.println("⛔ Rôle invalide : " + user.getRole());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Rôle invalide");
        }

        if (user.getCodeExpirationDate() != null && user.getCodeExpirationDate().isAfter(LocalDateTime.now())) {
            System.out.println("🚫 Un code est déjà actif jusqu’à : " + user.getCodeExpirationDate());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Un code a déjà été envoyé. Veuillez réessayer plus tard.");
        }

        String code = generateSixDigitCode();
        user.setVerificationCode(code);
        user.setCodeExpirationDate(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        String smsContent = "Votre code de vérification Declitech est : " + code;
        smsService.sendSms(phone, smsContent);

        System.out.println("✅ Code envoyé à : " + phone + " | Code : " + code);
        return ResponseEntity.ok("Code envoyé par SMS");
    }


    private String generateSixDigitCode() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }

    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody CodeDTO dto) {
        Optional<User> parentOpt = userRepository.findByPhone(dto.getPhoneNumber());

        if (parentOpt.isPresent()) {
            User parent = parentOpt.get();

            if (parent.getVerificationCode() != null &&
                    parent.getVerificationCode().equals(dto.getCode()) &&
                    parent.getCodeExpirationDate() != null &&
                    parent.getCodeExpirationDate().isAfter(LocalDateTime.now())) {

                parent.setIsVerified(true);
                parent.setVerificationCode(null);
                parent.setVerificationCode(null);
                userRepository.save(parent);

                return ResponseEntity.ok("Compte vérifié avec succès");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Code invalide ou expiré");
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Numéro non trouvé");
    }

}
