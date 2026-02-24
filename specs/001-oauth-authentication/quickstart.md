# Phase 1: Quickstart Guide

**Date**: 2026-02-24  
**Target Audience**: Backend developers implementing OAuth2 authentication  
**Duration**: 5-7 days from start to production-ready merge

---

## Quick Overview

Phase 1 implements Google OAuth2 authentication for the Task Management System backend. Users authenticate via Google, receive JWT tokens, and access protected endpoints.

**Key Deliverables**:
- ✅ User entity with role-based access control
- ✅ Google OAuth2 configuration (Spring Security)
- ✅ 4 authentication endpoints (/login, /callback, /profile, /logout)
- ✅ JWT token service (generation + validation)
- ✅ Security headers and CORS configuration
- ✅ Manual test plan completion

**Git Branch**: `001-oauth-authentication`

---

## Prerequisites

Before starting implementation:

- [ ] Git branch created and checked out: `git checkout -b 001-oauth-authentication`
- [ ] Backend project structure reviewed (see plan.md)
- [ ] PostgreSQL database running locally via Docker Compose
- [ ] Google OAuth credentials created in Google Cloud Console
- [ ] Environment variables set in `.env.local` (GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, JWT_SECRET)
- [ ] Dependencies added to pom.xml (see Dependencies section below)

---

## Step 1: Dependencies

Add to `backend/pom.xml`:

```xml
<!-- Spring Boot 3.3.0 dependencies (core) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- OAuth2 Client for Google authentication -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-client</artifactId>
</dependency>

<!-- OAuth2 Resource Server for JWT validation -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-resource-server</artifactId>
</dependency>

<!-- JWT library for token creation/validation -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>

<!-- Lombok for reducing boilerplate -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
</dependency>

<!-- Jakarta Validation API -->
<dependency>
    <groupId>jakarta.validation</groupId>
    <artifactId>jakarta.validation-api</artifactId>
</dependency>
```

**Verify**: `mvn clean install` should complete without errors.

---

## Step 2: Entity & Repository

### File: `src/main/java/com/karthik/entity/User.java`

```java
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_google_id", columnList = "google_id", unique = true),
    @Index(name = "idx_users_email", columnList = "email", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 255)
    private String googleId;
    
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;
    
    @Column(nullable = true, length = 2048)
    private String avatarUrl;
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String bio;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

### File: `src/main/java/com/karthik/entity/UserRole.java`

```java
public enum UserRole {
    ADMIN("Admin user can manage all resources"),
    USER("Regular user can manage own resources");
    
    private final String description;
    UserRole(String description) { this.description = description; }
    public String getDescription() { return description; }
}
```

### File: `src/main/java/com/karthik/repository/UserRepository.java`

```java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByGoogleId(String googleId);
    Optional<User> findByEmail(String email);
}
```

**Verify**: Repository compiles without errors.

---

## Step 3: DTOs

### File: `src/main/java/com/karthik/dto/UserDTO.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String email;
    private String name;
    private UserRole role;
    private String avatarUrl;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### File: `src/main/java/com/karthik/dto/LoginResponse.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String redirectUrl;
}
```

### File: `src/main/java/com/karthik/dto/ApiResponse.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private Boolean success;
    private T data;
    private ErrorInfo error;
    private String timestamp;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ErrorInfo {
    private String code;
    private String message;
    private Object details;
}
```

---

## Step 4: Configuration

### File: `src/main/resources/application.yaml`

```yaml
spring:
  application:
    name: task-management-backend
  
  datasource:
    url: jdbc:postgresql://localhost:5432/taskdb
    username: taskuser
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
  
  jpa:
    hibernate:
      ddl-auto: update  # Change to 'validate' in production
    database-platform: org.hibernate.dialect.PostgreSQL15Dialect
    show-sql: false
  
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            redirect-uri: http://localhost:8080/api/auth/callback
            scope: openid,email,profile

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400  # 24 hours in seconds

cors:
  allowed-origins: http://localhost:3000
