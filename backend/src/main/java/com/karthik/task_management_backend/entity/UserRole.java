package com.karthik.task_management_backend.entity;

/**
 * User role enumeration for role-based access control.
 * 
 * - ADMIN: Full system access, can manage all resources and users
 * - USER: Regular user, can manage own resources only (default)
 */
public enum UserRole {
    ADMIN("Admin user can manage all resources and other users"),
    USER("Regular user can manage own resources only");
    
    private final String description;
    
    UserRole(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
