export interface User {
  id: string;
  name: string;
  email: string;
  avatarUrl?: string | null;
  timezone: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateUserRequest {
  name: string;
  email: string;
  avatarUrl?: string;
  timezone: string;
}

export interface UpdateUserRequest {
  name: string;
  avatarUrl?: string;
  timezone: string;
}
