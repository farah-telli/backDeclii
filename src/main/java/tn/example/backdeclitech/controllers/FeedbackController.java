package tn.example.backdeclitech.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tn.example.backdeclitech.DTO.ApiResponse;
import tn.example.backdeclitech.DTO.FeedbackRequest;
import tn.example.backdeclitech.DTO.FeedbackResponse;
import tn.example.backdeclitech.entities.User;
import tn.example.backdeclitech.services.FeedbackService;

import java.util.List;


@RestController
@RequestMapping("/api/feedbacks")
@AllArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<?> createFeedback(@Valid @RequestBody FeedbackRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentification requise.");
            }

            User parent = (User) authentication.getPrincipal();
            FeedbackResponse response = feedbackService.createFeedback(request, parent);

            return ResponseEntity.ok(new ApiResponse("Avis ajouté avec succès", response));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Vous avez déjà donné un avis pour cette réservation.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'ajout de l'avis : " + e.getMessage());
        }
    }

    @GetMapping("/exists/{reservationId}")
    public ResponseEntity<Boolean> checkFeedbackExists(@PathVariable Long reservationId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            User parent = (User) authentication.getPrincipal();
            boolean exists = feedbackService.feedbackExists(reservationId, parent);

            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getall")
    public ResponseEntity<List<FeedbackResponse>> getAllFeedbacks() {
        try {
            List<FeedbackResponse> feedbacks = feedbackService.getAllFeedbacks();
            return ResponseEntity.ok(feedbacks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/process/{id}")
    public ResponseEntity<?> processFeedback(@PathVariable int id) {
        try {
            FeedbackResponse response = feedbackService.markAsProcessed(id);
            return ResponseEntity.ok(new ApiResponse("Avis traité avec succès", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du traitement de l'avis : " + e.getMessage());
        }
    }

}