package com.karthik.task_management_backend.exception;

/**
 * Exception for JWT token validation and processing failures
 */
public class JwtException extends RuntimeException {
    
    public JwtException(String message) {
        super(message);
    }
    
    public JwtException(String message, Throwable cause) {
        super(message, cause);
    }
}
