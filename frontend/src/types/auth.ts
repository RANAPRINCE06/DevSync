import { User } from './user';

export interface LoginRequest {
  email: string;
  password: string;
  rememberMe?: boolean;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  timezone?: string;
}

export interface AuthResponse {
  user: User;
  token: string;
  tokenType?: string;
}

export interface UserSession {
  user: User;
  token: string;
  rememberMe: boolean;
}

export interface ChangePasswordRequest {
  currentPassword?: string;
  newPassword?: string;
}
