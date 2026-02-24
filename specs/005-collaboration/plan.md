# Implementation Plan: Collaboration & Notifications

**Branch**: `005-collaboration` | **Phase Duration**: 6-7 days  
**Depends On**: Phase 2 (User Profiles), Phase 3 (Tasks)

---

## Summary

Enable task collaboration: task comments, @mentions, real-time notifications, and activity feed. Build foundation for multi-user task workflows.

---

## Technical Context

- **Language**: Java 21, Spring Boot 3.3.0 + WebSocket
- **Database**: PostgreSQL 15+ with JSON notifications
- **Real-time**: Spring WebSocket for live updates
- **Performance**: <100ms comment creation, <500ms feed loading

---

## Constitution Check: ✅ PASS

---

## Key Features

### 1. Task Comments
- Endpoint: `POST /api/tasks/{id}/comments` (add comment)
- Endpoint: `GET /api/tasks/{id}/comments` (list comments)
- Field: CommentText (1-5000 chars, supports @mentions)
- History: Edit/delete comments with timestamp tracking

### 2. @Mentions
- Format: `@username` in comment text
- Detection: Parse comment text for @mentions
- Notification: System sends notification to mentioned user
- Link: Mentioned user can navigate to task

### 3. Activity Feed & Notifications
- Track events: Task created, status changed, assigned, commented
- Endpoint: `GET /api/users/me/notifications` (paginated)
- Endpoint: `GET /api/users/me/feed` (activity log)
- Mark read: `PUT /api/notifications/{id}/read`

### 4. Real-Time Updates (WebSocket)
- Subscribe to task: User receives live comments, status changes
- Endpoint: `ws://api/tasks/{id}/subscribe`
- Broadcast: All connected users see changes in real-time

---

## Database Changes

```sql
-- Comments table
CREATE TABLE task_comments (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Activity/notifications table
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    event_type VARCHAR(50), -- COMMENT, STATUS_CHANGE, ASSIGNED, MENTIONED
    task_id BIGINT REFERENCES tasks(id),
    triggered_by BIGINT REFERENCES users(id),
    content TEXT,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_notifications_user_read ON notifications(user_id, read_at);
CREATE INDEX idx_comments_task ON task_comments(task_id);
```

---

## Implementation Roadmap

**Days 1-2: Comments & @Mentions (10-12 hours)**
- Implement TaskComment entity
- Build comment endpoints (CRUD)
- Parse @mentions from comment text
- Create mention detection logic

**Days 3-4: Notifications & Feed (10-12 hours)**
- Create Notification entity
- Build event tracking system
- Implement notification endpoints
- Build activity feed aggregation

**Days 5-6: WebSocket Real-Time (8-10 hours)**
- Configure Spring WebSocket
- Implement task subscription
- Broadcast task changes to subscribers
- Test concurrent connections

**Days 6-7: Testing (6-8 hours)**
- Manual testing checklist
- WebSocket connection testing
- Concurrent user scenarios

---

## Testing Checklist

- [ ] Add comment to task
- [ ] Edit comment
- [ ] Delete comment (soft delete)
- [ ] Parse @mentions from comment
- [ ] Send notification to mentioned user
- [ ] List comments on task
- [ ] Get all notifications for user
- [ ] Mark notification as read
- [ ] Load activity feed (paginated)
- [ ] Subscribe to task via WebSocket
- [ ] Receive comment notification via WebSocket
- [ ] Receive status change notification via WebSocket
- [ ] Multiple users connected to same task receive updates
- [ ] WebSocket connection cleanup on disconnect

---

## Success Criteria

✅ Phase 5 Complete:
- Comments CRUD working with @mention support
- Notifications generated for all events
- Activity feed displaying correctly
- WebSocket real-time updates working
- Multiple concurrent connections stable
- All manual tests passing
