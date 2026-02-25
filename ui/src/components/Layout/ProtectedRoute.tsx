/**
 * Protected Route Component
 * Wraps routes that require authentication
 */

import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks';
import { ROUTES } from '../../config/constants';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const navigate = useNavigate();
  const { isAuthenticated, loading, initialize } = useAuth();
  const [initialized, setInitialized] = React.useState(false);

  useEffect(() => {
    const checkAuth = async () => {
      // Initialize auth state on first load
      if (!initialized) {
        await initialize();
        setInitialized(true);
      }
    };

    checkAuth();
  }, [initialized, initialize]);

  if (loading || !initialized) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-gray-50">
        <div className="flex flex-col items-center gap-4">
          <div className="h-12 w-12 animate-spin rounded-full border-4 border-solid border-blue-600 border-r-transparent" />
          <p className="text-gray-600 font-medium">Loading...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    navigate(ROUTES.LOGIN);
    return null;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
