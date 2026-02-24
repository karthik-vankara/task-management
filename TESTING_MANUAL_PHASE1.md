# Phase 1 Backend Manual Testing Guide

## Prerequisites

1. **PostgreSQL Database Setup**
   ```bash
   # Create local database
   createdb task_management_local
   
   # Verify connection
   psql -U postgres -d task_management_local -c "\dt"
   ```

2. **Environment Variables (Backend)**
   ```bash
   # Copy example to actual .env.local
   cp backend/.env.local.example backend/.env.local
   
   # Edit .env.local with your values:
   # - GOOGLE_CLIENT_ID (from Google Cloud Console)
   # - GOOGLE_CLIENT_SECRET
   # - JWT_SECRET (generate with: openssl rand -base64 32)
   ```

3. **Start Backend**
   ```bash
   cd backend
   mvn clean install
   ./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
   ```
   Server should start on: http://localhost:8080

## Manual Test Cases

### T036: User Creation on OAuth Callback

**Objective:** Verify user is created in database on OAuth callback

**Steps:**
1. Start backend with local profile
2. Call: `POST http://localhost:8080/api/auth/login`
3. Response should contain `redirectUrl` to Google OAuth
4. (Simulate) Call: `GET http://localhost:8080/api/auth/callback?code=test-code&state=test-state`
5. Response should contain JWT token
6. Query database: `SELECT * FROM users WHERE email LIKE '%test%';`

**Expected:**
- ✓ User record created in database
- ✓ Columns populated: id, google_id, email, name, role (USER), created_at, updated_at
- ✓ Unique indexes enforced (no duplicate emails)
- ✓ Response includes: token, userId, email, name, role

---

### T037: JWT Token Generation Format and Expiration

**Objective:** Verify JWT token structure and expiration

**Steps:**
1. Extract JWT from callback response: `token=<jwt>`
2. Decode base64: `echo <jwt> | cut -d. -f1 | base64 -d | jq`
3. Verify header contains: `"alg": "HS256", "typ": "JWT"`
4. Decode payload: `echo <jwt> | cut -d. -f2 | base64 -d | jq`
5. Verify payload contains:
   - `userId`: number (user ID from database)
   - `email`: string (user email)
   - `role`: string ("USER" or "ADMIN")
   - `iat`: number (issued-at timestamp)
   - `exp`: number (expiration timestamp, should be ~24 hours after iat)

**Expected:**
- ✓ Header algorithm is HS256
- ✓ Payload contains all required claims
- ✓ Expiration = issued-at + 86400000ms (24 hours)
- ✓ Token signature is valid (verified during validation)

**Using curl:**
```bash
# Decode header
curl -s http://localhost:8080/api/auth/callback?code=test&state=test \
  | jq -r '.token' | cut -d. -f1 | base64 -d | jq

# Decode payload
JWT_TOKEN=$(curl -s http://localhost:8080/api/auth/callback?code=test&state=test \
  | jq -r '.token')
echo $JWT_TOKEN | cut -d. -f2 | base64 -d | jq
```

---

### T038: 401 Errors on Protected Endpoints Without Token

**Objective:** Verify access control - protected endpoints reject requests without valid JWT

**Steps:**
1. Call protected endpoint WITHOUT authorization header:
   ```bash
   curl -X GET http://localhost:8080/api/auth/profile
   ```
2. Response status should be 401 Unauthorized
3. Response body should contain error message

4. Repeat with INVALID token:
   ```bash
   curl -X GET http://localhost:8080/api/auth/profile \
     -H "Authorization: Bearer invalid-token"
   ```
5. Response status should be 401

6. Call with VALID token:
   ```bash
   curl -X GET http://localhost:8080/api/auth/profile \
     -H "Authorization: Bearer <valid-jwt-token>"
   ```
7. Response status should be 200 with user profile

**Expected:**
- ✓ No token → 401 Unauthorized
- ✓ Invalid token → 401 Unauthorized
- ✓ Valid token → 200 OK with user data
- ✓ Error responses include: message, code, status, timestamp, path

---

### T039: CORS Headers in Responses

**Objective:** Verify CORS headers are present for frontend communication

**Steps:**
1. Make preflight request from frontend origin:
   ```bash
   curl -i -X OPTIONS http://localhost:8080/api/auth/login \
     -H "Origin: http://localhost:3000" \
     -H "Access-Control-Request-Method: POST" \
     -H "Access-Control-Request-Headers: Content-Type,Authorization"
   ```

2. Verify response headers include:
   - `Access-Control-Allow-Origin: http://localhost:3000`
   - `Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS`
   - `Access-Control-Allow-Headers: Authorization, Content-Type, Accept, X-Requested-With`
   - `Access-Control-Allow-Credentials: true`
   - `Access-Control-Max-Age: 3600`

3. Verify actual request includes CORS headers:
   ```bash
   curl -i -X POST http://localhost:8080/api/auth/login \
     -H "Origin: http://localhost:3000" \
     -H "Content-Type: application/json"
   ```

