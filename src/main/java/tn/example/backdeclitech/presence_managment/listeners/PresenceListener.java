package tn.example.backdeclitech.presence_managment.listeners;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.example.backdeclitech.entities.Child;
import tn.example.backdeclitech.entities.Reservation;
import tn.example.backdeclitech.events.child_related_events.ChildUpdatedEvent;
import tn.example.backdeclitech.presence_managment.entities.Presence;
import tn.example.backdeclitech.presence_managment.entities.PresenceStatus;
import tn.example.backdeclitech.presence_managment.services.PresenceService;
import tn.example.backdeclitech.services.ReservationService.ReservationCanceledEvent;
import tn.example.backdeclitech.services.ReservationService.ReservationCreatedEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class PresenceListener {
    private final PresenceService presenceService;


    @Async
    @EventListener
    public void handleReservationCreated(ReservationCreatedEvent event) {
        try {
            Reservation reservation = event.getReservation();
            if (reservation.isPenalise()) {
                log.warn("Reservation is penalized, skipping presence creation for reservation ID: {}",
                        reservation.getId());
                return;
            }
            Presence presence = Presence.builder()
                    .child(reservation.getChild())
                    .site(reservation.getModule().getCoBuildSpace())
                    .reservationDate(reservation.getDateReservation())
                    .status(PresenceStatus.PENDING)
                    .build();
            presenceService.savePresence(presence);
        } catch (Exception ex) {
            log.error("Error handling ReservationCreatedEvent: {}", ex.getMessage(), ex);
        }
    }

    @Async
    @EventListener
    public void handleReservationCanceled(ReservationCanceledEvent event) {
        try {
            Reservation reservation = event.getReservation();
            presenceService.deletePendingPresencesForReservation(reservation);
        } catch (Exception ex) {
            log.error("Error handling ReservationCanceledEvent: {}", ex.getMessage(), ex);
        }
    }

    @Async
    @EventListener
    public void handleChildUpdated(ChildUpdatedEvent event) {
        try {
            Child child = event.getChild();
            log.info("Child with ID: {} was updated. Updating their presence records.", child.getId());
            presenceService.updatePresenceNamesForChild(child);
        } catch (Exception ex) {
            log.error("Error handling ChildUpdatedEvent: {}", ex.getMessage(), ex);
        }
    }
}
