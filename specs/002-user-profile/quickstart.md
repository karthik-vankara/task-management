# Phase 2: User Profile Management - Quickstart Guide

---

## Overview

This guide will walk you through implementing user profile management APIs in Phase 2. Estimated time: **4-5 days**.

### Architecture
```
Controller (ProfileController)
    ↓
Service (ProfileService)
    ↓
Repository (UserRepository)
    ↓
Database (users table + avatar storage)
```

---

## Step 1: Add Maven Dependencies

Add to `pom.xml`:
```xml
<!-- Avatar processing -->
<dependency>
    <groupId>net.coobird</groupId>
    <artifactId>thumbnailator</artifactId>
    <version>[0.4, 0.5)</version>
</dependency>

<!-- File upload handling -->
<dependency>
    <groupId>commons-io</groupId>
    <artifactId>commons-io</artifactId>
    <version>2.11.0</version>
</dependency>

<!-- MIME type detection -->
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-core</artifactId>
    <version>2.9.1</version>
</dependency>
```

Run: `mvn clean install`

---

## Step 2: Update User Entity

File: `src/main/java/com/karthik/task_management_backend/entity/User.java`

```java
import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    // Phase 1 fields (existing)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String googleId;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Enumerated(EnumType.STRING)
    private UserRole role;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Phase 2 fields (NEW)
    @Column(length = 255)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    @Column(name = "avatar_url")
    private String avatarUrl;
    
    // Constructor
    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
```

---

## Step 3: Create DTOs

File: `src/main/java/com/karthik/task_management_backend/dto/UserProfileDTO.java`

```java
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserProfileDTO {
    private Long id;
    private String email;
    private String name;
    private String bio;
    private String avatarUrl;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

File: `src/main/java/com/karthik/task_management_backend/dto/UserProfileUpdateDTO.java`

```java
import lombok.Data;
import javax.validation.constraints.*;

