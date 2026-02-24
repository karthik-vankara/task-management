# Phase 4: Advanced Task Features - Implementation Tasks

**Branch:** `phase/4-advanced-tasks`  
**Estimated Duration:** 5-6 days  
**Depends On:** Phase 3 (Task CRUD complete)

---

## Backend Tasks

### T001-T010: Search & Full-Text Indexing
- [ ] T001 Add full-text search index on tasks table in migration `V4__add_search_index.sql`
- [ ] T002 Create TaskSearchSpecification for PostgreSQL full-text search
- [ ] T003 Implement `GET /api/tasks/search?q=keyword` endpoint in TaskController
- [ ] T004 Add search performance optimization (< 500ms for 100k tasks)
- [ ] T005 Create SearchResultDTO for search responses
- [ ] T006 Test search functionality (manual: search by title/description/category)
- [ ] T007 Test search ranking (manual: most relevant results first)
- [ ] T008 Add search query validation (prevent SQL injection)
- [ ] T009 Implement search filters (search + status/priority)
- [ ] T010 Add rate limiting to search endpoint (20/min)

### T011-T020: Advanced Filtering & Sorting
- [ ] T011 Add more filter options: dueDateFrom, dueDateTo, createdDateFrom, createdDateTo
- [ ] T012 Create FilterCriteria DTO for complex filter combinations
- [ ] T013 Implement sorting: by due_date ASC/DESC, priority DESC, created_at DESC
- [ ] T014 Add "sort" query parameter to `GET /api/tasks`
- [ ] T015 Test combined filters (status + priority + due date range)
- [ ] T016 Performance test: 3-filter query < 300ms on 100k tasks
- [ ] T017 Add filter parameter validation
- [ ] T018 Create saved filters functionality (optional Phase 4 enhancement)
- [ ] T019 Test filter edge cases (empty results, all tasks matching, etc.)
- [ ] T020 Document all filter combinations in API contract

### T021-T030: Bulk Operations
- [ ] T021 Implement `PATCH /api/tasks/bulk/status` endpoint
- [ ] T022 Implement `DELETE /api/tasks/bulk` endpoint
- [ ] T023 Implement `PATCH /api/tasks/bulk/assign` endpoint
- [ ] T024 Add request DTO for bulk operations (list of task IDs)
- [ ] T025 Implement authorization checks for each task in bulk operation
- [ ] T026 Add transaction support (all succeed or all fail)
- [ ] T027 Validate status transitions in bulk update
- [ ] T028 Test bulk update (manual: select 5 tasks, change status)
- [ ] T029 Test bulk delete with validation (409 if any have comments)
- [ ] T030 Test bulk assign (manual: assign multiple tasks to user)

