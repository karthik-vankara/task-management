# Implementation Plan: Task Management CRUD API

**Branch**: `003-task-crud` | **Date**: 2026-02-24 | **Phase Duration**: 6-7 days  
**Depends On**: Phase 1 (OAuth), Phase 2 (User Profiles)

---

## Summary

Implement complete Task Management CRUD operations with support for task creation, retrieval, status updates, and deletion. Supports filtering, pagination, and task assignment with comprehensive authorization logic.

---

## Technical Context

**Language/Version**: Java 21, Spring Boot 3.3.0  
**Primary Dependencies**: Spring Data JPA with Specifications pattern, Hibernate  
**Storage**: PostgreSQL 15+ with Task table and 6 indexes  
**Testing**: Manual testing per spec checklist  
**Target Platform**: Backend API service  
**Project Type**: web-service (REST API)  
**Performance Goals**: <200ms CRUD, <300ms list queries with filters  
**Constraints**: Pagination (default limit=10), 100k total tasks support  
**Scale**: Support 1000 concurrent task operations

---

## Constitution Check

**GATE**: ✅ **PASS**

- ✅ Spec-first: Complete specification with 7 user stories
- ✅ Minimal cost: Spring ecosystem, PostgreSQL, no paid services
- ✅ Quality: Authorization enforcement, validation, error handling
- ✅ Phase-based: Self-contained, builds on Phase 1-2
- ✅ Git workflow: Phase branch, semantic commits

---

## Project Structure

### Documentation

```text
specs/003-task-crud/
├── spec.md                    # Feature specification
├── plan.md                    # This file
├── research.md                # Phase 0: Task query optimization patterns
├── data-model.md              # Phase 1: Task entity schema and indexes
├── quickstart.md              # Implementation guide
├── contracts/
│   └── task-api.md            # API endpoint specs for CRUD
└── tasks.md                   # Phase 2: Granular task breakdown
```

### Source Code (Backend)

```text
backend/src/main/java/com/karthik/
├── entity/
│   ├── Task.java              # JPA entity with all fields
│   └── TaskStatus.java        # Enum: OPEN, IN_PROGRESS, COMPLETED
├── dto/
│   ├── TaskDTO.java           # Task response
│   ├── CreateTaskRequest.java
│   ├── UpdateTaskRequest.java
│   └── TaskListItemDTO.java   # Paginated list item
├── controller/
│   └── TaskController.java    # REST endpoints for CRUD
├── service/
│   └── TaskService.java       # Business logic for task management
├── repository/
│   ├── TaskRepository.java    # JpaRepository with custom queries
│   └── TaskSpecification.java # Dynamic query builder patterns
├── validator/
│   └── TaskValidator.java     # Field validation, state transitions
└── exception/
    └── TaskNotFoundException.java
```

---

## Implementation Roadmap

### Days 1-2: Entity & Queries (10-12 hours)

**Tasks**:
1. Create Task entity with all 11 fields (2 hrs)
2. Create TaskStatus enum and validators (1 hr)
3. Create 6 database indexes (via Hibernate/migrations) (1.5 hrs)
4. Create TaskRepository with custom finder methods (2 hrs)
5. Create TaskSpecification for dynamic filtering (2 hrs)
6. Create TaskDTO, CreateTaskRequest, UpdateTaskRequest (1.5 hrs)
7. Write comprehensive validators (TaskValidator) (1.5 hrs)

**Deliverable**: Complete data layer with queries ready for service implementation

**Commits**:
```
[PHASE 3] Add Task entity with status enum and indexes
[PHASE 3] Implement dynamic task query specifications
```

### Days 3-4: Service Layer (12-14 hours)

**Tasks**:
1. Implement TaskService.createTask with validation (2 hrs)
2. Implement TaskService.getTasks with pagination/filtering (2 hrs)
3. Implement TaskService.getTaskById with authorization (1 hrs)
4. Implement TaskService.updateTask with partial updates (2 hrs)
5. Implement TaskService.updateTaskStatus with state machine (1.5 hrs)
6. Implement TaskService.deleteTask with ownership check (1 hr)
7. Implement TaskService.assignTask with user validation (1.5 hrs)
8. Add comprehensive error handling with error codes (1.5 hrs)

**Deliverable**: Complete business logic layer with all operations

**Commits**:
```
[PHASE 3] Implement task CRUD service layer
[PHASE 3] Add task status transitions and authorization checks
```

### Days 5-6: Controller & API (10-12 hours)

**Tasks**:
1. Create TaskController with 7 endpoints (3 hrs)
2. Map POST /api/tasks endpoint (1.5 hrs)
3. Map GET /api/tasks endpoint with filters (1.5 hrs)
4. Map GET /api/tasks/{id} endpoint (1 hr)
5. Map PUT /api/tasks/{id} endpoint (1.5 hrs)
6. Map PUT /api/tasks/{id}/status endpoint (1 hrs)
7. Map DELETE /api/tasks/{id} endpoint (1 hr)
8. Map PUT /api/tasks/{id}/assign endpoint (1 hr)
9. Add standardized response wrapper (0.5 hrs)

