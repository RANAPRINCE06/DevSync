import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { User, UpdateUserRequest } from '@/types/user';
import { LoginRequest, RegisterRequest } from '@/types/auth';
import { authApi } from '@/services/authApi';
import { userApi } from '@/services/userApi';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (request: LoginRequest) => Promise<User>;
  register: (request: RegisterRequest) => Promise<User>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
  updateProfile: (data: UpdateUserRequest) => Promise<User>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  // Restore session on application startup
  useEffect(() => {
    const initializeAuth = async () => {
      try {
        const storedToken = authApi.getToken();
        const storedUser = authApi.getCurrentUser();

        if (storedToken && storedUser) {
          setToken(storedToken);
          setUser(storedUser);

          // Verify/refresh user state from backend in background
          try {
            const freshUser = await userApi.getUserById(storedUser.id);
            if (freshUser && freshUser.active) {
              setUser(freshUser);
            }
          } catch {
            // If verification fails silently keep local state or let interceptor handle
          }
        }
      } catch (err) {
        console.error('Error restoring session:', err);
      } finally {
        setIsLoading(false);
      }
    };

    initializeAuth();
  }, []);

  const login = async (request: LoginRequest): Promise<User> => {
    setIsLoading(true);
    try {
      const authResponse = await authApi.login(request);
      setUser(authResponse.user);
      setToken(authResponse.token);
      return authResponse.user;
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (request: RegisterRequest): Promise<User> => {
    setIsLoading(true);
    try {
      const authResponse = await authApi.register(request);
      setUser(authResponse.user);
      setToken(authResponse.token);
      return authResponse.user;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = async (): Promise<void> => {
    await authApi.logout();
    setUser(null);
    setToken(null);
  };

  const refreshUser = useCallback(async (): Promise<void> => {
    if (!user) return;
    try {
      const freshUser = await userApi.getUserById(user.id);
      setUser(freshUser);
    } catch (err) {
      console.error('Failed to refresh user:', err);
    }
  }, [user]);

  const updateProfile = async (data: UpdateUserRequest): Promise<User> => {
    if (!user) throw new Error('No authenticated user');
    const updated = await authApi.updateProfile(user.id, data);
    setUser(updated);
    return updated;
  };

  const isAuthenticated = !!token && !!user;

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated,
        isLoading,
        login,
        register,
        logout,
        refreshUser,
        updateProfile,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
