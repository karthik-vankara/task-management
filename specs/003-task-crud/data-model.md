# Phase 3: Task CRUD Operations - Data Model

---

## Task Entity

```java
@Entity
@Table(name = "tasks")
@Data
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 255, nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status; // OPEN, IN_PROGRESS, COMPLETED
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority; // LOW, MEDIUM, HIGH, URGENT
    
    @Column(length = 100)
    private String category;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "user_id", nullable = false)
    private Long userId; // Creator (immutable)
    
    @Column(name = "assigned_to_id")
    private Long assignedToId; // Assignee (optional)
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt; // Set when status = COMPLETED
}

public enum TaskStatus {
    OPEN, IN_PROGRESS, COMPLETED
}

public enum Priority {
    LOW, MEDIUM, HIGH, URGENT
}
```

---

## Database Schema

```sql
-- Tasks table
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

-- Enums for PostgreSQL
CREATE TYPE task_status AS ENUM ('OPEN', 'IN_PROGRESS', 'COMPLETED');
CREATE TYPE priority_enum AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'URGENT');

-- Strategic indexes (all 6 considered essential)
CREATE INDEX idx_tasks_user_id ON tasks(user_id);
CREATE INDEX idx_tasks_assigned_to_id ON tasks(assigned_to_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_priority ON tasks(priority);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_created_at_desc ON tasks(created_at DESC);

-- Foreign key constraints
ALTER TABLE tasks ADD CONSTRAINT fk_tasks_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE tasks ADD CONSTRAINT fk_tasks_assignee 
    FOREIGN KEY (assigned_to_id) REFERENCES users(id) ON DELETE SET NULL;

-- Validation constraints
ALTER TABLE tasks ADD CONSTRAINT chk_title_length CHECK (LENGTH(title) >= 1 AND LENGTH(title) <= 255);
ALTER TABLE tasks ADD CONSTRAINT chk_description_length CHECK (LENGTH(description) <= 5000);
ALTER TABLE tasks ADD CONSTRAINT chk_status_value CHECK (status IN ('OPEN', 'IN_PROGRESS', 'COMPLETED'));
ALTER TABLE tasks ADD CONSTRAINT chk_priority_value CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'));
```

---

## DTOs (Data Transfer Objects)

### TaskDTO (Read - full task object)
```json
{
  "id": 1,
  "title": "Fix login bug",
  "description": "OAuth callback returns null user",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "category": "Backend",
  "dueDate": "2025-02-01T17:00:00Z",
  "userId": 1,
  "assignedToId": 2,
  "createdAt": "2025-01-20T10:00:00Z",
  "updatedAt": "2025-01-20T14:30:00Z",
  "completedAt": null
}
```

### TaskCreateDTO (Write - create task)
```json
{
  "title": "Fix login bug",
  "description": "OAuth callback returns null user",
  "priority": "HIGH",
  "category": "Backend",
  "dueDate": "2025-02-01T17:00:00Z"
}
```

### TaskUpdateDTO (Write - update task)
```json
{
  "title": "Fix login bug - JWT validation",
  "description": "OAuth callback returns null user - check JWT generation",
  "priority": "URGENT",
  "category": "Backend",
  "dueDate": "2025-02-01T17:00:00Z"
}
```

### TaskStatusUpdateDTO (Write - status change)
```json
{
  "status": "IN_PROGRESS"
}
```

### TaskAssignDTO (Write - assign task)
```json
{
  "assignedToId": 2
}
```

---

## Validation Rules

### API Layer (JSR-303)
```java
@NotBlank(message = "Title required")
@Size(min = 1, max = 255)
private String title;

@Size(max = 5000)
private String description;

@NotNull
@Pattern(regexp = "OPEN|IN_PROGRESS|COMPLETED")
private TaskStatus status;

@NotNull
@Pattern(regexp = "LOW|MEDIUM|HIGH|URGENT")
private Priority priority;

@FutureOrPresent(message = "Due date cannot be in past")
private LocalDateTime dueDate;
```