4. Response should include Access-Control-* headers

**Expected:**
- ✓ Preflight returns 200 OK (no errors)
- ✓ All CORS headers present in response
- ✓ Allowed origins match configured list
- ✓ Credentials allowed (true)
- ✓ Methods and headers match configuration

---

### T040: Security Headers (X-Content-Type-Options, X-Frame-Options, X-XSS-Protection)

**Objective:** Verify security headers prevent common attacks

**Steps:**
1. Make any request to backend:
   ```bash
   curl -i http://localhost:8080/api/auth/login
   ```

2. Verify response headers include:
   - `X-Content-Type-Options: nosniff`
   - `X-Frame-Options: DENY` or `SAMEORIGIN`
   - `X-XSS-Protection: 1; mode=block`
   - `Content-Security-Policy: default-src 'self'; ...`

3. Test that browser would prevent:
   - MIME type sniffing (X-Content-Type-Options: nosniff)
   - Clickjacking (X-Frame-Options: DENY)
   - Reflected XSS (X-XSS-Protection: 1; mode=block)

**Expected:**
- ✓ All security headers present in responses
- ✓ Headers have appropriate restrictive values
- ✓ CSP policy present and reasonable

**Check with curl:**
```bash
curl -i http://localhost:8080/api/auth/login | grep -i "^x-\|^content-security"
```

---

## Rate Limiting Tests

### Test Rate Limiting on /api/auth/login (10/min limit)

```bash
# Should succeed (1-10)
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/auth/login 2>/dev/null | jq '.redirectUrl'
done

# Should fail with 429 (11+)
curl -X POST http://localhost:8080/api/auth/login
# Expected: 429 Too Many Requests
```

### Test Rate Limiting on /api/auth/callback (5/min limit)

```bash
# Should succeed (1-5)
for i in {1..5}; do
  curl http://localhost:8080/api/auth/callback?code=test&state=test 2>/dev/null
done

# Should fail with 429 (6+)
curl http://localhost:8080/api/auth/callback?code=test&state=test
# Expected: 429 Too Many Requests
```

---

## Database Verification

### Verify User Entity Structure

```sql
-- Check table exists
\dt users;

-- Check columns
\d users;

-- Expected columns:
-- id (SERIAL PRIMARY KEY)
-- google_id (VARCHAR, UNIQUE NOT NULL)
-- email (VARCHAR, UNIQUE NOT NULL)
-- name (VARCHAR)
-- role (VARCHAR, DEFAULT 'USER')
-- avatar_url (VARCHAR)
-- bio (TEXT)
-- created_at (TIMESTAMP)
-- updated_at (TIMESTAMP)

-- Check indexes
\di users*;

-- Expected indexes:
-- idx_users_google_id (UNIQUE)
-- idx_users_email (UNIQUE)
```

### Query Created Users

```sql
SELECT id, email, name, role, created_at FROM users ORDER BY created_at DESC;
```

---

## Troubleshooting

### JWT Secret too short
**Error:** `IllegalArgumentException: secret must be at least 256 bits`
**Fix:** Set JWT_SECRET to 32+ characters (run: `openssl rand -base64 32`)

### Database connection refused
**Error:** `java.sql.SQLException: Connection refused`
**Fix:** Ensure PostgreSQL is running and database exists
```bash
createdb task_management_local
psql -l  # List databases
```

### CORS errors in frontend
**Error:** `Access to XMLHttpRequest from origin has been blocked by CORS policy`
**Fix:** Ensure frontend URL in SecurityConfig matches Origin header

### Rate limit too strict
**Error:** Requests failing immediately after startup
**Fix:** Check in-memory rate limit map isn't persisting across server restarts

---

## Performance Validation

### Measure Login Endpoint Response Time

```bash
time curl -X POST http://localhost:8080/api/auth/login
# Expected: < 200ms
```

### Measure JWT Validation Performance

```bash
# Generate token
TOKEN=$(curl -s http://localhost:8080/api/auth/callback?code=test&state=test \
  | jq -r '.token')

# Time profile endpoint (validates JWT on each request)
for i in {1..100}; do
  curl -s http://localhost:8080/api/auth/profile \
    -H "Authorization: Bearer $TOKEN" > /dev/null
done

# Should complete quickly (< 100ms per request)
```

---

## Compliance Checklist

- [ ] T036: User created in database on OAuth callback
- [ ] T037: JWT token has correct format and 24-hour expiration
- [ ] T038: 401 errors returned for protected endpoints without token
- [ ] T039: CORS headers present for frontend communication
- [ ] T040: Security headers (X-Content-Type-Options, X-Frame-Options, X-XSS-Protection) present
- [ ] Rate limiting active (10/min login, 5/min callback)
- [ ] Database schema matches User entity
- [ ] No sensitive data logged (JWT tokens, passwords)
- [ ] All endpoints return consistent error format
- [ ] Performance within acceptable limits (< 200ms per request)

All tests passing → Phase 1 Backend COMPLETE ✓
