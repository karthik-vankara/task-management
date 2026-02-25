/**
 * Type definitions for Task Management System
 */

export interface User {
  id: number;
  googleId: string;
  email: string;
  name: string;
  role: 'ADMIN' | 'USER';
  avatarUrl?: string;
  bio?: string;
  createdAt: string;
  updatedAt: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface OAuth2LoginResponse {
  redirectUrl: string;
  state: string;
}

export interface ApiResponse<T> {
  data: T;
  status: number;
  message?: string;
}

export interface AuthError {
  message: string;
  code?: string;
  details?: unknown;
}
