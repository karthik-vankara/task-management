# Phase 1: Data Model & Schema

**Status**: ✅ COMPLETE  
**Date**: 2026-02-24  
**Scope**: User entity, database schema, and supporting enums for OAuth2 authentication

---

## Entities

### 1. User Entity

**Purpose**: Store authenticated users from Google OAuth with role-based access control.

**Java Implementation**:
```java
package com.karthik.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

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
```

**Fields**:
- `id` (Long): Primary key, auto-increment
- `googleId` (String, UNIQUE, NOT NULL): Google user ID from OAuth provider
- `email` (String, UNIQUE, NOT NULL): User's email address from Google
- `name` (String, NOT NULL): User's display name from Google profile
- `role` (UserRole enum, NOT NULL): ADMIN or USER (default USER)
- `avatarUrl` (String, nullable): URL to Google profile picture
- `bio` (String, nullable): User bio field (future use)
- `createdAt` (LocalDateTime, NOT NULL): Audit timestamp
- `updatedAt` (LocalDateTime, NOT NULL): Audit timestamp

**Validation Rules**:
- `googleId`: Must be unique, non-empty, 1-255 chars
- `email`: Must be valid email format, unique, 1-255 chars
- `name`: Must be non-empty, 1-255 chars
- `role`: Must be ADMIN or USER (enum constraint)
- `avatarUrl`: If provided, must be valid URL
- `createdAt`/`updatedAt`: Auto-managed by Hibernate timestamps

**Indexes**:
```sql
CREATE UNIQUE INDEX idx_users_google_id ON users(google_id);
CREATE UNIQUE INDEX idx_users_email ON users(email);
```

Rationale: Enable fast user lookups during OAuth callback and profile retrieval.

### 2. UserRole Enum

**Purpose**: Enforce role-based access control with two tiers.

**Java Implementation**:
```java
package com.karthik.entity;

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
```

**Values**:
- `ADMIN`: Full system access, manage tasks/users, configure settings
- `USER`: Restricted access, manage own tasks only (default)

**Database Representation**: Stored as VARCHAR(50) enum string in PostgreSQL.

---

## Database Schema

### users Table

```sql
CREATE TABLE public.users (
    id BIGSERIAL PRIMARY KEY,
    google_id character varying(255) NOT NULL UNIQUE,
    email character varying(255) NOT NULL UNIQUE,
    name character varying(255) NOT NULL,
    role character varying(50) NOT NULL DEFAULT 'USER',
    avatar_url character varying(2048),
    bio TEXT,
    created_at timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for fast lookups during OAuth callback
CREATE UNIQUE INDEX idx_users_google_id ON public.users(google_id);
CREATE UNIQUE INDEX idx_users_email ON public.users(email);

-- Constraint to enforce valid roles (optional, if NOT using enum type)
ALTER TABLE public.users
    ADD CONSTRAINT chk_user_role CHECK (role IN ('ADMIN', 'USER'));
```

### Column Specifications

| Column | Type | Nullable | Unique | Default | Index | Notes |
|--------|------|----------|--------|---------|-------|-------|
| id | BIGSERIAL | No | Yes (PK) | auto | Yes | Auto-increment primary key |
| google_id | VARCHAR(255) | No | Yes | | Yes | Google OAuth ID, used for lookup |
| email | VARCHAR(255) | No | Yes | | Yes | User email, used for lookup |
| name | VARCHAR(255) | No | No | | No | Display name from Google |
| role | VARCHAR(50) | No | No | 'USER' | No | ADMIN or USER enum |
| avatar_url | VARCHAR(2048) | Yes | No | NULL | No | Google profile picture URL |
| bio | TEXT | Yes | No | NULL | No | User bio (future Phase 2) |
| created_at | TIMESTAMP | No | No | NOW() | No | Audit timestamp |
| updated_at | TIMESTAMP | No | No | NOW() | No | Audit timestamp |

---

## Relationships

**Phase 1 Scope**: User entity stands alone. No foreign keys.

**Future Relationships** (Phase 2+):
- User → Task (one-to-many): User creates many tasks
- User → Task (assignee, optional): User assigned to tasks
- User → UserSession (one-to-many): User may have multiple active sessions

---

## State Management

### User Lifecycle States

```
[CREATED] → [ACTIVE] → [INACTIVE]
```

