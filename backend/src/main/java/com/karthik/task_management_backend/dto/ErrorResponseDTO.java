package com.karthik.task_management_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Error Response DTO
 * 
 * Detailed error information for failed API requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponseDTO {
    
    /**
     * Human-readable error message
     */
    private String message;
    
    /**
     * Machine-readable error code for client handling
     * Examples: UNAUTHORIZED, FORBIDDEN, NOT_FOUND, VALIDATION_ERROR, INTERNAL_SERVER_ERROR
     */
    private String code;
    
    /**
     * HTTP status code
     */
    private int status;
    
    /**
     * Request timestamp
     */
    private long timestamp;
    
    /**
     * Request path that caused the error
     */
    private String path;
    
    /**
     * Detailed validation errors (for VALIDATION_ERROR code)
     */
    private Object details;
}
