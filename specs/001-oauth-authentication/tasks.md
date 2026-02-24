# Phase 1: OAuth2 Authentication - Implementation Tasks

**Branch:** `phase/1-authentication`  
**Estimated Duration:** 5-7 days  
**Depends On:** Phase 0 (Architecture complete)

---

## Overview

Break down Phase 1 specification into concrete, implementation-ready tasks for backend and UI. Each task specifies exact files, endpoints, and acceptance criteria.

---

## Backend Tasks (Java/Spring Boot)

### T001-T005: User Entity & Database Setup
- [ ] T001 Create User entity with Google OAuth fields in `src/main/java/com/karthik/task_management_backend/entity/User.java`
- [ ] T002 Create UserRole enum (ADMIN, USER) in `src/main/java/com/karthik/task_management_backend/entity/UserRole.java`
- [ ] T003 Create UserRepository interface in `src/main/java/com/karthik/task_management_backend/repository/UserRepository.java`
- [ ] T004 Create initial database migration `V1__create_users_table.sql` (Phase 8 Flyway setup)
- [ ] T005 Add Maven dependencies (spring-security-oauth2-client, jjwt, Lombok) to `pom.xml`

### T006-T010: JWT Service Implementation
- [ ] T006 Create JwtService class in `src/main/java/com/karthik/task_management_backend/service/JwtService.java` with token generation
- [ ] T007 Implement token validation method in JwtService (signature, expiration checks)
- [ ] T008 Create custom JwtAuthenticationToken class in `src/main/java/com/karthik/task_management_backend/security/JwtAuthenticationToken.java`
- [ ] T009 Add JWT secret to environment variables (.env.local, application.yaml)
- [ ] T010 Test token generation/validation locally (manual test with `mvn test` or console)

### T011-T015: Spring Security & OAuth2 Configuration
- [ ] T011 Create SecurityConfig class in `src/main/java/com/karthik/task_management_backend/config/SecurityConfig.java`
- [ ] T012 Configure Spring Security with OAuth2 support (Google provider)
- [ ] T013 Configure CORS globally for frontend domain in SecurityConfig
- [ ] T014 Configure JWT filter and request authentication filter
- [ ] T015 Set up OAuth2 client properties in `application.yaml` (clientId, clientSecret, redirectUri)

### T016-T020: Authentication Service
- [ ] T016 Create AuthService class in `src/main/java/com/karthik/task_management_backend/service/AuthService.java`
- [ ] T017 Implement OAuth2 login initiation (return redirect URL to Google)
- [ ] T018 Implement OAuth2 callback handler (exchange code for token, create/update user)
- [ ] T019 Implement JWT token generation for authenticated users
- [ ] T020 Implement logout logic (invalidate token on client side)

### T021-T025: Auth Controller Endpoints
- [ ] T021 Create AuthController in `src/main/java/com/karthik/task_management_backend/controller/AuthController.java`
- [ ] T022 Implement `POST /api/auth/login` endpoint (return Google redirect URL)
- [ ] T023 Implement `GET /api/auth/callback` endpoint (handle OAuth callback, return JWT)
- [ ] T024 Implement `GET /api/auth/profile` endpoint (return authenticated user profile)
- [ ] T025 Implement `POST /api/auth/logout` endpoint (acknowledge logout)

### T026-T030: DTOs & Response Formatting
- [ ] T026 Create UserDTO in `src/main/java/com/karthik/task_management_backend/dto/UserDTO.java`
- [ ] T027 Create LoginResponseDTO in `src/main/java/com/karthik/task_management_backend/dto/LoginResponseDTO.java`
- [ ] T028 Create ApiResponse<T> generic wrapper in `src/main/java/com/karthik/task_management_backend/dto/ApiResponse.java`
- [ ] T029 Create ErrorResponseDTO in `src/main/java/com/karthik/task_management_backend/dto/ErrorResponseDTO.java`
- [ ] T030 Implement error handling interceptor for consistent error responses

