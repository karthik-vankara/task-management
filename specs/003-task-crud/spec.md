# Task Management CRUD API Specification

**Version**: 1.0.0  
**Phase**: 3  
**Date**: 2026-02-24  
**Depends On**: Phase 1 (OAuth), Phase 2 (User Profiles)

---

## Overview

Implement complete Task Management CRUD operations with support for creating, reading, updating, and deleting tasks. Includes task status tracking (OPEN, IN_PROGRESS, COMPLETED), priority levels (LOW, MEDIUM, HIGH, URGENT), and due date management.

## User Stories

### US-1: Create a New Task
**As a** logged-in user  
**I want to** create a new task with title, description, due date, and priority  
**So that** I can track work that needs to be completed

**Acceptance Criteria**:
- [ ] POST /api/tasks creates task with required fields
- [ ] Task fields: title (required), description, dueDate, priority, category
- [ ] Status defaults to OPEN
- [ ] Priority defaults to MEDIUM
- [ ] Creator automatically set to current user
- [ ] Returns 201 with created task
- [ ] Returns 400 for validation errors

### US-2: View My Tasks
**As a** logged-in user  
**I want to** view a list of all my tasks  
**So that** I can see what I need to work on

**Acceptance Criteria**:
- [ ] GET /api/tasks returns list of user's tasks
- [ ] Supports pagination (limit=10, offset=0)
- [ ] Supports filtering by status (status=OPEN,IN_PROGRESS)
- [ ] Supports sorting (sort=dueDate,priority)
- [ ] Returns tasks created by user + assigned to user
- [ ] Response time < 200ms for 100 tasks

### US-3: View Task Details
**As a** logged-in user  
**I want to** view complete details of a specific task  
**So that** I can see all information related to it

**Acceptance Criteria**:
- [ ] GET /api/tasks/{taskId} returns task with all fields
- [ ] Includes: id, title, description, status, priority, dueDate, creator, assignee
- [ ] Returns 404 if task not found
- [ ] Returns 403 if user is not creator/assignee
- [ ] Includes created/updated timestamps

### US-4: Update Task Details
**As a** task creator  
**I want to** update task details (title, description, priority, etc.)  
**So that** I can keep task information current

**Acceptance Criteria**:
- [ ] PUT /api/tasks/{taskId} updates task fields
- [ ] Only created by/assigned to user can update
- [ ] Supports partial updates (PATCH)
- [ ] Cannot change taskId or creationDate
- [ ] Returns 200 with updated task
- [ ] Returns 403 if unauthorized

### US-5: Update Task Status
**As a** task assignee  
**I want to** change task status (OPEN → IN_PROGRESS → COMPLETED)  
**So that** I can track task progress

**Acceptance Criteria**:
- [ ] PUT /api/tasks/{taskId}/status changes only status field
- [ ] Valid transitions: OPEN → IN_PROGRESS → COMPLETED
- [ ] Completed tasks show completionDate
- [ ] Returns 404 if task not found
- [ ] Returns 403 if user not assigned to task (or is creator with perms)

### US-6: Delete a Task
**As a** task creator  
**I want to** delete a task I no longer need  
**So that** my task list stays relevant

**Acceptance Criteria**:
- [ ] DELETE /api/tasks/{taskId} removes task
- [ ] Only task creator can delete
- [ ] Returns 204 No Content on success
- [ ] Returns 404 if task not found
- [ ] Returns 403 if not creator

### US-7: Assign Task to Another User
**As a** task creator  
**I want to** assign a task to another user  
**So that** they are notified of work to do

**Acceptance Criteria**:
- [ ] PUT /api/tasks/{taskId}/assign sets assignee
- [ ] Only creator can assign tasks
- [ ] Assignee must exist as valid user
- [ ] Returns 400 if assignee not found
- [ ] Assignee receives notification (Phase 5)

## Functional Requirements

- FR1: User can create task (POST /api/tasks)
- FR2: User can view their tasks with pagination and filtering (GET /api/tasks)
- FR3: User can view task details (GET /api/tasks/{taskId})
- FR4: User can update task details (PUT /api/tasks/{taskId})
- FR5: User can change task status (PUT /api/tasks/{taskId}/status)
- FR6: Task creator can delete task (DELETE /api/tasks/{taskId})
- FR7: Task creator can assign task to another user (PUT /api/tasks/{taskId}/assign)
- FR8: System enforces authorization (only creator/assignee see/edit tasks)
- FR9: System validates task data (title length, date format, status enum)

