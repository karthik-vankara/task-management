# Phase 3: Task CRUD Operations - Implementation Tasks

**Branch:** `phase/3-task-crud`  
**Estimated Duration:** 6-7 days  
**Depends On:** Phases 1-2 (Auth + Profiles complete)

---

## Backend Tasks (Java/Spring Boot)

### T001-T005: Task Entity & Enums
- [ ] T001 Create Task entity in `src/main/java/com/karthik/task_management_backend/entity/Task.java` with 11 fields
- [ ] T002 Create TaskStatus enum (OPEN, IN_PROGRESS, COMPLETED) in `src/main/java/com/karthik/task_management_backend/entity/TaskStatus.java`
- [ ] T003 Create Priority enum (LOW, MEDIUM, HIGH, URGENT) in `src/main/java/com/karthik/task_management_backend/entity/Priority.java`
- [ ] T004 Add @PrePersist/@PreUpdate lifecycle methods for timestamps and status transitions
- [ ] T005 Create database migration `V3__create_tasks_table.sql` with 6 indexes

### T006-T010: Task Repository & Specifications
- [ ] T006 Create TaskRepository in `src/main/java/com/karthik/task_management_backend/repository/TaskRepository.java`
- [ ] T007 Extend JpaSpecificationExecutor for dynamic filtering
- [ ] T008 Create TaskSpecifications in `src/main/java/com/karthik/task_management_backend/repository/TaskSpecifications.java`
- [ ] T009 Implement specifications for: byUserId, byStatus, byPriority, byCategory, dueDateBetween
- [ ] T010 Create indexes in migration: user_id, assigned_to_id, status, priority, due_date, created_at DESC

### T011-T015: Task Service Layer
- [ ] T011 Create TaskService in `src/main/java/com/karthik/task_management_backend/service/TaskService.java`
- [ ] T012 Implement createTask method with validation and authorization
- [ ] T013 Implement listTasks with dynamic filtering and pagination
- [ ] T014 Implement getTask with creator/assignee authorization check
- [ ] T015 Implement updateTask (creator only) with field updates

### T016-T020: Task Service - Status & Assignment
- [ ] T016 Implement updateStatus method with state machine validation (OPEN→IN_PROGRESS→COMPLETED)
- [ ] T017 Set completedAt timestamp when status = COMPLETED
- [ ] T018 Implement deleteTask (creator only, 409 if comments exist)
- [ ] T019 Implement assignTask method (creator only, validate assignee exists)
- [ ] T020 Handle task authorization: creator vs assignee permissions

### T021-T025: Task DTOs & Controller
- [ ] T021 Create TaskDTO in `src/main/java/com/karthik/task_management_backend/dto/TaskDTO.java`
- [ ] T022 Create TaskCreateDTO with validation annotations
- [ ] T023 Create TaskUpdateDTO for partial updates
- [ ] T024 Create TaskStatusUpdateDTO for status transitions
- [ ] T025 Create TaskAssignDTO for assignment operations

### T026-T030: Task Controller Endpoints
- [ ] T026 Create TaskController in `src/main/java/com/karthik/task_management_backend/controller/TaskController.java`
- [ ] T027 Implement `POST /api/tasks` endpoint (create task)
- [ ] T028 Implement `GET /api/tasks` with query params (status, priority, category, pagination)
- [ ] T029 Implement `GET /api/tasks/{taskId}` endpoint (get task details)
- [ ] T030 Implement `PUT /api/tasks/{taskId}` endpoint (update task fields)

### T031-T035: Task Controller - Status & Operations
- [ ] T031 Implement `PUT /api/tasks/{taskId}/status` endpoint (change status)
- [ ] T032 Implement `DELETE /api/tasks/{taskId}` endpoint (delete task)
- [ ] T033 Implement `PUT /api/tasks/{taskId}/assign` endpoint (assign to user)
- [ ] T034 Add rate limiting to task endpoints
- [ ] T035 Add task-related error codes and exception handling

### T036-T040: Task Validation & Error Handling
- [ ] T036 Add custom exceptions: TaskNotFoundException, TaskAuthorizationException
- [ ] T037 Implement authorization checks (creator, assignee validation)
- [ ] T038 Add validation for status transitions (prevent invalid transitions)
- [ ] T039 Add validation for task deletion (409 if has comments)
- [ ] T040 Test authorization (manual: non-creator cannot edit/delete)

### T041-T045: Backend Testing & Hardening
- [ ] T041 Test task creation (manual: POST endpoint, verify DB)
- [ ] T042 Test task list with filters (manual: status, priority, category filters work)
- [ ] T043 Test status transitions (manual: OPEN→IN_PROGRESS→COMPLETED, completedAt set)
- [ ] T044 Test authorization (manual: creator can edit, assignee cannot edit)
- [ ] T045 Test deletion prevention (manual: 409 when task has comments)

---

## Frontend Tasks (React/TypeScript)

### T046-T050: Task Store & API Integration
- [ ] T046 Create Zustand store `src/store/taskStore.ts` with tasks state and CRUD actions
- [ ] T047 Create `src/services/taskService.ts` with API methods for all endpoints
- [ ] T048 Create TaskSearchCriteria type in `src/types/tasks.ts`
- [ ] T049 Create enum for TaskStatus and Priority in `src/types/tasks.ts`
- [ ] T050 Implement pagination state in taskStore (offset, limit, hasMore)

