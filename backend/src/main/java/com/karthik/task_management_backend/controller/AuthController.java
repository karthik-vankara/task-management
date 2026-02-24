package com.karthik.task_management_backend.controller;

import com.karthik.task_management_backend.service.AuthService;
import com.karthik.task_management_backend.service.AuthService.AuthorizationUrlResponse;
import com.karthik.task_management_backend.service.AuthService.CallbackResponse;
import com.karthik.task_management_backend.service.AuthService.LogoutResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * 
 * REST API endpoints for OAuth2 and JWT authentication:
 * 1. POST /api/auth/login - Initiate OAuth2 login with Google
 * 2. GET /api/auth/callback - Handle OAuth2 callback from Google
 * 3. GET /api/auth/profile - Get current authenticated user profile
 * 4. POST /api/auth/logout - Logout (stateless confirmation)
 * 
 * OAuth2 Login Flow:
 * 1. Frontend calls POST /api/auth/login
 * 2. Backend returns Google OAuth URL and state parameter
 * 3. Frontend redirects user to Google OAuth URL
 * 4. User authenticates with Google and grants permission
 * 5. Google redirects back to /api/auth/callback?code=...&state=...
 * 6. Frontend receives authorization code
 * 7. Frontend calls GET /api/auth/callback?code=...&state=...
 * 8. Backend exchanges code for user info
 * 9. Backend creates/updates user in database
 * 10. Backend returns JWT token
 * 11. Frontend stores JWT and includes in Authorization header
 * 12. Protected endpoints are now accessible with Bearer token
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Initiate OAuth2 login flow
     * 
     * Generates a secure state parameter and returns the Google OAuth2 authorization URL.
     * Frontend should redirect user to this URL for authentication.
     * 
     * Request: POST /api/auth/login
     * Response: { redirectUrl: "https://accounts.google.com/o/oauth2/v2/auth?...", state: "..." }
     * 
     * @return OAuth2 authorization URL and state parameter
     */
    @PostMapping("/login")
    public ResponseEntity<AuthorizationUrlResponse> login() {
        log.info("Login request received");
        
        AuthorizationUrlResponse response = authService.generateLoginUrl();
        
        log.info("Returning OAuth2 authorization URL");
        return ResponseEntity.ok(response);
    }

    /**
     * Handle OAuth2 callback from Google
     * 
     * Google redirects user back to this endpoint with authorization code and state.
     * Exchange code for user information, create/update user, and return JWT token.
     * 
     * Request: GET /api/auth/callback?code=...&state=...
     * Response: { token: "...", userId: 123, email: "...", name: "...", role: "USER" }
     * 
     * Frontend should then:
     * 1. Extract token from response
     * 2. Store in localStorage or secure cookie
     * 3. Include in Authorization header for subsequent requests
     * 
     * @param code OAuth2 authorization code from Google
     * @param state CSRF protection state parameter (backend should validate)
     * @return JWT token and user information
     */
    @GetMapping("/callback")
    public ResponseEntity<CallbackResponse> callback(
            @RequestParam String code,
            @RequestParam String state
    ) {
        log.info("OAuth2 callback received with state: {}", state);
        
        try {
            CallbackResponse response = authService.handleOAuthCallback(code, state);
            log.info("Successfully authenticated user: {}", response.getEmail());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error handling OAuth2 callback", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CallbackResponse.builder()
                            .build());
        }
    }

    /**
     * Get current authenticated user profile
     * 
     * Returns the profile of the currently authenticated user.
     * Requires valid JWT token in Authorization header.
     * 
     * Request: GET /api/auth/profile
     * Header: Authorization: Bearer <JWT_TOKEN>
     * Response: { userId: 123, email: "user@example.com", name: "User", role: "USER", ... }
     * 
     * Protected endpoint - returns 401 if not authenticated.
     * 
     * @return Current user profile
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile() {
        log.debug("Profile request received");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Profile request without authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Extract user information from JWT token
        // Token was validated and claims extracted by JwtAuthenticationFilter
        Object principal = authentication.getPrincipal();
        
        // For JWT authentication, extract from SecurityContext
        Long userId = null;
        String email = null;
        String name = null;
        String role = null;

        if (authentication instanceof com.karthik.task_management_backend.security.JwtAuthenticationToken) {
            com.karthik.task_management_backend.security.JwtAuthenticationToken jwtToken = 
                    (com.karthik.task_management_backend.security.JwtAuthenticationToken) authentication;
            userId = jwtToken.getUserId();
            email = jwtToken.getEmail();
            role = jwtToken.getRole();
            // Note: name would need to be fetched from database in production
        }

        UserProfileResponse response = UserProfileResponse.builder()
                .userId(userId)
                .email(email)
                .name(name)
                .role(role)
                .build();

        log.info("Returning profile for user: {}", userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Logout endpoint
     * 
     * Confirms logout to client. Since JWT authentication is stateless,
     * the actual logout happens on the client side by removing the token from storage.
     * 
     * Request: POST /api/auth/logout
     * Header: Authorization: Bearer <JWT_TOKEN>
     * Response: { message: "Logout successful", timestamp: ... }
     * 
     * Protected endpoint - requires authentication.
     * Optional: In production, could implement token blacklist for enforcement.
     * 
     * @return Logout confirmation
     */
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout() {
        log.debug("Logout request received");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Long userId = null;
        if (authentication instanceof com.karthik.task_management_backend.security.JwtAuthenticationToken) {
            com.karthik.task_management_backend.security.JwtAuthenticationToken jwtToken = 
                    (com.karthik.task_management_backend.security.JwtAuthenticationToken) authentication;
            userId = jwtToken.getUserId();
        }

        LogoutResponse response = authService.logout(userId);
        log.info("Logout successful for user: {}", userId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * User profile response DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class UserProfileResponse {
        private Long userId;
        private String email;
        private String name;
        private String role;
    }
}
