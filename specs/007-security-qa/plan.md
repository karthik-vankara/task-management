# Implementation Plan: Security, Testing & Quality

**Branch**: `007-security-qa` | **Phase Duration**: 5-6 days  
**Depends On**: All previous phases (1-6)

---

## Summary

Harden security: audit logging, API rate limiting, input validation, SQL injection prevention, and comprehensive security review. Add security headers, HTTPS enforcement, and data protection measures.

---

## Technical Context

- **Language**: Java 21, Spring Boot 3.3.0
- **Security**: Spring Security, Spring Actuator
- **Database**: PostgreSQL 15+ with audit trail
- **Performance**: Rate limiting max 100 req/s per user

---

## Constitution Check: ✅ PASS

---

## Key Features

### 1. Audit Logging
- Track all data mutations: CREATE, UPDATE, DELETE
- Endpoint: `GET /api/audit/logs` (admin only)
- Fields: userId, action, entity, oldValue, newValue, timestamp
- Retention: 1 year minimum

### 2. API Rate Limiting
- Per-user limits:
  - /api/auth/* → 10 req/min
  - /api/tasks → 100 req/min
  - /api/users → 50 req/min
  - Search → 20 req/min
- Return 429 (Too Many Requests) when exceeded
- Use Spring RateLimiter or Bucket4j

### 3. Input Validation & Sanitization
- Validate all request fields (length, type, format)
- Sanitize HTML/JS in text fields (remove <script> tags)
- Use JSR-303 validators + custom validators
- Prevent SQL injection (use parameterized queries)

### 4. Security Headers
- HSTS: Strict-Transport-Security
- CSP: Content-Security-Policy
- X-Content-Type-Options: nosniff
- X-Frame-Options: DENY
- X-XSS-Protection: 1; mode=block

### 5. JWT Security Hardening
- Validate JWT signature on every request
- Check expiration
- Validate claims
- Implement token refresh (optional)

---

## Database Changes

```sql
-- Audit logging table
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    action VARCHAR(50), -- CREATE, UPDATE, DELETE
    entity_type VARCHAR(100), -- TASK, USER, COMMENT
    entity_id BIGINT,
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Create retention policy
CREATE INDEX idx_audit_user_action ON audit_logs(user_id, action);
CREATE INDEX idx_audit_created ON audit_logs(created_at DESC);
```

---

## Implementation Roadmap

**Days 1-2: Audit Logging (8-10 hours)**
- Create AuditLog entity and repository
- Implement audit aspect/interceptor
- Log all mutations automatically
- Create audit endpoint (admin only)

**Days 3: Rate Limiting (6-8 hours)**
- Add Bucket4j dependency
- Configure rate limits per endpoint
- Implement rate limiter interceptor
- Return proper 429 responses

**Days 4: Input Validation & Sanitization (6-8 hours)**
- Add custom validators for all entities
- Implement HTML sanitization (jsoup library)
- Add exception handler for validation errors
- Verify SQL injection protection

**Days 5-6: Security Review & Testing (8-10 hours)**
- Add security headers globally
- Review JWT validation
- Manual security testing:
  - Test rate limits
  - Test SQL injection attempts
  - Test XSS attempts
  - Test CSRF (if applicable)
  - Verify audit logs created

---

## Testing Checklist

- [ ] Audit log created on task creation
- [ ] Audit log created on task update
- [ ] Audit log tracks old/new values
- [ ] Get audit logs endpoint (admin only)
- [ ] Rate limit: /api/auth/* (10/min)
- [ ] Rate limit: /api/tasks (100/min)
- [ ] Rate limit returning 429 when exceeded
- [ ] Input validation: empty title rejected
- [ ] Input validation: too long description rejected
- [ ] HTML sanitization: <script> tags removed
- [ ] SQL injection attempt blocked
- [ ] JWT signature validation working
- [ ] JWT expiration validation working
- [ ] Security headers present in response
- [ ] HSTS header present
- [ ] CSP header present

---

## Success Criteria

✅ Phase 7 Complete:
- Audit logging system operational
- Rate limiting protecting API
- Input validation preventing malicious input
- HTML sanitization protecting from XSS
- Security headers hardening responses
- JWT validation comprehensive
- Manual security tests all passing
- No known vulnerabilities identified
