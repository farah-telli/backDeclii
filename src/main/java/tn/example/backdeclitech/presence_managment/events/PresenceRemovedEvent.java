package tn.example.backdeclitech.presence_managment.events;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;
import tn.example.backdeclitech.presence_managment.entities.Presence;

@Getter
public class PresenceRemovedEvent extends ApplicationEvent {

    private final List<Presence> presence;

    /**
     * Create a new PresenceCreatedEvent.
     *
     * @param source Source of the event
     * @param presence The created presence
     */
    public PresenceRemovedEvent(Object source, List<Presence> presence) {
        super(source);
        this.presence = presence;
    }
    
}
