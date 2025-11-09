package tn.example.backdeclitech.presence_managment.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.example.backdeclitech.presence_managment.entities.SiteEntryPresence;
import tn.example.backdeclitech.presence_managment.events.PresenceCreatedEvent;
import tn.example.backdeclitech.presence_managment.services.SiteEntryPresenceService;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener for creating SiteExitPresence records when a Presence is created.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SiteExitPresenceListener {

    private final SiteEntryPresenceService siteEntryPresenceService;

    /**
     * Handle PresenceCreatedEvent to create a SiteExitPresence if applicable.
     *
     * @param event The PresenceCreatedEvent
     */
    @Async
    @EventListener
    public void onPresenceCreated(PresenceCreatedEvent event) {
        var presence = event.getPresence();
        log.info("Handling PresenceCreatedEvent for presence ID: {}", presence.getId());

        try {
            log.info("Creating SiteEntryPresence for presence ID: {}", presence.getId());
            siteEntryPresenceService.createSiteEntryPresence(SiteEntryPresence.builder()
                    .presence(presence)
                    .build());
        } catch (Exception ex) {
            log.error("Error creating SiteEntryPresence for presence ID: {}", presence.getId(), ex);
        }
    }
}