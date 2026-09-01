export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'COMPLETED' | 'BLOCKED' | 'CANCELLED';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface Task {
  id: string;
  goalId: string;
  goalTitle: string;
  assigneeId: string;
  assigneeName: string;
  assigneeEmail: string;
  teamId: string;
  teamName: string;
  title: string;
  description?: string | null;
  status: TaskStatus;
  priority: TaskPriority;
  estimatedMinutes?: number | null;
  actualMinutes?: number | null;
  dueDate?: string | null;
  completedAt?: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTaskRequest {
  goalId: string;
  assigneeId: string;
  title: string;
  description?: string;
  status?: TaskStatus;
  priority?: TaskPriority;
  estimatedMinutes?: number;
  dueDate?: string;
}

export interface UpdateTaskRequest {
  title: string;
  description?: string;
  status?: TaskStatus;
  priority?: TaskPriority;
  estimatedMinutes?: number;
  actualMinutes?: number;
  dueDate?: string;
  active?: boolean;
}

export interface TaskFilterParams {
  goalId?: string;
  assigneeId?: string;
  teamId?: string;
  status?: TaskStatus;
  priority?: TaskPriority;
  dueDate?: string;
  active?: boolean;
  page?: number;
  size?: number;
  sort?: string;
}
