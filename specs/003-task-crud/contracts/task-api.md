# Phase 3: Task CRUD Operations - API Contracts

---

## Endpoint 1: POST /api/tasks
**Create New Task**

### Request
```
POST /api/tasks HTTP/1.1
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "title": "Fix login bug",
  "description": "OAuth callback returns null user",
  "priority": "HIGH",
  "category": "Backend",
  "dueDate": "2025-02-01T17:00:00Z"
}
```

### Validation
- title: 1-255 characters, non-empty (required)
- description: 0-5000 characters (optional)
- priority: LOW, MEDIUM, HIGH, URGENT (required, default MEDIUM)
- category: Free-form text (optional)
- dueDate: Must be future or present (optional)
- status: Always set to OPEN (immutable on creation)
- userId: Set to authenticated user (immutable)

### Response (201 Created)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Fix login bug",
    "description": "OAuth callback returns null user",
    "status": "OPEN",
    "priority": "HIGH",
    "category": "Backend",
    "dueDate": "2025-02-01T17:00:00Z",
    "userId": 1,
    "assignedToId": null,
    "createdAt": "2025-01-20T15:00:00Z",
    "updatedAt": "2025-01-20T15:00:00Z",
    "completedAt": null
  },
  "timestamp": "2025-01-20T15:00:00Z"
}
```

### Error Responses
- **400 Bad Request**: Invalid field (title empty, priority invalid, dueDate in past)
- **401 Unauthorized**: Invalid JWT
- **422 Unprocessable Entity**: Validation failure

---

## Endpoint 2: GET /api/tasks
**List Tasks with Filtering & Pagination**

### Request
```
GET /api/tasks?status=OPEN&priority=HIGH&limit=50&offset=0 HTTP/1.1
Authorization: Bearer {jwt_token}
```

### Query Parameters
- `status`: Filter by status (OPEN, IN_PROGRESS, COMPLETED)
- `priority`: Filter by priority (LOW, MEDIUM, HIGH, URGENT)
- `category`: Filter by category (exact match)
- `dueDateFrom`: Filter tasks with dueDate >= (ISO 8601)
- `dueDateTo`: Filter tasks with dueDate <= (ISO 8601)
- `limit`: Page size (default 50, max 100)
- `offset`: Pagination offset (default 0)
- `sort`: Order by (due_date, priority, created_at, default = created_at DESC)

### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "tasks": [
      {
        "id": 1,
        "title": "Fix login bug",
        "status": "IN_PROGRESS",
        "priority": "HIGH",
        "dueDate": "2025-02-01T17:00:00Z",
        "userId": 1,
        "assignedToId": 2,
        "createdAt": "2025-01-20T15:00:00Z",
        "updatedAt": "2025-01-20T15:30:00Z"
      }
    ],
    "total": 125,
    "limit": 50,
    "offset": 0,
    "hasMore": true
  },
  "timestamp": "2025-01-20T15:35:00Z"
}
```

### Performance
- Returns only current user's tasks (userId filter implicit)
- Combined filters (status + priority + category): < 300ms
- Pagination with sorting: < 300ms for 100k tasks

### Error Responses
- **401 Unauthorized**: Invalid JWT
- **400 Bad Request**: Invalid filter value (priority not in enum)

---

## Endpoint 3: GET /api/tasks/{taskId}
**Get Task Details**

### Request
```
GET /api/tasks/1 HTTP/1.1
Authorization: Bearer {jwt_token}
```

### Path Parameters
- `taskId`: Task ID to retrieve

### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Fix login bug",
    "description": "OAuth callback returns null user",
    "status": "IN_PROGRESS",
    "priority": "HIGH",
    "category": "Backend",
    "dueDate": "2025-02-01T17:00:00Z",
    "userId": 1,
    "assignedToId": 2,
    "createdAt": "2025-01-20T15:00:00Z",
    "updatedAt": "2025-01-20T15:30:00Z",
    "completedAt": null
  },
  "timestamp": "2025-01-20T15:40:00Z"
}
```

### Authorization
- User who created task: Can see (all fields)
- User who is assigned: Can see (all fields)
- Other users: 403 Forbidden

### Error Responses
- **401 Unauthorized**: Invalid JWT
- **403 Forbidden**: User not task creator/assignee
- **404 Not Found**: Task not found

---

## Endpoint 4: PUT /api/tasks/{taskId}
**Update Task Details**

### Request
```
PUT /api/tasks/1 HTTP/1.1
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "title": "Fix login bug - JWT validation",
  "description": "Check JWT generation in callback",
  "priority": "URGENT",
  "category": "Backend",
  "dueDate": "2025-02-02T17:00:00Z"
}
```

### Validation
- Can only update: title, description, priority, category, dueDate
- Cannot update: status (use PUT /api/tasks/{taskId}/status), userId, assignedToId

### Response (200 OK)
Returns updated task object with new updatedAt timestamp.

### Authorization
- Creator: Can update all fields
- Assignee: Cannot update (403 Forbidden)

### Error Responses
- **400 Bad Request**: Invalid field
- **401 Unauthorized**: Invalid JWT
- **403 Forbidden**: Not task creator
- **404 Not Found**: Task not found

---

## Endpoint 5: PUT /api/tasks/{taskId}/status
**Update Task Status**

### Request
```
PUT /api/tasks/1/status HTTP/1.1
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "status": "COMPLETED"
}
```

### Validation
- status: Must be valid enum (OPEN, IN_PROGRESS, COMPLETED)
- Transitions: OPEN → {IN_PROGRESS, COMPLETED}, IN_PROGRESS → COMPLETED only
- No reversals: Cannot go back to OPEN

### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "COMPLETED",
    "completedAt": "2025-01-20T15:50:00Z",
    "updatedAt": "2025-01-20T15:50:00Z"
  },
  "timestamp": "2025-01-20T15:50:00Z"
}
```

