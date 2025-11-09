package tn.example.backdeclitech.presence_managment.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.example.backdeclitech.presence_managment.entities.ModuleSessionPresence;
import tn.example.backdeclitech.presence_managment.entities.Presence;
import tn.example.backdeclitech.presence_managment.events.PresenceCreatedEvent;
import tn.example.backdeclitech.presence_managment.services.ModuleSessionPresenceService;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener for creating ModuleSessionPresence records when a Presence is
 * created.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ModuleSessionPresenceListener {

    private final ModuleSessionPresenceService moduleSessionPresenceService;

    /**
     * Handle PresenceCreatedEvent to create a ModuleSessionPresence if applicable.
     *
     * @param event The PresenceCreatedEvent
     */
    @Async
    @EventListener
    public void onPresenceCreated(PresenceCreatedEvent event) {
        Presence presence = event.getPresence();

        log.info("Handling PresenceCreatedEvent for presence ID: {}", presence.getId());

        if (presence.getSession() == null) {
            log.debug("No session associated with presence ID: {}, skipping ModuleSessionPresence creation",
                    presence.getId());
            return;
        }

        try {
            log.info("Creating ModuleSessionPresence for presence ID: {}", presence.getId());
            moduleSessionPresenceService.createModuleSessionPresence(ModuleSessionPresence.builder()
                    .presence(presence)
                    .build());
            log.info("Successfully created ModuleSessionPresence for presence ID: {}", presence.getId());
        } catch (Exception ex) {
            log.error("Error creating ModuleSessionPresence for presence ID: {}", presence.getId(), ex);
        }
    }

}