1. **CREATED**: User first authenticated via Google OAuth
   - State: Record inserted in users table
   - Trigger: `POST /api/auth/callback` with valid OAuth code
   - Data: googleId, email, name, avatarUrl populated from Google

2. **ACTIVE**: User can authenticate and access protected resources
   - State: User record exists, JWT issued on login
   - Trigger: `POST /api/auth/login` or `GET /api/auth/profile` with valid JWT
   - Data: All fields populated, updatedAt refreshed

3. **INACTIVE**: User deactivated (future phase)
   - State: isActive flag set to false
   - Trigger: Admin request or user self-deletion
   - Data: Record retained for audit, login rejected

**Phase 1 Scope**: Only CREATED and ACTIVE states implemented. Soft-delete in future phases.

---

## Validation Rules

### At API Layer (Spring Validation)

```java
// UserDTO for API responses
@Data
public class UserDTO {
    private Long id;
    
    @NotBlank
    @Length(min = 1, max = 255)
    private String email;
    
    @NotBlank
    @Length(min = 1, max = 255)
    private String name;
    
    @NotNull
    private UserRole role;
    
    private String avatarUrl;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### At Database Layer (Constraints)

```sql
-- NOT NULL constraints already specified in column definition
-- UNIQUE constraints on google_id and email
-- CHECK constraint for role enum values
-- Indexes ensure fast lookups
```

### Business Logic Validation

| Rule | Trigger | Error Code | Action |
|------|---------|-----------|--------|
| Gmail verified | OAuth callback | OAUTH_ERROR | Reject if email not verified by Google |
| Email unique | User creation | USER_CREATION_FAILED | Prevent duplicate email (FK violation) |
| Google ID unique | User creation | USER_CREATION_FAILED | Prevent duplicate googleId |
| Role valid | User creation/update | INVALID_ROLE | Only ADMIN or USER allowed |
| Name non-empty | OAuth mapping | USER_CREATION_FAILED | Reject if Google profile has no name |

---

## Data Generation & Seeding (Phase 1)

**No seed data required for Phase 1.**

Users are created on-demand during OAuth flow. For testing:
- Use real Google credentials in `.env.local`
- Manually authenticate via `/api/auth/login` endpoint
- Verify user created in PostgreSQL via `SELECT * FROM users;`

---

## Performance Considerations

### Query Patterns

**Primary Lookups**:
```java
// Find user by Google ID (during OAuth callback)
User findByGoogleId(String googleId);

// Find user by email (during profile update)
User findByEmail(String email);

// Find user by ID (during profile retrieval)
User findById(Long id);
```

**Indexes Ensure**:
- `findByGoogleId()`: <50ms lookup on 10k user table
- `findByEmail()`: <50ms lookup on 10k user table
- `findById()`: <1ms lookup (primary key)

### Connection Pooling

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10          # Sufficient for Phase 1 (1000 concurrent)
      minimum-idle: 2                 # Keep 2 idle connections
      connection-timeout: 20000       # 20 seconds
      idle-timeout: 600000            # 10 minutes
```

### Caching (Future)

No caching implemented in Phase 1. JWT validation is < 100ms without caching.

---

## Migration Strategy

### Local Development

Use Spring Boot auto-DDL (Hibernate `spring.jpa.hibernate.ddl-auto=create-drop`):
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop  # Drop and recreate on startup
    database-platform: org.hibernate.dialect.PostgreSQL15Dialect
```

### Production

Manually manage schema via SQL scripts or Flyway (future enhancement):
```sql
-- File: V1_0__Create_users_table.sql
CREATE TABLE users ( ... );
CREATE UNIQUE INDEX idx_users_google_id ON users(google_id);
CREATE UNIQUE INDEX idx_users_email ON users(email);
```

---

## Audit Trail

Timestamps on User entity enable basic audit:
- `createdAt`: When user first authenticated
- `updatedAt`: When user last logged in or profile updated

For detailed audit (who changed what), see Phase 7 (Security & Audit).

---

## Summary

**Phase 1 Data Model**:
- ✅ Single User entity with role-based access control
- ✅ Indexes on oauth_id and email for <50ms lookups
- ✅ Validation at API and database layers
- ✅ Schema compatible with PostgreSQL free tier
- ✅ Prepared for future relationships (Tasks, Sessions)

**Ready for implementation**: Database schema DDL, JPA entity class, and validation ready for Spring Boot application.
