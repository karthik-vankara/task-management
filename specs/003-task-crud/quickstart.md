# Phase 3: Task CRUD Operations - Quickstart Guide

---

## Overview

Implement complete task management CRUD operations in Phase 3. Estimated time: **6-7 days**.

### Architecture
```
Controller (TaskController)
    ↓
Service (TaskService)
    ↓
Repository (TaskRepository with Specifications)
    ↓
Database (tasks table + 6 indexes)
```

---

## Step 1: Add Maven Dependencies

Add to `pom.xml`:
```xml
<!-- QueryDSL for dynamic queries -->
<dependency>
    <groupId>com.querydsl</groupId>
    <artifactId>querydsl-jpa</artifactId>
    <version>5.0.0</version>
</dependency>

<!-- Lombok for boilerplate -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.30</version>
</dependency>
```

Run: `mvn clean install -DskipTests`

---

## Step 2: Create Task Entity

File: `src/main/java/com/karthik/task_management_backend/entity/Task.java`

```java
import javax.persistence.*;
import javax.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 255, nullable = false)
    @NotBlank
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;
    
    @Column(length = 100)
    private String category;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "assigned_to_id")
    private Long assignedToId;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = TaskStatus.OPEN;
        }
        if (this.priority == null) {
            this.priority = Priority.MEDIUM;
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.status == TaskStatus.COMPLETED && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }
}

public enum TaskStatus {
    OPEN, IN_PROGRESS, COMPLETED
}

public enum Priority {
    LOW, MEDIUM, HIGH, URGENT
}
```

---

## Step 3: Create Task DTOs

File: `src/main/java/com/karthik/task_management_backend/dto/TaskDTO.java`

```java
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String category;
    private LocalDateTime dueDate;
    private Long userId;
    private Long assignedToId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
}
```

File: `src/main/java/com/karthik/task_management_backend/dto/TaskCreateDTO.java`

```java
import lombok.Data;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
public class TaskCreateDTO {
    @NotBlank(message = "Title required")
    @Size(min = 1, max = 255)
    private String title;
    
    @Size(max = 5000)
    private String description;
    
    @Pattern(regexp = "LOW|MEDIUM|HIGH|URGENT", message = "Invalid priority")
    private String priority;
    
    private String category;
    
    @FutureOrPresent
    private LocalDateTime dueDate;
}
```

File: `src/main/java/com/karthik/task_management_backend/dto/TaskUpdateDTO.java`

```java
import lombok.Data;
import javax.validation.constraints.*;

@Data
public class TaskUpdateDTO {
    @Size(min = 1, max = 255)
    private String title;
    
    @Size(max = 5000)
    private String description;
    
    @Pattern(regexp = "LOW|MEDIUM|HIGH|URGENT")
    private String priority;
    
    private String category;
    
    private LocalDateTime dueDate;
}
```

File: `src/main/java/com/karthik/task_management_backend/dto/TaskStatusUpdateDTO.java`

```java
import lombok.Data;
import javax.validation.constraints.*;

@Data
public class TaskStatusUpdateDTO {
    @NotNull
    @Pattern(regexp = "OPEN|IN_PROGRESS|COMPLETED")
    private String status;
}
```

---

## Step 4: Create TaskRepository

File: `src/main/java/com/karthik/task_management_backend/repository/TaskRepository.java`

```java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, 
                                        JpaSpecificationExecutor<Task> {
    List<Task> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Task> findByAssignedToIdOrderByCreatedAtDesc(Long assignedToId);
    List<Task> findByUserIdAndStatus(Long userId, TaskStatus status);
    Optional<Task> findByIdAndUserId(Long id, Long userId);
}
```

---

## Step 5: Create Task Specifications

File: `src/main/java/com/karthik/task_management_backend/repository/TaskSpecifications.java`

```java
import org.springframework.data.jpa.domain.Specification;
import javax.persistence.criteria.*;

public class TaskSpecifications {
    public static Specification<Task> byUserId(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }
    
    public static Specification<Task> byStatus(TaskStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
    
    public static Specification<Task> byPriority(Priority priority) {
        return (root, query, cb) -> cb.equal(root.get("priority"), priority);
    }
    
    public static Specification<Task> byCategory(String category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }
    
    public static Specification<Task> dueDateBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> cb.between(root.get("dueDate"), from, to);
    }
}
```

---

## Step 6: Create TaskService

File: `src/main/java/com/karthik/task_management_backend/service/TaskService.java`

