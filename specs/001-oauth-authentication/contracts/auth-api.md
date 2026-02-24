# API Contract: OAuth2 Authentication Endpoints

**Version**: 1.0.0  
**Date**: 2026-02-24  
**Scope**: Google OAuth2 login, callback handling, profile retrieval, and logout

---

## Design Principles

1. **Consistency**: All endpoints follow standard response format (success/error with timestamp)
2. **Security**: JWT validation on protected endpoints, CSRF state protection on OAuth flow
3. **Statelessness**: Endpoints are stateless except for persistent JWT token storage (client-side)
4. **Error Handling**: Standardized error response with error codes for client-side handling
5. **Documentation**: Every endpoint includes request/response examples and error cases

---

## Response Format Standards

### Success Response (HTTP 200, 201)

```json
{
  "success": true,
  "data": {
    /* endpoint-specific data */
  },
  "timestamp": "2026-02-24T10:30:00Z"
}
```

**Fields**:
- `success`: Boolean, always `true` for success
- `data`: Object or array (varies by endpoint)
- `timestamp`: ISO 8601 UTC timestamp of response

### Error Response (HTTP 400, 401, 500)

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE_ENUM",
    "message": "Human-readable error description",
    "details": {
      /* optional error-specific fields */
    }
  },
  "timestamp": "2026-02-24T10:30:00Z"
}
```

**Fields**:
- `success`: Boolean, always `false` for error
- `error.code`: Standardized error code (see Error Codes section)
- `error.message`: Human-readable message for UI display
- `error.details`: Optional field-level details (e.g., validation errors)
- `timestamp`: ISO 8601 UTC timestamp of response

---

## Endpoints

### 1. POST /api/auth/login

**Purpose**: Initiate Google OAuth2 authentication flow.  
**Authentication**: None (public endpoint)  
**Rate Limit**: 10 requests/minute per IP

#### Request

```http
POST /api/auth/login HTTP/1.1
Host: api.taskmanagement.com
Content-Type: application/json

{}
```

**Body**: Empty JSON object (no query parameters)

#### Success Response (HTTP 200)

```json
{
  "success": true,
  "data": {
    "redirectUrl": "https://accounts.google.com/o/oauth2/v2/auth?client_id=YOUR_CLIENT_ID&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fapi%2Fauth%2Fcallback&response_type=code&scope=openid%20email%20profile&state=RANDOM_STATE_VALUE"
  },
  "timestamp": "2026-02-24T10:30:00Z"
}
```

**Response Fields**:
- `redirectUrl`: Full Google OAuth URL for frontend to redirect to
  - Includes client_id, redirect_uri, scope, state, response_type
  - State is cryptographically random, 32+ bytes, base64-encoded
  - Valid for 10 minutes only

#### Error Response (HTTP 500)

```json
{
  "success": false,
  "error": {
    "code": "OAUTH_CONFIG_ERROR",
    "message": "OAuth2 is not properly configured"
  },
  "timestamp": "2026-02-24T10:30:00Z"
}
```

#### Implementation Notes

- Generate random state: `SecureRandom(32).nextBytes()` base64-encoded
- Store state in memory cache with 10-minute expiration
- For production (stateless): Store encrypted in Redis or signed JWT
- Client must follow redirect and authenticate with Google
- Upon completion, Google redirects to /api/auth/callback

---

### 2. GET /api/auth/callback

**Purpose**: Handle Google OAuth2 authorization code callback.  
**Authentication**: None required (but validates state token)  
**Rate Limit**: 5 requests/minute per IP (CSRF abuse prevention)

#### Request

```http
GET /api/auth/callback?code=4%2F0AX...&state=RANDOM_STATE_VALUE HTTP/1.1
Host: api.taskmanagement.com
```

**Query Parameters**:
- `code` (required): Authorization code from Google
- `state` (required): CSRF protection token (must match sent state)
- `error` (optional): If user denies, Google sends `error=access_denied`

#### Success Response (HTTP 302 Redirect)

```http
HTTP/1.1 302 Found
Location: http://localhost:3000/dashboard?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Set-Cookie: HttpOnly; Secure; SameSite=Strict
```

**Response**: Redirect to frontend dashboard with JWT token in query parameter

**Token in URL**:
- JWT access token included as query param for frontend to store in localStorage
- For production: Use Set-Cookie header with HttpOnly flag instead

#### Error Response (HTTP 400)

**Case 1: Invalid State**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_STATE",
    "message": "CSRF token validation failed. Please try login again."
  },
  "timestamp": "2026-02-24T10:30:00Z"
}
```

