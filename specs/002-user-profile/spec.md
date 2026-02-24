# User Profile Management API Specification

**Version**: 1.0.0  
**Phase**: 2  
**Date**: 2026-02-24  
**Depends On**: Phase 1 (OAuth Authentication)

---

## Overview

Implement complete user profile management enabling authenticated users to view and edit their profile information, avatar, and preferences. Extends the Phase 1 User entity with editable fields while maintaining immutable OAuth2 identity fields.

## User Stories

### US-1: View My Profile
**As a** logged-in user  
**I want to** view my complete profile information with all fields  
**So that** I can see and manage my personal data

**Acceptance Criteria**:
- [ ] GET /api/users/me returns complete user profile
- [ ] Profile includes: id, email, name, bio, avatarUrl, role, createdAt, updatedAt
- [ ] Email and googleId are displayed but not editable
- [ ] Avatar displays correctly for all image formats
- [ ] Response time < 100ms

### US-2: Edit Profile Information
**As a** logged-in user  
**I want to** update my name and bio  
**So that** I can maintain current profile information

**Acceptance Criteria**:
- [ ] PUT /api/users/me accepts name and bio fields
- [ ] Name field accepts 1-255 characters
- [ ] Bio field accepts 0-5000 characters
- [ ] Updates reflected immediately on subsequent GET
- [ ] Email and googleId cannot be modified via API
- [ ] Returns 400 for validation errors

### US-3: Upload Profile Avatar
**As a** logged-in user  
**I want to** upload a profile picture or use Google's avatar  
**So that** other users see my profile image

**Acceptance Criteria**:
- [ ] PUT /api/users/me/avatar accepts multipart/form-data image
- [ ] Supported formats: JPEG, PNG, WebP (max 5MB)
- [ ] Image resized to 200x200px
- [ ] Stored in cloud storage or as base64
- [ ] URL returned in response
- [ ] Returns 413 if file exceeds size limit

### US-4: View User Directory (Public Profiles)
**As a** logged-in user  
**I want to** see other users' public profiles  
**So that** I can identify task assignees and collaborators

**Acceptance Criteria**:
- [ ] GET /api/users returns paginated list of all users
- [ ] Each user shows: id, name, avatarUrl, email (for same workspace)
- [ ] Pagination supports limit=10, offset=0 parameters
- [ ] Respects role-based visibility (users see other users in same workspace)
- [ ] Response time < 200ms for 10k user list

### US-5: Get User By ID
**As a** logged-in user or system  
**I want to** fetch a specific user's public profile  
**So that** I can display task assignee information

**Acceptance Criteria**:
- [ ] GET /api/users/{userId} returns user profile
- [ ] Returns 404 if user not found
- [ ] Public fields only: id, name, email, avatarUrl, role
- [ ] Private fields (bio) hidden for other users' profiles
- [ ] Own profile shows all fields

## Functional Requirements

- FR1: User can retrieve their profile (GET /api/users/me)
- FR2: User can update their profile name and bio (PUT /api/users/me)
- FR3: User can upload or change profile avatar (PUT /api/users/me/avatar)
- FR4: User can view directory of all users (GET /api/users?limit=10&offset=0)
- FR5: User can view any user's public profile (GET /api/users/{userId})
- FR6: System prevents modification of OAuth2 fields (email, googleId)
- FR7: System enforces field-level validation (name length, bio length, image format)

## Non-Functional Requirements

- NFR1: Performance: Profile retrieval < 100ms, avatar upload < 2s
- NFR2: Scalability: Support 10k concurrent profile views, 10k total users
- NFR3: Security: PII protected, avatar uploads validated, CORS enforced
- NFR4: Usability: Clear error messages for validation failures, supported formats listed

## Data Requirements

### Profile Fields
- `name`: String, 1-255 chars, user-editable
- `bio`: String, 0-5000 chars, user-editable, optional
- `avatarUrl`: String (URL), user-provided or Google's
- `email`: String, immutable (from OAuth)
- `googleId`: String, immutable, unique
- `role`: Enum (ADMIN/USER), immutable, set during OAuth
- `createdAt`: DateTime, immutable, audit
- `updatedAt`: DateTime, auto-updated

### Database Changes (vs Phase 1)
- Migrate `bio` column from nullable text to searchable indexed field
- Add `avatarUrl` index for quick lookups
- Add `updatedAt` index for sorting profiles by recent activity

## Success Metrics

### User Perspective
- User can view and edit profile in < 3 seconds
- Avatar appears correctly in all components
- Clear validation errors guide corrections

### Business Perspective
- 100% of authenticated users have complete profiles
- Directory enables collaboration discovery
- Profile management reduces support tickets

### Technical Perspective
- <100ms profile retrieval without caching
- <2s avatar upload for typical 500KB image
- No N+1 queries in directory listing

## Acceptance Criteria (Complete)

- [ ] GET /api/users/me returns complete user profile
- [ ] PUT /api/users/me updates name/bio with validation
- [ ] PUT /api/users/me/avatar accepts and processes images
- [ ] GET /api/users returns paginated user directory
- [ ] GET /api/users/{userId} returns public profile or self profile
- [ ] Immutable fields (email, googleId, role) cannot be modified
- [ ] All endpoints return standardized API response format
- [ ] Error responses include specific error codes
- [ ] CORS configured for frontend access
- [ ] Security headers present on all responses
- [ ] Avatar files validated (format, size, content-type)
- [ ] Manual test checklist completed (see Test Plan)

## Out of Scope for Phase 2

The following features will be implemented in later phases:
- User preferences (theme, notifications)
- User search functionality (Phase 4)
- User blocking/reporting (Phase 5)
- User audit logs (Phase 7)
- User profile images with CDN/S3 storage (Phase 8)
- User account deletion (Phase 7)

## Dependencies

**Phase 1 Complete**: User OAuth authentication, JWT tokens working  
**Database**: PostgreSQL can store and index profile data  
**Libraries**: Image processing library (optional, can resize server-side)

## Technical Notes

- Use Spring Data JPA for profile CRUD operations
- Implement separate validator for profile fields
- Consider caching directory list (5-minute TTL) for performance
- Avatar upload: validate MIME type + magic bytes for security
- Profile updates should be transactional (prevent partial updates)
