# Phase 2: User Profile Management - Research & Analysis

---

## Clarifications Resolved

### 1. Avatar Storage Approach
**Decision**: File system storage with CDN path
**Rationale**: Simple, cost-effective, avoids S3 costs on free tier
**Alternatives Considered**: AWS S3, database BLOB (slow), Base64 encoding (large payloads)
**Implementation**: Store in `uploads/avatars/` directory, serve via static file handler

### 2. Profile Visibility
**Decision**: Public profiles visible to authenticated users, private to anonymous
**Rationale**: Encourages collaboration, requires authentication for discovery
**Alternatives Considered**: Fully public (privacy risk), fully private (limits collaboration)
**Implementation**: GET /users/{id} requires JWT, returns public profile (no email)

### 3. Avatar Upload Constraints
**Decision**: Max 5MB, JPEG/PNG only, resize to 200x200px
**Rationale**: Reasonable for user avatars, prevents abuse, mobile-friendly
**Alternatives Considered**: 20MB (too large), 1MB (too restrictive), no resizing (bandwidth waste)
**Implementation**: Use ImageIO or Thumbnails library for resize, validate MIME type

### 4. Directory Pagination
**Decision**: Cursor-based pagination with 50 users per page
**Rationale**: Efficient for large user bases, prevents offset DOS attacks
**Alternatives Considered**: Offset-based (simpler but less efficient), no limit (memory issues)
**Implementation**: Use lastSeenId parameter, ORDER BY created_at DESC

---

## Technology Validation

| Technology | Purpose | Confidence | Notes |
|------------|---------|-----------|-------|
| Spring Data JPA | Profile CRUD | 100% | Proven in Phase 1 |
| ImageIO | Avatar resize | 95% | Built into Java, production-ready |
| Thumbnails library | Avatar optimization | 95% | net.coobird/thumbnailator, ~200kb JAR |
| Spring File Upload | Multipart handling | 100% | Native Spring support |
| PostgreSQL JSONB | Profile extensions | 95% | Can store custom fields future-proof |

---

## Best Practices Identified

### Profile Update Workflow
1. Validate: Name length (1-255), bio length (0-5000), avatarUrl format
2. Sanitize: Remove HTML/JS from bio
3. Update: Atomic transaction, track updateAt timestamp
4. Cache invalidate: Clear profile cache for user

### Avatar Security
- Validate file upload before processing
- Use random UUID for filename (prevent directory traversal)
- Set immutable content headers (cache for 1 year)
- HTTPS only for production

### Performance Optimization
- Cache profile in Redis (TTL 1 hour)
- Database index on email + googleId (fast lookup)
- Avatar CDN/static file handler (serve without Java)
- Pagination limit: 50 users (no N+1 queries)

---

## No Blockers Identified

✅ All technologies free/open-source  
✅ Avatar storage cloud-provider agnostic  
✅ ImageIO built into Java runtime  
✅ No license concerns  
✅ Integration with Phase 1 auth straightforward
