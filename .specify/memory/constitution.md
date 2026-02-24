# Task Management System - Development Constitution

**Version:** 1.0.0  
**Ratification Date:** 2026-02-23  
**Last Amended Date:** 2026-02-23

---

## Preamble

This Constitution establishes the foundational principles, standards, and governance for the Task Management System development. All development, deployment, and architectural decisions shall conform to these principles. This document is binding and supersedes ad-hoc decisions.

---

## Project Overview

Building an industry-ready Task Management SaaS application with minimal cost using modern tech stack.

- **Target Duration:** 8 phases, 4-6 weeks estimated
- **Team Structure:** Solo developer
- **Deployment Target:** Free tier cloud services (Vercel, Render)
- **Cost Model:** $0/month target

---

## Core Principles

### PRINCIPLE 1: Spec-First Development
**Non-Negotiable Rules:**
- Define specifications BEFORE writing any code
- Specifications are the single source of truth
- Use SpecKit command order: Constitution → Specify → Plan → Tasks → Implement
- No exploratory coding; all changes must reference a spec
- Spec changes only via amendment to constitution

**Rationale:**
Prevents rework, ensures architecture consistency, reduces cognitive overhead during implementation.

### PRINCIPLE 2: Minimal Cost & Open Source
**Non-Negotiable Rules:**
- Use ONLY free tier or open-source technologies
- No paid SaaS services (exceptions: domain registration, essential services approved monthly)
- Efficient resource utilization (no over-engineering)
- Prefer battle-tested libraries over custom implementations
- Monthly cost review documented

**Rationale:**
Ensures project sustainability and accessibility to broader community. Open source enables long-term maintainability.

### PRINCIPLE 3: Quality Over Quantity
**Non-Negotiable Rules:**
- Industry-grade code quality standards applied from day 1
- Security best practices enforced in every layer
- Code follows language-specific style guides
- Clean architecture principles (separation of concerns, SOLID)
- Manual testing before each merge (no automated unit tests to reduce AI usage)
- Must achieve code review standard before merge

**Rationale:**
Technical debt compounds. Quality foundation enables fast iteration without rework.

### PRINCIPLE 4: Phase-Based Delivery
**Non-Negotiable Rules:**
- Each phase produces a complete, independently deployable feature
- Git branch created per phase: `phase/N-feature-name`
- All phase work isolated on phase branch
- Manual testing checklist completed before merge
- Phase status tagged with semantic version: `v0.N.0`
- No partial merges (phase complete or not merged)

**Rationale:**
Clear delivery points enable feedback, reduce merge conflicts, support rollback capability.

### PRINCIPLE 5: Git Workflow & Version Control
**Non-Negotiable Rules:**
- Main branch = always production-ready
- All work on phase branches
- Commit messages: `[PHASE N] human-readable description`
- Merge with rebase to maintain linear history
- Tag each completed phase: `v0.N.0`
- Provide merge commit summary with phase checklist

**Rationale:**
Linear history improves bisection capability; clear tags enable rollback and version identification.

---

## Technology Stack - FINAL DECISIONS

### Frontend Stack
- **Framework:** React 18 with TypeScript (type safety, ecosystem maturity)
- **Styling:** Tailwind CSS (utility-first, minimal bundle impact)
- **State Management:** Zustand (lightweight, minimal boilerplate vs Redux)
- **Routing:** React Router v6 (native support, stable API)
- **HTTP Client:** Axios (interceptor support, timeout handling)
- **UI Components:** Custom + Heroicons (avoid heavy component libraries)
- **Bundler:** Create React App (built-in, familiar, zero-config)
- **Build Output:** Static files for Vercel CDN

### Backend Stack
- **Framework:** Spring Boot 3.3.0 with Java 21 (modern, LTS, enterprise-grade)
- **Database:** PostgreSQL 15 (proven, scalable, ACID guarantee)
- **Authentication:** Google OAuth2 + JWT tokens (secure, user-friendly)
- **ORM:** Spring Data JPA with Hibernate (standard, query optimization)
- **API Architecture:** REST (HTTP-native, cacheable, stateless)
- **Build Tool:** Maven (declarative dependencies, reproducible builds)
- **Security Framework:** Spring Security 6 (OAuth2, CORS, headers)
- **Dependency Injection:** Spring IoC (native, no external framework)

### DevOps & Infrastructure
- **Containerization:** Docker (production parity for local dev)
- **Container Orchestration:** Docker Compose (local development, schema synchronization)
- **CI/CD:** GitHub Actions (free, integrated with repository)
- **Frontend Hosting:** Vercel free tier (React optimized, CDN, serverless)
- **Backend Hosting:** Render.com free tier (PostgreSQL managed included)
- **Database:** PostgreSQL managed service (12 months free from host provider)

### Monitoring & Logging
- **Backend Logging:** SLF4J + Logback (standard Spring Boot, structured JSON)
- **Frontend Errors:** Browser console + Sentry (optional integration)

---

## Code Quality Standards & Conventions

### Backend Java Standards
- **Language Version:** Java 21 (latest LTS)
- **Style Guide:** Google Java Style Guide (strict)
- **Naming Conventions:**
  - Classes: PascalCase (e.g., `UserService`, `TaskRepository`)
  - Methods/Variables: camelCase (e.g., `getUserById()`, `isTaskComplete`)
  - Constants: UPPER_SNAKE_CASE
  - Packages: `com.karthik.[layer].[domain]` (e.g., `com.karthik.service.task`)
