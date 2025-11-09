package tn.example.backdeclitech.presence_managment.exceptions;

import lombok.extern.slf4j.Slf4j;
import tn.example.backdeclitech.presence_managment.dto.ApiResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for Unified Presence System
 * 
 * Handles all presence-related exceptions and provides consistent error responses
 */
@RestControllerAdvice(basePackages = "tn.example.backdeclitech.unified_presence_system")
@Slf4j
public class PresenceExceptionHandler {
    
    @ExceptionHandler(PresenceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handlePresenceNotFoundException(PresenceNotFoundException ex) {
        log.error("Presence not found: {}", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(
                "Presence not found: " + ex.getMessage(),
                null
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(InvalidPresenceDataException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidPresenceDataException(InvalidPresenceDataException ex) {
        log.error("Invalid presence data: {}", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(
                "Invalid presence data: " + ex.getMessage(),
                null
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(
                "Invalid argument: " + ex.getMessage(),
                null
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error in unified presence system: {}", ex.getMessage(), ex);
        
        ApiResponse<Object> response = ApiResponse.error(
                "An unexpected error occurred in presence management",
                null
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
