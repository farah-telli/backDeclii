package tn.example.backdeclitech.events.child_related_events;

import org.springframework.context.ApplicationEvent;

import tn.example.backdeclitech.entities.Child;

public class ChildUpdatedEvent extends ApplicationEvent {
    private final Child child;

    public ChildUpdatedEvent(Object source, Child child) {
        super(source);
        this.child = child;
    }

    public Child getChild() {
        return child;
    }
}