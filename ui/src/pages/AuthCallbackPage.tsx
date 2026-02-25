/**
 * Auth Callback Page - Handles OAuth redirect
 */

import React, { useEffect, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../hooks';
import { ROUTES } from '../config/constants';
import authService from '../services/authService';
import toast from 'react-hot-toast';

export const AuthCallbackPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { login } = useAuth();
  const [error, setError] = React.useState<string | null>(null);
  const hasProcessed = useRef(false);  // Prevent multiple callback attempts

  useEffect(() => {
    // Prevent processing callback more than once
    if (hasProcessed.current) {
      return;
    }

    const handleCallback = async () => {
      try {
        // Check for OAuth error from Google
        const errorParam = searchParams.get('error');
        const errorDescription = searchParams.get('error_description');

        if (errorParam) {
          const message = errorDescription || 'Authentication failed';
          setError(message);
          toast.error(message);
          hasProcessed.current = true;
          setTimeout(() => navigate(ROUTES.LOGIN), 2000);
          return;
        }

        // Check if we got authorization code from Google
        const code = searchParams.get('code');
        const state = searchParams.get('state');

        if (code) {
          // We have authorization code from Google - exchange it for token
          // Call backend callback endpoint to exchange code for JWT token
          try {
            const response = await authService.handleOAuthCallbackCode(code, state || '');
            
            if (!response || !response.token) {
              throw new Error('No token received from backend');
            }

            hasProcessed.current = true;

            // Store token and fetch user data
            await login(response.token);
            
            // Redirect to dashboard
            navigate(ROUTES.DASHBOARD);
            return;
          } catch (backendError: any) {
            const message = backendError?.response?.data?.message || 'Failed to authenticate with backend';
            throw new Error(message);
          }
        }

        // Check if we already have token (fallback for direct token in URL)
        const token = searchParams.get('token');
        if (token) {
          // Direct token provided - store and login
          hasProcessed.current = true;
          await login(token);
          navigate(ROUTES.DASHBOARD);
          return;
        }

        // No code or token found
        setError('No authentication code received from Google');
        toast.error('No authentication code received from Google');
        hasProcessed.current = true;
        setTimeout(() => navigate(ROUTES.LOGIN), 2000);
      } catch (err: any) {
        const message = err?.message || 'Authentication failed';
        setError(message);
        toast.error(message);
        hasProcessed.current = true;
        setTimeout(() => navigate(ROUTES.LOGIN), 2000);
      }
    };

    handleCallback();
  }, []);  // Empty dependency array - only run on mount

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-blue-50 flex flex-col items-center justify-center px-4">
      <div className="text-center">
        {error ? (
          <>
            <svg className="h-16 w-16 text-red-600 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4v.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <h1 className="text-2xl font-bold text-gray-900 mb-2">Authentication Error</h1>
            <p className="text-gray-600 mb-4">{error}</p>
            <p className="text-sm text-gray-500">Redirecting to login...</p>
          </>
        ) : (
          <>
            <svg className="h-16 w-16 text-blue-600 mx-auto mb-4 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002-2m0 0a2 2 0 012 2m-2-2v2m0 0h2" />
            </svg>
            <h1 className="text-2xl font-bold text-gray-900 mb-2">Completing Authentication</h1>
            <p className="text-gray-600">Please wait while we verify your credentials...</p>
          </>
        )}
      </div>
    </div>
  );
};

export default AuthCallbackPage;
