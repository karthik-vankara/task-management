# Implementation Plan: Dashboard & Analytics

**Branch**: `006-dashboard` | **Phase Duration**: 4-5 days  
**Depends On**: Phase 1 (Auth), Phase 3 (Tasks)

---

## Summary

Build user dashboard with task analytics, statistics, and progress visualization. Provide insights into task completion rates, productivity trends, and workload.

---

## Technical Context

- **Language**: Java 21, Spring Boot 3.3.0
- **Database**: PostgreSQL 15+ with aggregation queries
- **Performance**: <1s dashboard load (cached)
- **Frontend**: React charts (Chart.js, Recharts)

---

## Constitution Check: ✅ PASS

---

## Key Features

### 1. User Dashboard
- Endpoint: `GET /api/dashboard/overview`
- Content:
  - Tasks by status (counts: open, in progress, completed)
  - Upcoming tasks (next 7 days)
  - Recently completed tasks
  - Overdue tasks count
  - Completion rate (%)

### 2. Task Statistics
- Endpoint: `GET /api/dashboard/statistics`
- Returns:
  - Total tasks created
  - Average completion time
  - Tasks by priority distribution
  - Tasks by category distribution
  - Completion rate by priority

### 3. Productivity Timeline
- Endpoint: `GET /api/dashboard/productivity?period=7days`
- Returns:
  - Daily task completion count
  - Daily task creation count
  - Trend data for chart rendering

### 4. Workload Management
- Endpoint: `GET /api/dashboard/workload`
- Returns:
  - Tasks assigned to user (count, status)
  - Tasks created by user (count, status)
  - Capacity warning if > 20 open tasks

---

## Database Changes

```sql
-- No new tables needed, but add computed materialized view for performance

CREATE MATERIALIZED VIEW dashboard_statistics AS
SELECT 
    u.id as user_id,
    COUNT(DISTINCT t.id) as total_tasks,
    COUNT(DISTINCT CASE WHEN t.status = 'COMPLETED' THEN t.id END) as completed_tasks,
    COUNT(DISTINCT CASE WHEN t.status = 'OPEN' THEN t.id END) as open_tasks,
    COUNT(DISTINCT CASE WHEN t.status = 'IN_PROGRESS' THEN t.id END) as in_progress_tasks,
    ROUND(100.0 * COUNT(DISTINCT CASE WHEN t.status = 'COMPLETED' THEN t.id END) / 
          NULLIF(COUNT(DISTINCT t.id), 0), 2) as completion_rate,
    AVG(EXTRACT(DAY FROM t.completed_at - t.created_at)) as avg_completion_days
FROM users u
LEFT JOIN tasks t ON u.id = t.user_id
GROUP BY u.id;

-- Refresh periodically (every hour)
CREATE INDEX idx_dashboard_user ON dashboard_statistics(user_id);
```

---

## Implementation Roadmap

**Days 1-2: Dashboard Overview (6-8 hours)**
- Create dashboard service with aggregation queries
- Implement overview endpoint
- Add caching (Redis or Spring cache)
- Optimize queries for performance

**Days 3: Statistics & Timeline (6-8 hours)**
- Implement statistics endpoint
- Implement productivity timeline
- Add period parameter (7days, 30days, 90days, all)
- Test with large datasets

**Days 4-5: Frontend & Testing (6-8 hours)**
- Create React dashboard component
- Implement charts (Chart.js or Recharts)
- Add manual testing checklist
- Performance testing (load time < 1s)

---

## Testing Checklist

- [ ] Get dashboard overview
- [ ] Verify task counts by status
- [ ] Verify upcoming tasks list
- [ ] Verify overdue tasks identified
- [ ] Get statistics endpoint
- [ ] Verify completion rate calculation
- [ ] Get productivity timeline for 7 days
- [ ] Get productivity timeline for 30 days
- [ ] Get workload summary
- [ ] Verify capacity warning triggers at 20+ open tasks
- [ ] Dashboard loads in < 1 second
- [ ] Charts render correctly on frontend

---

## Success Criteria

✅ Phase 6 Complete:
- Dashboard overview showing all key metrics
- Statistics endpoint working correctly
- Productivity timeline data generated
- Workload management alerts functioning
- Charts rendering on React frontend
- Dashboard loads in < 1 second
- All manual tests passing
