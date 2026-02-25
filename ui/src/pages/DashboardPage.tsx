/**
 * Dashboard Page Component
 */

import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks';
import { ROUTES } from '../config/constants';

export const DashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const { user, logout, loading } = useAuth();

  const handleLogout = async () => {
    await logout();
    navigate(ROUTES.LOGIN);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Navigation */}
      <nav className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <div className="inline-flex h-9 w-9 items-center justify-center rounded-lg bg-blue-600">
              <svg className="h-5 w-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4" />
              </svg>
            </div>
            <span className="text-lg font-bold text-gray-900">Task Manager</span>
          </div>

          <div className="flex items-center gap-4">
            {user && (
              <>
                <div className="flex items-center gap-3">
                  {user.avatarUrl ? (
                    <img
                      src={user.avatarUrl}
                      alt={user.name || 'User'}
                      className="h-10 w-10 rounded-full"
                    />
                  ) : (
                    <div className="h-10 w-10 rounded-full bg-blue-600 flex items-center justify-center text-white font-semibold">
                      {user.name ? user.name.charAt(0).toUpperCase() : '?'}
                    </div>
                  )}
                  <div>
                    <p className="text-sm font-medium text-gray-900">{user.name || 'User'}</p>
                    <p className="text-xs text-gray-500">{user.role || 'USER'}</p>
                  </div>
                </div>
                <button
                  onClick={handleLogout}
                  disabled={loading}
                  className="inline-flex items-center gap-2 px-3 py-2 text-xs font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition"
                >
                  <svg className="h-3 w-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                  </svg>
                  Logout
                </button>
              </>
            )}
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="rounded-lg bg-white shadow-sm p-6">
          <div className="text-center">
            <h1 className="text-2xl font-bold text-gray-900 mb-2">
              Welcome, {user?.name}!
            </h1>
            <p className="text-sm text-gray-600 mb-6">
              Successfully authenticated with Google OAuth2
            </p>

            {/* User Info Card */}
            <div className="inline-block text-left bg-gray-50 rounded-lg p-6 border border-gray-200">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">
                User Information
              </h2>
              <div className="space-y-3 text-sm">
                <div className="flex justify-between gap-6">
                  <span className="font-medium text-gray-600">Name:</span>
                  <span className="text-gray-900">{user?.name}</span>
                </div>
                <div className="flex justify-between gap-6">
                  <span className="font-medium text-gray-600">Email:</span>
                  <span className="text-gray-900">{user?.email}</span>
                </div>
                <div className="flex justify-between gap-6">
                  <span className="font-medium text-gray-600">Role:</span>
                  <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                    {user?.role}
                  </span>
                </div>
                <div className="flex justify-between gap-6">
                  <span className="font-medium text-gray-600">User ID:</span>
                  <span className="text-gray-900 font-mono text-xs">{user?.id}</span>
                </div>
              </div>
            </div>

            {/* Phase 1 Status */}
            <div className="mt-8 p-4 bg-green-50 border border-green-200 rounded-lg">
              <div className="flex items-center justify-center gap-2 mb-1">
                <svg className="h-5 w-5 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                </svg>
                <h3 className="text-base font-semibold text-green-900">
                  Phase 1: OAuth2 Complete
                </h3>
              </div>
              <p className="text-sm text-green-800">
                You have successfully completed the OAuth2 authentication flow with Google Sign-In,
                JWT token management, and protected routes.
              </p>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default DashboardPage;
