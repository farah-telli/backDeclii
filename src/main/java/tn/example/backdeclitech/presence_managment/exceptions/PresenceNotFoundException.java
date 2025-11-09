package tn.example.backdeclitech.presence_managment.exceptions;

/**
 * Exception thrown when a presence record is not found
 */
public class PresenceNotFoundException extends RuntimeException {
    
    public PresenceNotFoundException(String message) {
        super(message);
    }
    
    public PresenceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