@Data
public class UserProfileUpdateDTO {
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 1, max = 255, message = "Name must be 1-255 characters")
    private String name;
    
    @Size(max = 5000, message = "Bio must be max 5000 characters")
    private String bio;
}
```

---

## Step 4: Update UserRepository Interface

File: `src/main/java/com/karthik/task_management_backend/repository/UserRepository.java`

```java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByGoogleId(String googleId);
    Optional<User> findByEmail(String email);
    
    // Pagination by createdAt
    Page<User> findByOrderByCreatedAtDesc(Pageable pageable);
}
```

---

## Step 5: Create ProfileService

File: `src/main/java/com/karthik/task_management_backend/service/ProfileService.java`

```java
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import net.coobird.thumbnailator.Thumbnails;
import java.io.File;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ProfileService {
    private final UserRepository userRepository;
    private static final String UPLOAD_DIR = "uploads/avatars/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    
    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
        ensureUploadDirExists();
    }
    
    @Transactional
    public UserProfileDTO updateProfile(Long userId, UserProfileUpdateDTO updateDTO) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setName(sanitize(updateDTO.getName()));
        user.setBio(sanitize(updateDTO.getBio()));
        user.setUpdatedAt(LocalDateTime.now());
        
        User updated = userRepository.save(user);
        return convertToDTO(updated);
    }
    
    @Transactional
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File too large (max 5MB)");
        }
        
        String mimeType = file.getContentType();
        if (!mimeType.equals("image/jpeg") && !mimeType.equals("image/png")) {
            throw new RuntimeException("Invalid file type. Accepted: JPEG, PNG");
        }
        
        try {
            String filename = UUID.randomUUID() + ".jpg";
            String filepath = UPLOAD_DIR + filename;
            
            // Resize to 200x200px
            Thumbnails.of(file.getInputStream())
                .size(200, 200)
                .outputFormat("jpg")
                .toFile(filepath);
            
            // Update user avatar URL
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            user.setAvatarUrl("/uploads/avatars/" + filename);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            return user.getAvatarUrl();
        } catch (Exception e) {
            throw new RuntimeException("Avatar upload failed: " + e.getMessage());
        }
    }
    
    public UserProfileDTO getProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDTO(user);
    }
    
    private String sanitize(String input) {
        if (input == null) return null;
        return input.replaceAll("<script>", "")
                   .replaceAll("</script>", "")
                   .replaceAll("<.*?>", "");
    }
    
    private UserProfileDTO convertToDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setBio(user.getBio());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setRole(user.getRole().toString());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
    
    private void ensureUploadDirExists() {
        Path path = Paths.get(UPLOAD_DIR);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create upload directory");
            }
        }
    }
}
```

---

## Step 6: Create ProfileController

File: `src/main/java/com/karthik/task_management_backend/controller/ProfileController.java`

```java
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class ProfileController {
    private final ProfileService profileService;
    private final UserRepository userRepository;
    
    public ProfileController(ProfileService profileService, UserRepository userRepository) {
        this.profileService = profileService;
        this.userRepository = userRepository;
    }
    
    @GetMapping("/me")
    public ApiResponse<UserProfileDTO> getCurrentUser(Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        return ApiResponse.success(profileService.getProfile(userId));
    }
    
    @PutMapping("/me")
    public ApiResponse<UserProfileDTO> updateProfile(
            @Valid @RequestBody UserProfileUpdateDTO updateDTO,
            Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        return ApiResponse.success(profileService.updateProfile(userId, updateDTO));
    }
    
    @PutMapping("/me/avatar")
    public ApiResponse<String> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        String avatarUrl = profileService.uploadAvatar(userId, file);
        return ApiResponse.success(avatarUrl);
    }
    
    @GetMapping
    public ApiResponse<Page<UserProfileDTO>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication auth) {
        if (size > 100) size = 100;
        Page<User> users = userRepository.findByOrderByCreatedAtDesc(
            PageRequest.of(page, size));
        Page<UserProfileDTO> dtoPage = users.map(u -> convertToDTO(u));
        return ApiResponse.success(dtoPage);
    }
    
    @GetMapping("/{userId}")
    public ApiResponse<UserProfileDTO> getUser(@PathVariable Long userId) {
        return ApiResponse.success(profileService.getProfile(userId));
    }
    
    private Long getUserIdFromAuth(Authentication auth) {
        // Extract userId from JWT claims or authentication context
        return ((JwtAuthenticationToken) auth).getUserId();
    }
    
    private UserProfileDTO convertToDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setBio(user.getBio());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setRole(user.getRole().toString());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
```

---

## Step 7: Update Spring Security Configuration

Add static resource mapping in `application.yaml`:

```yaml
spring:
  web:
    resources:
      static-locations: file:./uploads/, classpath:/static/
      cache:
        period: 31536000
        cachecontrol:
          max-age: 31536000
```

---

## Step 8: Manual Testing Checklist

**Prerequisite**: User must be authenticated (JWT token from Phase 1)

- [ ] GET /api/users/me → Returns current user profile (name/bio may be null)
- [ ] PUT /api/users/me with valid name/bio → Update succeeds
- [ ] PUT /api/users/me with name > 255 chars → Returns 422 validation error
- [ ] PUT /api/users/me/avatar with 5MB JPEG → Upload succeeds, resized to 200x200
- [ ] PUT /api/users/me/avatar with 10MB file → Returns 413 (too large)
- [ ] PUT /api/users/me/avatar with non-image → Returns 400 (invalid type)
- [ ] GET /api/users?page=0&size=50 → Returns paginated user list
- [ ] GET /api/users/{userId} → Returns public profile (no email exposure)
- [ ] Avatar URL accessible at returned path
- [ ] Profile updates reflect in subsequent GET /api/users/me calls

---

## Step 9: Deployment Checklist

- [ ] Code compiles without errors (`mvn clean install`)
- [ ] All manual tests passing
- [ ] Uploads directory created and writable
- [ ] Static resource serving configured
- [ ] No console errors on startup
- [ ] JWT authentication working from Phase 1
- [ ] CORS headers present in responses

---

## Success Criteria

✅ Phase 2 Implementation Complete:
- User profile CRUD fully working
- Avatar upload with resize functional
- User directory listing with pagination
- All 5 endpoints accessible and validated
- Manual testing checklist passing
- Ready for Phase 3 (task assignment will use user lookup)
