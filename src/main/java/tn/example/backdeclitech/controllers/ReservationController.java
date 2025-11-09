package tn.example.backdeclitech.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tn.example.backdeclitech.DTO.ApiResponse;
import tn.example.backdeclitech.DTO.ReservationRequest;
import tn.example.backdeclitech.DTO.ReservationResponse;
import tn.example.backdeclitech.entities.User;
import tn.example.backdeclitech.services.ReservationService;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody ReservationRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentification requise.");
            }

            User parent = (User) authentication.getPrincipal();
            ReservationResponse response = reservationService.createReservation(request, parent);

            return ResponseEntity.ok(new ApiResponse("Réservation créée avec succès.", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la création de la réservation : " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllParentReservations() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vous devez être connecté pour effectuer cette action.");
            }

            User parent = (User) authentication.getPrincipal();
            List<ReservationResponse> reservations = reservationService.getReservationsByParent(parent);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la récupération des réservations : " + e.getMessage());
        }
    }

    @GetMapping("/child/{childId}")
    public ResponseEntity<?> getReservationsByChild(@PathVariable Long childId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vous devez être connecté pour effectuer cette action.");
            }

            User parent = (User) authentication.getPrincipal();
            List<ReservationResponse> reservations = reservationService.getReservationsByChild(childId, parent);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la récupération des réservations : " + e.getMessage());
        }
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<?> getReservationsByModule(@PathVariable Long moduleId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vous devez être connecté pour effectuer cette action.");
            }

            User parent = (User) authentication.getPrincipal();
            List<ReservationResponse> reservations = reservationService.getReservationsByModule(moduleId, parent);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la récupération des réservations : " + e.getMessage());
        }
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<String> cancelReservation(@PathVariable Long reservationId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vous devez être connecté pour effectuer cette action.");
            }

            User parent = (User) authentication.getPrincipal();
            reservationService.cancelReservation(reservationId, parent);
            return ResponseEntity.ok("Réservation annulée avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'annulation de la réservation : " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllReservations() {
        try {
            List<ReservationResponse> reservations = reservationService.getAllReservations();
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération de toutes les réservations : " + e.getMessage());
        }
    }
}