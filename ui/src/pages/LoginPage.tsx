/**
 * Login Page Component - Simple & Clean OAuth2 Authentication
 */

import React, { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../hooks';
import GoogleSignInButton from '../components/Auth/GoogleSignInButton';
import { ROUTES } from '../config/constants';
import toast from 'react-hot-toast';

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { isAuthenticated, login, loading, error, clearError } = useAuth();
  const [sessionExpired, setSessionExpired] = React.useState(false);

  useEffect(() => {
    const token = searchParams.get('token');
    const sessionParam = searchParams.get('session');

    if (sessionParam === 'expired') {
      setSessionExpired(true);
      toast.error('Your session has expired. Please log in again.');
    }

    if (token) {
      login(token).then(() => {
        navigate(ROUTES.DASHBOARD);
      }).catch(() => {
        // Error handled by login action
      });
    } else if (isAuthenticated) {
      navigate(ROUTES.DASHBOARD);
    }
  }, [searchParams, isAuthenticated, navigate, login]);

  const handleDismissError = () => {
    clearError();
    setSessionExpired(false);
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 px-4">
      <div className="w-full max-w-sm">
        
        {/* Header */}
        <div className="text-center mb-8">
          <div className="inline-flex h-10 w-10 items-center justify-center rounded-lg bg-blue-600 mb-3">
            <svg className="h-5 w-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4" />
            </svg>
          </div>
          <h1 className="text-2xl font-bold text-gray-900">Task Manager</h1>
          <p className="text-gray-600 text-xs mt-1">Sign in to get started</p>
        </div>

        {/* Error Alert */}
        {(error || sessionExpired) && (
          <div className="mb-6 rounded-lg bg-red-50 border border-red-200 p-3">
            <div className="flex gap-2">
              <svg className="h-4 w-4 text-red-600 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4v2m0-10a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <div className="flex-1">
                <p className="text-xs text-red-800">
                  {error || 'Your session has expired. Please log in again.'}
                </p>
              </div>
              <button onClick={handleDismissError} className="text-red-400 hover:text-red-600">
                <svg className="h-3 w-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </div>
        )}

        {/* Login Card */}
        <div className="rounded-lg bg-white shadow-md p-6 border border-gray-200">
          <div className="mb-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-1">Welcome</h2>
            <p className="text-sm text-gray-600">Sign in with your Google account</p>
          </div>

          {/* Google Button */}
          <GoogleSignInButton disabled={loading} loading={loading} />

          {/* Divider */}
          <div className="relative my-6">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-gray-200"></div>
            </div>
            <div className="relative flex justify-center text-xs">
              <span className="bg-white px-2 text-gray-500">Secure & Safe</span>
            </div>
          </div>

          {/* Info */}
          <div className="text-xs text-gray-600 space-y-1.5">
            <p className="flex items-center gap-2">
              <svg className="h-3 w-3 text-green-600 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
              </svg>
              OAuth2 with Google
            </p>
            <p className="flex items-center gap-2">
              <svg className="h-3 w-3 text-green-600 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
              </svg>
              Password never stored
            </p>
            <p className="flex items-center gap-2">
              <svg className="h-3 w-3 text-green-600 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
              </svg>
              JWT encrypted
            </p>
          </div>
        </div>

        {/* Footer */}
        <p className="text-center text-xs text-gray-600 mt-6">
          By signing in, you agree to our Terms & Privacy Policy
        </p>
      </div>
    </div>
  );
};

export default LoginPage;
