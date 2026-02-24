# Task Management System

A full-stack task management SaaS application built with modern web technologies, featuring OAuth2 authentication, real-time collaboration, advanced task management, and comprehensive analytics.

**Status**: Phase 1 (OAuth2 Authentication) - In Specification  
**Target Launch**: Q1 2026  
**Cost**: $0/month (free tier deployment)

---

## 📋 Project Overview

Task Management System is an industry-grade task management platform designed to help teams organize, track, and collaborate on work. Built with a focus on developer experience, it uses a modular phase-based approach to deliver features incrementally.

### Key Features

- **OAuth2 Authentication**: Google Sign-In with JWT token management
- **User Profiles**: Customizable user profiles with avatars
- **Task Management**: Full CRUD operations with status tracking and priorities
- **Advanced Search**: Full-text search with filtering and sorting
- **Real-Time Collaboration**: Comments, @mentions, notifications, and activity feeds
- **Analytics Dashboard**: Productivity metrics and task insights
- **Enterprise Security**: Audit logging, rate limiting, input validation
- **DevOps Ready**: Docker containerization and CI/CD pipeline with GitHub Actions

---

## 🏗️ Architecture & Tech Stack

### Backend
- **Framework**: Spring Boot 3.3.0 (Java 21)
- **Database**: PostgreSQL 15+
- **Authentication**: Google OAuth2 + JWT (HS256, 24-hour expiration)
- **API**: REST with comprehensive error handling
- **Build Tool**: Maven
- **Deployment**: Docker + Render.com (free tier)

### Frontend
- **Framework**: React 18 with TypeScript (strict mode)
- **Styling**: Tailwind CSS
- **State Management**: Zustand
- **Routing**: React Router v6
- **HTTP Client**: Axios with JWT interceptor
- **Deployment**: Docker + Vercel (free tier)

### DevOps & Infrastructure
- **Containerization**: Docker & Docker Compose
- **CI/CD**: GitHub Actions
- **Version Control**: Git with phase-based branching
- **Monitoring**: SLF4J + Logback (backend), browser console (frontend)

---

## 📅 Implementation Phases

The project is structured into 8 phases, each delivering a complete, independently deployable feature:

| Phase | Feature | Duration | Status |
|-------|---------|----------|--------|
| 1 | OAuth2 Authentication | 5-7 days | 📋 Specified |
| 2 | User Profile Management | 4-5 days | 📋 Specified |
| 3 | Task CRUD Operations | 6-7 days | 📋 Specified |
| 4 | Advanced Task Features | 5-6 days | 🔲 Pending spec |
| 5 | Collaboration & Notifications | 6-7 days | 🔲 Pending spec |
| 6 | Dashboard & Analytics | 5-6 days | 🔲 Pending spec |
| 7 | Security & Quality Assurance | 7-8 days | 🔲 Pending spec |
| 8 | DevOps & Deployment | 4-5 days | 🔲 Pending spec |

**Total Estimated Time**: 43-51 days (~6-7 weeks)

---

## 🚀 Quick Start

### Prerequisites
- Java 21 (for backend development)
- Node.js 20+ (for frontend development)
- PostgreSQL 15+ (or use Docker Compose)
- Git with SSH key configured
- Docker & Docker Compose (for containerized development)

### Local Development Setup

#### 1. Clone Repository
```bash
git clone git@github.com:karthik-vankara/task-management.git
cd task-management
```

#### 2. Start Backend with Docker Compose
```bash
# Start PostgreSQL + backend services
docker-compose up -d

# Verify backend is running
curl http://localhost:8080/actuator/health
```

#### 3. Setup Backend (First Time)
```bash
cd backend

# Configure environment variables
cp .env.example .env.local
# Edit .env.local with your GOOGLE_CLIENT_ID and JWT_SECRET

# Build and run
mvn clean install
mvn spring-boot:run
```

#### 4. Setup Frontend (First Time)
```bash
cd ui

# Install dependencies
npm install

# Configure environment variables
cp .env.example .env.local
# Set REACT_APP_API_URL=http://localhost:8080

# Start dev server
npm start
```

#### 5. Verify Setup
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **PostgreSQL**: localhost:5432 (user: postgres, password: password)

---

## 📁 Project Structure