**Deliverable**: All 7 endpoints fully functional

**Commits**:
```
[PHASE 3] Add TaskController with CRUD endpoints
[PHASE 3] Implement request/response mapping with standardized format
```

### Days 6-7: Testing (8-10 hours)

**Tasks**:
1. Manual test task creation (1 hr)
2. Manual test task list with filters and pagination (2 hrs)
3. Manual test task updates (1.5 hrs)
4. Manual test status transitions (1 hr)
5. Manual test authorization (cannot edit others' tasks) (1 hr)
6. Manual test task deletion (1 hr)
7. Manual test task assignment (1 hr)
8. Load test with 100+ tasks (1 hr)

**Deliverable**: All manual tests pass, performance targets verified

**Commits**:
```
[PHASE 3] Complete manual testing checklist
```

---

## Key Implementation Details

### Task Creation

```java
// Endpoint: POST /api/tasks
// Payload: { title, description, dueDate, priority, category }
// Response: 201 Created + full task

// Logic:
1. Validate input
2. Set status = OPEN, priority = MEDIUM (defaults)
3. Set userId = current user (creator)
4. Set createdAt/updatedAt = now
5. Persist to DB
6. Return created task
```

### Task List Query With Filters

```java
// Endpoint: GET /api/tasks?status=OPEN&priority=HIGH&limit=10&offset=0
// Logic:
1. Build dynamic specification from filters
2. Apply pagination (limit, offset)
3. Order by dueDate, priority
4. Return tasks created by OR assigned to current user
5. Performance: Use lazy loading, avoid N+1 queries
```

### Authorization Pattern

```java
// For updates/deletes:
1. Check if user = creator
2. For status updates: check if user = assignee OR creator
3. For assignment: check if user = creator
4. Return 403 Forbidden if unauthorized
```

### Task Status Transitions

```
Valid: OPEN → IN_PROGRESS → COMPLETED
Invalid: COMPLETED → OPEN (not allowed)
Invalid: IN_PROGRESS → OPEN (not allowed)

When COMPLETED:
- Set completedAt = now
- Optionally notify creator
```

---

## Database Schema

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

-- Indexes for performance
CREATE INDEX idx_tasks_user_id ON tasks(user_id);
CREATE INDEX idx_tasks_assigned_to_id ON tasks(assigned_to_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_priority ON tasks(priority);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_created_at ON tasks(created_at DESC);
```

---

## Error Handling

| Code | HTTP | Scenario |
|------|------|----------|
| TASK_NOT_FOUND | 404 | Task ID doesn't exist |
| UNAUTHORIZED | 401 | JWT missing or invalid |
| FORBIDDEN | 403 | Non-creator/assignee trying to modify |
| INVALID_STATUS | 400 | Invalid status value or transition |
| INVALID_TASK_DATA | 400 | Missing required fields |
| INVALID_ASSIGNEE | 400 | Assignee user doesn't exist |

---

## Dependencies

- ✅ Phase 1 Complete (Auth, JWT)
- ✅ Phase 2 Complete (User profiles, User lookups)
- ✅ PostgreSQL running
- ✅ Spring Data JPA configured

---

## Testing Checklist

- [ ] Create task with required fields (POST)
- [ ] Create task with optional fields (description, etc.)
- [ ] List user's tasks (GET /api/tasks)
- [ ] Filter tasks by status
- [ ] Filter tasks by priority
- [ ] Sort tasks by dueDate
- [ ] Paginate results (limit, offset)
- [ ] Get single task (GET /api/tasks/{id})
- [ ] Update task fields (PUT)
- [ ] Cannot update non-existent task (404)
- [ ] Update task status only (PUT /tasks/{id}/status)
- [ ] Cannot reverse status transition
- [ ] Delete task as creator (204)
- [ ] Cannot delete as non-creator (403)
- [ ] Cannot delete non-existent task (404)
- [ ] Assign task to valid user
- [ ] Cannot assign to non-existent user (400)
- [ ] Authorization enforced (403 for unauthorized)
- [ ] All responses use standardized format
- [ ] Error responses include error codes
- [ ] Performance: List 100 tasks < 300ms
- [ ] Performance: CRUD < 200ms

---

## Success Criteria

✅ **Phase 3 Complete When**:
1. All 7 endpoints implemented and tested
2. All manual tests passing
3. Authorization enforced on all operations
4. Pagination and filtering working correctly
5. Status transitions validated
6. Database indexes created and active
7. Performance targets met (<300ms list, <200ms CRUD)
8. Code follows Google Java Style Guide
9. Git history clean with meaningful commits
10. Ready to merge to main with v0.3.0 tag

---

## Complexity: NO VIOLATIONS

Layered architecture with clean separation: controller → service → repository. All SOLID principles followed.