**Case 2: Authorization Code Exchange Failed**
```json
{
  "success": false,
  "error": {
    "code": "OAUTH_ERROR",
    "message": "Failed to exchange authorization code with Google. Please try again."
  },
  "timestamp": "2026-02-24T10:30:00Z"
}
```

**Case 3: User Denied Access**
```json
{
  "success": false,
  "error": {
    "code": "OAUTH_USER_DENIED",
    "message": "You denied access to Google. Please authorize to continue."
  },
  "timestamp": "2026-02-24T10:30:00Z"
}
```

**Case 4: User Creation Failed**
```json
{
  "success": false,
  "error": {
    "code": "USER_CREATION_FAILED",
    "message": "Failed to create user account. Please contact support.",
    "details": {
      "reason": "Email already exists"
    }
  },
  "timestamp": "2026-02-24T10:30:00Z"
}
```

#### Implementation Notes

- Validate state against stored value (exact match required)
- Exchange code for access_token via Google OAuth2 token endpoint
- Call Google UserInfo endpoint with access_token
- Extract googleId, email, name, avatarUrl
- Create or update User in database
- Generate JWT token with user claims
- Redirect to frontend with token (query param for local dev, Set-Cookie for prod)

---

### 3. GET /api/auth/profile

**Purpose**: Retrieve current authenticated user profile.  
**Authentication**: Required (JWT in Authorization header)  
**Rate Limit**: 100 requests/minute per user

#### Request

```http
GET /api/auth/profile HTTP/1.1
Host: api.taskmanagement.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Headers**:
- `Authorization: Bearer <JWT>` (required)
  - JWT must be valid and not expired
  - HS256 signature must verify against JWT_SECRET

#### Success Response (HTTP 200)

```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "John Doe",
    "role": "USER",
    "avatarUrl": "https://lh3.googleusercontent.com/a/default-user",
    "bio": "",
    "createdAt": "2026-02-24T10:30:00Z",
    "updatedAt": "2026-02-24T10:30:00Z"
  },
  "timestamp": "2026-02-24T10:30:00Z"
}
```

**Response Fields**:
- `id`: User's primary key in database
- `email`: User's email from Google profile
- `name`: User's display name from Google profile
- `role`: User's role (ADMIN or USER)
- `avatarUrl`: URL to Google profile picture
- `bio`: User's bio (empty until Phase 2 profile editing)
- `createdAt`: When user first authenticated
- `updatedAt`: When user profile last updated

#### Error Responses

**Case 1: Missing Authorization Header (HTTP 401)**
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Authorization header is missing"
  },
  "timestamp": "2026-02-24T10:30:00Z"
}
```

**Case 2: Invalid JWT Token (HTTP 401)**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_TOKEN",
    "message": "JWT token is invalid or malformed"
  },
  "timestamp": "2026-02-24T10:30:00Z"
}
```

**Case 3: Expired JWT Token (HTTP 401)**
```json
{
  "success": false,
  "error": {
    "code": "EXPIRED_TOKEN",
    "message": "JWT token has expired. Please login again."
  },
  "timestamp": "2026-02-24T10:30:00Z"
}
```

**Case 4: User Not Found (HTTP 404)**
```json
{
  "success": false,
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "User not found in database"
  },
  "timestamp": "2026-02-24T10:30:00Z"
}
```

#### Implementation Notes

- Extract JWT from `Authorization: Bearer` header
- Validate signature using JWT_SECRET
- Check expiration (iat + 86400 seconds)
- Extract user ID from JWT claims
- Query User by ID, return UserDTO
- No user modification; this is read-only

---

### 4. POST /api/auth/logout

**Purpose**: Logout current user (client-side token deletion).  
**Authentication**: Optional (JWT in Authorization header if provided)  
**Rate Limit**: 10 requests/minute per IP

#### Request

```http
POST /api/auth/logout HTTP/1.1
Host: api.taskmanagement.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Length: 0
```

**Body**: Empty  

**Headers** (optional):
- `Authorization: Bearer <JWT>` - If provided, will invalidate on server (future phase)

#### Success Response (HTTP 204 No Content)

```http
HTTP/1.1 204 No Content
```

No body returned. HTTP 204 indicates successful logout.

#### Alternative Success Response (HTTP 200)

```json
{
  "success": true,
  "data": {
    "message": "Logged out successfully"
  },
  "timestamp": "2026-02-24T10:30:00Z"
}
```

Use this if frontend expects JSON response (easier for debugging).

#### Error Response (HTTP 400)

**Case 1: Invalid Token (HTTP 401)**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_TOKEN",
    "message": "JWT token is invalid"
  },
  "timestamp": "2026-02-24T10:30:00Z"
}
```

