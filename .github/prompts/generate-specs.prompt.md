---
agent: 'agent'
description: 'Generate comprehensive specification files for Task Management System phases'
---

# Task Management System - Specification Generator

## Task
Generate complete, production-ready specification files for the Task Management System based on the requested phase.

## Project Context
- **Frontend:** React 18 + TypeScript + Tailwind CSS (folder: `ui/`)
- **Backend:** Spring Boot 3 (Java 21) + PostgreSQL + Google OAuth2 (folder: `backend/`)
- **Architecture:** Mono-repo with both apps in single VS Code workspace
- **Database:** PostgreSQL (taskdb, taskuser)
- **Authentication:** Google OAuth2

## Specification File Format

Each specification file must include:

### 1. Overview Section
- Clear description of feature/phase
- Business value and objectives
- Target users/stakeholders

### 2. Functional Requirements
- Detailed list of what the system should do
- User stories format: "As a [user], I want [action], so that [benefit]"
- Acceptance criteria for each requirement

### 3. Technical Architecture (Backend Focus)
- Entity/Data models (with field types)
- Service layer responsibilities
- Repository layer queries
- Controller endpoints (HTTP method, path, parameters)
- Request/Response schemas with examples
- Error handling strategy
- Security considerations
- Database migrations needed

### 4. UI/UX Architecture (Frontend Focus)
- Component hierarchy and structure
- Props and state management
- User flows and wireframes (ASCII or description)
- Form validation rules
- Error states and handling
- Loading states
- Responsive design considerations

### 5. API Specification (Backend)
For each endpoint, include: