package com.karthik.task_management_backend.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * JWT Authentication Provider
 * 
 * Spring Security provider that validates JwtAuthenticationToken instances.
 * 
 * When a JwtAuthenticationToken is presented for authentication:
 * - Verifies it's the correct token type this provider handles
 * - Ensures token is already authenticated (claims validated by filter)
 * - Converts role to Spring Security authority (ROLE_ prefix)
 * - Returns fully authenticated token for request processing
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            // Type check (should always be JwtAuthenticationToken at this point)
            if (!(authentication instanceof JwtAuthenticationToken)) {
                log.warn("Invalid authentication type: {}", authentication.getClass().getName());
                return null;
            }

            JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) authentication;

            // Extract claims that were validated by the filter
            String role = jwtToken.getRole();
            Long userId = jwtToken.getUserId();
            String email = jwtToken.getEmail();
            String name = jwtToken.getName();

            log.debug("Authenticating JWT token for userId: {}, email: {}, name: {}, role: {}", 
                    userId, email, name, role);

            // Convert role string to Spring Security authority format
            // Example: "ADMIN" → "ROLE_ADMIN"
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + role)
            );

            // Create authenticated token with authorities
            JwtAuthenticationToken authenticatedToken = 
                    new JwtAuthenticationToken(jwtToken.getToken(), userId, email, name, role, authorities);
            
            authenticatedToken.setAuthenticated(true);

            log.debug("JWT authentication successful for userId: {}", userId);
            return authenticatedToken;

        } catch (Exception e) {
            log.error("Error during JWT authentication", e);
            throw new AuthenticationException("Authentication failed", e) {};
        }
    }

    /**
     * Indicates this provider supports JwtAuthenticationToken
     */
    @Override
    public boolean supports(Class<?> authentication) {
        boolean isSupported = JwtAuthenticationToken.class.isAssignableFrom(authentication);
        if (isSupported) {
            log.debug("JwtAuthenticationProvider supports {}", authentication.getName());
        }
        return isSupported;
    }
}