```

### File: `src/main/java/com/karthik/config/SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .cors();
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
    
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
}
```

---

## Step 5: JWT Service

### File: `src/main/java/com/karthik/service/JwtTokenService.java`

```java
@Service
public class JwtTokenService {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration * 1000);
        
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("role", user.getRole().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.valueOf(claims.getSubject());
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
```

---

## Step 6: Auth Service

### File: `src/main/java/com/karthik/service/AuthService.java`

```java
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final OAuth2AuthorizedClientService oAuth2ClientService;
    
    public String generateOAuthLoginUrl() {
        String state = generateRandomState();
        // Store state in cache (Phase 1: simple approach)
        // For production: use Redis or encrypted JWT
        return buildGoogleOAuthUrl(state);
    }
    
    public String handleOAuthCallback(String code, String state) {
        // Validate state token
        // Exchange code for Google access token
        // Fetch user info from Google
        // Create/update User in database
        // Generate JWT token
        
        OAuth2User oAuth2User = exchangeCodeForUser(code);
        User user = createOrUpdateUser(oAuth2User);
        String jwtToken = jwtTokenService.generateToken(user);
        
        return jwtToken;
    }
    
    private String generateRandomState() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    private User createOrUpdateUser(OAuth2User oAuth2User) {
        String googleId = oAuth2User.getName();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");
        
        User user = userRepository.findByGoogleId(googleId)
                .orElse(User.builder()
                        .googleId(googleId)
                        .email(email)
                        .role(UserRole.USER)
                        .build());
        
        user.setName(name);
        user.setAvatarUrl(picture);
        
        return userRepository.save(user);
    }
    
    private OAuth2User exchangeCodeForUser(String code) {
        // Implementation depends on Spring Security OAuth2 setup
        // This is a simplified placeholder
        throw new NotImplementedException();
    }
}
```

---

## Step 7: Controller

### File: `src/main/java/com/karthik/controller/AuthController.java`

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login() {
        String redirectUrl = authService.generateOAuthLoginUrl();
        return ApiResponse.<LoginResponse>builder()
                .success(true)
                .data(LoginResponse.builder().redirectUrl(redirectUrl).build())
                .timestamp(Instant.now().toString())
                .build();
    }
    
    @GetMapping("/callback")
    public void callback(@RequestParam String code, @RequestParam String state, 
                        HttpServletResponse response) throws IOException {
        String token = authService.handleOAuthCallback(code, state);
        String redirectUrl = "http://localhost:3000/dashboard?token=" + token;
        response.sendRedirect(redirectUrl);
    }
    
    @GetMapping("/profile")
    public ApiResponse<UserDTO> getProfile(@RequestHeader("Authorization") String auth) {
        String token = auth.replace("Bearer ", "");
        Long userId = jwtTokenService.getUserIdFromToken(token);
        User user = userRepository.findById(userId).orElseThrow();
        UserDTO userDTO = convertToDTO(user);
        
        return ApiResponse.<UserDTO>builder()
                .success(true)
                .data(userDTO)
                .timestamp(Instant.now().toString())
                .build();
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }
    
    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
```

---

## Step 8: Environment Configuration

### File: `.env.local`

```env
# Google OAuth Credentials (from Google Cloud Console)
GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-client-secret

# JWT Configuration
JWT_SECRET=your-256-bit-secret-min-32-bytes-base64-encoded

# Database Configuration
DB_PASSWORD=taskuser_password
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/taskdb
SPRING_DATASOURCE_USERNAME=taskuser
```

**Important**: Add `.env.local` to `.gitignore` (already done in earlier phase).

---

## Step 9: Testing Checklist

### Manual Testing

