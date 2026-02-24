package com.karthik.task_management_backend.config;

import com.nimbusds.jose.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;

/**
 * OAuth2 Client Configuration
 * 
 * Configures Google OAuth2 client registration for the application.
 * Loads client credentials from environment variables to prevent exposing secrets in code.
 * 
 * This configuration is loaded by Spring Security's DefaultClientRegistrationRepository
 * which enables auto-configuration of OAuth2 login and client credentials flow.
 * 
 * Environment variables required:
 * - GOOGLE_CLIENT_ID: OAuth2 application client ID from Google Cloud Console
 * - GOOGLE_CLIENT_SECRET: OAuth2 application client secret from Google Cloud Console
 */
@Configuration
@Slf4j
public class OAuth2Config {

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret:}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri:http://localhost:8080/login/oauth2/code/google}")
    private String googleRedirectUri;

    /**
     * In-memory client registration repository
     * 
     * Provides programmatic Google OAuth2 client configuration.
     * This allows validated configuration before bean creation.
     * 
     * The repository is loaded by Spring Security's default OAuth2ClientContext
     * which manages OAuth2 requests and token exchanges.
     */
    @Bean
    public InMemoryClientRegistrationRepository clientRegistrationRepository() {
        log.debug("Configuring OAuth2 client registration repository");

        // Validate required credentials are provided
        if (!isValidCredentials()) {
            log.warn("OAuth2 client credentials not fully configured. Set GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET environment variables.");
        }

        ClientRegistration googleRegistration = ClientRegistration
                .withRegistrationId("google")
                .clientId(googleClientId)
                .clientSecret(googleClientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(googleRedirectUri)
                .scope("openid", "profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v1/userinfo")
                .userNameAttributeName(IdTokenClaimNames.SUB)  // Use 'sub' claim as principal name
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .clientName("google")
                .build();

        log.info("Google OAuth2 client registration configured with redirect URI: {}", googleRedirectUri);
        return new InMemoryClientRegistrationRepository(googleRegistration);
    }

    /**
     * Validates that required OAuth2 credentials are configured
     */
    private boolean isValidCredentials() {
        boolean clientIdValid = googleClientId != null && !googleClientId.isBlank();
        boolean clientSecretValid = googleClientSecret != null && !googleClientSecret.isBlank();

        if (!clientIdValid) {
            log.error("GOOGLE_CLIENT_ID environment variable not set or empty");
        }
        if (!clientSecretValid) {
            log.error("GOOGLE_CLIENT_SECRET environment variable not set or empty");
        }

        return clientIdValid && clientSecretValid;
    }
}
