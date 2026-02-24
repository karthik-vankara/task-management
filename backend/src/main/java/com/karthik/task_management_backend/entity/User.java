package com.karthik.task_management_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * User entity for OAuth2 Google authentication.
 * 
 * Stores authenticated users with Google OAuth identity and role-based access control.
 * Fields like googleId and email are immutable (from OAuth provider).
 * 
 * Indexes:
 * - idx_users_google_id: Fast lookup during OAuth callback
 * - idx_users_email: Fast lookup during profile retrieval
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_google_id", columnList = "google_id", unique = true),
    @Index(name = "idx_users_email", columnList = "email", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 255)
    private String googleId;
    
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    
    @Column(nullable = true, length = 2048)
    private String avatarUrl;
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String bio;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
