package tn.example.backdeclitech.presence_managment.events;

import lombok.Getter;
import tn.example.backdeclitech.presence_managment.entities.Presence;

import org.springframework.context.ApplicationEvent;

/**
 * Event triggered when a Presence is created.
 */
@Getter
public class PresenceCreatedEvent extends ApplicationEvent {

    private final Presence presence;

    /**
     * Create a new PresenceCreatedEvent.
     *
     * @param source Source of the event
     * @param presence The created presence
     */
    public PresenceCreatedEvent(Object source, Presence presence) {
        super(source);
        this.presence = presence;
    }
}