### T031-T040: Task Templates
- [ ] T031 Create TaskTemplate entity with fields for template
- [ ] T032 Create migration `V5__create_task_templates.sql`
- [ ] T033 Implement `POST /api/tasks/{id}/template` (create template from task)
- [ ] T034 Implement `GET /api/task-templates` (list user's templates)
- [ ] T035 Implement `POST /api/tasks/from-template/{templateId}` (create task from template)
- [ ] T036 Implement `DELETE /api/task-templates/{templateId}` (delete template)
- [ ] T037 Test template creation and usage (manual: create template, create task from it)
- [ ] T038 Add template naming and description fields
- [ ] T039 Test template duplication (create multiple tasks from one template)
- [ ] T040 Add permissions: only template creator can use/delete

### T041-T050: Recurring Tasks
- [ ] T041 Create RecurringTaskRule entity with frequency field
- [ ] T042 Create migration `V6__create_recurring_rules.sql`
- [ ] T043 Implement `POST /api/tasks/{id}/recurring` endpoint
- [ ] T044 Add frequency options: DAILY, WEEKLY, MONTHLY, YEARLY
- [ ] T045 Create ScheduledTask service for nightly job
- [ ] T046 Implement nightly job to generate next occurrences
- [ ] T047 Test recurring task generation (manual: create recurring task, verify next generated)
- [ ] T048 Add end date support for recurring tasks
- [ ] T049 Test recurring task cancellation
- [ ] T050 Add recurring task metadata to task DTO (show if recurring, next occurrence)

---

## Frontend Tasks

### T051-T060: Search UI
- [ ] T051 Create SearchBar component in `src/components/SearchBar.tsx`
- [ ] T052 Add search input to TasksPage header
- [ ] T053 Implement search submission (debounced input)
- [ ] T054 Display search results with ranking indicator
- [ ] T055 Add "clear search" button
- [ ] T056 Test search functionality (manual: search works, results displayed)
- [ ] T057 Add search loading state
- [ ] T058 Implement search result highlighting
- [ ] T059 Test search + filter combination
- [ ] T060 Add keyboard shortcuts for search (Cmd+K)

### T061-T070: Advanced Filters UI
- [ ] T061 Extend FilterPanel with date range pickers
- [ ] T062 Add saved filters feature (save/load/delete filter presets)
- [ ] T063 Implement "favorite" filters
- [ ] T064 Create FilterPresetList component to show saved filters
- [ ] T065 Test filter application (manual: all filter types work)
- [ ] T066 Test date range filters
- [ ] T067 Add filter count badge (show number of active filters)
- [ ] T068 Implement "clear all filters" button
- [ ] T069 Test filter persistence (save to localStorage)
- [ ] T070 UI for sorting: dropdown with sort options

### T071-T080: Bulk Operations UI
- [ ] T071 Add checkbox to each TaskCard for multi-select
- [ ] T072 Create bulk action toolbar (appears when items selected)
- [ ] T073 Implement "Select All" checkbox in TaskList
- [ ] T074 Create bulk status change UI
- [ ] T075 Create bulk delete UI with confirmation
- [ ] T076 Create bulk assign UI with user selector
- [ ] T077 Test multi-select (manual: click checkboxes, toolbar appears)
- [ ] T078 Test bulk operations (manual: select multiple, change status)
- [ ] T079 Add bulk action counter (showing "X selected")
- [ ] T080 Test bulk operation errors (show which failed)

### T081-T090: Templates & Recurring Tasks UI
- [ ] T081 Create "Create Template" button on task detail page
- [ ] T082 Create TemplateList component to show user's templates
- [ ] T083 Implement template creation confirmation dialog
- [ ] T084 Add "Create from Template" button to template list
- [ ] T085 Create RecurringTaskSetup component for setting up recurring
- [ ] T086 Add frequency selector (Daily/Weekly/Monthly/Yearly)
- [ ] T087 Add end date picker for recurring rules
- [ ] T088 Test template workflow (manual: create, use, delete template)
- [ ] T089 Test recurring task setup (manual: create recurring task)
- [ ] T090 Display recurring indicator on task card

---

## Integration Tests

### T091-T095: End-to-End Advanced Workflows
- [ ] T091 E2E: Search for task → verify results → filter results → updated correctly
- [ ] T092 E2E: Select multiple tasks → change status in bulk → list updates
- [ ] T093 E2E: Create template from task → create new task from template → verify data
- [ ] T094 E2E: Set up recurring task (weekly) → verify next occurrence generated nightly
- [ ] T095 E2E: Complex filter (status + priority + due date) → results accurate

---

## Acceptance Criteria

- [ ] Full-text search functional and < 500ms for 100k tasks
- [ ] All filter combinations working correctly
- [ ] Bulk operations atomic (all succeed or all fail)
- [ ] Task templates create/use/delete functional
- [ ] Recurring tasks generate correctly on schedule
- [ ] All new UI components functional and styled
- [ ] All manual tests passing

---

## Git Workflow

```bash
git checkout -b phase/4-advanced-tasks origin/main
git commit -m "[PHASE 4] Advanced task features implementation"
git checkout main && git merge --ff-only phase/4-advanced-tasks
git tag v0.4.0 -a -m "Phase 4: Advanced Task Features Complete"
```

---

## Success Criteria Summary

✅ **Phase 4 Implementation Complete:**
- Full-text search working on large datasets
- Advanced filtering and sorting functional
- Bulk operations available and tested
- Task templates streamlining creation
- Recurring tasks automating workflows
- Ready for Phase 5 (collaboration)