#### Implementation Notes

- Phase 1: Server-side logout is stateless (no token invalidation)
- Client deletes JWT from localStorage
- Token remains valid until expiration (24 hours)
- For production: Use token blacklist or shorter expiration
- POST used instead of GET for semantic correctness (state-changing operation)

---

## CORS Configuration

### Allowed Origins

```
http://localhost:3000           (local development)
https://yourdomain.vercel.app   (production frontend)
```

### Allowed Methods

```
GET, POST, OPTIONS
```

### Allowed Headers

```
Content-Type
Authorization
```

### Exposed Headers

```
X-Total-Count    (if pagination used in future phases)
Content-Type
```

### Credentials

```
true (required for cookies, but JWT is in headers/query for Phase 1)
```

---

## Error Codes Reference

| Code | HTTP | Meaning | Client Action |
|------|------|---------|----------------|
| `OAUTH_CONFIG_ERROR` | 500 | OAuth2 not configured | Retry or contact support |
| `INVALID_STATE` | 400 | CSRF token invalid | Restart login flow |
| `OAUTH_ERROR` | 400 | Google OAuth failure | Restart login flow |
| `OAUTH_USER_DENIED` | 400 | User denied consent | Retry with permission |
| `USER_CREATION_FAILED` | 500 | Database error | Retry or contact support |
| `UNAUTHORIZED` | 401 | Missing auth header | Redirect to login |
| `INVALID_TOKEN` | 401 | JWT invalid/malformed | Redirect to login |
| `EXPIRED_TOKEN` | 401 | JWT expired | Redirect to login |
| `USER_NOT_FOUND` | 404 | User ID not in DB | Rare; contact support |

---

## Rate Limiting

Endpoints are rate-limited to prevent abuse:

| Endpoint | Limit | Window | Note |
|----------|-------|--------|------|
| POST /login | 10 | 1 minute | High limit (multiple attempts okay) |
| GET /callback | 5 | 1 minute | Low limit (CSRF abuse prevention) |
| GET /profile | 100 | 1 minute | High limit (normal API use) |
| POST /logout | 10 | 1 minute | Moderate limit |

**Implementation**: Use Spring RateLimit or Bucket4j library (future enhancement).

---

## Security Headers

All responses include:

```
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains (production only)
```

---

## JWT Token Structure

See [jwt-payload.md](jwt-payload.md) for detailed JWT payload contract.

---

## Testing the API

### Manual Test Flow

1. **Get OAuth URL**:
   ```bash
   curl -X POST http://localhost:8080/api/auth/login
   ```
   → Returns redirectUrl

2. **Authorize with Google**:
   - Copy redirectUrl into browser
   - Login to Google account
   - Grant permissions

3. **Receive Callback**:
   - Browser redirected to /api/auth/callback?code=...&state=...
   - Redirected to frontend with ?token=JWT

4. **Retrieve Profile**:
   ```bash
   curl -H "Authorization: Bearer <JWT>" http://localhost:8080/api/auth/profile
   ```
   → Returns user data

5. **Logout**:
   ```bash
   curl -X POST -H "Authorization: Bearer <JWT>" http://localhost:8080/api/auth/logout
   ```
   → Returns 204 No Content

---

## Summary

**Phase 1 API Contract**:
- ✅ 4 endpoints with clear request/response contracts
- ✅ Standardized response format for all endpoints
- ✅ Comprehensive error codes for client-side handling
- ✅ Security best practices (CORS, headers, CSRF)
- ✅ Rate limiting and validation rules

**Ready for frontend integration** and backend implementation.
