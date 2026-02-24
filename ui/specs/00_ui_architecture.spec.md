# UI Architecture Specification

## Overview
Define UI/UX principles, design system, component patterns, and directory structure for the Task Management System frontend.

## Objectives
- [ ] Establish design system (colors, typography, spacing)
- [ ] Define component organization patterns
- [ ] Outline responsive design strategy
- [ ] Set state management architecture
- [ ] Define routing structure
- [ ] Establish error handling and loading patterns
- [ ] Create accessibility guidelines

## Functional Requirements
- Responsive design (mobile 320px - desktop 1920px)
- Accessible keyboard navigation
- WCAG 2.1 AA compliance
- Consistent styling using Tailwind CSS
- Proper loading and error states
- Form validation with user feedback

## Technical Architecture

### Technology Stack
- React 18 with TypeScript (strict mode)
- Tailwind CSS for styling
- Zustand for global state
- React Router v6 for navigation
- Axios for API calls
- React Query (optional) for server state

### Directory Structure
```
src/
├── components/       # Reusable UI components
│   ├── common/      # Buttons, inputs, layouts
│   ├── forms/       # Form components
│   └── modals/      # Modal dialogs
├── pages/           # Page-level components
├── services/        # API services, utilities
├── types/           # TypeScript types
├── hooks/           # Custom React hooks
├── store/           # Zustand stores
└── styles/          # Global Tailwind config
```

## Design System

### Color Palette
- **Primary:** Blue (#3B82F6)
- **Secondary:** Indigo (#6366F1)
- **Danger:** Red (#EF4444)
- **Warning:** Amber (#F59E0B)
- **Success:** Green (#10B981)
- **Neutral:** Gray scale (#495057 to #F8F9FA)

### Typography
- **Display:** Inter, 32px, 700 weight
- **Heading 1:** Inter, 28px, 600 weight
- **Heading 2:** Inter, 24px, 600 weight
- **Body:** Inter, 16px, 400 weight
- **Small:** Inter, 14px, 400 weight

### Spacing
- Base unit: 4px
- Standard gaps: 4, 8, 12, 16, 20, 24, 32px

### Responsive Breakpoints
- Mobile: 320px - 480px
- Tablet: 481px - 768px
- Desktop: 769px+

## Component Patterns

### Button Component
```typescript
interface ButtonProps {
  variant: 'primary' | 'secondary' | 'danger';
  size: 'sm' | 'md' | 'lg';
  disabled?: boolean;
  loading?: boolean;
  onClick: () => void;
  children: React.ReactNode;
}
```

### Input Component
```typescript
interface InputProps {
  label?: string;
  placeholder?: string;
  error?: string;
  required?: boolean;
  type?: 'text' | 'email' | 'password';
  value: string;
  onChange: (value: string) => void;
}
```

## State Management Architecture

### Zustand Stores
- **authStore** - User auth state, tokens
- **taskStore** - Task list, filters, pagination
- **uiStore** - Modals, notifications, sidebar

### API Service Layer
```typescript
// services/api.ts
export const taskService = {
  createTask: (data: TaskCreateDTO) => api.post('/tasks', data),
  listTasks: (params: QueryParams) => api.get('/tasks', { params }),
  updateTask: (id: number, data: TaskUpdateDTO) => api.put(`/tasks/${id}`, data),
  deleteTask: (id: number) => api.delete(`/tasks/${id}`),
};
```

## Routing Structure

### Route Tree
```
/ (public)
├── /login (public)
├── /callback (public)
├── /dashboard (protected)
├── /tasks (protected)
│   └── /tasks/:id (protected)
├── /profile (protected)
└── /notfound (public)
```

## Page Components & Layouts

### Layout Structure
```
App
├── Header
├── Sidebar (desktop) / MobileNav (mobile)
├── Main Router
│   ├── LoginPage
│   ├── DashboardPage
│   ├── TaskListPage
│   ├── TaskDetailPage
│   └── ProfilePage
└── Footer (optional)
```

## Error Handling & Loading States

### Loading Pattern
- Show skeleton loader during data fetch
- Disable buttons while loading
- Show retry option on API error

### Error Pattern
```typescript
// Toast notification
notify({
  type: 'error',
  message: 'Failed to create task',
  action: { label: 'Retry', handler: retry }
});
```

## Accessibility Guidelines
- All buttons and links keyboard accessible
- Color not only means of communication
- Form labels properly associated
- ARIA attributes for complex components
- 1.4:1 minimum contrast ratio

## Performance Requirements
- Lazy load routes
- Optimize re-renders (useMemo, useCallback)
- Bundle size target: <2MB
- Lighthouse score >90
- Fast First Contentful Paint (<2s)

## Dependencies
- react: 18.x
- react-dom: 18.x
- typescript: 5.x
- tailwindcss: 3.x
- zustand: 4.x
- react-router-dom: 6.x
- axios: 1.x
- react-icons: (Heroicons)

## Test Plan
- [ ] Component unit tests
- [ ] Page integration tests
- [ ] Accessibility tests
- [ ] Responsive design tests
- [ ] Cross-browser tests

## Acceptance Criteria
- [ ] Design system documented
- [ ] Component library outlined
- [ ] Routing structure approved
- [ ] State management pattern defined
- [ ] Accessibility guidelines met

## Notes
This architecture ensures consistency across all UI components and pages. Developers should refer to this spec when creating new components or pages.
