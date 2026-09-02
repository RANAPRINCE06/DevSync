import { apiClient } from './client';
import { ApiResponse } from '@/types/api';
import { User, UpdateUserRequest } from '@/types/user';
import { LoginRequest, RegisterRequest, AuthResponse } from '@/types/auth';

const TOKEN_STORAGE_KEY = 'devsync_auth_token';
const USER_STORAGE_KEY = 'devsync_auth_user';

export const authApi = {
  login: async (request: LoginRequest): Promise<AuthResponse> => {
    const res = await apiClient.post<ApiResponse<AuthResponse>>('/auth/login', {
      email: request.email.trim(),
      password: request.password,
    });

    const authData = res.data.data;
    const storage = request.rememberMe !== false ? localStorage : sessionStorage;
    storage.setItem(TOKEN_STORAGE_KEY, authData.token);
    storage.setItem(USER_STORAGE_KEY, JSON.stringify(authData.user));

    return authData;
  },

  register: async (request: RegisterRequest): Promise<AuthResponse> => {
    const res = await apiClient.post<ApiResponse<AuthResponse>>('/auth/register', {
      name: request.name.trim(),
      email: request.email.trim(),
      password: request.password,
      timezone: request.timezone || 'UTC',
    });

    const authData = res.data.data;
    localStorage.setItem(TOKEN_STORAGE_KEY, authData.token);
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(authData.user));

    return authData;
  },

  logout: async (): Promise<void> => {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    localStorage.removeItem(USER_STORAGE_KEY);
    sessionStorage.removeItem(TOKEN_STORAGE_KEY);
    sessionStorage.removeItem(USER_STORAGE_KEY);
  },

  getCurrentUser: (): User | null => {
    const raw = localStorage.getItem(USER_STORAGE_KEY) || sessionStorage.getItem(USER_STORAGE_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as User;
    } catch {
      return null;
    }
  },

  getToken: (): string | null => {
    return localStorage.getItem(TOKEN_STORAGE_KEY) || sessionStorage.getItem(TOKEN_STORAGE_KEY);
  },

  updateProfile: async (id: string, data: UpdateUserRequest): Promise<User> => {
    const res = await apiClient.put<ApiResponse<User>>(`/users/${id}`, data);
    const updated = res.data.data;
    const current = authApi.getCurrentUser();
    if (current && current.id === id) {
      const merged = { ...current, ...updated };
      if (localStorage.getItem(USER_STORAGE_KEY)) {
        localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(merged));
      }
      if (sessionStorage.getItem(USER_STORAGE_KEY)) {
        sessionStorage.setItem(USER_STORAGE_KEY, JSON.stringify(merged));
      }
    }
    return updated;
  },
};
