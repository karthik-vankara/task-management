/**
 * App configuration and constants
 */

// API Configuration
export const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';
export const API_ENDPOINTS = {
  AUTH_LOGIN: '/api/auth/login',
  AUTH_CALLBACK: '/api/auth/callback',
  AUTH_PROFILE: '/api/auth/profile',
  AUTH_LOGOUT: '/api/auth/logout',
};

// OAuth2 Configuration
export const GOOGLE_CLIENT_ID = process.env.REACT_APP_GOOGLE_CLIENT_ID || '';

// Storage Keys
export const STORAGE_KEYS = {
  AUTH_TOKEN: 'auth_token',
  USER_DATA: 'user_data',
};

// Routes
export const ROUTES = {
  LOGIN: '/login',
  CALLBACK: '/auth/callback',
  DASHBOARD: '/dashboard',
  PROFILE: '/profile',
  HOME: '/',
};

// Token expiration time (24 hours in milliseconds)
export const TOKEN_EXPIRATION_TIME = 24 * 60 * 60 * 1000;

// Error messages
export const ERROR_MESSAGES = {
  OAUTH_FAILED: 'Failed to connect to Google. Please try again.',
  ACCOUNT_SETUP_FAILED: 'Account setup failed. Please try again.',
  NETWORK_ERROR: 'No internet connection. Please check your network.',
  SESSION_EXPIRED: 'Your session has expired. Please log in again.',
  INVALID_TOKEN: 'Invalid authentication token.',
  UNAUTHORIZED: 'You are not authorized to access this resource.',
};

// Success messages
export const SUCCESS_MESSAGES = {
  LOGIN_SUCCESS: 'Successfully logged in!',
  LOGOUT_SUCCESS: 'Successfully logged out.',
};
