package com.karthik.task_management_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Data Transfer Object
 * 
 * Used for API responses when returning user information.
 * Does NOT include sensitive fields like googleId or authentication data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    
    @JsonProperty("id")
    private Long userId;
    
    private String email;
    private String name;
    private String role;
    private String avatarUrl;
    private String bio;
    
    @JsonProperty("createdAt")
    private long createdTimestamp;
    
    @JsonProperty("updatedAt")
    private long updatedTimestamp;
}
