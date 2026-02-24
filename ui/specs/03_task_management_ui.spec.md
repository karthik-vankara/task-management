# Task Management UI Specification

## Overview
Implement task list view, creation/editing modal, and CRUD operations with filtering and pagination.

## Objectives
- [ ] Task list page with cards
- [ ] Task creation/editing modal
- [ ] Delete confirmation dialog
- [ ] Filtering by status
- [ ] Pagination controls
- [ ] Loading and error states

## Components

### 1. TaskListPage
**Route:** `/tasks`

**Layout:**
```
TaskListPage
├── Header ("Tasks")
├── Controls
│   ├── CreateButton
│   ├── FilterPanel
│   └── SearchBar (optional)
├── TaskList
│   ├── TaskCard (repeated)
│   └── EmptyState
└── PaginationControls
```

### 2. TaskCard Component
```typescript
interface TaskCardProps {
  task: Task;
  onEdit: (id: number) => void;
  onDelete: (id: number) => void;
  onStatus: (id: number, status: TaskStatus) => void;
}
```

**Display:**
- Title
- Description (truncated)
- Status badge (colored)
- Created date
- Action buttons: Edit, Delete, Mark Complete

### 3. TaskFormModal Component
```typescript
interface TaskFormModalProps {
  task?: Task;
  open: boolean;
  loading?: boolean;
  onSave: (data: CreateTaskRequest) => Promise<void>;
  onCancel: () => void;
}
```

**Fields:**
- Title (required)
- Description (optional)
- Status selector

### 4. DeleteConfirmationDialog Component
```typescript
interface DeleteConfirmationDialogProps {
  open: boolean;
  title: string;
  message: string;
  onConfirm: () => void;
  onCancel: () => void;
  loading?: boolean;
}
```

### 5. FilterPanel Component
**Filters:**
- Status: OPEN, IN_PROGRESS, COMPLETED
- All, Open, In Progress, Completed tabs

## Data Flow

### Load Tasks
```typescript
useEffect(() => {
  fetchTasks({
    page: currentPage,
    size: PAGE_SIZE,
    status: selectedStatus
  });
}, [currentPage, selectedStatus]);
```

### Create Task
```typescript
async handleCreate(data: CreateTaskRequest) {
  await taskService.createTask(data);
  await refreshTaskList();
  closeModal();
  notify({ type: 'success', message: 'Task created' });
}
```

### Update Task
```typescript
async handleUpdate(id: number, data: UpdateTaskRequest) {
  await taskService.updateTask(id, data);
  await refreshTaskList();
  closeModal();
  notify({ type: 'success', message: 'Task updated' });
}
```

### Delete Task
```typescript
async handleDelete(id: number) {
  await taskService.deleteTask(id);
  await refreshTaskList();
  notify({ type: 'success', message: 'Task deleted' });
}
```

## Zustand Store

```typescript
interface TaskState {
  tasks: Task[];
  loading: boolean;
  error: string | null;
  currentPage: number;
  totalPages: number;
  selectedStatus: TaskStatus | null;
  
  fetchTasks: (params: QueryParams) => Promise<void>;
  createTask: (data: CreateTaskRequest) => Promise<void>;
  updateTask: (id: number, data: UpdateTaskRequest) => Promise<void>;
  deleteTask: (id: number) => Promise<void>;
  setSelectedStatus: (status: TaskStatus | null) => void;
  setCurrentPage: (page: number) => void;
}
```

## UI Patterns

### Status Badge
- OPEN: Blue background, "Open"
- IN_PROGRESS: Yellow background, "In Progress"
- COMPLETED: Green background, "✓ Completed"

### Card Actions
- Edit button: Opens edit modal
- Delete button: Opens confirmation dialog
- Status button: Marks as complete or reopens

### Pagination
- Previous/Next buttons
- Page number display: "Page 1 of 5"
- Jump to page input (optional)

### Empty State
```
┌─────────────────────────┐
│    No tasks yet         │
│  Create one to start    │
│   [Create Task Button]  │
└─────────────────────────┘
```

### Error State
```
┌─────────────────────────┐
│  ⚠️ Failed to load      │
│   tasks. Try again?    │
│    [Retry Button]       │
└─────────────────────────┘
```

## Form Validation

```typescript
const taskSchema = {
  title: {
    required: true,
    minLength: 1,
    maxLength: 100
  },
  description: {
    maxLength: 1000
  }
};
```

## Responsive Design
- Mobile: Single column, full-width cards
- Tablet: Possibly 2-column layout
- Desktop: 2-3 column layout with sidebar

## Styling (Tailwind)

### Task Card
```
class="border rounded-lg p-4 hover:shadow-lg transition-shadow"
```

### Status Badge
```
class="inline-block px-3 py-1 rounded-full text-xs font-semibold"
```

### Button Styles
- Primary (Create): Blue with white text
- Secondary (Edit): Gray outline
- Danger (Delete): Red outline

## Dependencies
- axios (API calls)
- zustand (state)
- react-hook-form (validation)
- Tailwind CSS (styling)

## Test Plan
- [ ] Task list loads and displays
- [ ] Create button opens modal
- [ ] Form validates before submit
- [ ] Task created appears in list
- [ ] Edit button populates form
- [ ] Task updated immediately
- [ ] Delete button shows confirmation
- [ ] Task removed after delete
- [ ] Pagination works
- [ ] Filter by status works
- [ ] Error message shows on API failure
- [ ] Responsive on mobile/desktop

## Acceptance Criteria
- [ ] All CRUD operations work
- [ ] List displays with pagination
- [ ] Filtering and sorting work
- [ ] Responsive design
- [ ] Error handling
- [ ] Loading states

## Notes
- Consider infinite scroll for future (vs pagination)
- Bulk actions (select multiple) for future phase
- Drag-to-reorder for future phase
