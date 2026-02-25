/**
 * useAuth custom hook for easy access to auth state
 */

import { useAuthStore } from '../context/authStore';

export const useAuth = () => {
  const {
    user,
    token,
    loading,
    error,
    isAuthenticated,
    setUser,
    setToken,
    setLoading,
    setError,
    clearError,
    login,
    logout,
    checkAuth,
    initialize,
  } = useAuthStore();

  return {
    user,
    token,
    loading,
    error,
    isAuthenticated,
    setUser,
    setToken,
    setLoading,
    setError,
    clearError,
    login,
    logout,
    checkAuth,
    initialize,
  };
};

export default useAuth;