## Non-Functional Requirements

- NFR1: Performance: Task CRUD operations < 200ms, list queries < 300ms
- NFR2: Scalability: Support 1000 concurrent task operations, 100k total tasks
- NFR3: Security: Task visibility restricted to creator/assignee, input validation
- NFR4: Usability: Clear status indicators, pagination support, error messages

## Data Requirements

### Task Entity Fields
- `id`: Long, auto-increment primary key
- `title`: String, 1-255 chars, NOT NULL, indexed
- `description`: String, 0-5000 chars, optional
- `status`: Enum (OPEN, IN_PROGRESS, COMPLETED), default OPEN, indexed
- `priority`: Enum (LOW, MEDIUM, HIGH, URGENT), default MEDIUM, indexed
- `category`: String, 0-100 chars, optional
- `dueDate`: DateTime, optional, indexed for sorting
- `userId`: Long, FK to User, NOT NULL, indexed (creator)
- `assignedToId`: Long, FK to User, optional, indexed (assignee)
- `createdAt`: DateTime, auto-set, NOT NULL
- `updatedAt`: DateTime, auto-updated, NOT NULL
- `completedAt`: DateTime, optional (set when status = COMPLETED)

### Database Indexes
```sql
CREATE INDEX idx_tasks_user_id ON tasks(user_id);
CREATE INDEX idx_tasks_assigned_to_id ON tasks(assigned_to_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_priority ON tasks(priority);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_created_at ON tasks(created_at DESC);
```

### API Response Format
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Implement login",
    "description": "Add Google OAuth login...",
    "status": "IN_PROGRESS",
    "priority": "HIGH",
    "category": "Backend",
    "dueDate": "2026-03-01T23:59:59Z",
    "userId": 1,
    "assignedToId": 2,
    "assignedToName": "John Doe",
    "createdAt": "2026-02-24T10:30:00Z",
    "updatedAt": "2026-02-24T11:00:00Z"
  },
  "timestamp": "2026-02-24T11:00:00Z"
}
```

## Success Metrics

### User Perspective
- Users can create and manage tasks efficiently
- Task status clearly shows progress
- Task list is organized and filterable

### Business Perspective
- 100% of tasks created have valid data
- Task tracking enables project visibility
- Zero task data loss during operations

### Technical Perspective
- <200ms CRUD operations
- <300ms list queries with filters
- Supports 100k tasks with sub-second performance

## Acceptance Criteria (Complete)

- [ ] POST /api/tasks creates task with validation
- [ ] GET /api/tasks returns paginated, filterable task list
- [ ] GET /api/tasks/{taskId} returns task details
- [ ] PUT /api/tasks/{taskId} updates task fields
- [ ] PUT /api/tasks/{taskId}/status changes status only
- [ ] DELETE /api/tasks/{taskId} removes task (creator only)
- [ ] PUT /api/tasks/{taskId}/assign assigns to user
- [ ] Authorization enforced (creator/assignee checks)
- [ ] All responses follow standardized format
- [ ] Error responses include error codes
- [ ] Database indexes created for performance
- [ ] Manual test checklist completed

## Out of Scope for Phase 3

The following features will be implemented in later phases:
- Task search (Phase 4)
- Task templates/recurring tasks (Phase 4)
- Task dependencies/subtasks (Phase 4)
- Task comments (Phase 5)
- Task activity history (Phase 7)
- Bulk task operations (Phase 4)
- Task export/import (Phase 8)

## Dependencies

**Phase 1 Complete**: User authentication, JWT valid  
**Phase 2 Complete**: User profiles, assignee lookup  
**Database**: PostgreSQL with Task table with all indexes  
**Libraries**: Hibernate, Spring Data JPA for ORM

## Technical Notes

- Use Spring Data JPA with specifications pattern for complex queries
- Implement separate TaskValidator for field validation
- Task status transitions: OPEN → IN_PROGRESS → COMPLETED (no reversals)
- AssignedToId is optional (unassigned tasks allowed)
- Use database-level CASCADE DELETE for integrity (optional)
- Consider adding soft-delete for audit trail (Phase 7)
- Implement pagination to prevent memory issues with large datasets
