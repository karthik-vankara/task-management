package com.karthik.task_management_backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API Response Wrapper
 * 
 * Standardized response format for all API endpoints.
 * Wraps data or error information with metadata.
 * 
 * Success Response Example:
 * {
 *   "success": true,
 *   "data": { ... },
 *   "timestamp": 1234567890,
 *   "error": null
 * }
 * 
 * Error Response Example:
 * {
 *   "success": false,
 *   "data": null,
 *   "timestamp": 1234567890,
 *   "error": {
 *     "message": "Invalid token",
 *     "code": "UNAUTHORIZED"
 *   }
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    /**
     * Indicates overall success or failure of the request
     */
    private boolean success;
    
    /**
     * Response data (populated on success)
     */
    private T data;
    
    /**
     * Unix timestamp when response was generated
     */
    private long timestamp;
    
    /**
     * Error information (populated on failure)
     */
    private ErrorInfo error;
    
    /**
     * Creates a successful API response
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Creates a failed API response
     */
    public static <T> ApiResponse<T> error(String message, String code) {
        return ApiResponse.<T>builder()
                .success(false)
                .timestamp(System.currentTimeMillis())
                .error(ErrorInfo.builder()
                        .message(message)
                        .code(code)
                        .build())
                .build();
    }
    
    /**
     * Error information nested in response
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorInfo {
        private String message;
        private String code;
    }
}
