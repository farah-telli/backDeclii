package tn.example.backdeclitech.scheduling;

import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tn.example.backdeclitech.services.ReservationService;

@Component
@AllArgsConstructor
public class ReservationScheduler {

    private final ReservationService reservationService;

    // Exécuter toutes les 10 minutes
    @Scheduled(fixedRate = 600000)
    public void updateReservationStatuses() {
        System.out.println("Exécution de la tâche planifiée: mise à jour des statuts de réservation...");
        reservationService.updateCompletedReservations();
    }
}