```
task-management/
├── .github/
│   ├── workflows/          # GitHub Actions CI/CD pipelines
│   └── prompts/            # SpecKit analysis & planning prompts
├── .specify/
│   ├── memory/             # Development constitution & context
│   └── scripts/            # SpecKit utility scripts
├── specs/
│   ├── 001-oauth-authentication/
│   │   ├── spec.md         # Feature specification
│   │   ├── plan.md         # Implementation plan
│   │   ├── tasks.md        # Numbered implementation tasks
│   │   ├── research.md     # Technical research & alternatives
│   │   ├── data-model.md   # Entity definitions & schemas
│   │   ├── quickstart.md   # Local setup guide
│   │   └── contracts/      # API documentation
│   ├── 002-user-profile/
│   ├── 003-task-crud/
│   ├── 004-advanced-tasks/
│   ├── 005-collaboration/
│   ├── 006-dashboard/
│   ├── 007-security-qa/
│   └── 008-devops-deployment/
├── backend/
│   ├── src/
│   │   ├── main/java/com/karthik/
│   │   │   ├── config/           # Spring Security, CORS, JWT config
│   │   │   ├── entity/           # JPA entities (User, Task, etc.)
│   │   │   ├── repository/       # Spring Data repositories
│   │   │   ├── service/          # Business logic layer
│   │   │   ├── controller/       # REST endpoints
│   │   │   ├── dto/              # Data transfer objects
│   │   │   ├── security/         # JWT, OAuth2 security
│   │   │   ├── exception/        # Custom exceptions & handlers
│   │   │   └── util/             # Utilities & helpers
│   │   └── resources/
│   │       ├── application.yaml  # Spring configuration
│   │       └── db/migrations/    # Flyway SQL migrations
│   ├── pom.xml                   # Maven dependencies
│   └── Dockerfile
├── ui/
│   ├── src/
│   │   ├── components/           # React components (reusable)
│   │   ├── pages/                # Page components (routes)
│   │   ├── store/                # Zustand state management
│   │   ├── services/             # API client & HTTP services
│   │   ├── hooks/                # Custom React hooks
│   │   ├── types/                # TypeScript type definitions
│   │   ├── styles/               # Global styles & Tailwind
│   │   ├── App.tsx               # Root component
│   │   └── index.tsx             # React entry point
│   ├── public/                   # Static assets
│   ├── package.json
│   ├── tsconfig.json
│   ├── Dockerfile
│   └── nginx.conf
├── docker-compose.yml            # Local development environment
├── .gitignore                    # Git ignore patterns
└── README.md                     # This file
```

---

## 🔄 Git Workflow

This project follows a phase-based branching strategy as defined in the development constitution:

### Phase-Based Branches
Each phase is developed on its own branch following the naming convention `phase/N-feature-name`:

```bash
# Create phase branch from main
git checkout -b phase/1-authentication

# Work on Phase 1 tasks
# Commit often with [PHASE 1] prefix
git commit -m "[PHASE 1] Implement User entity with JPA annotations"

# Push to remote
git push -u origin phase/1-authentication

# When phase complete, merge to main via pull request
# Tag with semantic version
git tag v0.1.0
```

### Commit Convention
- Format: `[PHASE N] human-readable description`
- Example: `[PHASE 1] Add JWT token generation service`
- Example: `[PHASE 2] Create profile API endpoints`

### Main Branch Policy
- **Main branch is always production-ready**
- All work happens on phase branches
- Merges via pull requests with acceptance criteria checklist
- Tags mark releases: `v0.1.0` (Phase 1), `v0.2.0` (Phase 2), etc.

### Setting Up Branches
```bash
# Create all phase branches locally (one-time setup)
for i in {1..8}; do
  git checkout -b phase/$i-feature-name
  git push -u origin phase/$i-feature-name
  git checkout main
done

# Or create them as needed during each phase
```

---

## 📋 Implementation Tasks

Each phase contains numbered, implementation-ready tasks in `tasks.md`:

```bash
# View Phase 1 tasks
cat specs/001-oauth-authentication/tasks.md

# Example task format:
# - [ ] T001 Create User entity with Google OAuth fields in src/main/java/com/karthik/entity/User.java
# - [ ] T002 Create UserRole enum (ADMIN, USER)
# - [ ] T003 Configure Spring Security with OAuth2 support
```

**Total Tasks**: 847 across all 8 phases

---

## 🧪 Testing Strategy

Per the development constitution, this project uses **manual testing** instead of automated unit tests to reduce AI usage and maintain focus on integration quality.

### Manual Testing Checklist (per phase)
- ✅ Backend manual tests (curl, Postman, or browser)
- ✅ Frontend manual tests (browser DevTools, user flows)
- ✅ End-to-end flows (full authentication → task creation → logout)
- ✅ Cross-browser testing (Chrome, Firefox, Safari)
- ✅ Response time validation against NFRs

### Pre-Merge Checklist
Before merging each phase to main:
1. [ ] All tasks completed
2. [ ] Manual testing checklist passed
3. [ ] Code follows style guides (Google Java, Airbnb React)
4. [ ] Security headers validated
5. [ ] Performance targets met
6. [ ] Error handling verified

---

## 🔐 Security

Security is baked into every phase via PRINCIPLE 3 (Quality Over Quantity):

