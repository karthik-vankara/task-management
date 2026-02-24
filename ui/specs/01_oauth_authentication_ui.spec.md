# OAuth2 Authentication UI Specification

## Overview
Implement Google Sign-In UI component, authentication context, protected routes, and token management for the Task Management System frontend.

## Objectives
- [ ] AuthContext with Zustand store
- [ ] useAuth custom hook
- [ ] GoogleSignInButton component
- [ ] LoginPage component
- [ ] ProtectedRoute wrapper
- [ ] Token storage and validation
- [ ] Error handling and loading states

## Functional Requirements
- Google Sign-In button with Google brand guidelines
- Login page with auth error handling
- Automatic redirect to dashboard on successful auth
- Protected routes redirect to login if not authenticated
- Token persisted in localStorage
- Token validation on app load
- Loading spinner during OAuth redirect

## Technical Architecture

### AuthContext (Zustand Store)
```typescript
interface AuthState {
  user: User | null;
  token: string | null;
  loading: boolean;
  error: string | null;
  login: (googleToken: string) => Promise<void>;
  logout: () => Promise<void>;
  checkAuth: () => Promise<void>;
  clearError: () => void;
}
```

### API Service
```typescript
// services/authService.ts
export const authService = {
  initiateLogin: () => api.post('/auth/login'),
  handleCallback: (code: string, state: string) => {},
  logout: () => api.post('/auth/logout'),
  getProfile: () => api.get('/auth/profile'),
};
```

## Components

### 1. GoogleSignInButton Component
**Props:**
```typescript
interface GoogleSignInButtonProps {
  disabled?: boolean;
  loading?: boolean;
  onClick: () => void;
}
```

**Behavior:**
- Displays Google "Sign in with" button
- Shows loading spinner during OAuth flow
- Disabled state during loading
- Follows Google brand guidelines

### 2. LoginPage Component
**Structure:**
```
LoginPage
├── Logo/Header
├── SignIn Section
│   ├── Title
│   ├── GoogleSignInButton
│   └── Error Message (if any)
└── Footer/Links
```

**Behavior:**
- Redirect to Google OAuth on button click
- Show loading spinner
- Display error if OAuth fails
- Redirect to dashboard on success

### 3. ProtectedRoute Component
**Props:**
```typescript
interface ProtectedRouteProps {
  children: React.ReactNode;
}
```

**Behavior:**
- Check if user is authenticated
- If not, redirect to `/login`
- If yes, render children
- Show loading spinner while checking

### 4. useAuth Hook
```typescript
const useAuth = () => {
  const { user, token, loading, error } = useAuthStore();
  const isAuthenticated = !!user && !!token;
  return { user, token, loading, error, isAuthenticated };
};
```

## Token Management

### Storage Strategy
- Store JWT in localStorage: `key: 'auth_token'`
- Store user data in Zustand store (not persisted)
- Set token in axios default headers on app load

### Token Lifecycle
1. On app load, check localStorage for token
2. If token exists, validate by calling `GET /auth/profile`
3. If valid, populate user in store
4. If invalid/expired, clear token and redirect to login
5. On logout, clear token and user

### Axios Interceptor
```typescript
// Add Authorization header to all requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 responses
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Clear token and redirect to login
    }
  }
);
```

## OAuth Flow (Frontend)

### Step 1: Initiate Login
1. User clicks GoogleSignInButton
2. Frontend calls `POST /api/auth/login`
3. Backend returns Google OAuth URL
4. Frontend opens new browser window to Google URL

### Step 2: Redirect from Google
1. Google redirects to `http://localhost:8080/api/auth/callback?code=...`
2. Backend exchanges code for tokens
3. Backend redirects to `http://localhost:3000?token=eyJhbGc...`

### Step 3: Handle Callback in Frontend
1. App component detects token in URL query param
2. Stores token in localStorage
3. Calls `GET /auth/profile` to fetch user data
4. Redirects to dashboard

## Error Handling

### Login Errors
- OAuth server error → Show: "Failed to connect to Google"
- User creation failed → Show: "Account setup failed, try again"
- Network error → Show: "No internet connection"

### Protected Route Errors
- Invalid token → Redirect to login
- Token expired → Redirect to login with "Session expired" message

## Loading States

### During OAuth Flow
```
GoogleSignInButton
└── Show spinner overlay with "Redirecting to Google..."
```

### During Profile Check
```
Protected route wrapper
└── Show full-page spinner while validating auth
```

## Styling (Tailwind)

### GoogleSignInButton
- Use official Google button styling
- Colors: White background, gray border
- Typography: Roboto font (imported from Google)
- Size: 44px height, ~220px width

### LoginPage
- Centered card layout
- Hero image or background gradient
- Responsive: Stack on mobile, side-by-side on desktop

## Page Routes

### /login
- Public route
- Redirect to dashboard if already authenticated

### /callback
- Private handler route
- Processes OAuth callback
- Not directly accessed by user

### /dashboard (and protected routes)
- Protected by ProtectedRoute wrapper
- Redirect to /login if not authenticated

## Error States

```typescript
interface AuthError {
  code: 'OAUTH_ERROR' | 'TOKEN_EXPIRED' | 'NETWORK_ERROR';
  message: string;
  retryable: boolean;
}
```

## Performance Requirements
- Auth check on app load <1s
- OAuth redirect <500ms
- No unnecessary re-renders of protected routes

## Dependencies
- @react-oauth/google (optional, for advanced OAuth features)
- axios (already in stack)
- zustand (state management)
- react-router-dom (routing)
- react-icons/fa (Google icon)

## Test Plan
- [ ] Google Sign-In button renders
- [ ] OAuth flow works end-to-end
- [ ] Protected routes redirect when not auth
- [ ] Protected routes allow access when auth
- [ ] Token persists in localStorage
- [ ] Token clears on logout
- [ ] Error messages display correctly

## Acceptance Criteria
- [ ] Google Sign-In button visible on login page
- [ ] OAuth flow works without errors
- [ ] User redirected to dashboard after login
- [ ] Protected routes protected
- [ ] Logout clears token and redirects
- [ ] App rehydrates auth on page refresh

## Notes
- Keep auth flow simple; front-end should not perform OAuth code exchange
- Backend handles all OAuth2 security
- Frontend only stores and manages tokens
