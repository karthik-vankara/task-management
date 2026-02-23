# Task Management System - Copilot Instructions

## Project Structure
- **Backend**: `./backend/` - Spring Boot (Java) REST API
- **Frontend**: `./ui/` - React (TypeScript) UI

## Tech Stack
- Frontend: React 18+, TypeScript, Tailwind CSS
- Backend: Spring Boot 3.x, Java 17+, Spring Security, Spring Data JPA
- Database: PostgreSQL 14+
- Authentication: Google OAuth2

## Database Connection
- Host: localhost
- Port: 5432
- Database: taskdb
- User: (provided in .env)
- Password: (provided in .env)

## Running Locally
- Backend: `cd backend && mvn spring-boot:run`
- Frontend: `cd ui && npm start`

## Important Patterns
- Use Spring REST conventions for APIs
- Use TypeScript for all React code
- Use JPA entities with proper relationships
- All APIs require authentication (Google OAuth JWT token)

## Code Style
- Backend: Follow Google Java Style Guide
- Frontend: Follow Airbnb React Style Guide
- Use meaningful variable names
- Add JSDoc/Javadoc for public methods