```java
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import java.time.LocalDateTime;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    
    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }
    
    @Transactional
    public TaskDTO createTask(Long userId, TaskCreateDTO createDTO) {
        Task task = new Task();
        task.setTitle(createDTO.getTitle());
        task.setDescription(createDTO.getDescription());
        task.setPriority(Priority.valueOf(createDTO.getPriority() != null ? 
            createDTO.getPriority() : "MEDIUM"));
        task.setCategory(createDTO.getCategory());
        task.setDueDate(createDTO.getDueDate());
        task.setUserId(userId);
        task.setStatus(TaskStatus.OPEN);
        
        Task saved = taskRepository.save(task);
        return convertToDTO(saved);
    }
    
    public Page<TaskDTO> listTasks(Long userId, TaskSearchCriteria criteria, 
                                   Pageable pageable) {
        Specification<Task> spec = Specification.where(
            TaskSpecifications.byUserId(userId));
        
        if (criteria.getStatus() != null) {
            spec = spec.and(TaskSpecifications.byStatus(
                TaskStatus.valueOf(criteria.getStatus())));
        }
        
        if (criteria.getPriority() != null) {
            spec = spec.and(TaskSpecifications.byPriority(
                Priority.valueOf(criteria.getPriority())));
        }
        
        if (criteria.getCategory() != null) {
            spec = spec.and(TaskSpecifications.byCategory(criteria.getCategory()));
        }
        
        if (criteria.getDueDateFrom() != null && criteria.getDueDateTo() != null) {
            spec = spec.and(TaskSpecifications.dueDateBetween(
                criteria.getDueDateFrom(), criteria.getDueDateTo()));
        }
        
        Page<Task> tasks = taskRepository.findAll(spec, pageable);
        return tasks.map(this::convertToDTO);
    }
    
    public TaskDTO getTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        
        if (!task.getUserId().equals(userId) && 
            !task.getAssignedToId().equals(userId)) {
            throw new RuntimeException("Forbidden");
        }
        
        return convertToDTO(task);
    }
    
    @Transactional
    public TaskDTO updateTask(Long taskId, Long userId, TaskUpdateDTO updateDTO) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        
        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("Only creator can update");
        }
        
        if (updateDTO.getTitle() != null) {
            task.setTitle(updateDTO.getTitle());
        }
        if (updateDTO.getDescription() != null) {
            task.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getPriority() != null) {
            task.setPriority(Priority.valueOf(updateDTO.getPriority()));
        }
        if (updateDTO.getCategory() != null) {
            task.setCategory(updateDTO.getCategory());
        }
        if (updateDTO.getDueDate() != null) {
            task.setDueDate(updateDTO.getDueDate());
        }
        
        Task updated = taskRepository.save(task);
        return convertToDTO(updated);
    }
    
    @Transactional
    public TaskDTO updateStatus(Long taskId, Long userId, String newStatus) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        
        if (!task.getUserId().equals(userId) && 
            !task.getAssignedToId().equals(userId)) {
            throw new RuntimeException("Forbidden");
        }
        
        TaskStatus targetStatus = TaskStatus.valueOf(newStatus);
        
        // Validate transition
        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new RuntimeException("Cannot transition from COMPLETED");
        }
        
        task.setStatus(targetStatus);
        if (targetStatus == TaskStatus.COMPLETED) {
            task.setCompletedAt(LocalDateTime.now());
        }
        
        Task updated = taskRepository.save(task);
        return convertToDTO(updated);
    }
    
    @Transactional
    public void deleteTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        
        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("Only creator can delete");
        }
        
        taskRepository.delete(task);
    }
    
    @Transactional
    public TaskDTO assignTask(Long taskId, Long userId, Long assignedToId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        
        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("Only creator can assign");
        }
        
        if (assignedToId != null) {
            userRepository.findById(assignedToId)
                .orElseThrow(() -> new RuntimeException("Assignee not found"));
        }
        
        task.setAssignedToId(assignedToId);
        Task updated = taskRepository.save(task);
        return convertToDTO(updated);
    }
    
    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus().toString());
        dto.setPriority(task.getPriority().toString());
        dto.setCategory(task.getCategory());
        dto.setDueDate(task.getDueDate());
        dto.setUserId(task.getUserId());
        dto.setAssignedToId(task.getAssignedToId());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        dto.setCompletedAt(task.getCompletedAt());
        return dto;
    }
}
```

---

## Step 7: Create TaskController

File: `src/main/java/com/karthik/task_management_backend/controller/TaskController.java`

