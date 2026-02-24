# Phase 0: Research & Clarifications

**Status**: ✅ COMPLETE  
**Date**: 2026-02-24  
**Research Scope**: Resolved all clarifications and best practices for OAuth2 authentication

---

## Clarifications Summary

All critical ambiguities have been resolved via `/speckit.clarify` command on 2026-02-24:

### Decision 1: JWT Token Expiration
- **Question**: Is 1 hour acceptable, or should it be different?
- **Decision**: **24 hours** (longer sessions acceptable for MVP)
- **Rationale**: 24-hour tokens reduce friction while providing security-conscious approach. Users re-authenticate daily, balancing UX and security. Industry standard for web apps.
- **Implementation Impact**: `exp` claim set to current time + 86400 seconds

### Decision 2: User Roles & Permission Model
- **Question**: What user roles/personas should the system support?
- **Decision**: **Two-tier model: ADMIN and USER**
  - ADMIN: Can manage all tasks and users (future phases)
  - USER: Can manage own tasks only (default role)
- **Rationale**: Necessary for scalability to 10k users. Supports future team/multi-tenant features. Role stored in database and JWT payload.
- **Implementation Impact**: UserRole enum, role field in User entity, role claim in JWT, service layer authorization checks

### Decision 3: Out-of-Scope Features
- **Question**: Which authentication features should be explicitly out-of-scope?
- **Decision**: **Exclude for Phase 1**:
  - Social login with other providers (GitHub, Facebook)
  - Password-based backup authentication
  - Multi-factor authentication (MFA)
  - Email verification
  - Account recovery/reset
- **Rationale**: MVP focus on Google OAuth only. Other features in future phases. Reduces Phase 1 scope to 5-7 days delivery.
- **Implementation Impact**: No conditional code paths for alternatives; raises clear error if non-Google auth attempted

### Decision 4: Scalability Targets
- **Question**: What are the expected scalability targets for user load?
- **Decision**: **1000 concurrent users, 10,000 total registered users**
- **Rationale**: Supports small-to-medium team/organization. Achievable on free-tier infrastructure without optimization.
- **Implementation Impact**: Database indexes on google_id, email. Connection pooling tuning. JWT validation performance <100ms.

---

## Technical Research: Best Practices

### OAuth2 with Spring Boot 3 + Google

**Finding**: Spring Boot's built-in OAuth2 client auto-configuration is production-ready.

- **Spring Security 6** provides `OAuth2LoginConfigurer` and `OAuth2ResourceServerConfigurer`
- Google provider is pre-configured in Spring Security (auto-discovery works)
- No need for manual Authorization header parsing or state storage in most cases
- Can use `@EnableWebSecurity` + `@Configuration` for a typical setup

**Decision**: Use Spring Security auto-configuration for OAuth2 client registration. Custom code needed only for:
- JWT generation instead of default session storage
- State parameter persistent storage (if needed for CSRF protection)
- Custom claims in JWT (role, email)

### JWT Library Selection: jjwt

**Finding**: `io.jsonwebtoken:jjwt` (v0.12+) is industry-standard for Java JWT handling.

- Supports HS256 algorithm natively
- Handles token signing, verification, claims extraction
- Integrates cleanly with Spring Security
- Active maintenance and security updates

**Decision**: Use jjwt v0.12+ for JWT creation and validation. Configuration via application.yaml for:
```yaml
jwt:
  secret: ${JWT_SECRET}  # Min 256 bits (32 bytes base64-encoded)
  expiration: 86400      # 24 hours in seconds
```

### PostgreSQL Schema Design

**Finding**: PostgreSQL with proper indexing handles 10k users efficiently.

- B-tree indexes on unique columns (google_id, email) enable <50ms user lookups
- ACID guarantees ensure no race conditions during user creation
- Managed service handles replication and backups

**Decision**: Create indexes:
```sql
CREATE UNIQUE INDEX idx_users_google_id ON users(google_id);
CREATE UNIQUE INDEX idx_users_email ON users(email);
```

### CORS Configuration

**Finding**: Spring Security 6 recommends setting CORS before authentication.

- Configure `CorsConfigurationSource` bean
- Allow frontend origin (http://localhost:3000 for dev, https://yourdomain.vercel.app for prod)
- Allow credentials: true for JWT in Authorization header
- Preflight requests automatically handled

**Decision**: Create `CorsConfig` class with `@Configuration` bean returning `CorsConfigurationSource`.

### Security Headers

**Finding**: OWASP recommends headers to prevent XSS, clickjacking, MIME sniffing.

- `X-Content-Type-Options: nosniff` - Prevent MIME type sniffing
- `X-Frame-Options: DENY` - Prevent clickjacking
- `X-XSS-Protection: 1; mode=block` - Legacy XSS protection
- `Strict-Transport-Security: max-age=31536000; includeSubDomains` - HSTS for production

**Decision**: Configure headers in Spring Security filter chain via `SecurityFilterChain` bean.

---

## Technology Validation

| Technology | Decision | Confidence | Notes |
|------------|----------|-----------|-------|
| Spring Boot 3.3 | ✅ Confirmed | 100% | Latest LTS, OAuth2 first-class support |
| Java 21 | ✅ Confirmed | 100% | Latest LTS, available on Render.com |
| PostgreSQL 15 | ✅ Confirmed | 100% | Enterprise-grade, free managed tier available |
| Spring Security 6 | ✅ Confirmed | 100% | OAuth2 and JWT support built-in |
| jjwt v0.12+ | ✅ Confirmed | 100% | Industry standard, actively maintained |
| Google OAuth2 | ✅ Confirmed | 100% | Free tier, stable API |
| Render.com | ✅ Confirmed | 95% | Free tier verified for Java/PostgreSQL |
| 24-hour tokens | ✅ Confirmed | 95% | Acceptable security/UX tradeoff |
| Role-based access | ✅ Confirmed | 100% | Industry standard, enables future scaling |

---

## No Blockers Identified

✅ All technical decisions validated  
✅ All clarifications resolved  
✅ All dependencies available on free tier  
✅ Specification complete and detailed  

**Status**: Ready for Phase 1 design and implementation planning.
