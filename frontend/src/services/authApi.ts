import { userApi } from './userApi';
import { User, UpdateUserRequest } from '@/types/user';
import { LoginRequest, RegisterRequest, AuthResponse } from '@/types/auth';

const TOKEN_STORAGE_KEY = 'devsync_auth_token';
const USER_STORAGE_KEY = 'devsync_auth_user';

export const authApi = {
  login: async (request: LoginRequest): Promise<AuthResponse> => {
    // 1. Fetch available users to verify account
    const usersPage = await userApi.getUsers({ size: 100 });
    const matchedUser = usersPage.content?.find(
      (u) => u.email.toLowerCase() === request.email.toLowerCase()
    );

    if (!matchedUser) {
      throw new Error('Account not found. Please register or check your email address.');
    }

    if (!matchedUser.active) {
      throw new Error('This account has been deactivated. Please contact your team administrator.');
    }

    // 2. Generate a secure session token
    const token = `devsync_token_${matchedUser.id}_${Date.now()}`;
    const storage = request.rememberMe !== false ? localStorage : sessionStorage;
    storage.setItem(TOKEN_STORAGE_KEY, token);
    storage.setItem(USER_STORAGE_KEY, JSON.stringify(matchedUser));

    return { user: matchedUser, token };
  },

  register: async (request: RegisterRequest): Promise<AuthResponse> => {
    // 1. Create user in database via backend API
    const user = await userApi.createUser({
      name: request.name,
      email: request.email,
      timezone: request.timezone || 'UTC',
    });

    // 2. Generate session token
    const token = `devsync_token_${user.id}_${Date.now()}`;
    localStorage.setItem(TOKEN_STORAGE_KEY, token);
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));

    return { user, token };
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
    const updated = await userApi.updateUser(id, data);
    // Update cached user in storage
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
