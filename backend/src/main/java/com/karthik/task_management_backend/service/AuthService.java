package com.karthik.task_management_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karthik.task_management_backend.entity.User;
import com.karthik.task_management_backend.entity.UserRole;
import com.karthik.task_management_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
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
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.oauth2.state-param-length:32}")
    private int stateParamLength;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret:}")
    private String googleClientSecret;

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
                throw new RuntimeException("Google OAuth2 client not configured");
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
     * Exchanges authorization code for tokens and extracts user information from Google.
     * 
     * Flow:
     * 1. Receive authorization code from Google redirect
     * 2. Exchange code for access token and ID token
     * 3. Decode ID token to get user claims (email, name, picture)
     * 4. Create or update user in database
     * 5. Generate JWT token for frontend
     * 
     * @param code OAuth2 authorization code from Google
     * @param state State parameter for CSRF validation
     * @return JWT token and user information
     */
    @Transactional
    public CallbackResponse handleOAuthCallback(String code, String state) {
        log.info("Processing OAuth2 callback with authorization code");
        
        try {
            if (code == null || code.isEmpty()) {
                log.error("Authorization code is null or empty");
                throw new RuntimeException("Authorization code is required");
            }

            if (googleClientId == null || googleClientId.isEmpty()) {
                log.error("GOOGLE_CLIENT_ID is not configured");
                throw new RuntimeException("Google client ID not configured");
            }

            if (googleClientSecret == null || googleClientSecret.isEmpty()) {
                log.error("GOOGLE_CLIENT_SECRET is not configured");
                throw new RuntimeException("Google client secret not configured");
            }

            log.debug("Step 1: Exchanging authorization code for tokens");
            // Exchange authorization code for tokens
            TokenResponse tokenResponse = exchangeCodeForTokens(code);
            
            if (tokenResponse == null) {
                log.error("Token response is null");
                throw new RuntimeException("Failed to obtain tokens from Google");
            }

            if (tokenResponse.id_token == null) {
                log.error("ID token is null in response");
                throw new RuntimeException("Failed to obtain ID token from Google");
            }

            log.debug("Step 2: Successfully obtained ID token from Google");

            // Extract user claims from ID token
            Map<String, Object> claims = parseIdToken(tokenResponse.id_token);
            
            String googleId = (String) claims.get("sub");
            String email = (String) claims.get("email");
            String name = (String) claims.get("name");
            String avatarUrl = (String) claims.get("picture");

            if (googleId == null || googleId.isEmpty()) {
                log.error("Missing sub (googleId) in Google ID token claims: {}", claims.keySet());
                throw new RuntimeException("Invalid user data from Google: missing sub claim");
            }

            if (email == null || email.isEmpty()) {
                log.error("Missing email in Google ID token claims");
                throw new RuntimeException("Invalid user data from Google: missing email claim");
            }

            log.info("Step 3: Extracted user info from ID token - email: {}, name: {}, googleId: {}", 
                    email, name, googleId);

            // Create or update user with real data
            log.debug("Step 4: Creating or updating user in database");
            User user = createOrUpdateUser(googleId, email, name, avatarUrl);

            // Generate JWT token with name included
            log.debug("Step 5: Generating JWT token");
            String jwtToken = jwtService.generateToken(user.getId(), user.getEmail(), user.getName(), user.getRole().name());
            
            log.info("OAuth2 callback completed successfully - JWT generated for user: {} ({})", 
                    user.getId(), user.getEmail());

            return CallbackResponse.builder()
                    .token(jwtToken)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole().name())
                    .build();

        } catch (Exception e) {
            log.error("Error processing OAuth2 callback: {}", e.getMessage());
            log.error("Exception class: {}", e.getClass().getName());
            if (e.getCause() != null) {
                log.error("Root cause: {}", e.getCause().getMessage());
                e.getCause().printStackTrace();
            }
            e.printStackTrace();
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }

    /**
     * Exchange authorization code for Google OAuth tokens
     * 
     * Makes POST request to Google token endpoint with form-encoded body.
     * Google returns access_token and id_token.
     * 
     * @param code Authorization code from Google
     * @return TokenResponse with access_token and id_token
     */
    private TokenResponse exchangeCodeForTokens(String code) {
        try {
            ClientRegistration googleClient = clientRegistrationRepository.findByRegistrationId("google");
            if (googleClient == null) {
                log.error("Google OAuth2 client not configured in application.yaml");
                throw new RuntimeException("Google OAuth2 client not configured");
            }

            String tokenUrl = googleClient.getProviderDetails().getTokenUri();
            String redirectUri = googleClient.getRedirectUri();

            log.debug("Token URL: {}", tokenUrl);
            log.debug("Redirect URI: {}", redirectUri);
            log.debug("Authorization code: {}", code.substring(0, Math.min(10, code.length())) + "...");

            // Build request body as form-encoded data (not URL parameters)
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("client_id", googleClientId);
            requestBody.add("client_secret", googleClientSecret);
            requestBody.add("code", code);
            requestBody.add("redirect_uri", redirectUri);
            requestBody.add("grant_type", "authorization_code");

            // Set Content-Type header
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestBody, headers);

            log.debug("Sending token exchange request to Google");

            // Exchange code for tokens
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(tokenUrl, entity, String.class);

            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                log.error("Google token endpoint returned status: {}", responseEntity.getStatusCode());
                log.error("Response body: {}", responseEntity.getBody());
                throw new RuntimeException("Google token endpoint error: " + responseEntity.getStatusCode());
            }

            String response = responseEntity.getBody();

            if (response == null || response.isEmpty()) {
                log.error("Empty response from Google token endpoint");
                throw new RuntimeException("Empty response from Google token endpoint");
            }

            log.debug("Successfully received response from Google token endpoint");
            log.debug("Parsing token response");

            TokenResponse tokenResponse = objectMapper.readValue(response, TokenResponse.class);
            
            if (tokenResponse.id_token == null) {
                log.error("No id_token in Google response. Response: {}", response);
                throw new RuntimeException("No id_token in Google response");
            }

            log.info("Successfully exchanged authorization code for tokens");
            return tokenResponse;

        } catch (Exception e) {
            log.error("Error exchanging authorization code for tokens: {}", e.getMessage());
            log.error("Exception type: {}", e.getClass().getName());
            if (e.getCause() != null) {
                log.error("Cause: {}", e.getCause().getMessage());
            }
            throw new RuntimeException("Failed to exchange authorization code: " + e.getMessage(), e);
        }
    }

    /**
     * Parse Google ID token to extract user claims
     * 
     * Decodes JWT (no signature verification - ID token signature verified by Google)
     * Extracts claims: sub (google ID), email, name, picture
     * 
     * @param idToken JWT ID token from Google
     * @return Map of claims from token payload
     */
    private Map<String, Object> parseIdToken(String idToken) {
        try {
            // ID token format: header.payload.signature
            // We decode the payload (claims)
            String[] parts = idToken.split("\\.");
            if (parts.length != 3) {
                throw new RuntimeException("Invalid ID token format");
            }

            // Decode payload (add padding if necessary)
            String payload = parts[1];
            payload = payload.replace("-", "+").replace("_", "/");
            int padding = 4 - (payload.length() % 4);
            if (padding != 4) {
                payload += "=".repeat(padding);
            }

            byte[] decodedBytes = Base64.getDecoder().decode(payload);
            String decodedPayload = new String(decodedBytes, StandardCharsets.UTF_8);
            
            log.debug("Decoded ID token payload: {}", decodedPayload);

            @SuppressWarnings("unchecked")
            Map<String, Object> claims = objectMapper.readValue(decodedPayload, Map.class);
            
            return claims;

        } catch (Exception e) {
            log.error("Error parsing ID token", e);
            throw new RuntimeException("Failed to parse ID token", e);
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

    /**
     * Response DTO for Google token endpoint
     * 
     * Maps the JSON response from Google's token exchange endpoint.
     * Includes all fields that Google returns, ignores unknown properties
     * to handle future API changes gracefully.
     */
    @lombok.Data
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class TokenResponse {
        public String access_token;
        public String id_token;
        public int expires_in;
        public String token_type;
        public String scope;  // Google includes scope in response
        public String refresh_token;  // May include refresh token
    }
}