### T051-T055: Task List Components
- [ ] T051 Create TasksPage component in `src/pages/TasksPage.tsx`
- [ ] T052 Create TaskList component in `src/components/TaskList.tsx`
- [ ] T053 Create TaskCard component in `src/components/TaskCard.tsx` to display task summary
- [ ] T054 Create TaskDetailPage component in `src/pages/TaskDetailPage.tsx` for full task view
- [ ] T055 Create FilterPanel component in `src/components/FilterPanel.tsx` with status/priority/category filters

### T056-T060: Task Creation & Editing
- [ ] T056 Create TaskForm component in `src/components/TaskForm.tsx` for create/edit
- [ ] T057 Implement form validation (title required, description max 5000)
- [ ] T058 Add date picker for dueDate field
- [ ] T059 Implement create task submission (POST endpoint)
- [ ] T060 Implement edit task submission (PUT endpoint)

### T061-T065: Task Status & Assignment UI
- [ ] T061 Create StatusBadge component in `src/components/StatusBadge.tsx` for visual status display
- [ ] T062 Create PriorityBadge component in `src/components/PriorityBadge.tsx`
- [ ] T063 Implement status change UI (dropdown/buttons to change task status)
- [ ] T064 Create AssignUserForm component to assign task to another user
- [ ] T065 Add user search/selector for assignment

### T066-T070: Task Deletion & Pagination
- [ ] T066 Create DeleteConfirmationDialog component in `src/components/DeleteConfirmationDialog.tsx`
- [ ] T067 Implement delete task functionality with confirmation
- [ ] T068 Add error handling for deletion (409 if comments exist)
- [ ] T069 Implement pagination: offset, limit, load more button
- [ ] T070 Add sorting UI: by due date, priority, created date

### T071-T075: Frontend Validation & Error Handling
- [ ] T071 Add client-side form validation for all task fields
- [ ] T072 Display validation errors in form UI
- [ ] T073 Add error messages for authorization failures (403 - cannot edit)
- [ ] T074 Add loading states during task operations
- [ ] T075 Implement optimistic UI updates (show change immediately, revert on error)

### T076-T080: Frontend Testing & UX
- [ ] T076 Test task creation (manual: form submission, verify list updates)
- [ ] T077 Test task edit (manual: edit form, submit, changes reflected)
- [ ] T078 Test task deletion (manual: delete button, confirmation, removed from list)
- [ ] T079 Test status changes (manual: click status button, status updates)
- [ ] T080 Test task filtering (manual: select filters, list updates)

---

## Integration Tests (Manual E2E)

### T081-T085: End-to-End Task Workflows
- [ ] T081 E2E: User creates task → task appears in list → can edit → can update status
- [ ] T082 E2E: User creates task → assigns to another user → assignee sees task
- [ ] T083 E2E: Status transitions work: OPEN → IN_PROGRESS → COMPLETED, no reversals
- [ ] T084 E2E: Filter by status shows only tasks with that status
- [ ] T085 E2E: Delete task without comments works, with comments returns 409

---

## Acceptance Criteria Checklist

### Backend Acceptance
- [ ] Task CRUD endpoints all functional and return ApiResponse format
- [ ] Status state machine enforced (no invalid transitions)
- [ ] Authorization: creator can edit/delete, assignee can only change status forward
- [ ] Filters combine correctly (status + priority < 300ms on 100k tasks)
- [ ] Pagination with offset/limit working correctly
- [ ] Assignment: only creator can assign, assignee lookup validates
- [ ] Deletion: allowed without comments, 409 if comments exist
- [ ] Database indexes created for <200ms CRUD operations
- [ ] completedAt set only when status = COMPLETED

### Frontend Acceptance
- [ ] Task list page displays all user's tasks
- [ ] Create task form functional and submits to backend
- [ ] Edit task form loads existing data and submits updates
- [ ] Status badge shows current status and is clickable for changes
- [ ] Filter panel updates list when criteria changed
- [ ] Pagination working: load more fetches next page
- [ ] Delete confirmation dialog prevents accidental deletion
- [ ] Assignment UI allows selecting another user
- [ ] TypeScript strict mode enforced

### All Manual Tests Passing
- [ ] Manual test checklist from phase 3 spec.md (25+ items) completed

---

## Git Workflow

```bash
git checkout -b phase/3-task-crud origin/main
# ... implement tasks ...
git commit -m "[PHASE 3] Task CRUD operations implementation"
git push origin phase/3-task-crud
git checkout main
git merge --ff-only phase/3-task-crud
git tag v0.3.0 -a -m "Phase 3: Task CRUD Operations Complete"
```

---

## Success Criteria Summary

✅ **Phase 3 Implementation Complete:**
- Full task CRUD working (Create, Read, Update, Delete)
- Status state machine enforced (no reversals)
- Authorization model implemented and tested
- Filtering and pagination functional
- Task assignment feature working
- All manual tests passing
- Ready for Phase 4 (advanced task features)
