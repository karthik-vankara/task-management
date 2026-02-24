# API Contract: JWT Token Payload

**Version**: 1.0.0  
**Date**: 2026-02-24  
**Scope**: JWT token structure, claims, generation, and validation for OAuth2 authentication

---

## JWT Overview

**Format**: JSON Web Token (JWT)  
**Algorithm**: HS256 (HMAC with SHA-256)  
**Encoding**: Base64URL  
**Standard**: RFC 7519

**Structure**: `Header.Payload.Signature`

---

## Token Header

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Fields**:
- `alg`: Algorithm used for signing (must be HS256)
- `typ`: Token type (must be "JWT")

---

## Token Payload (Claims)

### Standard Claims (RFC 7519)

```json
{
  "sub": "1",
  "iat": 1708782600,
  "exp": 1708869000
}
```

**Standard Fields**:
- `sub` (Subject): User ID as string (maps to User.id)
  - Type: String
  - Example: "1", "123", "456"
  - Required: Yes

- `iat` (Issued At): Unix timestamp when token was created
  - Type: Long (seconds since epoch)
  - Example: 1708782600 (2026-02-24T10:30:00Z)
  - Required: Yes
  - Validation: Must be current time or in past

- `exp` (Expiration): Unix timestamp when token expires
  - Type: Long (seconds since epoch)
  - Example: 1708869000 (2026-02-25T10:30:00Z, 24 hours later)
  - Required: Yes
  - Validation: Must be greater than iat, typically iat + 86400 (24 hours)

### Custom Claims

```json
{
  "sub": "1",
  "email": "user@example.com",
  "role": "USER",
  "iat": 1708782600,
  "exp": 1708869000
}
```

**Custom Fields**:
- `email`: User's email address from Google
  - Type: String
  - Example: "user@example.com"
  - Required: Yes
  - Purpose: Quick email identification without database lookup

- `role`: User's role for authorization
  - Type: String (enum)
  - Example: "USER" or "ADMIN"
  - Required: Yes
  - Values: "USER", "ADMIN"
  - Purpose: Enable service layer authorization based on role

### Complete Payload Example

```json
{
  "sub": "42",
  "email": "john.doe@example.com",
  "role": "USER",
  "iat": 1708782600,
  "exp": 1708869000
}
```

---

## Token Signature

```
signature = HMAC-SHA256(
  base64url(header) + "." + base64url(payload),
  secret
)
```

**Secret**: 
- Stored in environment variable `JWT_SECRET`
- Minimum 256 bits (32 bytes) for HS256
- Example (base64-encoded): `a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2`
- Format: UTF-8 string, read from `.env.local` or environment

**Signature Verification**:
```java
SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
Jwts.parserBuilder()
    .setSigningKey(key)
    .build()
    .parseClaimsJws(token);  // Throws if signature invalid
```

---

## Token Generation

### Java Implementation (Spring Boot with jjwt)

```java
package com.karthik.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtTokenService {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;  // 86400 seconds = 24 hours
    
    public String generateToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration * 1000);
        
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
```

**Configuration** (application.yaml):
```yaml
jwt:
  secret: ${JWT_SECRET}           # Read from environment
  expiration: 86400               # 24 hours in seconds
```

**Environment** (.env.local):
```
JWT_SECRET=a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2
```

### Token Generation Flow

1. **Extract User Data**: After OAuth callback succeeds
   ```
   User {id: 42, email: "user@example.com", role: "USER"}
   ```

2. **Create Claims**:
   ```json
   {
     "sub": "42",
     "email": "user@example.com",
     "role": "USER",
     "iat": 1708782600,
     "exp": 1708869000
   }
   ```

3. **Sign Token**: Using JWT_SECRET with HS256
   ```
   Token = base64url(header) + "." + base64url(payload) + "." + base64url(signature)
   ```

4. **Return to Client**:
   - Via redirect query param: `?token=eyJhbGc...`
   - Or via Set-Cookie header (production)

---

## Token Validation

### Java Implementation (Custom Filter)

```java
package com.karthik.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {
    
    private final String jwtSecret;
    
    public JwtAuthenticationProvider(@Value("${jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }
    
    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid or expired JWT token", e);
        }
    }
    
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return Long.valueOf(claims.getSubject());
    }
    
    public String getEmailFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("email", String.class);
    }
    
    public String getRoleFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("role", String.class);
    }
}
```

### Validation Checks

