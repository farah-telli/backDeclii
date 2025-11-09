package tn.example.backdeclitech.presence_managment.events;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;
import tn.example.backdeclitech.presence_managment.entities.Presence;

@Getter
public class PresenceUpdatedEvent extends ApplicationEvent{

    private final Presence presence;

    /**
     * Create a new PresenceUpdatedEvent.
     *
     * @param source Source of the event
     * @param presence The updated presence
     */
    public PresenceUpdatedEvent(Object source, Presence presence) {
        super(source);
        this.presence = presence;
    }
}