- [ ] **Login Flow**: `/api/auth/login` returns valid Google OAuth URL
- [ ] **OAuth Redirect**: Browser redirects to Google login
- [ ] **OAuth Grant**: User grants permissions in Google login
- [ ] **Callback**: Backend receives callback with code/state
- [ ] **User Created**: New User created in PostgreSQL
- [ ] **Token Generated**: JWT token returned to frontend
- [ ] **Profile Retrieval**: `/api/auth/profile` returns user data
- [ ] **Token Validation**: Invalid tokens rejected with 401
- [ ] **Role in JWT**: Token contains role claim
- [ ] **CORS Headers**: Frontend can call backend endpoints
- [ ] **Security Headers**: Response includes security headers
- [ ] **Logout**: `/api/auth/logout` returns 204 No Content

### Database Verification

```sql
-- Check user created
SELECT * FROM users;

-- Verify indexes
SELECT * FROM pg_indexes WHERE tablename = 'users';

-- Check constraints
SELECT constraint_name, constraint_type 
FROM information_schema.table_constraints 
WHERE table_name = 'users';
```

---

## Step 10: Deployment

### Production Checklist

- [ ] `jwt.secret` minimum 256 bits (32 bytes)
- [ ] Update `CORS_ALLOWED_ORIGINS` for production domain
- [ ] Set `HTTPS_ONLY=true` for cookies (if using Set-Cookie)
- [ ] Review OAuth credentials for production Google project
- [ ] Update database connection string
- [ ] Test full flow on staging environment
- [ ] Create git tag: `git tag v0.1.0`
- [ ] Merge to main: `git checkout main && git merge 001-oauth-authentication`
- [ ] Push to production: `git push origin main --tags`

---

## File Summary

**Created Files** (minimum viable set):

| File | Purpose | Lines | Status |
|------|---------|-------|--------|
| User.java | JPA entity | ~60 | Create |
| UserRole.java | Enum for roles | ~10 | Create |
| UserRepository.java | Data access | ~10 | Create |
| UserDTO.java | API response | ~15 | Create |
| LoginResponse.java | Login response | ~10 | Create |
| ApiResponse.java | Standard response | ~20 | Create |
| SecurityConfig.java | Spring Security config | ~40 | Create |
| JwtTokenService.java | JWT utils | ~50 | Create |
| AuthService.java | Business logic | ~60 | Create |
| AuthController.java | REST endpoints | ~80 | Create |
| application.yaml | Configuration | ~40 | Update |
| .env.local | Secrets | ~10 | Update |
| pom.xml | Dependencies | ~30 | Update |

**Estimated LOC**: ~400 lines (excluding tests)

**Time Estimate**: 5-7 days for experienced Spring Boot developer

---

## Common Issues & Fixes

### Issue: CORS error on callback
**Fix**: Update `CORS_ALLOWED_ORIGINS` in SecurityConfig for frontend domain

### Issue: JWT validation fails
**Fix**: Ensure `JWT_SECRET` is >= 32 bytes and matches between generation and validation

### Issue: User not found after OAuth
**Fix**: Check PostgreSQL connection and userRepository.save() is returning persisted entity

### Issue: Google OAuth returns error_access_denied
**Fix**: Add correct Google project credentials and verify redirect URI in Google Cloud Console

---

## Next Steps

After Phase 1 Merge:

1. **Phase 2**: Implement User Profile endpoints (CRUD for bio, name, etc.)
2. **Phase 3**: Implement Task CRUD endpoints (protected by Phase 1 auth)
3. **Phase 4+**: Advanced features (search, filters, collaboration)

---

## References

- [Spring Security OAuth2 Docs](https://spring.io/guides/tutorials/spring-boot-oauth2/)
- [jjwt GitHub](https://github.com/jwtk/jjwt)
- [JWT.io](https://jwt.io/) - Token debugging
- [Google OAuth 2.0](https://developers.google.com/identity/protocols/oauth2)
- Constitution: `.specify/memory/constitution.md`
- API Contract: `contracts/auth-api.md`
- JWT Payload: `contracts/jwt-payload.md`

---

## Support

For questions during implementation:
1. Refer to spec.md for requirements
2. Check contracts/ for API details
3. Review data-model.md for schema
4. Follow constitution.md for coding standards

**Status**: Ready for implementation ✅