| Check | Condition | Error Code | Action |
|-------|-----------|-----------|--------|
| Signature Valid | HMAC signature matches | `INVALID_TOKEN` | Reject |
| Not Expired | `exp` > current time | `EXPIRED_TOKEN` | Reject |
| Well-formed | Base64 decoding succeeds | `INVALID_TOKEN` | Reject |
| Contains Claims | `sub`, `email`, `role` present | `INVALID_TOKEN` | Reject |
| Not Issued in Future | `iat` <= current time | `INVALID_TOKEN` | Reject |

### Validation Flow (JWT Filter)

```
1. Extract token from Authorization: Bearer header
2. Parse token (validates Base64)
3. Verify signature (validates HMAC)
4. Check expiration (iat/exp claims)
5. Extract claims (sub, email, role)
6. Load User entity from userId (sub claim)
7. Continue request with SecurityContext set
```

---

## Token Transmission

### During OAuth Callback

```
GET /api/auth/callback?code=...&state=...
→ Backend validates and generates JWT
→ Backend redirects to: http://localhost:3000/dashboard?token=eyJhbGc...
→ Frontend extracts token from query string
→ Frontend stores in localStorage: localStorage.setItem('token', token)
```

### For Subsequent API Calls

**Frontend Code**:
```javascript
// src/services/api.js
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
});

// Add JWT to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
```

**Backend Receives**:
```http
GET /api/auth/profile HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI0MiIsImVtYWlsIjoidXNlckBleGFtcGxlLmNvbSIsInJvbGUiOiJVU0VSIiwiaWF0IjoxNzA4NzgyNjAwLCJleHAiOjE3MDg4NjkwMDB9.signature...
```

---

## Example Tokens

### Development Token (decoded)

**Header**:
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload**:
```json
{
  "sub": "1",
  "email": "admin@example.com",
  "role": "ADMIN",
  "iat": 1708782600,
  "exp": 1708869000
}
```

**Full Token**:
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJhZG1pbkBleGFtcGxlLmNvbSIsInJvbGUiOiJBRE1JTiIsImlhdCI6MTcwODc4MjYwMCwiZXhwIjoxNzA4ODY5MDAwfQ.SIGNATURE
```

### Test Token (user role)

**Payload**:
```json
{
  "sub": "2",
  "email": "user@example.com",
  "role": "USER",
  "iat": 1708782600,
  "exp": 1708869000
}
```

### Expired Token

**Payload**:
```json
{
  "sub": "1",
  "email": "admin@example.com",
  "role": "ADMIN",
  "iat": 1708696200,
  "exp": 1708782600              # Expired (before current time)
}
```

---

## Security Considerations

### Secret Management

- **Do NOT** commit JWT_SECRET to git (use .gitignore)
- **Do** store in .env.local for local development
- **Do** use environment variables in production (Render.com secrets)
- **Minimum entropy**: 256 bits (32 bytes) for HS256

### Token Lifespan

- **Expiration**: 24 hours (balances security with UX)
- **No refresh**: Users re-authenticate after 24 hours (Phase 1)
- **No sliding window**: No automatic extension on activity

### XSS Prevention (Frontend)

- Store token in localStorage (vulnerable to XSS but better than sessionStorage/cookies for CSRF)
- Never store in DOM attributes
- Sanitize all user inputs

### CSRF Prevention (Backend)

- State parameter validated on OAuth callback
- CORS configured to allow only known origins
- SameSite cookies set to Strict (if using Set-Cookie)

### Token Revocation (Future)

- Phase 1: No server-side token tracking
- Phase 7: Implement token blacklist or JWT jti (JWT ID) claims

---

## Testing

### Manual Token Testing

1. **Generate Token via Oauth Flow**:
   - `/api/auth/login` → Get redirectUrl
   - Authenticate with Google
   - `/api/auth/callback` → Receive token

2. **Decode Token (jwt.io)**:
   - Paste token at https://jwt.io
   - Verify payload matches expectations
   - Verify signature valid (requires secret)

3. **Test Expiration**:
   - Use token immediately (should work)
   - Wait 24+ hours, retry (should fail with EXPIRED_TOKEN)

4. **Test Invalid Token**:
   - Modify token payload or signature
   - Call `/api/auth/profile`
   - Should receive INVALID_TOKEN error

---

## Summary

**JWT Token Contract**:
- ✅ Standard HS256 algorithm for security
- ✅ 24-hour expiration balancing UX and security
- ✅ Contains user ID, email, and role for quick claim extraction
- ✅ Signature validates against JWT_SECRET
- ✅ Clear error codes for validation failures
- ✅ Transmission via Authorization header or query parameter

**Ready for backend JWT service implementation** and **frontend token storage/transmission**.