### T031-T035: Validation & Error Handling
- [ ] T031 Add JSR-303 validation annotations to User entity
- [ ] T032 Create custom exception classes (OAuthException, JwtException, UserNotFoundException) in `src/main/java/com/karthik/task_management_backend/exception/`
- [ ] T033 Create GlobalExceptionHandler in `src/main/java/com/karthik/task_management_backend/exception/GlobalExceptionHandler.java`
- [ ] T034 Implement rate limiting headers for `/api/auth/*` endpoints
- [ ] T035 Add logging for authentication events (INFO level)

### T036-T040: Backend Testing & Hardening
- [ ] T036 Test user creation on OAuth callback (manual: login via Google, verify user in DB)
- [ ] T037 Test JWT token generation format and expiration (manual: decode token, verify claims)
- [ ] T038 Test authentication on protected endpoints (manual: verify 401 without token)
- [ ] T039 Test CORS headers in responses (manual: check response headers for Access-Control-*)
- [ ] T040 Verify security headers present (X-Content-Type-Options, X-Frame-Options, etc.)

---

## Frontend Tasks (React/TypeScript)

### T041-T045: Authentication Store & Hooks
- [ ] T041 Create Zustand store `src/store/authStore.ts` with user state and login/logout actions
- [ ] T042 Create custom hook `src/hooks/useAuth.ts` for consuming auth store
- [ ] T043 Create custom hook `src/hooks/useAuthCheck.ts` for verifying auth on mount
- [ ] T044 Implement JWT persistence in localStorage in authStore
- [ ] T045 Create type definitions `src/types/auth.ts` (User, LoginResponse, ApiResponse)

### T046-T050: API Client Setup
- [ ] T046 Create Axios instance in `src/services/apiClient.ts` with base URL
- [ ] T047 Add JWT interceptor to attach token to all requests in `Authorization` header
- [ ] T048 Add error interceptor for handling 401 responses (redirect to login)
- [ ] T049 Create `src/services/authService.ts` with login, callback, logout API methods
- [ ] T050 Test API client locally (manual: verify network requests in DevTools)

### T051-T055: Authentication Components
- [ ] T051 Create GoogleSignInButton component in `src/components/GoogleSignInButton.tsx`
- [ ] T052 Implement click handler to redirect to backend `/api/auth/login` endpoint
- [ ] T053 Create LoginPage component in `src/pages/LoginPage.tsx` with sign-in UI
- [ ] T054 Create OAuthCallbackPage component in `src/pages/OAuthCallbackPage.tsx` to handle callback
- [ ] T055 Implement callback logic: extract code, exchange for JWT, redirect to home

### T056-T060: Protected Routes & Authorization
- [ ] T056 Create ProtectedRoute HOC in `src/components/ProtectedRoute.tsx`
- [ ] T057 Implement redirect to login for unauthenticated users
- [ ] T058 Create HomePage component in `src/pages/HomePage.tsx` (authenticated landing)
- [ ] T059 Add route configuration in `src/App.tsx` with public and protected routes
- [ ] T060 Test route protection (manual: verify unauthenticated cannot access /home)

### T061-T065: User Profile Display & Logout
- [ ] T061 Create UserProfile component in `src/components/UserProfile.tsx` to display logged-in user
- [ ] T062 Add profile data fetch on HomePage mount
- [ ] T063 Create LogoutButton component in `src/components/LogoutButton.tsx`
- [ ] T064 Implement logout logic: clear localStorage, call backend, redirect to login
- [ ] T065 Add profile display in header/navbar

### T066-T070: Error Handling & UX
- [ ] T066 Create error message display component `src/components/ErrorMessage.tsx`
- [ ] T067 Add error state to authStore and display on login page
- [ ] T068 Implement loading states during OAuth flow (show spinner on callback page)
- [ ] T069 Add user-friendly error messages for common scenarios (network error, OAuth failure)
- [ ] T070 Verify TypeScript strict mode enabled in `tsconfig.json`

### T071-T075: Frontend Testing & Validation
- [ ] T071 Test full login flow manually (Google button → OAuth callback → localStorage update)
- [ ] T072 Test logout flow (button click → localStorage clear → redirect to login)
- [ ] T073 Test protected route access (can access /home when logged in, redirects when not)
- [ ] T074 Test token persistence (refresh page while logged in, should remain logged in)
- [ ] T075 Test API errors handling (manual: simulate 401 response, verify redirects to login)

