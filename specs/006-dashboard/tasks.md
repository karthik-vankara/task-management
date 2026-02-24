# Phase 6: Dashboard & Analytics - Implementation Tasks

**Branch:** `phase/6-dashboard`  
**Estimated Duration:** 4-5 days  
**Depends On:** Phases 1-5 (previous phases complete)

---

## Backend Tasks

### T001-T010: Analytics Endpoints
- [ ] T001 Create AnalyticsService in `src/main/java/com/karthik/task_management_backend/service/AnalyticsService.java`
- [ ] T002 Implement `GET /api/dashboard/overview` endpoint
- [ ] T003 Create DashboardOverviewDTO with task counts and statistics
- [ ] T004 Calculate: open tasks, in-progress, completed, completion rate
- [ ] T005 Retrieve: upcoming tasks (next 7 days), overdue tasks
- [ ] T006 Add recent completed tasks to overview
- [ ] T007 Test overview endpoint (manual: GET endpoint, verify calculations)
- [ ] T008 Add caching for dashboard data (1-hour TTL)
- [ ] T009 Implement cache invalidation on task updates
- [ ] T010 Optimize queries for performance (< 1s response)

### T011-T020: Statistics & Aggregations
- [ ] T011 Implement `GET /api/dashboard/statistics` endpoint
- [ ] T012 Calculate total tasks, average completion time
- [ ] T013 Aggregate tasks by priority distribution
- [ ] T014 Aggregate tasks by category distribution
- [ ] T015 Calculate completion rate by priority
- [ ] T016 Create StatisticsDTO for response
- [ ] T017 Create materialized view for statistics (V9__create_statistics_view.sql)
- [ ] T018 Test statistics calculations (manual: verify aggregations)
- [ ] T019 Add statistics filtering (date range optional)
- [ ] T020 Test statistics performance (< 500ms response)

### T021-T030: Productivity Timeline
- [ ] T021 Implement `GET /api/dashboard/productivity?period=7days` endpoint
- [ ] T022 Support period options: 7days, 30days, 90days, all
- [ ] T023 Query task completion by day
- [ ] T024 Query task creation by day
- [ ] T025 Create ProductivityTimelineDTO with daily counts
- [ ] T026 Add trend analysis (upward/downward)
- [ ] T027 Test productivity timeline (manual: verify daily counts)
- [ ] T028 Test period parameter (manual: 7days vs 30days different)
- [ ] T029 Test timeline with no tasks
- [ ] T030 Optimize query performance (< 500ms)

### T031-T040: Workload Management
- [ ] T031 Implement `GET /api/dashboard/workload` endpoint
- [ ] T032 Calculate tasks assigned to user (count by status)
- [ ] T033 Calculate tasks created by user (count by status)
- [ ] T034 Add capacity warning if > 20 open tasks
- [ ] T035 Create WorkloadDTO with warnings
- [ ] T036 Add workload score calculation
- [ ] T037 Test workload endpoint (manual: verify counts)
- [ ] T038 Test capacity warning trigger (manual: create > 20 tasks)
- [ ] T039 Add workload trend (more/fewer tasks than last week)
- [ ] T040 Test workload with no open tasks

---

## Frontend Tasks

### T041-T050: Dashboard Layout
- [ ] T041 Create DashboardPage component in `src/pages/DashboardPage.tsx`
- [ ] T042 Create dashboard grid layout (responsive, 2-column on desktop, 1-column mobile)
- [ ] T043 Create StatisticsWidget component for dashboard cards
- [ ] T044 Create TaskCountWidget component (open, in-progress, completed)
- [ ] T045 Add refresh button for dashboard data
- [ ] T046 Implement dashboard data fetching on page load
- [ ] T047 Test dashboard page rendering (manual: layout displays)
- [ ] T048 Test responsive layout (manual: desktop and mobile)
- [ ] T049 Add loading states while fetching data
- [ ] T050 Add error states if data fetch fails

### T051-T060: Statistics Visualization
- [ ] T051 Install charting library (Chart.js or Recharts)
- [ ] T052 Create CompletionRateChart component (pie chart)
- [ ] T053 Create PriorityDistributionChart component (bar chart)
- [ ] T054 Create CategoryDistributionChart component (horizontal bar)
- [ ] T055 Test chart rendering (manual: verify charts display)
- [ ] T056 Add chart legends and labels
- [ ] T057 Add chart hover tooltips
- [ ] T058 Add chart legend click filtering
- [ ] T059 Test chart responsiveness (manual: resize window)
- [ ] T060 Add chart color customization

### T061-T070: Productivity Timeline
- [ ] T061 Create ProductivityChart component (line chart)
- [ ] T062 Display daily task creation count
- [ ] T063 Display daily completion count
- [ ] T064 Add two-line chart (creation vs completion)
- [ ] T065 Create period selector (7days, 30days, 90days, all)
- [ ] T066 Update chart when period changes
- [ ] T067 Test timeline chart (manual: verify data points)
- [ ] T068 Add trend indicator (arrow up/down)
- [ ] T069 Add X-axis date labels
- [ ] T070 Test timeline with different periods

### T071-T080: Workload & Recommendations
- [ ] T071 Create WorkloadSummary component
- [ ] T072 Display task counts: assigned, created, capacity
- [ ] T073 Show capacity warning if triggered
- [ ] T074 Create RecentActivityWidget component
- [ ] T075 Display recent completed tasks
- [ ] T076 Display upcoming due tasks
- [ ] T077 Show workload trend (more tasks than last week)
- [ ] T078 Test workload display (manual: verify counts)
- [ ] T079 Test capacity warning display
- [ ] T080 Add recommendations based on workload

### T081-T090: Dashboard Interactivity
- [ ] T081 Add click-through from statistics to task list
- [ ] T082 Add date range picker for custom period
- [ ] T083 Create "View All Tasks" link from widgets
- [ ] T084 Add metric detail pages (click task count → filter to OPEN)
- [ ] T085 Implement dashboard customization (show/hide widgets)
- [ ] T086 Save dashboard preferences to localStorage
- [ ] T087 Test widget interactions (manual: click through to tasks)
- [ ] T088 Add export dashboard as PDF (optional)
- [ ] T089 Add refresh interval option
- [ ] T090 Test all dashboard interactions

---

## Integration Tests

### T091-T095: End-to-End Dashboard Workflows
- [ ] T091 E2E: User completes task → dashboard completion count increases
- [ ] T092 E2E: User creates multiple tasks → workload chart updates
- [ ] T093 E2E: User views productivity timeline → accurate daily data
- [ ] T094 E2E: Dashboard click-through to tasks → filter applied correctly
- [ ] T095 E2E: Dashboard refreshes on task updates

---

## Acceptance Criteria

- [ ] Dashboard overview showing all key metrics
- [ ] Statistics calculated and displayed correctly
- [ ] Productivity timeline accurate
- [ ] Workload management functional
- [ ] All charts rendering and interactive
- [ ] Dashboard responsive and performant
- [ ] All manual tests passing

---

## Git Workflow

```bash
git checkout -b phase/6-dashboard origin/main
git commit -m "[PHASE 6] Dashboard and analytics implementation"
git checkout main && git merge --ff-only phase/6-dashboard
git tag v0.6.0 -a -m "Phase 6: Dashboard & Analytics Complete"
```

---

## Success Criteria Summary

✅ **Phase 6 Implementation Complete:**
- Dashboard overview with all key metrics displayed
- Statistics visualized with charts
- Productivity timeline tracking trends
- Workload management with capacity warnings
- Ready for Phase 7 (security hardening)
