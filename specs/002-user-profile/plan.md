# Implementation Plan: User Profile Management API

**Branch**: `002-user-profile` | **Date**: 2026-02-24 | **Phase Duration**: 4-5 days  
**Depends On**: Phase 1 OAuth (complete)

---

## Summary

Extend Phase 1 User entity to support editable user profiles (name, bio, avatar) with directory listing and public profile views. Implement 5 endpoints for profile management with role-based authorization and file upload validation.

---

## Technical Context

**Language/Version**: Java 21, Spring Boot 3.3.0  
**Primary Dependencies**: Spring Data JPA, Apache Commons FileUpload, Image processing (optional)  
**Storage**: PostgreSQL 15+ for profile data, filesystem or cloud for avatars  
**Testing**: Manual testing per spec checklist  
**Target Platform**: Backend API service  
**Project Type**: web-service (REST API)  
**Performance Goals**: <100ms profile retrieval, <2s avatar upload  
**Constraints**: Avatar files max 5MB, image resize to 200x200px  
**Scale**: Support 10k users, 1000 concurrent profile operations

---

## Constitution Check

**GATE**: ✅ **PASS**

- ✅ Spec-first: Complete specification before coding
- ✅ Minimal cost: No paid services, all open-source
- ✅ Quality: Follow Google Java Style Guide, security best practices
- ✅ Phase-based: Self-contained, independently deployable
- ✅ Git workflow: Phase branch, semantic commits, rebase merge

---

## Project Structure

### Documentation

```text
specs/002-user-profile/
├── spec.md                    # Feature specification
├── plan.md                    # This file
├── research.md                # Phase 0: Best practices for file uploads
├── data-model.md              # Phase 1: Profile schema, avatar storage
├── quickstart.md              # Implementation guide
├── contracts/
│   ├── profile-api.md         # API endpoint specs
│   └── avatar-upload.md       # File upload contract
└── tasks.md                   # Phase 2: Granular task breakdown
```

### Source Code (Backend)

```text
backend/src/main/java/com/karthik/
├── entity/
│   └── UserProfile.java       # Extends User with profile fields
├── dto/
│   ├── ProfileDTO.java        # Profile response
│   ├── UpdateProfileRequest.java
│   └── UserDirectoryDTO.java  # List item
├── controller/
│   └── ProfileController.java # GET/PUT user endpoints
├── service/
│   ├── ProfileService.java    # Profile CRUD logic
│   └── AvatarService.java     # File upload/storage
├── repository/
│   └── (UserRepository enhanced for profile queries)
├── validator/
│   └── ProfileValidator.java  # Field validation
└── util/
    └── AvatarUtil.java        # Image processing helpers
```

---

## Implementation Roadmap

### Days 1-2: Data Model & Validators (8-10 hours)

**Tasks**:
1. Create ProfileValidator with field length/format rules (2 hrs)
2. Add avatar storage mechanism (filesystem or base64) (2 hrs)
3. Create ProfileDTO and UpdateProfileRequest (1.5 hrs)
4. Create UserDirectoryDTO for list responses (1 hr)
5. Database migrations (if needed) (1 hr)
6. Write avatar validation utilities (1 hr)

**Deliverable**: Profile entities, validators, and DTOs ready for API implementation

**Commits**:
```
[PHASE 2] Add profile data validation and DTOs
[PHASE 2] Implement avatar storage and file utilities
```

### Days 3-4: API Endpoints (10-12 hours)

**Tasks**:
1. Create ProfileController with 5 endpoints (3 hrs)
2. Implement ProfileService.getProfile (1 hr)
3. Implement ProfileService.updateProfile (1.5 hrs)
4. Implement AvatarService.uploadAvatar (2 hrs)
5. Implement ProfileService.getDirectory (1.5 hrs)
6. Implement ProfileService.getPublicProfile (1 hr)
7. Add authorization checks (1.5 hrs)

**Deliverable**: All 5 endpoints fully functional with validation and error handling

**Commits**:
```
[PHASE 2] Create ProfileController with all endpoints
[PHASE 2] Implement profile update with field validation
[PHASE 2] Implement avatar upload with file validation
```

### Days 4-5: Testing & Hardening (8-10 hours)

**Tasks**:
1. Manual test all 5 endpoints (2 hrs)
2. Test avatar upload with various formats (1.5 hrs)
3. Test authorization/authorization checks (1.5 hrs)
4. Test pagination on directory endpoint (1 hr)
5. Load test with 100+ users (1 hr)
6. Security review: CORS, headers, input sanitization (1.5 hr)
7. Performance optimization if needed (1 hr)

**Deliverable**: All manual tests pass, performance targets met

**Commits**:
```
[PHASE 2] Add security headers and CORS validation
[PHASE 2] Complete manual testing checklist
```

---

## Key Implementation Details

### Profile Update Logic

```java
// Only editable fields
- name (1-255 chars)
- bio (0-5000 chars)
- (avatar via separate endpoint)

// Immutable, returned but not editable
- email (from OAuth)
- googleId (from OAuth)
- role (set at creation)
```

### Avatar Upload Flow

1. Validate file (MIME type, magic bytes, size <5MB)
2. Resize to 200x200px
3. Store (filesystem, database, or cloud)
4. Return URL to client
5. Update User.avatarUrl

### Directory Listing Query

```sql
-- Efficient paginated query with sorting
SELECT id, name, email, avatar_url, role, updated_at
FROM users
ORDER BY updated_at DESC
LIMIT 10 OFFSET 0;
```

---

## Error Handling

| Code | HTTP | Scenario |
|------|------|----------|
| INVALID_PROFILE_DATA | 400 | Name too long, bio invalid format |
| FILE_TOO_LARGE | 413 | Avatar exceeds 5MB |
| INVALID_IMAGE_FORMAT | 400 | Unsupported image type |
| UNAUTHORIZED | 401 | JWT invalid or missing |
| FORBIDDEN | 403 | User cannot modify other's profile |
| NOT_FOUND | 404 | User ID doesn't exist |

---

## Dependencies

- ✅ Phase 1 Complete (Auth, JWT tokens)
- ✅ PostgreSQL running (schema migration if needed)
- ✅ Image processing library (Apache Commons Imaging or similar)
- ✅ File upload library (if not using Spring's MultipartFile)

---

## Testing Checklist

**Before Merge**:
- [ ] Retrieve own profile (GET /api/users/me)
- [ ] Update profile fields (PUT /api/users/me)
- [ ] Upload avatar (PUT /api/users/me/avatar)
- [ ] Retrieve user directory (GET /api/users?limit=10&offset=0)
- [ ] Retrieve other user's public profile (GET /api/users/{userId})
- [ ] Cannot edit someone else's profile (403)
- [ ] Validation errors return 400 with error codes
- [ ] Avatar file validation works (reject too large, invalid format)
- [ ] Authorization header required (401 without JWT)
- [ ] Response format standardized with success/error
- [ ] CORS headers present
- [ ] Performance: Profile retrieval < 100ms
- [ ] Performance: Avatar upload < 2 seconds

---

## Success Criteria

✅ **Phase 2 Complete When**:
1. All 5 endpoints implemented and tested
2. All manual tests from spec passing
3. Avatar upload validated and stored correctly
4. Directory listing supports pagination
5. Authorization enforced (creator/assignee patterns)
6. Code follows Google Java Style Guide
7. Git history clean (commits follow format)
8. Performance targets met
9. Security review passed
10. Ready to merge to main

---

## Complexity: NO VIOLATIONS

No constitution violations; leverages Phase 1 foundation with clean separation of profile concerns.
