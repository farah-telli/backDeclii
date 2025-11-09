package tn.example.backdeclitech.presence_managment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    private String error;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    public ApiResponse(String message, T data) {
        this.success = true;
        this.message = message;
        this.data = data;
        this.error = null;
        this.timestamp = LocalDateTime.now();
    }
    
    public ApiResponse(String message) {
        this.success = true;
        this.message = message;
        this.data = null;
        this.error = null;
        this.timestamp = LocalDateTime.now();
    }
    
    public ApiResponse(boolean success, String message, T data, String error) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.error = error;
        this.timestamp = LocalDateTime.now();
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }
    
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, null);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, null);
    }
    
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data, null);
    }
    
    public static <T> ApiResponse<T> error(String message, String errorDetails) {
        return new ApiResponse<>(false, message, null, errorDetails);
    }
    
    public static <T> ApiResponse<T> error(String message, String errorDetails, T data) {
        return new ApiResponse<>(false, message, data, errorDetails);
    }
}
