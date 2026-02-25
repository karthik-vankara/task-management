package com.karthik.task_management_backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Service for token generation and validation.
 * 
 * Handles OAuth2 JWT tokens with HS256 algorithm.
 * Token expiration: 24 hours
 * Payload: userId, email, role, iat, exp
 */
@Service
@Slf4j
public class JwtService {
    
    @Value("${app.jwt.secret:}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration:86400000}")  // 24 hours in milliseconds
    private long jwtExpiration;
    
    /**
     * Generate JWT token for authenticated user.
     * 
     * @param userId User ID from database
     * @param email User email address
     * @param name User name
     * @param role User role (ADMIN or USER)
     * @return JWT token string (HS256 signed)
     */
    public String generateToken(Long userId, String email, String name, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("name", name);
        claims.put("role", role);
        
        String token = createToken(claims, userId.toString());
        log.info("JWT token generated for user: {} ({})", email, name);
        return token;
    }
    
    /**
     * Create JWT token with given claims and subject.
     * 
     * @param claims Custom claims to include in token
     * @param subject User ID as subject
     * @return Signed JWT token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtExpiration);
        
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Validate JWT token signature and expiration.
     * 
     * @param token JWT token string to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            log.warn("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }
    
    /**
     * Extract name from JWT token.
     * 
     * @param token JWT token string
     * @return User name
     */
    public String getNameFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("name", String.class);
    }

    /**
     * Extract user ID from JWT token.
     * 
     * @param token JWT token string
     * @return User ID as Long
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }
    
    /**
     * Extract email from JWT token.
     * 
     * @param token JWT token string
     * @return User email address
     */
    public String getEmailFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("email", String.class);
    }
    
    /**
     * Extract role from JWT token.
     * 
     * @param token JWT token string
     * @return User role (ADMIN or USER)
     */
    public String getRoleFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("role", String.class);
    }
    
    /**
     * Extract all claims from JWT token.
     * 
     * @param token JWT token string
     * @return Claims object containing all token data
     */
    private Claims getAllClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Check if token is expired.
     * 
     * @param token JWT token string
     * @return true if token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception ex) {
            log.warn("Error checking token expiration: {}", ex.getMessage());
            return true;
        }
    }
}