---

## Integration Tests (Manual E2E)

### T076-T080: End-to-End User Flows
- [ ] T076 E2E: User clicks Google Sign In → OAuth popup → approved → redirected to home
- [ ] T077 E2E: User logged in, JWT visible in localStorage and sent in API requests
- [ ] T078 E2E: User profile endpoint returns correct user data from backend
- [ ] T079 E2E: User clicks logout → localStorage cleared → redirected to login page
- [ ] T080 E2E: Refresh page while logged in → user state restored from localStorage

---

## Configuration & Environment

### T081-T085: Environment Variables & Secrets
- [ ] T081 Create `.env.local` file with REACT_APP_API_URL=http://localhost:8080
- [ ] T082 Create backend `.env.local` with GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, JWT_SECRET
- [ ] T083 Update `backend/application.yaml` with OAuth2 client configuration
- [ ] T084 Update `backend/application-local.yaml` for local development overrides
- [ ] T085 Verify .env.local files are in .gitignore (not committed)

---

## Acceptance Criteria Checklist

### Backend Acceptance
- [ ] User entity created with googleId, email, role fields
- [ ] JWT generation produces valid HS256 tokens with 24-hour expiration
- [ ] OAuth2 flow: login → callback → user created/updated → JWT returned
- [ ] All 4 endpoints respond with ApiResponse format (success: true/false)
- [ ] Security headers present in all responses
- [ ] CORS configured for http://localhost:3000
- [ ] Rate limiting active on auth endpoints
- [ ] No hardcoded secrets in code (all environment variables)
- [ ] Errors return consistent error format with error codes

### Frontend Acceptance
- [ ] Google Sign In button functional and redirects to backend
- [ ] OAuth callback page executes and stores JWT in localStorage
- [ ] Protected routes redirect unauthenticated users to login
- [ ] User profile displays after authentication
- [ ] Logout clears token and redirects to login
- [ ] Token persists across page refreshes
- [ ] TypeScript strict mode enforced (no `any` types)
- [ ] All API calls include JWT in Authorization header
- [ ] 401 responses trigger logout and redirect to login

### Cross-Layer Acceptance
- [ ] Backend server starts without errors: `mvn spring-boot:run`
- [ ] Frontend dev server starts without errors: `npm start`
- [ ] Network requests visible in browser DevTools
- [ ] No console warnings or errors during normal usage
- [ ] Manual QA checklist from spec.md passed (13 items)

---

## Git Workflow

### Branch Setup
```bash
git checkout -b phase/1-authentication
```

### Commit Convention
All commits should be prefixed with `[PHASE 1]`:
```
[PHASE 1] Implement User entity and OAuth2 configuration
[PHASE 1] Create Auth controller endpoints
[PHASE 1] Set up Zustand auth store and hooks
```

### Before Merge
1. All tasks completed (checked off above)
2. All manual tests passing
3. Code follows style guide (Java: Google, TypeScript: Airbnb)
4. No console errors or warnings
5. TypeScript compiles without errors

### Merge & Tag
```bash
git checkout main
git merge --ff-only phase/1-authentication
git tag v0.1.0 -a -m "Phase 1: OAuth2 Authentication Complete"
git push origin main v0.1.0
```

---

## Success Criteria Summary

✅ **Phase 1 Implementation Complete:**
- Full Google OAuth2 flow working end-to-end
- JWT tokens generated and validated correctly
- Protected routes enforcing authentication
- User profiles retrievable and displayed
- All manual tests passing
- Zero hardcoded secrets
- Industry-grade code quality standards met
- Ready for merge to main and deployment

---

## Notes

- **No unit tests:** Per constitution, rely on manual E2E testing
- **Token storage:** JWT in localStorage (acceptable for Phase 1; consider HttpOnly cookies in Phase 7)
- **File paths:** All paths must exist or be created during implementation
- **Testing:** Use manual test checklist from spec.md as verification
- **Documentation:** Register completed tasks in spec.md under "Clarifications" section during implementation
