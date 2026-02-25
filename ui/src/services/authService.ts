/**
 * Authentication API Service
 */

import api from './api';
import { API_ENDPOINTS } from '../config/constants';
import { OAuth2LoginResponse, User } from '../types';

export const authService = {
  /**
   * Initiate OAuth2 login flow
   * Returns Google OAuth login URL
   */
  initiateLogin: async (): Promise<OAuth2LoginResponse> => {
    try {
      const response = await api.post<OAuth2LoginResponse>(API_ENDPOINTS.AUTH_LOGIN);
      return response.data;
    } catch (error) {
      console.error('Error initiating login:', error);
      throw error;
    }
  },

  /**
   * Handle OAuth callback by exchanging authorization code for JWT token
   */
  handleOAuthCallbackCode: async (code: string, state: string) => {
    try {
      const response = await api.get<any>(API_ENDPOINTS.AUTH_CALLBACK, {
        params: { code, state },
      });
      return response.data;
    } catch (error) {
      console.error('Error handling OAuth callback:', error);
      throw error;
    }
  },

  /**
   * Get user profile (validates token)
   */
  getProfile: async (): Promise<User> => {
    try {
      const response = await api.get<User>(API_ENDPOINTS.AUTH_PROFILE);
      return response.data;
    } catch (error) {
      console.error('Error fetching profile:', error);
      throw error;
    }
  },

  /**
   * Logout user (revoke token on backend)
   */
  logout: async (): Promise<void> => {
    try {
      await api.post(API_ENDPOINTS.AUTH_LOGOUT);
    } catch (error) {
      console.error('Error logging out:', error);
      // Still clear local storage even if logout fails
    }
  },

  /**
   * Validate if current token is valid
   */
  validateToken: async (): Promise<boolean> => {
    try {
      await authService.getProfile();
      return true;
    } catch (error) {
      console.error('Token validation failed:', error);
      return false;
    }
  },
};

export default authService;
