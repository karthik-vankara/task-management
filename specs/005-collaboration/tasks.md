# Phase 5: Collaboration & Notifications - Implementation Tasks

**Branch:** `phase/5-collaboration`  
**Estimated Duration:** 6-7 days  
**Depends On:** Phases 1-4 (previous phases complete)

---

## Backend Tasks

### T001-T010: Comments Implementation
- [ ] T001 Create TaskComment entity in migration `V7__create_task_comments.sql`
- [ ] T002 Create CommentRepository extending JpaRepository
- [ ] T003 Implement `POST /api/tasks/{id}/comments` endpoint (add comment)
- [ ] T004 Implement `GET /api/tasks/{id}/comments` endpoint (list comments)
- [ ] T005 Implement `PUT /api/tasks/{id}/comments/{commentId}` endpoint (edit comment)
- [ ] T006 Implement `DELETE /api/tasks/{id}/comments/{commentId}` endpoint (soft delete comment)
- [ ] T007 Create CommentDTO with user info
- [ ] T008 Add authorization: only comment author can edit/delete
- [ ] T009 Add comment text validation (1-5000 chars)
- [ ] T010 Test comment CRUD (manual: add/edit/delete comments)

### T011-T020: @Mentions Implementation
- [ ] T011 Parse comment text for @username mentions
- [ ] T012 Create MentionParser utility in `src/main/java/com/karthik/task_management_backend/util/MentionParser.java`
- [ ] T013 Extract user mentions from comment before saving
- [ ] T014 Validate mentioned users exist
- [ ] T015 Create mention links in comment response
- [ ] T016 Test mention detection (manual: add comment with @user)
- [ ] T017 Test invalid mentions (manual: @nonexistent rejected)
- [ ] T018 Add URL-friendly mention links
- [ ] T019 Test mention extraction from complex comments
- [ ] T020 Add mention count to comment metadata

### T021-T030: Notifications System
- [ ] T021 Create Notification entity in migration `V8__create_notifications.sql`
- [ ] T022 Add notification types: COMMENT, MENTIONED, STATUS_CHANGED, ASSIGNED
- [ ] T023 Create NotificationRepository
- [ ] T024 Implement `GET /api/notifications` endpoint (list user notifications)
- [ ] T025 Implement `PUT /api/notifications/{id}/read` endpoint (mark read)
- [ ] T026 Implement notification creation when events occur
- [ ] T027 Add notification preferences (optional: email, push, in-app)
- [ ] T028 Test notification generation (manual: comment → notification created)
- [ ] T029 Test mark as read (manual: notification status updates)
- [ ] T030 Add notification pagination

### T031-T040: Activity Feed & Event Tracking
- [ ] T031 Create ActivityFeed entity for audit trail
- [ ] T032 Track events: task created, status changed, commented, assigned
- [ ] T033 Implement `GET /api/tasks/{id}/activity` endpoint (task activity log)
- [ ] T034 Implement `GET /api/users/me/feed` endpoint (personal activity feed)
- [ ] T035 Add timestamp and user info to events
- [ ] T036 Create event type enum (CREATE, UPDATE, STATUS_CHANGE, COMMENT, ASSIGN)
- [ ] T037 Test activity tracking (manual: perform action, verify logged)
- [ ] T038 Add activity pagination
- [ ] T039 Add activity filtering by event type
- [ ] T040 Test activity feed retrieval

### T041-T050: WebSocket Real-Time Updates
- [ ] T041 Configure Spring WebSocket in SecurityConfig
- [ ] T042 Create WebSocketConfig in `src/main/java/com/karthik/task_management_backend/config/WebSocketConfig.java`
- [ ] T043 Implement WebSocket endpoint `/ws/tasks/{taskId}`
- [ ] T044 Create WebSocketHandler for task subscriptions
- [ ] T045 Broadcast task updates to subscribed users (status change)
- [ ] T046 Broadcast comment notifications when new comment added
- [ ] T047 Handle WebSocket connection/disconnection
- [ ] T048 Test WebSocket connection (manual: connect, verify open)
- [ ] T049 Test real-time status update (manual: user 1 changes status, user 2 receives update)
- [ ] T050 Test concurrent connections (multiple users on same task)

---

## Frontend Tasks

