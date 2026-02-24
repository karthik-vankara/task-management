# Implementation Plan: Advanced Task Features

**Branch**: `004-advanced-tasks` | **Phase Duration**: 5-6 days  
**Depends On**: Phase 3 (Task CRUD)

---

## Summary

Implement advanced task features: full-text search, task filtering by category/due date, priority-based sorting, and bulk operations. Add recurring task support and task templates for faster creation.

---

## Technical Context

- **Language**: Java 21, Spring Boot 3.3.0
- **Database**: PostgreSQL 15+ with full-text search
- **Performance**: <500ms search on 100k tasks
- **Testing**: Manual testing checklist

---

## Constitution Check: ✅ PASS

---

## Key Features

### 1. Full-Text Search
- Endpoint: `GET /api/tasks/search?q=login%20backend`
- Searches: title, description, category
- Returns: Paginated results with relevance scoring
- Performance: < 500ms for 100k tasks with indexes

### 2. Advanced Filtering
- Filter by: dueDate range, priority, category
- Combine multiple filters
- Endpoint: `GET /api/tasks?dueDateFrom=2026-03-01&dueDateTo=2026-03-31&priority=HIGH`

### 3. Bulk Operations
- Bulk update status: `PATCH /api/tasks/bulk/status` with task IDs
- Bulk delete: `DELETE /api/tasks/bulk` with task IDs
- Bulk assign: `PATCH /api/tasks/bulk/assign` with user ID
- Ensure authorization on each task

### 4. Task Templates
- Create template from task: `POST /api/tasks/{id}/template`
- List templates: `GET /api/task-templates`
- Create task from template: `POST /api/tasks/from-template/{templateId}`

### 5. Recurring Tasks
- Entity: RecurringTaskRule (frequency, endDate, etc.)
- Create: `POST /api/tasks/{id}/recurring` with recurrence pattern
- Auto-generate: System job runs nightly to create next occurrence

---

## Database Changes

```sql
-- Full-text search index
CREATE INDEX idx_tasks_search ON tasks 
  USING gin(to_tsvector('english', title || ' ' || description));

-- Task templates table
CREATE TABLE task_templates (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority VARCHAR(50),
    category VARCHAR(100),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Recurring task rules
CREATE TABLE recurring_task_rules (
    id BIGSERIAL PRIMARY KEY,
    original_task_id BIGINT NOT NULL REFERENCES tasks(id),
    frequency VARCHAR(50),
    end_date TIMESTAMP,
    next_occurrence_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);
```

---

## Implementation Roadmap

**Days 1-2: Search & Filtering (8-10 hours)**
- Implement PostgreSQL full-text search
- Create search endpoint with pagination
- Add filtering specifications for advanced queries
- Test search performance

**Days 3-4: Bulk Operations (8-10 hours)**
- Implement bulk update/delete with authorization
- Add transaction safety
- Implement bulk assignment

**Days 5-6: Templates & Recurring (8-10 hours)**
- Create template entity and storage
- Implement template CRUD endpoints
- Implement recurring task generation logic
- Schedule nightly job for recurring task generation

---

## Testing Checklist

- [ ] Search for tasks by keyword
- [ ] Filter tasks by date range
- [ ] Combine multiple filters
- [ ] Bulk update status
- [ ] Bulk delete with authorization
- [ ] Bulk assign to user
- [ ] Create task template from existing task
- [ ] List templates
- [ ] Create task from template
- [ ] Set up recurring task with weekly frequency
- [ ] Verify nightly job creates next occurrence
- [ ] Search performance on 100k tasks < 500ms

---

## Success Criteria

✅ Phase 4 Complete:
- Full-text search implemented
- Advanced filtering working
- Bulk operations with authorization
- Task templates CRUD
- Recurring tasks auto-generated nightly
- All manual tests passing
- Performance targets met