- **Boilerplate Reduction:** Lombok annotations (@Getter, @Setter, @Builder)
- **Exception Handling:** Custom exceptions per domain (e.g., `TaskNotFoundException`)
- **Logging Levels:**
  - INFO: State transitions, key business events
  - WARN: Recoverable errors, deprecated usage
  - ERROR: Unrecoverable errors, external API failures
- **Security Non-Negotiables:**
  - ALL inputs validated (JSR-303 annotations)
  - SQL injection prevention (parameterized queries always)
  - CORS headers configured per phase requirement
  - No hardcoded secrets (environment variables for all)

### Frontend TypeScript Standards
- **Language Mode:** TypeScript strict mode (`compilerOptions.strict: true`)
- **Style Guide:** Airbnb React Style Guide (with TypeScript extensions)
- **Naming Conventions:**
  - Components: PascalCase (e.g., `TaskCard`, `UserProfile`)
  - Hooks: camelCase with `use` prefix (e.g., `useAuth`, `useTasks`)
  - Types/Interfaces: PascalCase (e.g., `TaskDTO`, `UserProfile`)
  - Constants: UPPER_SNAKE_CASE
- **Component Approach:** Functional components with hooks ONLY (no class components)
- **Types:** Explicit types everywhere, NO `any` type except justified exceptions
- **Folder Structure:**
  - `src/components/` - Reusable UI components
  - `src/pages/` - Page-level components
  - `src/services/` - API and business logic services
  - `src/types/` - TypeScript types and interfaces
  - `src/hooks/` - Custom React hooks
  - `src/store/` - Zustand stores
- **Styling:** Tailwind utility classes exclusively, NO inline styles or CSS modules
- **Error Handling:** Try-catch blocks with user-friendly error messages (never expose stack traces to user)

### Shared Standards
- **Comments:** Only for WHY, not WHAT (code should be self-documenting)
- **Documentation:** README.md per component folder, API endpoints documented in spec files
- **Dependencies:** Minimize external dependencies (prefer stdlib/framework utilities)
- **Performance:** Lazy loading for components, optimized database queries

---

## API Design Standards

### Request/Response Format

**Success Response (HTTP 200-201):**
```json
{
  "success": true,
  "data": {
    /* actual response data */
  },
  "timestamp": "2026-02-24T10:30:00Z"
}
```

**Error Response (HTTP 400-500):**
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE_ENUM",
    "message": "Human-readable error message",
    "details": {
      "field": "error details if applicable"
    }
  },
  "timestamp": "2026-02-24T10:30:00Z"
}
```

### HTTP Methods & Status Codes
- **GET:** 200 (success), 404 (not found), 401 (unauthorized)
- **POST:** 201 (created), 400 (validation error), 409 (conflict)
- **PUT:** 200 (updated), 404 (not found), 400 (validation error)
- **DELETE:** 204 (no content), 404 (not found)

### Authentication & Authorization
- **Bearer Token:** `Authorization: Bearer <JWT_TOKEN>`
- **CORS:** Enable only frontend domain in CORS config
- **Rate Limiting:** Implement per endpoint in production (TBD in phase)

---

## Governance & Amendment Procedures

### Version Numbering
- **MAJOR.MINOR.PATCH**
- **MAJOR:** Backward-incompatible principle removal or redefinition
- **MINOR:** New principle/section added or materially expanded guidance
- **PATCH:** Clarifications, wording, typo fixes, non-semantic refinements

### Amendment Process
1. Create issue in GitHub with `constitution` label
2. Document proposed change with rationale
3. Update constitution.md with amendment
4. Increment version (PATCH for clarifications, MINOR for new principle)
5. Update `LAST_AMENDED_DATE`
6. Run consistency check against all spec files
7. Merge to main with commit message: `docs(constitution): vX.Y.Z - amendment reason`

### Compliance Review
- Constitution review: End of each 2 phases (after phase 1, 3, 5, 7)
- Developer self-assessment: Weekly via checklist
- Automated checks: Linting, type checking on each commit

---

## Principles Compliance Checklist

**Every developer (including solo) must:**
- [ ] Review constitution at project start
- [ ] Read relevant spec before coding each phase
- [ ] Verify git branch is `phase/N-*` before committing
- [ ] Commit message includes `[PHASE N]` prefix
- [ ] Code follows language style guide (verified by linter)
- [ ] Manual testing checklist completed before merge request
- [ ] Merge includes summary linking back to specs

---

## Exceptions & Override Policy

**Exception Request Process:**
- Technology decision variance: Requires cost-benefit analysis in PR description
- Code standard variance: Requires documented justification with architectural reason
- Security standard variance: PROHIBITED (no exceptions allowed)

**Approval Authority:** Maintainer review (self-approval only if solo, but document reasoning)

---

## Glossary

- **Spec-First:** Specifications written and approved before implementation begins
- **Phase:** Cohesive feature set, independently deployable
- **Industry-Grade:** Production-ready code quality: security, maintainability, performance
- **Manual Testing:** Human-executed test cases (documented in phase checklist)
- **Free Tier:** Services with free plans supporting $0/month target
- **SOLID:** Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion

---

## Questions & Clarifications

For questions about principles or standards, refer to the relevant phase specification files. Unresolved questions require constitution amendment before proceeding.

---

**Constitution Ratified:** 2026-02-23  
**Status:** Active  
**Next Review:** After Phase 2 completion
