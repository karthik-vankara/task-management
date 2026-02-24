package com.karthik.task_management_backend.repository;

import com.karthik.task_management_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity.
 * 
 * Provides database operations for User CRUD and OAuth-specific queries.
 * Used extensively during OAuth callback and profile retrieval.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by Google OAuth ID.
     * Used during OAuth callback to check if user already exists.
     * 
     * @param googleId Google user ID from OAuth provider
     * @return Optional containing User if found, empty otherwise
     */
    Optional<User> findByGoogleId(String googleId);
    
    /**
     * Find user by email address.
     * Used for profile lookup and duplicate prevention.
     * 
     * @param email User's email address
     * @return Optional containing User if found, empty otherwise
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if user exists by Google ID.
     * Lightweight existence check without loading full entity.
     * 
     * @param googleId Google user ID
     * @return true if user exists, false otherwise
     */
    boolean existsByGoogleId(String googleId);
}
