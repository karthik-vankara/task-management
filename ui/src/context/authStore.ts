/**
 * Authentication Store using Zustand
 */

import { create } from 'zustand';
import { User } from '../types';
import authService from '../services/authService';
import { STORAGE_KEYS, ERROR_MESSAGES } from '../config/constants';
import toast from 'react-hot-toast';

export interface AuthState {
  user: User | null;
  token: string | null;
  loading: boolean;
  error: string | null;
  isAuthenticated: boolean;
  
  // Actions
  setUser: (user: User | null) => void;
  setToken: (token: string | null) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  clearError: () => void;
  
  // Main auth actions
  login: (token: string) => Promise<void>;
  logout: () => Promise<void>;
  checkAuth: () => Promise<void>;
  initialize: () => Promise<void>;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  token: null,
  loading: false,
  error: null,
  isAuthenticated: false,

  setUser: (user) => set({ user, isAuthenticated: !!user }),
  setToken: (token) => set({ token }),
  setLoading: (loading) => set({ loading }),
  setError: (error) => set({ error }),
  clearError: () => set({ error: null }),

  /**
   * Login with token from OAuth callback
   */
  login: async (token: string) => {
    set({ loading: true, error: null });
    try {
      // Store token
      localStorage.setItem(STORAGE_KEYS.AUTH_TOKEN, token);
      set({ token });

      // Fetch user profile
      const user = await authService.getProfile();
      localStorage.setItem(STORAGE_KEYS.USER_DATA, JSON.stringify(user));
      
      set({
        user,
        isAuthenticated: true,
        loading: false,
      });

      toast.success('Successfully logged in!');
    } catch (error: any) {
      const errorMessage = error?.response?.data?.message || ERROR_MESSAGES.ACCOUNT_SETUP_FAILED;
      set({
        error: errorMessage,
        loading: false,
        token: null,
        isAuthenticated: false,
      });
      localStorage.removeItem(STORAGE_KEYS.AUTH_TOKEN);
      toast.error(errorMessage);
      throw error;
    }
  },

  /**
   * Logout user
   */
  logout: async () => {
    set({ loading: true });
    try {
      await authService.logout();
      localStorage.removeItem(STORAGE_KEYS.AUTH_TOKEN);
      localStorage.removeItem(STORAGE_KEYS.USER_DATA);
      
      set({
        user: null,
        token: null,
        isAuthenticated: false,
        loading: false,
        error: null,
      });

      toast.success('Successfully logged out!');
    } catch (error) {
      console.error('Logout error:', error);
      // Clear auth state anyway
      localStorage.removeItem(STORAGE_KEYS.AUTH_TOKEN);
      localStorage.removeItem(STORAGE_KEYS.USER_DATA);
      
      set({
        user: null,
        token: null,
        isAuthenticated: false,
        loading: false,
      });
    }
  },

  /**
   * Check if user is authenticated
   */
  checkAuth: async () => {
    const token = localStorage.getItem(STORAGE_KEYS.AUTH_TOKEN);
    
    if (!token) {
      set({
        user: null,
        token: null,
        isAuthenticated: false,
      });
      return;
    }

    set({ loading: true });
    try {
      const user = await authService.getProfile();
      localStorage.setItem(STORAGE_KEYS.USER_DATA, JSON.stringify(user));
      
      set({
        user,
        token,
        isAuthenticated: true,
        loading: false,
        error: null,
      });
    } catch (error) {
      console.error('Auth check failed:', error);
      localStorage.removeItem(STORAGE_KEYS.AUTH_TOKEN);
      localStorage.removeItem(STORAGE_KEYS.USER_DATA);
      
      set({
        user: null,
        token: null,
        isAuthenticated: false,
        loading: false,
      });
    }
  },

  /**
   * Initialize auth state from storage and validate
   */
  initialize: async () => {
    const token = localStorage.getItem(STORAGE_KEYS.AUTH_TOKEN);
    const userData = localStorage.getItem(STORAGE_KEYS.USER_DATA);

    if (token && userData) {
      try {
        const user = JSON.parse(userData);
        set({
          user,
          token,
          isAuthenticated: true,
        });

        // Validate token with backend
        await get().checkAuth();
      } catch (error) {
        console.error('Auth initialization error:', error);
        localStorage.removeItem(STORAGE_KEYS.AUTH_TOKEN);
        localStorage.removeItem(STORAGE_KEYS.USER_DATA);
        
        set({
          user: null,
          token: null,
          isAuthenticated: false,
        });
      }
    }
  },
}));

export default useAuthStore;
