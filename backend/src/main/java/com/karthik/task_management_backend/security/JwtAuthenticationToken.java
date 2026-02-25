package com.karthik.task_management_backend.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * JWT Authentication Token for Spring Security.
 * 
 * Wraps JWT token data and integrates with Spring Security's authentication mechanism.
 * Used by JWT filter to authenticate requests.
 */
@Getter
@Setter
public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    
    private String token;
    private Long userId;
    private String email;
    private String name;
    private String role;
    private Object principal;
    private Object credentials;
    
    /**
     * Constructor for unauthenticated token (before validation).
     * 
     * @param token JWT token string
     */
    public JwtAuthenticationToken(String token) {
        super(null);
        this.token = token;
        this.setAuthenticated(false);
    }
    
    /**
     * Constructor for authenticated token (after validation).
     * 
     * @param token JWT token string
     * @param userId User ID from token
     * @param email Email from token
     * @param name Name from token
     * @param role Role from token
     * @param authorities Spring Security authorities
     */
    public JwtAuthenticationToken(String token, Long userId, String email, String name, String role, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
        this.principal = email;
        this.credentials = token;
        this.setAuthenticated(true);
    }
    
    @Override
    public Object getCredentials() {
        return this.credentials;
    }
    
    @Override
    public Object getPrincipal() {
        return this.principal;
    }
    
    @Override
    public String getName() {
        return this.email;
    }
}
