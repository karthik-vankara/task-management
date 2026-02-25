/**
 * Google Sign In Button Component
 */

import React from 'react';
import { useAuth } from '../../hooks';
import authService from '../../services/authService';
import { ERROR_MESSAGES } from '../../config/constants';
import toast from 'react-hot-toast';

export interface GoogleSignInButtonProps {
  disabled?: boolean;
  loading?: boolean;
  onClick?: () => void;
}

export const GoogleSignInButton: React.FC<GoogleSignInButtonProps> = ({
  disabled = false,
  loading = false,
  onClick,
}) => {
  const { setError } = useAuth();
  const [isLoading, setIsLoading] = React.useState(loading);

  const handleClick = async () => {
    if (disabled || isLoading) return;

    try {
      setIsLoading(true);
      setError(null);
      
      // Call backend to get OAuth login URL
      const response = await authService.initiateLogin();
      
      if (response.redirectUrl) {
        // Store state in sessionStorage for validation in callback
        if (response.state) {
          sessionStorage.setItem('oauth_state', response.state);
        }
        // Redirect to Google OAuth
        window.location.href = response.redirectUrl;
      }
    } catch (error: any) {
      const errorMessage = error?.response?.data?.message || ERROR_MESSAGES.OAUTH_FAILED;
      setError(errorMessage);
      toast.error(errorMessage);
      setIsLoading(false);
    }

    onClick?.();
  };

  return (
    <button
      onClick={handleClick}
      disabled={disabled || isLoading}
      className={`
        w-full px-6 py-3 
        bg-white border-2 border-gray-300 rounded-lg
        font-roboto font-medium text-gray-700
        hover:bg-gray-50 hover:border-gray-400
        disabled:opacity-50 disabled:cursor-not-allowed
        transition duration-200
        flex items-center justify-center gap-3
        ${isLoading ? 'opacity-50 cursor-not-allowed' : ''}
      `}
    >
      {isLoading ? (
        <>
          <div className="inline-block h-4 w-4 animate-spin rounded-full border-2 border-solid border-gray-700 border-r-transparent" />
          <span>Redirecting to Google...</span>
        </>
      ) : (
        <>
          <svg className="w-4 h-4" viewBox="0 0 24 24">
            <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
            <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
            <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
            <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
          </svg>
          <span>Sign in with Google</span>
        </>
      )}
    </button>
  );
};

export default GoogleSignInButton;
