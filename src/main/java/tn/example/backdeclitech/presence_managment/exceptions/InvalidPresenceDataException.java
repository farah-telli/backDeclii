package tn.example.backdeclitech.presence_managment.exceptions;

/**
 * Exception thrown when presence data is invalid or fails validation
 */
public class InvalidPresenceDataException extends RuntimeException {
    
    public InvalidPresenceDataException(String message) {
        super(message);
    }
    
    public InvalidPresenceDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
