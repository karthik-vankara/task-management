# Phase 2: User Profile Management - Implementation Tasks

**Branch:** `phase/2-user-profiles`  
**Estimated Duration:** 4-5 days  
**Depends On:** Phase 1 (Auth complete)

---

## Backend Tasks (Java/Spring Boot)

### T001-T005: User Profile Entity Extensions
- [ ] T001 Extend User entity with name, bio, avatarUrl fields in `src/main/java/com/karthik/task_management_backend/entity/User.java`
- [ ] T002 Add validation annotations (@NotBlank, @Length) for profile fields
- [ ] T003 Create database migration `V2__add_user_profile_fields.sql` (Phase 8 Flyway)
- [ ] T004 Create ProfileValidator in `src/main/java/com/karthik/task_management_backend/validator/ProfileValidator.java`
- [ ] T005 Add Maven dependency for avatar processing: thumbnailator, commons-io, tika-core

### T006-T010: Profile DTOs & Service
- [ ] T006 Create UserProfileDTO in `src/main/java/com/karthik/task_management_backend/dto/UserProfileDTO.java`
- [ ] T007 Create UserProfileUpdateDTO in `src/main/java/com/karthik/task_management_backend/dto/UserProfileUpdateDTO.java`
- [ ] T008 Create UserDirectoryDTO in `src/main/java/com/karthik/task_management_backend/dto/UserDirectoryDTO.java`
- [ ] T009 Create ProfileService in `src/main/java/com/karthik/task_management_backend/service/ProfileService.java`
- [ ] T010 Implement avatar upload logic with resize to 200x200px in ProfileService

### T011-T015: Profile Controller Endpoints
- [ ] T011 Create ProfileController in `src/main/java/com/karthik/task_management_backend/controller/ProfileController.java`
- [ ] T012 Implement `GET /api/users/me` endpoint (get current user profile)
- [ ] T013 Implement `PUT /api/users/me` endpoint (update name/bio)
- [ ] T014 Implement `PUT /api/users/me/avatar` endpoint (upload and resize avatar)
- [ ] T015 Implement `GET /api/users` endpoint (list users with pagination)

### T016-T020: Directory & Public Profile
- [ ] T016 Implement `GET /api/users/{userId}` endpoint (public profile view)
- [ ] T017 Add cursor-based pagination (lastSeenId parameter)
- [ ] T018 Create database indexes on email, googId, created_at, name
- [ ] T019 Add HTML sanitization for bio field (remove script tags)
- [ ] T020 Configure static file serving for avatars at `/uploads/avatars/`

### T021-T025: Profile Testing & Hardening
- [ ] T021 Test profile update with valid data (manual: PUT endpoint, verify DB)
- [ ] T022 Test profile update validation (manual: too long bio, over 255 chars name)
- [ ] T023 Test avatar upload (manual: 5MB max, JPEG/PNG only, resize works)
- [ ] T024 Test directory listing pagination (manual: 50 users per page)
- [ ] T025 Test public profile access (manual: verify email/googleId not exposed)

---

## Frontend Tasks (React/TypeScript)

### T026-T030: Profile Page Components
- [ ] T026 Create ProfilePage component in `src/pages/ProfilePage.tsx`
- [ ] T027 Create ProfileForm component in `src/components/ProfileForm.tsx` for editing
- [ ] T028 Create AvatarUpload component in `src/components/AvatarUpload.tsx`
- [ ] T029 Create UserCard component in `src/components/UserCard.tsx` for directory display
- [ ] T030 Create UserDirectory component in `src/pages/UserDirectory.tsx`

### T031-T035: Profile Store & API Integration
- [ ] T031 Extend authStore in `src/store/authStore.ts` with profile update actions
- [ ] T032 Create `src/services/profileService.ts` with API methods for profile CRUD
- [ ] T033 Implement avatar file upload handling and progress tracking
- [ ] T034 Add error handling for profile updates (validation errors, network errors)
- [ ] T035 Implement loading states for profile operations

### T036-T040: Profile Display & Editing
- [ ] T036 Fetch current user profile on page load (GET /api/users/me)
- [ ] T037 Display profile information (name, bio, avatar) on ProfilePage
- [ ] T038 Implement profile edit UI with form inputs
- [ ] T039 Implement form validation on client side (required fields, length limits)
- [ ] T040 Handle avatar upload with preview before submission

### T041-T045: Directory & Search
- [ ] T041 Fetch and display user directory with pagination
- [ ] T042 Implement "Load More" button for pagination
- [ ] T043 Display each user as card with avatar, name, join date
- [ ] T044 Implement cursor-based pagination logic (lastSeenId)
- [ ] T045 Add click-to-view functionality (click user card → public profile)

### T046-T050: Frontend Testing & Validation
- [ ] T046 Test profile fetch and display (manual: name/bio/avatar show correctly)
- [ ] T047 Test profile edit (manual: change name/bio, verify submit, verify DB update)
- [ ] T048 Test avatar upload (manual: select file, preview, upload, verify on page)
- [ ] T049 Test profile validation (manual: too long input rejected on client)
- [ ] T050 Test directory pagination (manual: load users, click load more, new users appear)

---

## Integration Tests (Manual E2E)

### T051-T055: End-to-End Profile Flows
- [ ] T051 E2E: User logs in → views own profile → profile shows empty initially
- [ ] T052 E2E: User updates name/bio → saves changes → profile reflects changes
- [ ] T053 E2E: User uploads avatar → preview shows → uploads → avatar displayed
- [ ] T054 E2E: User browses directory → sees other users → clicks user → views public profile
- [ ] T055 E2E: Public profile hides email/googleId but shows name, bio, avatar

---

## Acceptance Criteria Checklist

### Backend Acceptance
- [ ] User profile fields (name, bio, avatarUrl) stored and retrieved correctly
- [ ] Avatar upload with max 5MB, JPEG/PNG only validation working
- [ ] Avatar auto-resized to 200x200px and saved to `/uploads/avatars/`
- [ ] Directory endpoint returns paginated users with cursor-based pagination
- [ ] Public profiles don't expose email or googleId
- [ ] Profile update only by owner (authorization enforced)
- [ ] All endpoints return ApiResponse format
- [ ] Indexes created for performance (<100ms profile fetch)

### Frontend Acceptance
- [ ] Profile page loads current user data on mount
- [ ] Edit form allows updating name and bio
- [ ] Avatar upload with preview displayed before submit
- [ ] Form validation prevents submission of invalid data
- [ ] Directory page shows paginated user list with cards
- [ ] Clicking user card navigates to public profile
- [ ] TypeScript strict mode enforced

### All Manual Tests Passing
- [ ] Manual test checklist from phase 2 spec.md (13 items) completed

---

## Git Workflow

```bash
git checkout -b phase/2-user-profiles origin/main
# ... implement tasks ...
git add .
git commit -m "[PHASE 2] User profile management implementation"
git push origin phase/2-user-profiles
# ... code review and testing ...
git checkout main
git merge --ff-only phase/2-user-profiles
git tag v0.2.0 -a -m "Phase 2: User Profile Management Complete"
```

---

## Success Criteria Summary

✅ **Phase 2 Implementation Complete:**
- User profiles viewable and editable
- Avatar upload and display functional
- User directory with pagination working
- Public profile view without exposing sensitive data
- All manual tests passing
- Ready for Phase 3 (task assignment requires user lookup)