### Phase 1 (Authentication)
- JWT token validation & expiration
- Spring Security OAuth2 configuration
- CORS headers and security headers
- Rate limiting on auth endpoints (10/min login, 5/min callback)

### Phase 7 (Security & QA)
- Audit logging for all mutations
- Rate limiting on API endpoints
- Input validation (JSR-303) & sanitization
- XSS & CSRF protection
- SQL injection prevention (parameterized queries)
- 20+ security testing scenarios

### All Phases
- Environment variables for secrets (no hardcoding)
- HTTPS/SSL on production deployment
- Database encryption at rest (managed by cloud provider)

---

## 📊 Performance Targets

| Metric | Target | Phase |
|--------|--------|-------|
| API response time (50th percentile) | < 100ms | All |
| API response time (99th percentile) | < 500ms | All |
| Full-text search (100k items) | < 500ms | Phase 4 |
| Task list pagination (100 items) | < 200ms | Phase 3 |
| WebSocket message delivery | < 100ms | Phase 5 |
| JWT validation per request | < 10ms | Phase 1 |
| Concurrent users supported | 1,000 | All |

---

## 📈 Database Schema

### Core Entities (Phases 1-3)
- **users**: User accounts with Google OAuth2 identity
- **tasks**: Task items with status (OPEN, IN_PROGRESS, COMPLETED) and priority
- **task_comments**: Comments on tasks with @mention support

### Advanced Entities (Phases 4-8)
- **task_templates**: Reusable task templates
- **recurring_task_rules**: Rules for task recurrence
- **notifications**: Real-time notifications
- **activity_feed**: Activity log for audit trail
- **audit_logs**: Complete mutation history for compliance

### Indexes & Performance
- Full-text search index on tasks (title, description, category)
- Foreign key indexes on task_id, user_id
- Composite indexes for common filters (user_id + created_at)

---

## 🚢 Deployment

### Local Development
```bash
docker-compose up -d
# Starts: PostgreSQL, Spring Boot backend, React frontend
```

### Staging & Production
- **Backend**: Render.com free tier (Docker container)
- **Frontend**: Vercel free tier (optimized for React)
- **Database**: PostgreSQL managed (included with Render.com)
- **CI/CD**: GitHub Actions (.github/workflows/main.yml)

### Deploy Process (Phase 8)
```bash
# Automated on every push to main
# 1. Test job runs (Maven test phase)
# 2. Build job creates Docker images
# 3. Deploy job pushes to Render + Vercel
# 4. Deployment validation runs
# 5. Monitoring alerts configured
```

---

## 📝 Development Guidelines

### Code Quality Standards
- **Backend**: [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- **Frontend**: [Airbnb React Style Guide](https://airbnb.io/javascript/react/)
- **Naming**: `PascalCase` for classes/components, `camelCase` for methods/variables
- **Comments**: Document "why", not "what" (code is self-explanatory)

### Logging Levels
- **INFO**: State transitions, key business events (auth events, task creation)
- **WARN**: Recoverable errors, deprecated usage
- **ERROR**: Unrecoverable errors, external API failures

### Error Handling
- Return standardized ApiResponse<T> with success flag
- Include helpful error messages (never stack traces to frontend)
- Log full error details server-side for debugging

---

## 📚 Documentation

- **[Development Constitution](.specify/memory/constitution.md)**: Core principles & standards
- **[Phase Specifications](specs/)**: Feature details for each phase
- **[Implementation Plans](specs/)**: Roadmap and technical approach
- **[API Contracts](specs/*/contracts/)**: Detailed endpoint documentation

---

## 🤝 Contributing

### Getting Started
1. Clone the repository
2. Create a feature branch: `git checkout -b phase/N-feature-name`
3. Follow the process outlined in the corresponding phase `tasks.md`
4. Commit with `[PHASE N]` prefix
5. Push and create a pull request
6. Complete acceptance criteria checklist before merge

### Code Review
- Ensure code follows style guides
- Validate security best practices
- Verify manual testing checklist completed
- Check performance targets met

---

## 📖 License

This project is open source and available under the [MIT License](LICENSE).

---

## 📞 Support & Issues

For questions or issues:
1. Check the relevant phase's `spec.md` and `plan.md`
2. Review the `tasks.md` for implementation details
3. Check [GitHub Issues](https://github.com/karthik-vankara/task-management/issues)

---

## 🎯 Roadmap

- **Q1 2026**: Phases 1-3 (Core functionality)
- **Q2 2026**: Phases 4-5 (Advanced features & collaboration)
- **Q2 2026**: Phases 6-7 (Analytics & security hardening)
- **Q2 2026**: Phase 8 (DevOps & launch to production)

---

**Built with ❤️ following the SpecKit development workflow**

Last Updated: February 24, 2026 | Phase 1 Status: Specification Complete