### T051-T060: Comments UI Components
- [ ] T051 Create CommentList component in `src/components/CommentList.tsx`
- [ ] T052 Create CommentForm component for adding comments
- [ ] T053 Create CommentItem component for displaying individual comments
- [ ] T054 Add edit button to CommentItem (edit own comments)
- [ ] T055 Add delete button with confirmation
- [ ] T056 Implement comment text rendering with proper formatting
- [ ] T057 Test comment list loading (manual: fetch and display comments)
- [ ] T058 Test add comment (manual: submit form, appears in list)
- [ ] T059 Test edit comment (manual: edit text, save, updates)
- [ ] T060 Test delete comment (manual: confirm, disappears from list)

### T061-T070: @Mentions UI
- [ ] T061 Add mention parsing to comment rendering
- [ ] T062 Create @mention link styling (clickable, navigates to user profile)
- [ ] T063 Implement mention autocomplete in comment form
- [ ] T064 Add user search dropdown on "@" character in form
- [ ] T065 Test mention autocomplete (manual: type @ in comment, suggestions appear)
- [ ] T066 Test mention link navigation (manual: click mention, navigate to user)
- [ ] T067 Add mention highlighting in rendered comments
- [ ] T068 Test mention parsing with multiple mentions
- [ ] T069 Add emoji support in comments (optional)
- [ ] T070 Test mention extraction from long comments

### T071-T080: Notifications UI
- [ ] T071 Create NotificationBell component in `src/components/NotificationBell.tsx`
- [ ] T072 Create NotificationPanel component for dropdown list
- [ ] T073 Display notification count badge on bell icon
- [ ] T074 Implement mark as read on notification click
- [ ] T075 Add notification type icons (comment, mention, assignment)
- [ ] T076 Implement notification grouping (by date)
- [ ] T077 Test notification display (manual: notification appears)
- [ ] T078 Test mark as read (manual: badge count decreases)
- [ ] T079 Add notification clear/delete functionality
- [ ] T080 Test notification sorting (newest first)

### T081-T090: Activity Feed UI
- [ ] T081 Create ActivityFeed component in `src/components/ActivityFeed.tsx`
- [ ] T082 Create ActivityItem component for displaying events
- [ ] T083 Add activity timeline styling
- [ ] T084 Display event type, user, timestamp, and action description
- [ ] T085 Add "View Task" link in activity items
- [ ] T086 Test activity feed loading (manual: fetch and display events)
- [ ] T087 Add activity filtering UI (filter by event type)
- [ ] T088 Test activity pagination (manual: load more events)
- [ ] T089 Add activity search by description
- [ ] T090 Test activity item navigation

### T091-T100: WebSocket Integration
- [ ] T091 Create WebSocket service in `src/services/websocketService.ts`
- [ ] T092 Implement task subscription (connect to `/ws/tasks/{taskId}`)
- [ ] T093 Handle real-time status updates from WebSocket
- [ ] T094 Handle real-time comment notifications
- [ ] T095 Update UI when WebSocket message received
- [ ] T096 Test WebSocket connection (manual: open dev tools, verify connection)
- [ ] T097 Test real-time updates (manual: user 1 changes status, user 2 sees update)
- [ ] T098 Handle WebSocket disconnection and reconnection
- [ ] T099 Add reconnection retry logic with exponential backoff
- [ ] T100 Test concurrent WebSocket connections

---

## Integration Tests

### T101-T105: End-to-End Collaboration Workflows
- [ ] T101 E2E: User 1 adds comment → User 2 receives notification in real-time
- [ ] T102 E2E: User 1 mentions @User2 in comment → User2 gets mentioned notification
- [ ] T103 E2E: User 1 changes task status → User 2 sees update via WebSocket
- [ ] T104 E2E: User 1 assigns task to User2 → User2 sees notification and activity log
- [ ] T105 E2E: Multiple users viewing same task → all receive updates simultaneously

---

## Acceptance Criteria

- [ ] Comments CRUD fully functional
- [ ] @mention detection and links working
- [ ] Notifications generated for all events
- [ ] Real-time WebSocket updates working
- [ ] Activity feed displaying all events
- [ ] Multiple concurrent connections stable
- [ ] All manual tests passing

---

## Git Workflow

```bash
git checkout -b phase/5-collaboration origin/main
git commit -m "[PHASE 5] Collaboration and notifications implementation"
git checkout main && git merge --ff-only phase/5-collaboration
git tag v0.5.0 -a -m "Phase 5: Collaboration & Notifications Complete"
```

---

## Success Criteria Summary

✅ **Phase 5 Implementation Complete:**
- Task comments with @mention support
- Notifications system for all events
- Real-time WebSocket updates working
- Activity feed visible to users
- Collaboration foundation ready for Phase 6
