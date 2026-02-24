# User Profile UI Specification

## Overview
Implement user profile viewing and editing pages with form validation and API integration.

## Objectives
- [ ] Profile page layout
- [ ] Profile form with validation
- [ ] Edit/view mode toggle
- [ ] Avatar display and upload
- [ ] Success/error notifications
- [ ] Loading states

## Components

### 1. ProfilePage Component
**Route:** `/profile`

**Layout:**
```
ProfilePage
├── Header ("My Profile")
├── ProfileHeader
│   ├── Avatar (large)
│   └── Name
├── ProfileForm
│   ├── EditButton (top-right)
│   └── FormFields
└── SaveCancel (if in edit mode)
```

**Features:**
- Display current user profile
- Show edit button in view mode
- Show save/cancel buttons in edit mode
- Load user data on mount
- Handle loading/error states

### 2. ProfileForm Component
**Props:**
```typescript
interface ProfileFormProps {
  user: User;
  onSave: (data: UserProfileUpdateRequest) => Promise<void>;
  loading?: boolean;
  error?: string;
}
```

**Form Fields:**
- Name (required, 1-100 chars)
- Bio (optional, max 500 chars)
- Avatar URL (optional, URL format)

**Features:**
- Real-time validation
- Character count for bio
- Show validation errors
- Disable submit when invalid

### 3. ProfileAvatar Component
**Props:**
```typescript
interface ProfileAvatarProps {
  url?: string;
  name: string;
  editable?: boolean;
  onUpload?: (file: File) => void;
  size?: 'sm' | 'md' | 'lg';
}
```

**Features:**
- Display avatar image with fallback
- Initials if no avatar
- Optional upload button overlay
- Loading skeleton in edit mode

## Form Validation

### Field Rules
```typescript
const profileSchema = {
  name: {
    required: true,
    minLength: 1,
    maxLength: 100,
    pattern: /^[a-zA-Z\s'-]*$/ // Letters, spaces, hyphens, apostrophes
  },
  bio: {
    maxLength: 500
  },
  avatarUrl: {
    pattern: /^https?:\/\/.+\.(jpg|jpeg|png|gif)$/
  }
};
```

### Validation Messages
- Name required: "Name is required"
- Name too long: "Name must not exceed 100 characters"
- Bio too long: "Bio must not exceed 500 characters"
- Invalid avatar URL: "Avatar URL must be a valid image URL"

## Data Flow

### Load Profile
```
useEffect(() => {
  authService.getProfile()
    .then(user => setProfileData(user))
    .catch(error => setError(error.message))
    .finally(() => setLoading(false))
}, [])
```

### Update Profile
```
onSave async (formData) => {
  try {
    setLoading(true)
    const updated = await userService.updateProfile(userId, formData)
    setProfileData(updated)
    notify({ type: 'success', message: 'Profile updated' })
    setEditMode(false)
  } catch (error) {
    notify({ type: 'error', message: error.message })
  }
}
```

## UI Patterns

### Edit Mode Toggle
- View Mode: Shows "Edit" button
- Click Edit: Switches to form with editable fields
- Save: Validates and submits
- Cancel: Discards changes

### Loading State
```
if (loading) {
  return <ProfileSkeleton />
}
```

### Error State
```
{error && (
  <Alert type="error" message={error} />
)}
```

### Success Notification
```
Toast({
  type: 'success',
  message: 'Profile updated successfully',
  duration: 3000
})
```

## Responsive Design
- Mobile: Single column, full-width form
- Tablet: Two column layout optional
- Desktop: Sidebar for additional info

## Styling (Tailwind)

### Profile Header
```
class="bg-gradient-to-r from-blue-500 to-indigo-600 p-8"
```

### Form Container
```
class="max-w-2xl mx-auto p-6"
```

### Button Styling
- Save: Primary (blue)
- Cancel: Secondary (gray)
- Edit: Outline (blue border)

## Dependencies
- axios (API calls)
- zustand (auth state)
- react-hook-form (form management)
- Tailwind CSS (styling)

## Test Plan
- [ ] Profile page loads and displays user data
- [ ] Edit mode enables form fields
- [ ] Form validation works correctly
- [ ] Save button submits valid form
- [ ] Cancel discards changes
- [ ] Error notification on API failure
- [ ] Success notification on save
- [ ] Responsive on mobile/desktop

## Acceptance Criteria
- [ ] User can view their profile
- [ ] User can edit name/bio/avatar
- [ ] Form validates before submit
- [ ] Changes persist on backend
- [ ] Error handling works
- [ ] UI responsive on all devices

## Notes
- Use react-hook-form for complex form management
- Consider avatar upload feature for future phase
- Auto-save could be added but not required for Phase 2
