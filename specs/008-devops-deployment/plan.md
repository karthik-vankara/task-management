# Implementation Plan: DevOps & Deployment

**Branch**: `008-devops-deployment` | **Phase Duration**: 4-5 days  
**Depends On**: All previous phases (1-7)

---

## Summary

Production-ready deployment: Docker containerization, GitHub Actions CI/CD pipeline, environment separation (dev/staging/prod), database migrations, and monitoring setup.

---

## Technical Context

- **Containerization**: Docker + Docker Compose
- **CI/CD**: GitHub Actions workflows
- **Hosting**: Render (backend) + Vercel (frontend)
- **Database**: PostgreSQL managed service
- **Cost Target**: $0/month (free tiers)

---

## Constitution Check: ✅ PASS

---

## Key Features

### 1. Docker Containerization
- Dockerfile for Java backend (multi-stage build)
- docker-compose.yml for local development (backend + PostgreSQL)
- Image optimization: minimize layers, use Alpine base
- Container size target: < 300MB

### 2. GitHub Actions CI/CD
- Workflow: test → build → deploy
- Test stage: Run Maven test suite
- Build stage: Compile, create JAR, build Docker image
- Deploy stage: Push to registry, deploy to Render
- Run on: Every push to main, Pull Request validation

### 3. Environment Management
- development: localhost, H2 in-memory DB, debug logging
- staging: Render preview environment, PostgreSQL staging DB
- production: Render production, PostgreSQL managed DB
- Use .env files with sensible defaults

### 4. Database Migrations
- Tool: Flyway or Liquibase
- Versioning: V1__initial.sql, V2__add_constraints.sql
- Auto-apply: Run on application startup
- Rollback support: Keep migration history

### 5. Monitoring & Alerts
- Spring Actuator endpoints: /actuator/health, /metrics
- Application logs: Structured logging with SLF4J
- Database monitoring: Query performance monitoring
- Error tracking: Optional (e.g., Sentry free tier)

---

## Implementation Roadmap

**Days 1-2: Docker & Docker Compose (8-10 hours)**
- Create multi-stage Dockerfile for backend
- Create docker-compose.yml for PostgreSQL
- Test local Docker development
- Optimize image size

**Day 3: GitHub Actions CI/CD (6-8 hours)**
- Create GitHub Actions workflow
- Configure test stage with Maven
- Configure build stage (Docker image)
- Configure deploy stage (Render webhook)

**Days 4-5: Migrations & Monitoring (8-10 hours)**
- Set up Flyway for database migrations
- Create initial schema migration
- Implement application health checks
- Configure structured logging
- Set up Render deployment hooks
- Verify production deployment process

---

## Deliverables

### Files to Create/Update
```
backend/
  Dockerfile                 (multi-stage Java build)
  docker-compose.yml         (PostgreSQL + backend)
  .github/workflows/
    deploy.yml              (GitHub Actions CI/CD)
  src/main/resources/db/
    migration/
      V1__initial.sql       (schema + Phase 1)
      V2__phase2.sql        (profiles)
      V3__phase3.sql        (tasks, comments, notifications)
```

### Database Schema Versions
- V1: User, Role tables, OAuth setup
- V2: User profiles extended (name, bio, avatarUrl)
- V3: Task CRUD tables (tasks, comments, notifications, recurring rules)
- V4: Dashboard materialized view (statistics)
- V5: Audit logging table

---

## Testing Checklist

- [ ] Docker build succeeds
- [ ] Docker image size < 300MB
- [ ] Docker Compose starts all services
- [ ] Backend connects to PostgreSQL in Docker
- [ ] GitHub Actions workflow triggers on push
- [ ] CI passes (tests run successfully)
- [ ] Docker image built and pushed
- [ ] Deployment triggers after successful build
- [ ] Application starts on Render
- [ ] Database migrations run on startup
- [ ] /actuator/health endpoint working
- [ ] Application health check passing
- [ ] Logs appear in Render console
- [ ] Production environment variables correct

---

## Success Criteria

✅ Phase 8 Complete:
- Application containerized with Docker
- Multi-stage build optimized
- Docker Compose enables local development
- CI/CD pipeline automated via GitHub Actions
- Database migrations versioned and managed
- Application deployable to Render
- Health checks and monitoring operational
- No manual deployment steps required
- Cost remains $0/month (free tier)