```java
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "http://localhost:3000")
public class TaskController {
    private final TaskService taskService;
    
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }
    
    @PostMapping
    public ApiResponse<TaskDTO> createTask(
            @Valid @RequestBody TaskCreateDTO createDTO,
            Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        return ApiResponse.success(taskService.createTask(userId, createDTO));
    }
    
    @GetMapping
    public ApiResponse<Page<TaskDTO>> listTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        TaskSearchCriteria criteria = new TaskSearchCriteria();
        criteria.setStatus(status);
        criteria.setPriority(priority);
        criteria.setCategory(category);
        
        if (size > 100) size = 100;
        Page<TaskDTO> tasks = taskService.listTasks(userId, criteria,
            PageRequest.of(page, size));
        return ApiResponse.success(tasks);
    }
    
    @GetMapping("/{taskId}")
    public ApiResponse<TaskDTO> getTask(@PathVariable Long taskId, 
                                        Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        return ApiResponse.success(taskService.getTask(taskId, userId));
    }
    
    @PutMapping("/{taskId}")
    public ApiResponse<TaskDTO> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskUpdateDTO updateDTO,
            Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        return ApiResponse.success(taskService.updateTask(taskId, userId, updateDTO));
    }
    
    @PutMapping("/{taskId}/status")
    public ApiResponse<TaskDTO> updateStatus(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskStatusUpdateDTO statusDTO,
            Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        return ApiResponse.success(
            taskService.updateStatus(taskId, userId, statusDTO.getStatus()));
    }
    
    @DeleteMapping("/{taskId}")
    public void deleteTask(@PathVariable Long taskId, Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        taskService.deleteTask(taskId, userId);
    }
    
    @PutMapping("/{taskId}/assign")
    public ApiResponse<TaskDTO> assignTask(
            @PathVariable Long taskId,
            @RequestBody TaskAssignDTO assignDTO,
            Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        return ApiResponse.success(
            taskService.assignTask(taskId, userId, assignDTO.getAssignedToId()));
    }
    
    private Long getUserIdFromAuth(Authentication auth) {
        return ((JwtAuthenticationToken) auth).getUserId();
    }
}
```

---

## Step 8: Create Database Migration

File: `src/main/resources/db/migration/V3__create_tasks.sql` (when using Flyway in Phase 8)

```sql
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    category VARCHAR(100),
    due_date TIMESTAMP,
    user_id BIGINT NOT NULL REFERENCES users(id),
    assigned_to_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP
);

CREATE INDEX idx_tasks_user_id ON tasks(user_id);
CREATE INDEX idx_tasks_assigned_to_id ON tasks(assigned_to_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_priority ON tasks(priority);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_created_at_desc ON tasks(created_at DESC);
```

---

## Step 9: Manual Testing Checklist

- [ ] POST /api/tasks with valid data → Returns 201 with task
- [ ] POST /api/tasks with empty title → Returns 400 validation error
- [ ] GET /api/tasks → Returns paginated list of current user's tasks
- [ ] GET /api/tasks?status=OPEN → Filters by status
- [ ] GET /api/tasks?priority=HIGH → Filters by priority
- [ ] GET /api/tasks?status=OPEN&priority=HIGH → Combined filters
- [ ] GET /api/tasks/{id} (as creator) → Returns task details
- [ ] GET /api/tasks/{id} (as non-creator) → Returns 403 Forbidden
- [ ] GET /api/tasks/999 → Returns 404 Not Found
- [ ] PUT /api/tasks/{id} (as creator) → Updates task
- [ ] PUT /api/tasks/{id} (as assignee) → Returns 403 Forbidden
- [ ] PUT /api/tasks/{id}/status (OPEN → IN_PROGRESS) → Status changes
- [ ] PUT /api/tasks/{id}/status (IN_PROGRESS → COMPLETED) → Sets completedAt
- [ ] PUT /api/tasks/{id}/status (COMPLETED → IN_PROGRESS) → Returns 400 (invalid transition)
- [ ] DELETE /api/tasks/{id} (as creator) → Task deleted
- [ ] DELETE /api/tasks/{id} (as assignee) → Returns 403 Forbidden
- [ ] PUT /api/tasks/{id}/assign → Assigns task to user
- [ ] PUT /api/tasks/{id}/assign with invalid userId → Returns 404

---

## Success Criteria

✅ Phase 3 Implementation Complete:
- Full task CRUD working
- Status state machine enforced
- Authorization rules implemented
- 6 indexes created (performance < 300ms queries)
- Filtering and pagination working
- All manual tests passing
- Ready for Phase 4 (advanced features)
