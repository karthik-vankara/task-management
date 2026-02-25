package com.karthik.task_management_backend.security;

import com.karthik.task_management_backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 * 
 * Extracts JWT tokens from HTTP Authorization headers and validates them.
 * If valid, creates an authenticated JwtAuthenticationToken and sets it in SecurityContext.
 * 
 * Executes once per request to:
 * - Extract Bearer token from "Authorization: Bearer <token>" header
 * - Validate token signature and expiration using JwtService
 * - Extract claims (userId, email, role) from token
 * - Create authenticated JwtAuthenticationToken
 * - Set in SecurityContext for endpoint processing
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = 7;  // Length of "Bearer "

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String token = extractTokenFromRequest(request);

            if (token != null) {
                log.debug("Extracted JWT token from request, validating...");
                
                // Validate token format and signature
                if (jwtService.validateToken(token)) {
                    log.debug("JWT token validation successful");
                    
                    // Extract claims from token
                    Long userId = jwtService.getUserIdFromToken(token);
                    String email = jwtService.getEmailFromToken(token);
                    String name = jwtService.getNameFromToken(token);
                    String role = jwtService.getRoleFromToken(token);
                    
                    log.debug("Extracted claims from token - userId: {}, email: {}, name: {}, role: {}", 
                            userId, email, name, role);
                    
                    // Create authenticated token with extracted claims
                    JwtAuthenticationToken authenticationToken = 
                            new JwtAuthenticationToken(token, userId, email, name, role, null);
                    
                    // Set in SecurityContext for request processing
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    log.debug("Set JWT authentication in SecurityContext");
                } else {
                    log.warn("JWT token validation failed");
                }
            }
        } catch (Exception exception) {
            log.error("Error processing JWT token", exception);
        }

        // Continue filter chain regardless of token processing result
        // Public endpoints will proceed without authentication
        // Protected endpoints will fail at authorization phase if no valid authentication
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts JWT token from "Authorization: Bearer <token>" header
     * 
     * @param request HTTP request
     * @return JWT token string, or null if not present or malformed
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (!StringUtils.hasText(authHeader)) {
            return null;
        }

        // Check for Bearer prefix
        if (!authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("Authorization header does not contain Bearer prefix");
            return null;
        }

        // Extract token (everything after "Bearer ")
        String token = authHeader.substring(BEARER_PREFIX_LENGTH);
        
        if (StringUtils.hasText(token)) {
            log.debug("Successfully extracted Bearer token from header");
            return token;
        }

        log.debug("Bearer token is empty");
        return null;
    }
}