### Authorization
- Creator: Can change status
- Assignee: Can change status forward (IN_PROGRESS, COMPLETED) only

### Error Responses
- **400 Bad Request**: Invalid status or illegal transition
- **401 Unauthorized**: Invalid JWT
- **403 Forbidden**: Not creator/assignee
- **404 Not Found**: Task not found

---

## Endpoint 6: DELETE /api/tasks/{taskId}
**Delete Task**

### Request
```
DELETE /api/tasks/1 HTTP/1.1
Authorization: Bearer {jwt_token}
```

### Validation
- Can only delete if no comments attached
- Returns 409 Conflict if comments exist

### Response (204 No Content)
```
HTTP/1.1 204 No Content
```

### Authorization
- Creator: Can delete
- Assignee: Cannot delete (403 Forbidden)

### Error Responses
- **401 Unauthorized**: Invalid JWT
- **403 Forbidden**: Not task creator
- **404 Not Found**: Task not found
- **409 Conflict**: Task has comments; delete comments first

---

## Endpoint 7: PUT /api/tasks/{taskId}/assign
**Assign Task to User**

### Request
```
PUT /api/tasks/1/assign HTTP/1.1
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "assignedToId": 2
}
```

### Validation
- assignedToId: Must be valid user ID
- Can set to null to unassign
- Cannot assign to self (optional, depends on business rule)

### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "assignedToId": 2,
    "updatedAt": "2025-01-20T16:00:00Z"
  },
  "timestamp": "2025-01-20T16:00:00Z"
}
```

### Authorization
- Creator: Can assign to anyone
- Assignee: Cannot reassign (403 Forbidden)

### Error Responses
- **400 Bad Request**: Assignee not found
- **401 Unauthorized**: Invalid JWT
- **403 Forbidden**: Not task creator
- **404 Not Found**: Task not found

---

## Error Codes Summary

| Code | Status | Meaning |
|------|--------|---------|
| UNAUTHORIZED | 401 | Invalid or missing JWT |
| INVALID_FIELD | 400 | Field validation failed |
| TASK_NOT_FOUND | 404 | Task does not exist |
| FORBIDDEN | 403 | User not creator/assignee |
| INVALID_STATUS_TRANSITION | 400 | Illegal status change (e.g., COMPLETED → OPEN) |
| TASK_HAS_COMMENTS | 409 | Cannot delete task with comments |
| USER_NOT_FOUND | 404 | Assigned user not found |
| INTERNAL_ERROR | 500 | Server error |

---

## Rate Limiting

| Endpoint | Limit | Window |
|----------|-------|--------|
| POST /api/tasks | 10/min | Create not frequent |
| GET /api/tasks | 100/min | List operations high volume |
| GET /api/tasks/{id} | 100/min | Detail view frequent |
| PUT /api/tasks/{id} | 10/min | Updates occasional |
| PUT /api/tasks/{id}/status | 20/min | Status changes frequent |
| DELETE /api/tasks/{id} | 5/min | Deletion rare |
| PUT /api/tasks/{id}/assign | 10/min | Assignment occasional |

---

## Example Request/Response Flow

### Create and Complete Task
1. POST /api/tasks → Create task, returns id=1, status=OPEN
2. PUT /api/tasks/1/assign → Assign to user 2
3. PUT /api/tasks/1/status → Change to IN_PROGRESS
4. PUT /api/tasks/1/status → Change to COMPLETED (sets completedAt)
5. GET /api/tasks/1 → Verify task completed

### Filter and List Tasks
1. GET /api/tasks?status=OPEN&priority=HIGH → Get high-priority open tasks
2. GET /api/tasks?dueDateFrom=2025-02-01&dueDateTo=2025-02-28 → Get February tasks
3. Get /api/tasks?offset=50&limit=50 → Get next page
