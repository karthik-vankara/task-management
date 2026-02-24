# Phase 2: User Profile Management - API Contracts

---

## Endpoint 1: GET /api/users/me
**Get Current User Profile**

### Request
```
GET /api/users/me HTTP/1.1
Authorization: Bearer {jwt_token}
```

### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "alice@example.com",
    "name": "Alice Johnson",
    "bio": "Product Manager | Coffee enthusiast",
    "avatarUrl": "https://example.com/uploads/avatars/uuid-abc123.jpg",
    "role": "USER",
    "createdAt": "2025-01-15T10:30:00Z",
    "updatedAt": "2025-01-20T14:45:30Z"
  },
  "timestamp": "2025-01-20T14:50:00Z"
}
```

### Error Responses
- **401 Unauthorized**: Missing or invalid JWT token
- **404 Not Found**: User profile not found (should not happen if JWT valid)

---

## Endpoint 2: PUT /api/users/me
**Update User Profile**

### Request
```
PUT /api/users/me HTTP/1.1
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "name": "Alice Johnson",
  "bio": "PM at StartupXYZ | Making task management delightful"
}
```

### Validation
- name: 1-255 characters, non-empty if provided
- bio: 0-5000 characters, optional (can be null/empty)
- Cannot modify: email, googleId, role, createdAt
- Sanitize bio: Remove HTML/JS (prevent XSS)

### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "alice@example.com",
    "name": "Alice Johnson",
    "bio": "PM at StartupXYZ | Making task management delightful",
    "avatarUrl": "https://example.com/uploads/avatars/uuid-abc123.jpg",
    "role": "USER",
    "createdAt": "2025-01-15T10:30:00Z",
    "updatedAt": "2025-01-20T14:52:00Z"
  },
  "timestamp": "2025-01-20T14:52:00Z"
}
```

### Error Responses
- **400 Bad Request**: Invalid field (name > 255 chars, bio > 5000 chars)
- **401 Unauthorized**: Invalid JWT
- **422 Unprocessable Entity**: Invalid data (e.g., immutable field modification attempt)

---

## Endpoint 3: PUT /api/users/me/avatar
**Upload User Avatar**

### Request
```
PUT /api/users/me/avatar HTTP/1.1
Authorization: Bearer {jwt_token}
Content-Type: multipart/form-data

file: <binary image>
```

### Validation
- Max size: 5MB
- Format: JPEG or PNG only
- Automatically resized to 200x200px
- Filename: Random UUID to prevent directory traversal
- Stored at: `uploads/avatars/{uuid}.jpg`

### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "avatarUrl": "https://example.com/uploads/avatars/uuid-abc123.jpg"
  },
  "timestamp": "2025-01-20T14:55:00Z"
}
```

### Error Responses
- **400 Bad Request**: 
  - File too large (> 5MB)
  - Invalid format (not JPEG/PNG)
  - File upload failed
- **401 Unauthorized**: Invalid JWT
- **413 Payload Too Large**: Upload size exceeds limit

---

## Endpoint 4: GET /api/users
**List All User Profiles (Directory)**

### Request
```
GET /api/users?limit=50&cursor=uuid-last-id HTTP/1.1
Authorization: Bearer {jwt_token}
```

### Query Parameters
- `limit`: Page size (default 50, max 100)
- `cursor`: Last user ID from previous page (for pagination)
- `search`: Optional search by name (future enhancement)

### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "users": [
      {
        "id": 1,
        "name": "Alice Johnson",
        "avatarUrl": "https://example.com/uploads/avatars/uuid-abc123.jpg",
        "createdAt": "2025-01-15T10:30:00Z"
      },
      {
        "id": 2,
        "name": "Bob Smith",
        "avatarUrl": "https://example.com/uploads/avatars/uuid-def456.jpg",
        "createdAt": "2025-01-16T09:00:00Z"
      }
    ],
    "hasMore": true,
    "nextCursor": "uuid-def456"
  },
  "timestamp": "2025-01-20T14:57:00Z"
}
```

### Error Responses
- **401 Unauthorized**: Invalid JWT
- **400 Bad Request**: Invalid cursor or limit

---

## Endpoint 5: GET /api/users/{userId}
**Get Public User Profile**

### Request
```
GET /api/users/123 HTTP/1.1
Authorization: Bearer {jwt_token}
```

### Path Parameters
- `userId`: User ID to retrieve

### Response (200 OK - Same as GET /api/users/me)
```json
{
  "success": true,
  "data": {
    "id": 123,
    "name": "Charlie Brown",
    "bio": "Software Engineer",
    "avatarUrl": "https://example.com/uploads/avatars/uuid-xyz789.jpg",
    "createdAt": "2025-01-12T08:15:00Z"
  },
  "timestamp": "2025-01-20T15:00:00Z"
}
```

### Note
- Returns public profile (no email/googleId/role)
- Same as authenticated user's profile for self (id match)

### Error Responses
- **401 Unauthorized**: Invalid JWT (authentication required)
- **404 Not Found**: User not found

---

## Error Codes Summary

| Code | Status | Meaning |
|------|--------|---------|
| UNAUTHORIZED | 401 | Invalid or missing JWT token |
| INVALID_REQUEST | 400 | Malformed request body |
| INVALID_FIELD | 422 | Field validation failed (too long, invalid format) |
| USER_NOT_FOUND | 404 | User does not exist |
| IMMUTABLE_FIELD | 422 | Attempted to modify read-only field |
| FILE_TOO_LARGE | 413 | Upload exceeds 5MB limit |
| INVALID_FILE_TYPE | 400 | File is not JPEG or PNG |
| INTERNAL_ERROR | 500 | Server error |

---

## Rate Limiting

| Endpoint | Limit | Window | Behavior |
|----------|-------|--------|----------|
| GET /api/users/me | 100/min | Per user | Essential operation |
| PUT /api/users/me | 10/min | Per user | Update not frequent |
| PUT /api/users/me/avatar | 5/min | Per user | Heavy operation (upload + resize) |
| GET /api/users | 50/min | Per user | Directory browsing |
| GET /api/users/{id} | 100/min | Per user | Profile lookup |

Return `429 Too Many Requests` with `Retry-After` header when exceeded.

---

## Security Headers

```
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
Content-Security-Policy: default-src 'self'
Cross-Origin-Resource-Sharing: Allow-Origin: https://frontend-domain
Cache-Control: public, max-age=60 (for profile GET)
```

---

## Example Request/Response Flow

### User Onboarding Flow
1. POST /api/auth/login → Get JWT token
2. GET /api/users/me → Verify profile created (name/bio null)
3. PUT /api/users/me → Set name + bio
4. PUT /api/users/me/avatar → Upload profile picture
5. GET /api/users/me → Confirm profile complete

### Directory Browse Flow
1. GET /api/users?limit=50 → First 50 users with cursor
2. GET /api/users?limit=50&cursor=last-id → Next 50 users
3. GET /api/users/123 → View specific user profile
