# Phase 2: User Profile Management - Data Model

---

## User Entity Extensions

**Phase 1 User Entity** (immutable):
- id (BIGSERIAL PK)
- googleId (VARCHAR unique)
- email (VARCHAR unique)
- role (ENUM: ADMIN, USER)
- createdAt (TIMESTAMP)
- updatedAt (TIMESTAMP)

**Phase 2 User Extensions** (editable):
```java
@Entity
@Table(name = "users")
public class User {
    // Phase 1 fields...
    
    @Column(name = "name", nullable = true, length = 255)
    private String name;
    
    @Column(name = "bio", columnDefinition = "TEXT", nullable = true)
    private String bio;
    
    @Column(name = "avatar_url", nullable = true)
    private String avatarUrl;
    
    // Validation
    // name: 1-255 chars, alphanumeric + spaces + common punctuation
    // bio: 0-5000 chars, no HTML/JS
    // avatarUrl: Valid URL format, HTTPS only
}
```

---

## Database Schema

```sql
-- Extend existing users table (Phase 1)
ALTER TABLE users ADD COLUMN name VARCHAR(255);
ALTER TABLE users ADD COLUMN bio TEXT;
ALTER TABLE users ADD COLUMN avatar_url VARCHAR(500);

-- Indexes for performance
CREATE INDEX idx_users_created_at ON users(created_at DESC);
CREATE INDEX idx_users_name ON users(name);

-- Bio full-text search (future)
CREATE INDEX idx_users_bio_search ON users 
  USING gin(to_tsvector('english', bio));
```

---

## DTOs (Data Transfer Objects)

### UserProfileDTO (Read - public profile)
```json
{
  "id": 1,
  "name": "Alice Johnson",
  "bio": "Product Manager | Coffee enthusiast",
  "avatarUrl": "https://example.com/avatars/uuid-abc123.jpg",
  "createdAt": "2025-01-15T10:30:00Z"
}
```

### UserProfileUpdateDTO (Write - profile edit)
```json
{
  "name": "Alice Johnson",
  "bio": "PM at StartupXYZ | Making task management delightful"
}
```

### UserDirectoryDTO (List)
```json
{
  "id": 1,
  "name": "Alice Johnson",
  "avatarUrl": "https://example.com/avatars/uuid-abc123.jpg"
}
```

---

## Validation Rules

### API Layer (JSR-303)
```java
@NotBlank(message = "Name cannot be blank")
@Length(min = 1, max = 255, message = "Name must be 1-255 characters")
private String name;

@Length(max = 5000, message = "Bio must be max 5000 characters")
private String bio;

@URL(message = "Avatar URL must be valid")
private String avatarUrl;
```

### Business Logic Layer
- Sanitize bio: Remove HTML/JS tags using HtmlUtils.htmlEscape()
- Validate avatarUrl: Must be HTTPS and accessible
- Prevent duplicate names: Check for uniqueness (optional, allow duplicates for now)

### Database Layer
```sql
ALTER TABLE users 
ADD CONSTRAINT chk_name_length CHECK (LENGTH(name) <= 255),
ADD CONSTRAINT chk_bio_length CHECK (LENGTH(bio) <= 5000),
ADD CONSTRAINT chk_avatar_url_https CHECK (avatar_url LIKE 'https://%');
```

---

## Avatar Storage

### File System Structure
```
backend/uploads/
  avatars/
    uuid-abc123.jpg    -- 200x200px, optimized
    uuid-def456.png
```

### Avatar Processing
1. **Upload validation**:
   - Max size: 5MB
   - Accepted types: image/jpeg, image/png
   - MIME validation: Use Apache Tika

2. **Processing pipeline**:
   - Generate random UUID filename
   - Resize to 200x200px using Thumbnails
   - Convert to single format (JPEG)
   - Save to uploads/avatars/{uuid}.jpg
   - Store URL in database

3. **Serving**:
   - Spring static resource handler: `spring.web.resources.static-locations=file:./uploads/`
   - Cache-Control: public, max-age=31536000 (1 year)
   - Return avatarUrl as: `/uploads/avatars/{uuid}.jpg`

---

## Relationships

```
User (1) ---------- (Many) UserProfile
  id               user_id
  email            name
  role             bio
  googleId         avatarUrl
```

No separate UserProfile table; fields merged into User entity (Phase 2 extends Phase 1).

---

## Lifecycle States

**User States** (no change from Phase 1):
- CREATED: New OAuth user registered
- ACTIVE: Profile completed (Phase 2)
- Inactive: Soft delete (Future implementation)

**Profile Fields**:
- name: NULL initially, user sets during onboarding or edit
- bio: NULL, optional, editable anytime
- avatarUrl: NULL, user uploads during onboarding or edit

---

## Indexes for Performance

| Column(s) | Type | Purpose | Query Time |
|-----------|------|---------|------------|
| id | PK | User lookup | < 1ms |
| googleId | Unique | OAuth lookup | < 1ms |
| email | Unique | Email lookup | < 1ms |
| created_at | Regular | Pagination, feed | < 50ms on 100k users |
| name | Regular | Directory search | < 100ms |
| bio (gin) | GiN | Full-text search | < 200ms |

---

## Performance Considerations

- **Profile fetch**: < 100ms (Primary key lookup)
- **Directory list**: < 300ms (50 users with pagination)
- **Avatar upload**: < 2s (5MB upload + resize + store)
- **Search by name**: < 500ms (Index on name)
- **Caching**: Redis cache profile for 1 hour (TTL)

---

## No Database Migration Needed Yet

Phase 2 extends Phase 1 User table with nullable columns:
```sql
ALTER TABLE users ADD COLUMN name VARCHAR(255);
ALTER TABLE users ADD COLUMN bio TEXT;
ALTER TABLE users ADD COLUMN avatar_url VARCHAR(500);
```

Flyway migration: V2__add_user_profile_fields.sql (Phase 8 - DevOps)