### Business Logic Layer
```java
// Before status transition
if (currentStatus == COMPLETED) {
  throw new RuntimeException("Cannot transition from COMPLETED");
}

// Before deletion
if (hasComments()) {
  throw new RuntimeException("Cannot delete task with comments");
}

// Before assignment
User assignee = userRepository.findById(assignedToId)
  .orElseThrow(() -> new RuntimeException("Assignee not found"));
```

### Database Layer
```sql
-- Checks enforced at schema level
CHECK (LENGTH(title) >= 1 AND LENGTH(title) <= 255)
CHECK (status IN ('OPEN', 'IN_PROGRESS', 'COMPLETED'))
CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'))
```

---

## Task Status State Machine

```
                  ┌─────────┐
                  │  OPEN   │ ← Initial state
                  └────┬────┘
                       │
                       ↓
                ┌──────────────┐
                │ IN_PROGRESS  │
                └──────┬───────┘
                       │
                       ↓
                ┌──────────────┐
                │  COMPLETED   │ ← Terminal state
                └──────────────┘
```

Rules:
- Tasks created in OPEN status
- OPEN → IN_PROGRESS (creator or assignee can do this)
- IN_PROGRESS → COMPLETED (creator or assignee can do this)
- No reversals (cannot go back to OPEN)
- When status = COMPLETED, set completedAt = NOW()

---

## Authorization Rules

### Create Task
- Any authenticated user can create tasks
- Creator ID = authenticated user ID (immutable)

### Read Task
- User who created it can always see
- User who is assigned to it can see
- Public read: No (tasks are private by default)

### Update Task (title/description/priority/category/dueDate)
- Only creator can update
- Assignee cannot modify task details

### Update Status
- Creator can always change status
- Assignee can change status to IN_PROGRESS or COMPLETED (progressing only)

### Assign/Reassign Task
- Only creator can assign task
- Can assign to any existing user
- Can unassign by setting assignedToId = null

### Delete Task
- Only creator can delete
- Cannot delete if task has comments (409 Conflict returned)
- Alternative: Archive instead (Phase 4+ feature)

---

## Indexes for Performance

| Columns | Type | Purpose | Query Time |
|---------|------|---------|------------|
| id | PK | Task lookup | < 1ms |
| user_id | Regular | Creator's tasks | < 50ms on 100k |
| assigned_to_id | Regular | Assigned tasks | < 50ms on 100k |
| status | Regular | Filter by status | < 100ms on 100k |
| priority | Regular | Filter by priority | < 100ms on 100k |
| due_date | Regular | Sort by due date | < 100ms on 100k |
| created_at DESC | Regular | Pagination, feed | < 100ms on 100k |

Combined filters example: status + priority + due_date = < 300ms on 100k

---

## Task Relationships

```
User (1) ──────────────── (Many) Task
  id                       user_id (creator)
  
User (1) ──────────────── (0..1) Task
  id                       assigned_to_id (assignee, optional)
  
Task (1) ───────────────── (Many) TaskComment
  id                       task_id
```

---

## Lifecycle States

**Task Creation**: title, description, status=OPEN, priority, dueDate, userId
**Task Assignment**: Optional assignedToId set
**Task Progress**: status → IN_PROGRESS
**Task Completion**: status → COMPLETED, completedAt = NOW()
**Task Deletion**: Hard delete (no soft delete in Phase 3)

---

## Performance Targets

| Operation | Target Time | Notes |
|-----------|-------------|-------|
| Create task | < 50ms | Simple INSERT |
| Fetch by ID | < 10ms | Primary key lookup |
| List tasks (50) | < 300ms | With filters + pagination |
| Update task | < 100ms | UPDATE + timestamp |
| Delete task | < 50ms | DELETE (or 409 if comments) |
| Change status | < 50ms | UPDATE status + completedAt |

---

## No New Tables Required

Task CRUD only adds the `tasks` table. Comments/notifications come in Phase 5.
