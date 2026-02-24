package com.karthik.task_management_backend.exception;

/**
 * Base exception for OAuth2 authentication failures
 */
public class OAuthException extends RuntimeException {
    
    public OAuthException(String message) {
        super(message);
    }
    
    public OAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
