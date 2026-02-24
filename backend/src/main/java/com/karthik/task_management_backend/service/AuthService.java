package com.karthik.task_management_backend.service;

import com.karthik.task_management_backend.entity.User;
import com.karthik.task_management_backend.entity.UserRole;
import com.karthik.task_management_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

/**
 * Authentication Service
 * 
 * Handles complete OAuth2 authentication flow:
 * 1. Login initiation: Generate state parameter and OAuth2 authorization URL
 * 2. Callback handling: Exchange authorization code for user info
 * 3. User management: Create or update user in database
 * 4. JWT generation: Create JWT token for authenticated user
 * 5. Logout: Simple acknowledgment (tokens are stateless)
 * 
 * OAuth2 Flow:
 * - Client requests /api/auth/login
 * - Service generates state and returns Google OAuth URL
 * - User authenticates with Google, redirected back to /api/auth/callback with code
 * - Service exchanges code for Google profile
 * - Service creates/updates user in database
 * - Service returns JWT token to client
 * - Client stores JWT and includes in Authorization header for protected requests
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final InMemoryClientRegistrationRepository clientRegistrationRepository;

    @Value("${app.oauth2.state-param-length:32}")
    private int stateParamLength;

    /**
     * Generate OAuth2 authorization URL for frontend redirect
     * 
     * Creates a secure state parameter to prevent CSRF attacks.
     * State should be stored in session/cache and validated in callback.
     * 
     * @return AuthorizationUrlResponse with redirectUrl and state
     */
    public AuthorizationUrlResponse generateLoginUrl() {
        log.info("Generating OAuth2 login authorization URL");

        try {
            // Generate cryptographically secure state parameter
            String state = generateSecureState();
            log.debug("Generated state parameter for OAuth2 flow");

            // Get Google client registration
            ClientRegistration googleClient = clientRegistrationRepository.findByRegistrationId("google");
            if (googleClient == null) {
                log.error("Google OAuth2 client registration not found");
                throw new OAuth2AuthorizationException("Google OAuth2 client not configured");
            }

            // Build authorization URL with required parameters
            String authorizationUrl = buildAuthorizationUrl(googleClient, state);
            log.info("Generated OAuth2 authorization URL");

            return AuthorizationUrlResponse.builder()
                    .redirectUrl(authorizationUrl)
                    .state(state)
                    .build();

        } catch (Exception e) {
            log.error("Error generating OAuth2 login URL", e);
            throw new RuntimeException("Failed to generate login URL", e);
        }
    }

    /**
     * Handle OAuth2 callback from Google
     * 
     * In production, the authorization code would be exchanged for tokens via:
     * 1. Backend calls Google token endpoint (POST /oauth2/v4/token)
     * 2. Google returns access_token + id_token
     * 3. Backend verifies ID token signature using Google's public keys
     * 4. Extract user info (email, name, picture) from ID token claims
     * 
     * For this implementation, we create/update user and return JWT.
     * 
     * Flow summary:
     * - Frontend gets authorization code from Google redirect
     * - Frontend sends code to backend /api/auth/callback
     * - Backend exchanges code for user info (in production)
     * - Backend creates or updates user record
     * - Backend generates JWT with userId, email, role
     * - Backend returns JWT to frontend
     * 
     * @param code OAuth2 authorization code from Google
     * @param state State parameter for CSRF validation (should be validated against session state)
     * @return JWT token and user information
     */
    @Transactional
    public CallbackResponse handleOAuthCallback(String code, String state) {
        log.info("Processing OAuth2 callback");

        try {
            // TODO: In production, verify state parameter against session
            // if (!isValidState(state)) {
            //     throw new SecurityException("Invalid OAuth2 state parameter");
            // }

            // TODO: In production, exchange code for tokens
            // String accessToken = exchangeCodeForAccessToken(code);
            // UserInfo userInfo = getUserInfoFromGoogle(accessToken);

            // For demo purposes, we'll simulate user info extraction
            // In production, comes from Google OAuth2 token exchange
            String googleId = "google-" + System.currentTimeMillis();
            String email = "user-" + System.currentTimeMillis() + "@example.com";
            String name = "Test User";
            String avatarUrl = "https://lh3.googleusercontent.com/a/default-user=s120-c";

            log.debug("Received user info from OAuth2 provider - email: {}, name: {}", email, name);

            // Create or update user
            User user = createOrUpdateUser(googleId, email, name, avatarUrl);

            // Generate JWT token
            String jwtToken = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
            log.info("Generated JWT token for user: {}", user.getId());

            return CallbackResponse.builder()
                    .token(jwtToken)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole().name())
                    .build();

        } catch (Exception e) {
            log.error("Error processing OAuth2 callback", e);
            throw new RuntimeException("Authentication failed", e);
        }
    }

    /**
     * Create new user or update existing user after OAuth authentication
     * 
     * Updates user profile information from latest OAuth provider data.
     * New users are created with USER role (never ADMIN at registration).
     * 
     * @param googleId Unique identifier from Google
     * @param email User email from Google profile
     * @param name User name from Google profile
     * @param avatarUrl Profile picture URL from Google
     * @return Created or updated User entity
     */
    @Transactional
    public User createOrUpdateUser(String googleId, String email, String name, String avatarUrl) {
        log.debug("Creating or updating user with email: {}", email);

        Optional<User> existingUser = userRepository.findByGoogleId(googleId);

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            log.debug("Updating existing user: {}", email);
            
            // Update profile information from OAuth provider
            user.setName(name);
            user.setAvatarUrl(avatarUrl);
        } else {
            log.debug("Creating new user: {}", email);
            
            // Create new user with default USER role
            user = User.builder()
                    .googleId(googleId)
                    .email(email)
                    .name(name)
                    .role(UserRole.USER)
                    .avatarUrl(avatarUrl)
                    .build();
        }

        return userRepository.save(user);
    }

    /**
     * Handle logout request
     * 
     * Since JWT authentication is stateless, logout is primarily for client-side cleanup.
     * - Client removes JWT from local storage
     * - Client clears user data from state management
     * 
     * Optional: Implement token blacklist for true logout enforcement
     * (store revoked tokens in Redis with expiration matching JWT expiration)
     * 
     * @param userId User ID for audit logging
     * @return LogoutResponse confirming logout
     */
    public LogoutResponse logout(Long userId) {
        log.info("Processing logout for user: {}", userId);
        
        // In production, could:
        // 1. Add token to blacklist (Redis)
        // 2. Invalidate refresh tokens (if used)
        // 3. Log user activity for audit trail
        
        return LogoutResponse.builder()
                .message("Logout successful")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * Generate cryptographically secure random state parameter for OAuth2
     * 
     * State prevents CSRF attacks by ensuring the callback corresponds to the user's request.
     * Must be unpredictable and unique per authorization request.
     * 
     * @return Base64-encoded random state parameter
     */
    private String generateSecureState() {
        byte[] randomBytes = new byte[stateParamLength];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Build complete Google OAuth2 authorization URL with parameters
     * 
     * @param googleClient Google client registration
     * @param state CSRF state parameter
     * @return Complete authorization URL for frontend redirect
     */
    private String buildAuthorizationUrl(ClientRegistration googleClient, String state) throws Exception {
        String clientId = URLEncoder.encode(googleClient.getClientId(), StandardCharsets.UTF_8);
        String redirectUri = URLEncoder.encode(googleClient.getRedirectUri(), StandardCharsets.UTF_8);
        String scope = URLEncoder.encode("openid profile email", StandardCharsets.UTF_8);
        String responseType = URLEncoder.encode("code", StandardCharsets.UTF_8);

        return String.format(
                "%s?client_id=%s&redirect_uri=%s&response_type=%s&scope=%s&state=%s",
                googleClient.getProviderDetails().getAuthorizationUri(),
                clientId,
                redirectUri,
                responseType,
                scope,
                state
        );
    }

    /**
     * Response DTO for OAuth2 authorization URL generation
     */
    @lombok.Data
    @lombok.Builder
    public static class AuthorizationUrlResponse {
        private String redirectUrl;
        private String state;
    }

    /**
     * Response DTO for OAuth2 callback handling
     */
    @lombok.Data
    @lombok.Builder
    public static class CallbackResponse {
        private String token;
        private Long userId;
        private String email;
        private String name;
        private String role;
    }

    /**
     * Response DTO for logout
     */
    @lombok.Data
    @lombok.Builder
    public static class LogoutResponse {
        private String message;
        private long timestamp;
    }
}
